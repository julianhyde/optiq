/*
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to you under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.rel.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eigenbase.rel.AggregateRelBase;
import org.eigenbase.rel.FilterRelBase;
import org.eigenbase.rel.JoinRelBase;
import org.eigenbase.rel.JoinRelType;
import org.eigenbase.rel.ProjectRelBase;
import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.SortRel;
import org.eigenbase.rel.TableAccessRelBase;
import org.eigenbase.rel.UnionRelBase;
import org.eigenbase.relopt.RelOptPulledUpPredicates;
import org.eigenbase.relopt.RelOptUtil;
import org.eigenbase.rex.RexBuilder;
import org.eigenbase.rex.RexCall;
import org.eigenbase.rex.RexInputRef;
import org.eigenbase.rex.RexNode;
import org.eigenbase.rex.RexPermuteInputsShuttle;
import org.eigenbase.rex.RexUtil;
import org.eigenbase.rex.RexVisitorImpl;
import org.eigenbase.sql.SqlKind;
import org.eigenbase.sql.fun.SqlStdOperatorTable;
import org.eigenbase.util.mapping.Mapping;
import org.eigenbase.util.mapping.MappingType;
import org.eigenbase.util.mapping.Mappings;

import net.hydromatic.optiq.BuiltinMethod;
import net.hydromatic.optiq.util.BitSets;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Utility to infer Predicates that are applicable above a RelNode.
 * This is currently used by {@link TransitivePredicatesOnJoinRule} to
 * infer <em>Predicates</em> that can be inferred from one side of a Join
 * to the other.
 * <p>
 * The PullUp Strategy is sound but not complete. Here are some of the
 * limitations:
 * <ol>
 * <li> For Aggregations we only PullUp predicates that only contain
 * Grouping Keys. This can be extended to infer predicates on Aggregation
 * expressions from  expressions on the aggregated columns. For e.g.
 * <pre>
 * select a, max(b) from R1 where b > 7 => max(b) > 7 or max(b) is null
 * </pre>
 * <li> For Projections we only look at columns that are projected without
 * any function applied. So:
 * <pre>
 * select a from R1 where a > 7 -> a > 7 is pulledUp from the Projection.
 * select a + 1 from R1 where a + 1 > 7 -> a + 1 > 7 is not pulledUp
 * </pre>
 * <li> There are several restrictions on Joins:
 * <ul>
 * <li> We only pullUp inferred predicates for now. Pulling up existing
 * predicates causes an explosion of duplicates. The existing predicates
 * are pushed back down as new predicates. Once we have rules to eliminate
 * duplicate Filter conditions, we should pullUp all predicates.
 * <li> For Left Outer: we infer new predicates from the left and set them
 * as applicable on the Right side. No predicates are pulledUp.
 * <li> Right Outer Joins are handled in an analogous manner.
 * <li> For Full Outer Joins no predicates are pulledUp or inferred.
 * </ul>
 * </ol>
 *
 */
public class RelMdPredicates {

  public static final RelMetadataProvider SOURCE = ReflectiveRelMetadataProvider
      .reflectiveSource(BuiltinMethod.PREDICATES.method, new RelMdPredicates());

  private static final List<RexNode> EMPTY_ARRAY = new ArrayList<RexNode>();

  public RelOptPulledUpPredicates getPredicates(TableAccessRelBase table) {
    return new RelOptPulledUpPredicates(EMPTY_ARRAY, EMPTY_ARRAY, EMPTY_ARRAY);
  }

  /**
   * <ol>
   * <li>create a mapping from input to projection. Map only positions that
   * directly reference an input column.
   * <li>Expressions that only contain above columns are retained in the
   * Project's pullExpressions list.
   * <li>For e.g. expression 'a + e = 9' below will not be pulled up because 'e'
   * is not in the projection list.
   *
   * <pre>
   * childPullUpExprs:      { a > 7, b + c < 10, a + e = 9}
   * projectionExprs:       {a, b, c, e/2}
   * projectionPullupExprs: { a > 7, b + c < 10}
   * </pre>
   *
   * </ol>
   */
  public RelOptPulledUpPredicates getPredicates(ProjectRelBase project) {
    RelNode child = project.getChild();
    RelOptPulledUpPredicates childInfo = RelMetadataQuery
        .getPulledUpPredicates(child);
    List<RexNode> childPullUpPredicates = childInfo.getPulledUpPredicates();

    List<RexNode> projectPullUpPredicates = new ArrayList<RexNode>();

    BitSet columnsMapped = new BitSet(child.getRowType().getFieldCount());
    Mapping m = Mappings.create(MappingType.PARTIAL_FUNCTION, child
        .getRowType().getFieldCount(), project.getRowType().getFieldCount());

    ListIterator<RexNode> li = project.getProjects().listIterator();
    while (li.hasNext()) {
      int pIdx = li.nextIndex();
      RexNode r = li.next();
      if (r instanceof RexInputRef) {
        int sIdx = ((RexInputRef) r).getIndex();
        m.set(sIdx, pIdx);
        columnsMapped.set(sIdx);
      }
    }

    /**
     * go over childPullUpPredicates. if a predicate only contains columns in
     * 'columnsMapped' construct a new predicate based on mapping.
     */
    for (RexNode r : childPullUpPredicates) {
      BitSet rCols = RelOptUtil.InputFinder.bits(r);
      if (BitSets.contains(columnsMapped, rCols)) {
        r = r.accept(new RexPermuteInputsShuttle(m, child));
        projectPullUpPredicates.add(r);
      }
    }
    return new RelOptPulledUpPredicates(projectPullUpPredicates, EMPTY_ARRAY,
        EMPTY_ARRAY);
  }

  /**
   * Add the Filter condition to the pulledPredicates list from the child.
   *
   * @param filter
   */
  public RelOptPulledUpPredicates getPredicates(FilterRelBase filter) {
    RelNode child = filter.getChild();
    RelOptPulledUpPredicates childInfo = RelMetadataQuery
        .getPulledUpPredicates(child);
    List<RexNode> childPullUpPredicates = childInfo.getPulledUpPredicates();

    List<RexNode> filterPullUpPredicates = new ArrayList<RexNode>();
    filterPullUpPredicates.addAll(childPullUpPredicates);
    filterPullUpPredicates
        .addAll(RelOptUtil.conjunctions(filter.getCondition()));
    return new RelOptPulledUpPredicates(filterPullUpPredicates, EMPTY_ARRAY,
        EMPTY_ARRAY);
  }

  public RelOptPulledUpPredicates getPredicates(JoinRelBase join) {
    RexBuilder rB = join.getCluster().getRexBuilder();
    RelNode lChild = join.getInput(0);
    RelNode rChild = join.getInput(1);

    RelOptPulledUpPredicates lchildInfo = RelMetadataQuery
        .getPulledUpPredicates(lChild);
    List<RexNode> lPullupPredicates = lchildInfo.getPulledUpPredicates();
    RelOptPulledUpPredicates rchildInfo = RelMetadataQuery
        .getPulledUpPredicates(rChild);
    List<RexNode> rPullupPredicates = rchildInfo.getPulledUpPredicates();

    JoinConditionBasedPredicateInference jI =
        new JoinConditionBasedPredicateInference(
        join, RexUtil.composeConjunction(rB, lPullupPredicates, false),
        RexUtil.composeConjunction(rB, rPullupPredicates, false));

    return jI.inferPredicates(false);
  }

  /**
   * pull up predicates that only contains references to columns in the
   * GroupSet. For e.g.
   *
   * <pre>
   * childPullUpExprs : { a > 7, b + c < 10, a + e = 9}
   * groupSet         : { a, b}
   * pulledUpExprs    : { a > 7}
   * </pre>
   *
   * @param agg
   */
  public RelOptPulledUpPredicates getPredicates(AggregateRelBase agg) {
    RelNode child = agg.getChild();
    RelOptPulledUpPredicates childInfo = RelMetadataQuery
        .getPulledUpPredicates(child);
    List<RexNode> childPullUpPredicates = childInfo.getPulledUpPredicates();

    List<RexNode> aggPullUpPredicates = new ArrayList<RexNode>();

    BitSet groupKeys = agg.getGroupSet();
    Mapping m = Mappings.create(MappingType.PARTIAL_FUNCTION, child
        .getRowType().getFieldCount(), agg.getRowType().getFieldCount());

    for (int i = 0, j = groupKeys.nextSetBit(0); j >= 0; i++, j = groupKeys
        .nextSetBit(j + 1)) {
      m.set(j, i);
    }

    for (RexNode r : childPullUpPredicates) {
      BitSet rCols = RelOptUtil.InputFinder.bits(r);
      if (BitSets.contains(groupKeys, rCols)) {
        r = r.accept(new RexPermuteInputsShuttle(m, child));
        aggPullUpPredicates.add(r);
      }
    }
    return new RelOptPulledUpPredicates(aggPullUpPredicates, EMPTY_ARRAY,
        EMPTY_ARRAY);
  }

  /**
   * the pulled up expression is a disjunction of its children.
   *
   * @param union
   */
  public RelOptPulledUpPredicates getPredicates(UnionRelBase union) {
    RexBuilder rB = union.getCluster().getRexBuilder();
    RelNode lChild = union.getInput(0);
    RelNode rChild = union.getInput(1);
    RelOptPulledUpPredicates lchildInfo = RelMetadataQuery
        .getPulledUpPredicates(lChild);
    List<RexNode> lPullupPredicates = lchildInfo.getPulledUpPredicates();
    RelOptPulledUpPredicates rchildInfo = RelMetadataQuery
        .getPulledUpPredicates(rChild);
    List<RexNode> rPullupPredicates = rchildInfo.getPulledUpPredicates();

    RexNode pullUpPredicate = RexUtil.composeDisjunction(
        rB,
        Lists.newArrayList(
            RexUtil.composeConjunction(rB, lPullupPredicates, false),
            RexUtil.composeConjunction(rB, rPullupPredicates, false)), false);

    /**
     * flatten the Disjunction.
     */
    if (pullUpPredicate instanceof RexCall) {
      RexCall orExp = (RexCall) pullUpPredicate;
      if (orExp.getOperator() == SqlStdOperatorTable.OR) {
        pullUpPredicate = rB.makeCall(SqlStdOperatorTable.OR,
            RexUtil.flatten(orExp.getOperands(), SqlStdOperatorTable.OR));
      }
    }

    List<RexNode> unionPullUpPredicates = new ArrayList<RexNode>();
    if (pullUpPredicate != null) {
      unionPullUpPredicates.addAll(RelOptUtil.conjunctions(pullUpPredicate));
    }
    return new RelOptPulledUpPredicates(unionPullUpPredicates, EMPTY_ARRAY,
        EMPTY_ARRAY);
  }

  public RelOptPulledUpPredicates getPredicates(SortRel sort) {
    RelNode child = sort.getInput(0);
    RelOptPulledUpPredicates childInfo = RelMetadataQuery
        .getPulledUpPredicates(child);
    List<RexNode> childPullUpPredicates = childInfo.getPulledUpPredicates();
    return new RelOptPulledUpPredicates(childPullUpPredicates, EMPTY_ARRAY,
        EMPTY_ARRAY);
  }

  /**
   * Utility to infer predicates from one side of the join that apply on the
   * other side. Contract is: - initialize with a {@link JoinRelBase} and
   * optional predicates applicable on its left and right subtrees. - you can
   * then ask it for equivalentPredicate(s) given a predicate.
   * <p>
   * So for:
   * <ol>
   * <li>'<code>R1(x) join R2(y) on x = y</code>' a call for
   * equivalentPredciates on '<code>x > 7</code>' will return '
   * <code>[y > 7]</code>'
   * <li>'<code>R1(x) join R2(y) on x = y join R3(z) on y = z</code>' a call for
   * equivalentPredciates on the second join '<code>x > 7</code>' will return '
   * <code>[y > 7, z > 7]</code>'
   * </ol>
   */
  static class JoinConditionBasedPredicateInference {

    JoinRelBase joinRel;
    int nSysFields;
    int nFieldsLeft;
    int nFieldsRight;
    BitSet leftFieldsBitSet;
    BitSet rightFieldsBitSet;
    BitSet allFieldsBitSet;
    SortedMap<Integer, BitSet> equivalence;
    Map<String, BitSet> exprFields;
    Set<String> allExprsDigests;
    Set<String> equalityPredicates;
    RexNode leftChildPredicates;
    RexNode rightChildPredicates;

    public JoinConditionBasedPredicateInference(JoinRelBase joinRel,
        RexNode lPreds, RexNode rPreds) {
      super();
      this.joinRel = joinRel;
      nFieldsLeft = joinRel.getLeft().getRowType().getFieldList().size();
      nFieldsRight = joinRel.getRight().getRowType().getFieldList().size();
      nSysFields = joinRel.getSystemFieldList().size();
      leftFieldsBitSet = BitSets.range(nSysFields, nSysFields + nFieldsLeft);
      rightFieldsBitSet = BitSets.range(nSysFields + nFieldsLeft, nSysFields
          + nFieldsLeft + nFieldsRight);
      allFieldsBitSet = BitSets.range(0, nSysFields + nFieldsLeft
          + nFieldsRight);
      leftChildPredicates = lPreds;
      rightChildPredicates = rPreds;

      exprFields = new HashMap<String, BitSet>();
      allExprsDigests = new HashSet<String>();

      if (leftChildPredicates != null) {
        Mappings.TargetMapping leftMapping = Mappings.createShiftMapping(
            nSysFields + nFieldsLeft, nSysFields, 0, nFieldsLeft);
        leftChildPredicates = leftChildPredicates
            .accept(new RexPermuteInputsShuttle(leftMapping, joinRel
                .getInput(0)));

        for (RexNode r : RelOptUtil.conjunctions(leftChildPredicates)) {
          exprFields.put(r.toString(), RelOptUtil.InputFinder.bits(r));
          allExprsDigests.add(r.toString());
        }
      }
      if (rightChildPredicates != null) {
        Mappings.TargetMapping rightMapping = Mappings.createShiftMapping(
            nSysFields + nFieldsLeft + nFieldsRight,
            nSysFields + nFieldsLeft, 0, nFieldsRight);
        rightChildPredicates = rightChildPredicates
            .accept(new RexPermuteInputsShuttle(rightMapping, joinRel
                .getInput(1)));

        for (RexNode r : RelOptUtil.conjunctions(rightChildPredicates)) {
          exprFields.put(r.toString(), RelOptUtil.InputFinder.bits(r));
          allExprsDigests.add(r.toString());
        }
      }

      equivalence = new TreeMap<Integer, BitSet>();
      equalityPredicates = new HashSet<String>();
      for (int i = 0; i < nSysFields + nFieldsLeft + nFieldsRight; i++) {
        BitSet b = new BitSet();
        b.set(i);
        equivalence.put(i, b);
      }

      /**
       * only process equivalences found in the join conditions. Processing
       * Equivalences from the left or right side infer predicates that are
       * already present in the Tree below the join.
       */
      RexBuilder rexBuilder = joinRel.getCluster().getRexBuilder();
      List<RexNode> exprs = RelOptUtil.conjunctions(compose(rexBuilder,
          Arrays.asList(joinRel.getCondition()/*
                                               * , leftChildPredicates,
                                               * rightChildPredicates
                                               */)));

      final EquivalenceFinder eF = new EquivalenceFinder();
      new ArrayList<Void>(Lists.transform(exprs, new Function<RexNode, Void>() {
        public Void apply(RexNode input) {
          return input.accept(eF);
        }
      }));

      equivalence = new Closure().compute();
    }

    /**
     * The PullUp Strategy is sound but not complete.
     * <ol>
     * <li>We only pullUp inferred predicates for now. Pulling up existing
     * predicates causes an explosion of duplicates. The existing predicates are
     * pushed back down as new predicates. Once we have rules to eliminate
     * duplicate Filter conditions, we should pullUp all predicates.
     * <li>For Left Outer: we infer new predicates from the left and set them as
     * applicable on the Right side. No predicates are pulledUp.
     * <li>Right Outer Joins are handled in an analogous manner.
     * <li>For Full Outer Joins no predicates are pulledUp or inferred.
     * </ol>
     *
     * @param includeEqualityInference
     * @return
     */
    public RelOptPulledUpPredicates inferPredicates(
        boolean includeEqualityInference) {
      List<RexNode> inferedPredicates = new ArrayList<RexNode>();
      Set<String> allExprsDigests = new HashSet<String>(this.allExprsDigests);
      if (joinRel.getJoinType() == JoinRelType.INNER
          || joinRel.getJoinType() == JoinRelType.LEFT) {
        infer(leftChildPredicates, allExprsDigests, inferedPredicates,
            includeEqualityInference,
            joinRel.getJoinType() == JoinRelType.LEFT ? rightFieldsBitSet
                : allFieldsBitSet);
      }
      if (joinRel.getJoinType() == JoinRelType.INNER
          || joinRel.getJoinType() == JoinRelType.RIGHT) {
        infer(rightChildPredicates, allExprsDigests, inferedPredicates,
            includeEqualityInference,
            joinRel.getJoinType() == JoinRelType.RIGHT ? leftFieldsBitSet
                : allFieldsBitSet);
      }

      Mappings.TargetMapping rightMapping = Mappings.createShiftMapping(
          nSysFields + nFieldsLeft + nFieldsRight, 0, nSysFields
              + nFieldsLeft, nFieldsRight);
      final RexPermuteInputsShuttle rpermute = new RexPermuteInputsShuttle(
          rightMapping, joinRel);
      Mappings.TargetMapping leftMapping = Mappings.createShiftMapping(
          nSysFields + nFieldsLeft, 0, nSysFields, nFieldsLeft);
      final RexPermuteInputsShuttle lpermute = new RexPermuteInputsShuttle(
          leftMapping, joinRel);
      List<RexNode> leftInferredPredicates = new ArrayList<RexNode>();
      List<RexNode> rightInferredPredicates = new ArrayList<RexNode>();

      for (RexNode iP : inferedPredicates) {
        BitSet iPBitSet = RelOptUtil.InputFinder.bits(iP);
        if (BitSets.contains(leftFieldsBitSet, iPBitSet)) {
          leftInferredPredicates.add(iP.accept(lpermute));
        } else if (BitSets.contains(rightFieldsBitSet, iPBitSet)) {
          rightInferredPredicates.add(iP.accept(rpermute));
        }
      }

      if (joinRel.getJoinType() == JoinRelType.INNER) {
        return new RelOptPulledUpPredicates(Iterables.concat(
            RelOptUtil.conjunctions(leftChildPredicates),
            RelOptUtil.conjunctions(rightChildPredicates),
            RelOptUtil.conjunctions(joinRel.getCondition()), inferedPredicates),
            leftInferredPredicates, rightInferredPredicates);
      } else if (joinRel.getJoinType() == JoinRelType.LEFT) {
        return new RelOptPulledUpPredicates(
            RelOptUtil.conjunctions(leftChildPredicates),
            leftInferredPredicates, rightInferredPredicates);
      } else if (joinRel.getJoinType() == JoinRelType.RIGHT) {
        return new RelOptPulledUpPredicates(
            RelOptUtil.conjunctions(rightChildPredicates),
            inferedPredicates,
            new ArrayList<RexNode>());
      } else {
        assert inferedPredicates.size() == 0;
        return new RelOptPulledUpPredicates(inferedPredicates,
            inferedPredicates, inferedPredicates);
      }
    }

    public RexNode leftPulledUpPredciates() {
      return leftChildPredicates;
    }

    public RexNode right() {
      return rightChildPredicates;
    }

    private void infer(RexNode predicates, Set<String> allExprsDigests,
        List<RexNode> inferedPredicates, boolean includeEqualityInference,
        BitSet inferringFields) {
      for (RexNode r : RelOptUtil.conjunctions(predicates)) {
        if (!includeEqualityInference
            && equalityPredicates.contains(r.toString())) {
          continue;
        }
        Iterator<Mapping> i = new ExprsItr(r);
        while (i.hasNext()) {
          Mapping m = i.next();
          RexNode tr = r.accept(new RexPermuteInputsShuttle(m, joinRel
              .getInput(0), joinRel.getInput(1)));
          if (BitSets
              .contains(inferringFields, RelOptUtil.InputFinder.bits(tr))
              && !allExprsDigests.contains(tr.toString())
              && !isAlwaysTrue(tr)) {
            inferedPredicates.add(tr);
            allExprsDigests.add(tr.toString());
          }
        }
      }
    }

    private void equivalent(int p1, int p2) {
      BitSet b = equivalence.get(p1);
      b.set(p2);

      b = equivalence.get(p2);
      b.set(p1);
    }

    RexNode compose(RexBuilder rexBuilder, Iterable<RexNode> exprs) {
      exprs = FluentIterable.from(exprs).filter(new Predicate<RexNode>() {
        public boolean apply(RexNode expr) {
          return expr != null;
        }
      });
      return RexUtil.composeConjunction(rexBuilder, exprs, false);
    }

    /**
     * Find expressions of the form 'col_x = col_y'.
     */
    class EquivalenceFinder extends RexVisitorImpl<Void> {

      protected EquivalenceFinder() {
        super(true);
      }

      @Override
      public Void visitCall(RexCall call) {
        if (call.getOperator().getKind() == SqlKind.EQUALS) {
          int lPos = pos(call.getOperands().get(0));
          int rPos = pos(call.getOperands().get(1));
          if (lPos != -1 && rPos != -1) {
            JoinConditionBasedPredicateInference.this.equivalent(lPos, rPos);
            JoinConditionBasedPredicateInference.this.equalityPredicates
                .add(call.toString());
          }
        }
        return null;
      }
    }

    /**
     * Setup equivalence Sets for each position. If i & j are equivalent then
     * they will have the same equivalence Set. The algorithm computes the
     * closure relation at each position for the position wrt to positions
     * greater than it. Once a closure is computed for a position, the closure
     * Set is set on all its descendants. So the closure computation buubles up
     * from lower positions and the final equivalence Set is propagated down
     * from the lowest element in the Set.
     *
     */
    class Closure {

      SortedMap<Integer, BitSet> compute() {
        SortedMap<Integer, BitSet> closure = new TreeMap<Integer, BitSet>();

        for (int pos : JoinConditionBasedPredicateInference.this.equivalence
            .keySet()) {
          computeClosure(closure, pos);
        }

        return closure;
      }

      private BitSet computeClosure(SortedMap<Integer,
          BitSet> closure, int pos) {
        BitSet o = closure.get(pos);
        if (o != null) {
          return o;
        }
        BitSet b = equivalence.get(pos);
        o = (BitSet) b.clone();
        int i = b.nextSetBit(pos + 1);
        for (; i >= 0; i = b.nextSetBit(i + 1)) {
          o.or(computeClosure(closure, i));
        }
        closure.put(pos, o);
        i = o.nextSetBit(pos + 1);
        for (; i >= 0; i = b.nextSetBit(i + 1)) {
          closure.put(i, o);
        }
        return o;
      }
    }

    /**
     * Given an expression return all the possible substitutions. For e.g. for
     * an expression 'a + b + c' and the following equivalences: <code>
     * a : {a, b}
     * b : {a, b}
     * c : {c, e}
     * </code>
     *
     * The following Mappings will be returned <code>
     * {a->a, b->a, c->c}
     * {a->a, b->a, c->e}
     * {a->a, b->b, c->c}
     * {a->a, b->b, c->e}
     * {a->b, b->a, c->c}
     * {a->b, b->a, c->e}
     * {a->b, b->b, c->c}
     * {a->b, b->b, c->e}
     * </code>
     *
     * which imply the following inferences <code>
     * a + a + c
     * a + a + e
     * a + b + c
     * a + b + e
     * b + a + c
     * b + a + e
     * b + b + c
     * b + b + e
     * </code>
     */
    class ExprsItr implements Iterator<Mapping> {

      int[] columns;
      BitSet[] columnSets;
      int[] iterationIdx;
      Mapping nextMapping;
      boolean firstCall;

      ExprsItr(RexNode predicate) {
        BitSet fields = exprFields.get(predicate.toString());
        nextMapping = null;

        if (fields.cardinality() == 0) {
          return;
        }
        columns = new int[fields.cardinality()];
        columnSets = new BitSet[fields.cardinality()];
        iterationIdx = new int[fields.cardinality()];
        for (int j = 0, i = fields.nextSetBit(0); i >= 0; i = fields
            .nextSetBit(i + 1), j++) {
          columns[j] = i;
          columnSets[j] = equivalence.get(i);
          iterationIdx[j] = 0;
        }
        firstCall = true;
      }

      public boolean hasNext() {
        if (columns == null) {
          return false;
        }
        if (firstCall) {
          initializeMapping();
          firstCall = false;
        } else {
          computeNextMapping(iterationIdx.length - 1);
        }
        return nextMapping != null;
      }

      public Mapping next() {
        return nextMapping;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      private void computeNextMapping(int level) {
        int t = columnSets[level].nextSetBit(iterationIdx[level]);
        if (t < 0) {
          if (level == 0) {
            nextMapping = null;
          } else {
            iterationIdx[level] = 0;
            computeNextMapping(level - 1);
          }
        } else {
          nextMapping.set(columns[level], t);
          iterationIdx[level] = t + 1;
        }
      }

      private void initializeMapping() {
        nextMapping = Mappings.create(MappingType.PARTIAL_FUNCTION, nSysFields
            + nFieldsLeft + nFieldsRight, nSysFields + nFieldsLeft
            + nFieldsRight);
        for (int i = 0; i < columnSets.length; i++) {
          BitSet c = columnSets[i];
          int t = c.nextSetBit(iterationIdx[i]);
          if (t < 0) {
            nextMapping = null;
            return;
          }
          nextMapping.set(columns[i], t);
          iterationIdx[i] = t + 1;
        }
      }

    }

    private int pos(RexNode expr) {
      if (expr instanceof RexInputRef) {
        return ((RexInputRef) expr).getIndex();
      }
      return -1;
    }

    private boolean isAlwaysTrue(RexNode predicate) {
      if (predicate instanceof RexCall) {
        RexCall c = (RexCall) predicate;
        if (c.getOperator().getKind() == SqlKind.EQUALS) {
          int lPos = pos(c.getOperands().get(0));
          int rPos = pos(c.getOperands().get(1));
          return lPos != -1 && lPos == rPos;
        }
      }
      return predicate.isAlwaysTrue();
    }
  }

}
