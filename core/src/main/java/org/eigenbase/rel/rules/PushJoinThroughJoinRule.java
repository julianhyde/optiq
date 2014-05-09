/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.rel.rules;

import java.util.*;

import org.eigenbase.rel.*;
import org.eigenbase.rel.RelFactories.ProjectFactory;
import org.eigenbase.relopt.*;
import org.eigenbase.rex.*;
import org.eigenbase.util.mapping.*;

import net.hydromatic.optiq.util.BitSets;

/**
 * Rule that pushes the right input of a join into through the left input of
 * the join, provided that the left input is also a join.
 *
 * <p>Thus, {@code (A join B) join C} becomes {@code (A join C) join B}. The
 * advantage of applying this rule is that it may be possible to apply
 * conditions earlier. For instance,</p>
 *
 * <pre>{@code
 * (sales as s join product_class as pc on true)
 * join product as p
 * on s.product_id = p.product_id
 * and p.product_class_id = pc.product_class_id}</pre>
 *
 * becomes
 *
 * <pre>{@code (sales as s join product as p on s.product_id = p.product_id)
 * join product_class as pc
 * on p.product_class_id = pc.product_class_id}</pre>
 *
 * <p>Before the rule, one join has two conditions and the other has none
 * ({@code ON TRUE}). After the rule, each join has one condition.</p>
 */
public class PushJoinThroughJoinRule extends RelOptRule {

  public static final RelOptRule RIGHT =
      new PushJoinThroughJoinRule(
          "PushJoinThroughJoinRule:right", true, JoinRel.class);
  public static final RelOptRule LEFT =
      new PushJoinThroughJoinRule(
          "PushJoinThroughJoinRule:left", false, JoinRel.class);

  private final boolean right;

  private final ProjectFactory projectFactory;

  /**
   * Creates a PushJoinThroughJoinRule.
   */
  private PushJoinThroughJoinRule(String description, boolean right,
      Class<? extends JoinRelBase> clazz) {
    this(description, right, clazz, RelFactories.DEFAULT_PROJECT_FACTORY);
  }

  public PushJoinThroughJoinRule(
      String description,
      boolean right,
      Class<? extends JoinRelBase> clazz, ProjectFactory pFactory) {
    super(
        operand(
            clazz,
            operand(clazz, any()),
            operand(RelNode.class, any())),
        description);
    this.right = right;
    projectFactory = pFactory;
  }

  @Override
  public void onMatch(RelOptRuleCall call) {
    if (right) {
      onMatchRight(call);
    } else {
      onMatchLeft(call);
    }
  }

  private void onMatchRight(RelOptRuleCall call) {
    final JoinRelBase topJoin = call.rel(0);
    final JoinRelBase bottomJoin = call.rel(1);
    final RelNode relC = call.rel(2);
    final RelNode relA = bottomJoin.getLeft();
    final RelNode relB = bottomJoin.getRight();
    final RelOptCluster cluster = topJoin.getCluster();

    //        topJoin
    //        /     \
    //   bottomJoin  C
    //    /    \
    //   A      B

    final int aCount = relA.getRowType().getFieldCount();
    final int bCount = relB.getRowType().getFieldCount();
    final int cCount = relC.getRowType().getFieldCount();
    final BitSet bBitSet = BitSets.range(aCount, aCount + bCount);

    // becomes
    //
    //        newTopJoin
    //        /        \
    //   newBottomJoin  B
    //    /    \
    //   A      C

    // If either join is not inner, we cannot proceed.
    // (Is this too strict?)
    if (topJoin.getJoinType() != JoinRelType.INNER
        || bottomJoin.getJoinType() != JoinRelType.INNER) {
      return;
    }

    // Permute the top condition so that it is in terms of the columns from
    // A and B, not permuted using bottomJoin.mapping. Columns from C are not
    // affected.
    final RexNode topCondition =
        topJoin.getCondition().accept(
            new RexPermuteInputsShuttle(
                Mappings.append(bottomJoin.mapping.inverse(),
                    Mappings.createIdentity(cCount)),
                relA, relB));

    // Split the condition of topJoin into a conjunction. Each of the
    // parts that does not use columns from B can be pushed down.
    final List<RexNode> intersecting = new ArrayList<RexNode>();
    final List<RexNode> nonIntersecting = new ArrayList<RexNode>();
    split(topCondition, bBitSet, intersecting, nonIntersecting);

    // If there's nothing to push down, it's not worth proceeding.
    if (nonIntersecting.isEmpty()) {
      return;
    }

    // Split the condition of bottomJoin into a conjunction. Each of the
    // parts that use columns from B will need to be pulled up.
    final List<RexNode> bottomIntersecting = new ArrayList<RexNode>();
    final List<RexNode> bottomNonIntersecting = new ArrayList<RexNode>();
    split(
        bottomJoin.getCondition(), bBitSet, bottomIntersecting,
        bottomNonIntersecting);

    // Re-map the conditions that were in the top join, now in the bottom join.
    // target: | A       | C      |
    // source: | A       | B | C      |
    final Mappings.TargetMapping bottomMapping =
        Mappings.createShiftMapping(
            aCount + bCount + cCount,
            0, 0, aCount,
            aCount, aCount + bCount, cCount);
    List<RexNode> newBottomList = new ArrayList<RexNode>();
    new RexPermuteInputsShuttle(bottomMapping, relA, relC)
        .visitList(nonIntersecting, newBottomList);
    final Mappings.TargetMapping bottomBottomMapping =
        Mappings.createShiftMapping(
            aCount + bCount,
            0, 0, aCount);
    new RexPermuteInputsShuttle(bottomBottomMapping, relA, relC)
        .visitList(bottomNonIntersecting, newBottomList);
    final RexBuilder rexBuilder = cluster.getRexBuilder();
    RexNode newBottomCondition =
        RexUtil.composeConjunction(rexBuilder, newBottomList, false);
    final JoinRelBase newBottomJoin =
        bottomJoin.copy(bottomJoin.getTraitSet(), newBottomCondition, relA,
            relC, bottomJoin.getJoinType(), null);

    // Re-map the conditions that were in the bottom join, now in the top join.
    // target: | A       | C      | B |
    // source: | A       | B | C      |
    final Mapping topMapping =
        Mappings.createShiftMapping(
            aCount + bCount + cCount,
            0, 0, aCount,
            aCount + cCount, aCount, bCount,
            aCount, aCount + bCount, cCount);
    List<RexNode> newTopList = new ArrayList<RexNode>();
    final RexPermuteInputsShuttle shuttle =
        new RexPermuteInputsShuttle(topMapping, newBottomJoin, relB);
    shuttle.visitList(intersecting, newTopList);
    shuttle.visitList(bottomIntersecting, newTopList);
    RexNode newTopCondition =
        RexUtil.composeConjunction(rexBuilder, newTopList, false);

    // Compute the mapping from the old output to the new output.
    final Mapping mapping = Mappings.createBijection(aCount + bCount + cCount);
    for (int t = 0; t < mapping.getTargetCount(); t++) {
      final int t2 = topJoin.mapping.getSource(t);
      final int s = t2 < aCount + bCount
          ? bottomJoin.mapping.getSource(t2)
          : t2;
      final int s2;
      if (s >= aCount + bCount) {
        s2 = s - bCount; // from input c
      } else if (s >= aCount) {
        s2 = s + cCount; // from input b
      } else {
        s2 = s; // from input a
      }
      mapping.set(s2, t);
      System.out.println("aCount=" + aCount + ", bCount=" + bCount + ", cCount="
          + cCount + ", s=" + s + ", s2=" + s2 + ", t=" + t + ", t2=" + t2);
    }
    @SuppressWarnings("SuspiciousNameCombination")
    final JoinRelBase newTopJoin =
        topJoin.copy(topJoin.getTraitSet(), newTopCondition, newBottomJoin,
            relB, topJoin.getJoinType(), mapping);

    call.transformTo(newTopJoin);
  }

  /**
   * Similar to {@link #onMatch}, but swaps the upper sibling with the left
   * of the two lower siblings, rather than the right.
   */
  private void onMatchLeft(RelOptRuleCall call) {
    final JoinRelBase topJoin = call.rel(0);
    final JoinRelBase bottomJoin = call.rel(1);
    final RelNode relC = call.rel(2);
    final RelNode relA = bottomJoin.getLeft();
    final RelNode relB = bottomJoin.getRight();
    final RelOptCluster cluster = topJoin.getCluster();

    //        topJoin
    //        /     \
    //   bottomJoin  C
    //    /    \
    //   A      B

    final int aCount = relA.getRowType().getFieldCount();
    final int bCount = relB.getRowType().getFieldCount();
    final int cCount = relC.getRowType().getFieldCount();
    final BitSet aBitSet = BitSets.range(aCount);

    // becomes
    //
    //        newTopJoin
    //        /        \
    //   newBottomJoin  A
    //    /    \
    //   C      B

    // If either join is not inner, we cannot proceed.
    // (Is this too strict?)
    if (topJoin.getJoinType() != JoinRelType.INNER
        || bottomJoin.getJoinType() != JoinRelType.INNER) {
      return;
    }

    // Split the condition of topJoin into a conjunction. Each of the
    // parts that does not use columns from A can be pushed down.
    final List<RexNode> intersecting = new ArrayList<RexNode>();
    final List<RexNode> nonIntersecting = new ArrayList<RexNode>();
    split(topJoin.getCondition(), aBitSet, intersecting, nonIntersecting);

    // If there's nothing to push down, it's not worth proceeding.
    if (nonIntersecting.isEmpty()) {
      return;
    }

    // Split the condition of bottomJoin into a conjunction. Each of the
    // parts that use columns from B will need to be pulled up.
    final List<RexNode> bottomIntersecting = new ArrayList<RexNode>();
    final List<RexNode> bottomNonIntersecting = new ArrayList<RexNode>();
    split(
        bottomJoin.getCondition(), aBitSet, bottomIntersecting,
        bottomNonIntersecting);

    // target: | C      | B |
    // source: | A       | B | C      |
    final int sourceCount = aCount + bCount + cCount;
    final Mapping bottomMapping =
        Mappings.createShiftMapping(
            sourceCount,
            cCount, aCount, bCount,
            0, aCount + bCount, cCount);
    List<RexNode> newBottomList = new ArrayList<RexNode>();
    new RexPermuteInputsShuttle(bottomMapping, relC, relB)
        .visitList(nonIntersecting, newBottomList);
    final Mapping bottomBottomMapping =
        Mappings.createShiftMapping(
            sourceCount,
            0, aCount + bCount, cCount,
            cCount, aCount, bCount);
    new RexPermuteInputsShuttle(bottomBottomMapping, relC, relB)
        .visitList(bottomNonIntersecting, newBottomList);
    final RexBuilder rexBuilder = cluster.getRexBuilder();
    RexNode newBottomCondition =
        RexUtil.composeConjunction(rexBuilder, newBottomList, false);
    final JoinRelBase newBottomJoin =
        bottomJoin.copy(bottomJoin.getTraitSet(), newBottomCondition, relC,
            relB, bottomJoin.getJoinType(), null);

    // target: | C      | B | A       |
    // source: | A       | B | C      |
    final Mapping topMapping =
        Mappings.create(MappingType.INVERSE_SURJECTION, sourceCount,
            sourceCount);
    for (int t = 0; t < sourceCount; t++) {
      final int t2 = topJoin.mapping.getSource(t);
      final int s = t2 < aCount + bCount
          ? bottomJoin.mapping.getSource(t2)
          : t2;
      final int s2;
      if (s >= aCount + bCount) {
        s2 = s - aCount - bCount; // from input c
      } else if (s >= aCount) {
        s2 = s - aCount + cCount; // from input b
      } else {
        s2 = s + cCount + bCount; // from input a
      }
      System.out.println("aCount=" + aCount + ", bCount=" + bCount + ", cCount="
          + cCount + ", s=" + s + ", s2=" + s2 + ", t=" + t + ", t2=" + t2);
      topMapping.set(s2, t2);
    }

    Object o = // TODO: remove
        Mappings.createShiftMapping(
            sourceCount,
            cCount + bCount, 0, aCount,
            cCount, aCount, bCount,
            0, aCount + bCount, cCount);
    List<RexNode> newTopList = new ArrayList<RexNode>();
    new RexPermuteInputsShuttle(topMapping, newBottomJoin, relA)
        .visitList(intersecting, newTopList);
    new RexPermuteInputsShuttle(topMapping, newBottomJoin, relA)
        .visitList(bottomIntersecting, newTopList);
    RexNode newTopCondition =
        RexUtil.composeConjunction(rexBuilder, newTopList, false);
    @SuppressWarnings("SuspiciousNameCombination")
    final JoinRelBase newTopJoin =
        topJoin.copy(topJoin.getTraitSet(), newTopCondition, newBottomJoin,
            relA, topJoin.getJoinType(),
            Mappings.compose(topMapping, topJoin.mapping));

    call.transformTo(newTopJoin);
  }

  /**
   * Splits a condition into conjunctions that do or do not intersect with
   * a given bit set.
   */
  static void split(
      RexNode condition,
      BitSet bitSet,
      List<RexNode> intersecting,
      List<RexNode> nonIntersecting) {
    for (RexNode node : RelOptUtil.conjunctions(condition)) {
      BitSet inputBitSet = RelOptUtil.InputFinder.bits(node);
      if (bitSet.intersects(inputBitSet)) {
        intersecting.add(node);
      } else {
        nonIntersecting.add(node);
      }
    }
  }
}

// End PushJoinThroughJoinRule.java
