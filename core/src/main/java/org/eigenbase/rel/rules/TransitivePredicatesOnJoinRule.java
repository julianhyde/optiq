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
package org.eigenbase.rel.rules;

import org.eigenbase.rel.FilterRel;
import org.eigenbase.rel.JoinRelBase;
import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.metadata.RelMetadataQuery;
import org.eigenbase.relopt.RelOptPulledUpPredicates;
import org.eigenbase.relopt.RelOptRule;
import org.eigenbase.relopt.RelOptRuleCall;
import org.eigenbase.rex.RexBuilder;
import org.eigenbase.rex.RexNode;
import org.eigenbase.rex.RexUtil;

import com.google.common.collect.ImmutableList;

/**
 * A Rule to apply inferred predicates from {@link RelMdPredicates}.
 * Predicates returned in {@link RelOptPulledUpPredicates} is applied
 * appropriately.
 *
 */
public class TransitivePredicatesOnJoinRule extends RelOptRule {

  /** The singleton. */
  public static final TransitivePredicatesOnJoinRule INSTANCE =
      new TransitivePredicatesOnJoinRule();

  /**
   *
   */
  private TransitivePredicatesOnJoinRule() {
    this(JoinRelBase.class);
  }

  /**
   *
   * @param clazz
   */
  public TransitivePredicatesOnJoinRule(Class<? extends JoinRelBase> clazz) {
    super(operand(clazz, any()));
  }

  @Override
  public void onMatch(RelOptRuleCall call) {
    JoinRelBase join = call.rel(0);
    RelOptPulledUpPredicates mdPreds = RelMetadataQuery
        .getPulledUpPredicates(join);

    ImmutableList<RexNode> pulledUpPredicates =
        mdPreds.getPulledUpPredicates();
    ImmutableList<RexNode> leftInferredPreds =
        mdPreds.getLeftInferredPredicates();
    ImmutableList<RexNode> rightInferredPreds =
        mdPreds.getRightInferredPredicates();

    if (leftInferredPreds.isEmpty()
        && rightInferredPreds.isEmpty()) {
      return;
    }

    RexBuilder rB = join.getCluster().getRexBuilder();
    RelNode lChild = join.getLeft();
    RelNode rChild = join.getRight();

    if (leftInferredPreds.size() > 0) {
      RelNode curr = lChild;
      lChild = createFilter(lChild,
          RexUtil.composeConjunction(rB, leftInferredPreds, false));
      call.getPlanner().onCopy(curr, lChild);
    }

    if (rightInferredPreds.size() > 0) {
      RelNode curr = rChild;
      rChild = createFilter(rChild,
          RexUtil.composeConjunction(rB, rightInferredPreds, false));
      call.getPlanner().onCopy(curr, rChild);
    }

    RelNode newRel = join.copy(join.getTraitSet(), join.getCondition(),
        lChild,
        rChild,
        join.getJoinType(), join.isSemiJoinDone());
    call.getPlanner().onCopy(join, newRel);

    call.transformTo(newRel);
  }

  // @todo: setup a {@link RelFactories} FilterFactory
  private RelNode createFilter(RelNode child, RexNode condition) {
    return new FilterRel(child.getCluster(), child, condition);
  }
}
