/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eigenbase.rel.rules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;
import org.eigenbase.reltype.RelDataTypeField;
import org.eigenbase.rex.*;

import net.hydromatic.optiq.util.BitSets;

import com.google.common.collect.ImmutableList;


/**
 * PushFilterPastAggRule implements the rule for pushing a {@link FilterRelBase}
 * past a {@link AggregateRelBase}.
 */
public class PushFilterPastAggRule extends RelOptRule {

  /** The default instance of
   * {@link org.eigenbase.rel.rules.PushFilterPastAggRule}.
   *
   * <p>It matches any kind of agg. or filter */
  public static final PushFilterPastAggRule INSTANCE =
      new PushFilterPastAggRule(
          FilterRelBase.class,
          RelFactories.DEFAULT_FILTER_FACTORY,
          AggregateRelBase.class);

  private final RelFactories.FilterFactory filterFactory;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a PushFilterPastAggRule.
   *
   * <p>If {@code filterFactory} is null, creates the same kind of filter as
   * matched in the rule. Similarly {@code aggregateFactory}.</p>
   */
  public PushFilterPastAggRule(
      Class<? extends FilterRelBase> filterClass,
      RelFactories.FilterFactory filterFactory,
      Class<? extends AggregateRelBase> aggregateClass) {
    super(
        operand(filterClass,
            operand(aggregateClass, any())));
    this.filterFactory = filterFactory;
  }

  //~ Methods ----------------------------------------------------------------

  // implement RelOptRule
  public void onMatch(RelOptRuleCall call) {
    final FilterRelBase filterRel = call.rel(0);
    final AggregateRelBase aggRel = call.rel(1);

    List<RexNode> condtions =
        RelOptUtil.conjunctions(filterRel.getCondition());
    BitSet groupKeys = aggRel.getGroupSet();
    RexBuilder rexBuilder = filterRel.getCluster().getRexBuilder();
    List<RelDataTypeField> origFields = aggRel.getRowType().getFieldList();
    int[] adjustments = new int[origFields.size()];
    List<RexNode> pushedConds = new ArrayList<RexNode>();

    for (RexNode cond : condtions) {
      BitSet rCols = RelOptUtil.InputFinder.bits(cond);
      if (BitSets.contains(groupKeys, rCols)) {
        pushedConds.add(cond.accept(new RelOptUtil.RexInputConverter(
            rexBuilder, origFields, aggRel.getInput(0).getRowType()
                .getFieldList(), adjustments)));
      }
    }

    RexNode pushedCond = RexUtil.composeConjunction(rexBuilder, pushedConds,
        true);

    if (pushedCond != null) {
      RelNode newFilterRel = filterFactory.createFilter(aggRel.getInput(0),
          pushedCond);
      RelNode newAggRel = aggRel.copy(aggRel.getTraitSet(),
          ImmutableList.of(newFilterRel));
      call.transformTo(newAggRel);
    }
  }
}
