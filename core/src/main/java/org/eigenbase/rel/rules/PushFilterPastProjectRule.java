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
import java.util.List;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;
import org.eigenbase.rex.*;

/**
 * PushFilterPastProjectRule implements the rule for pushing a {@link FilterRel}
 * past a {@link ProjectRel}.
 */
public class PushFilterPastProjectRule extends RelOptRule {
  /** The default instance of
   * {@link org.eigenbase.rel.rules.PushFilterPastJoinRule}.
   *
   * <p>It matches any kind of join or filter, and generates the same kind of
   * join and filter. It uses null values for {@code filterFactory} and
   * {@code projectFactory} to achieve this. */
  public static final PushFilterPastProjectRule INSTANCE =
      new PushFilterPastProjectRule(
          FilterRelBase.class, null,
          ProjectRelBase.class, null);

  private final RelFactories.FilterFactory filterFactory;
  private final RelFactories.ProjectFactory projectFactory;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a PushFilterPastProjectRule.
   *
   * <p>If {@code filterFactory} is null, creates the same kind of filter as
   * matched in the rule. Similarly {@code projectFactory}.</p>
   */
  public PushFilterPastProjectRule(
      Class<? extends FilterRelBase> filterClass,
      RelFactories.FilterFactory filterFactory,
      Class<? extends ProjectRelBase> projectRelBaseClass,
      RelFactories.ProjectFactory projectFactory) {
    super(
        operand(filterClass,
            operand(projectRelBaseClass, any())));
    this.filterFactory = filterFactory;
    this.projectFactory = projectFactory;
  }

  //~ Methods ----------------------------------------------------------------

  // implement RelOptRule
  public void onMatch(RelOptRuleCall call) {
    final FilterRelBase filterRel = call.rel(0);
    final ProjectRelBase projRel = call.rel(1);

    if (projectHasOver(projRel)) {
      return;
    }

    // convert the filter to one that references the child of the project
    RexNode newCondition =
        RelOptUtil.pushFilterPastProject(filterRel.getCondition(), projRel);

    RelNode newFilterRel =
        filterFactory == null
            ? filterRel.copy(filterRel.getTraitSet(), projRel.getChild(),
                newCondition)
            : filterFactory.createFilter(projRel.getChild(), newCondition);

    RelNode newProjRel =
        projectFactory == null
            ? projRel.copy(projRel.getTraitSet(), newFilterRel,
                projRel.getProjects(), projRel.getRowType())
            : projectFactory.createProject(newFilterRel, projRel.getProjects(),
                projRel.getRowType().getFieldNames());

    call.transformTo(newProjRel);
  }

  // In general a Filter cannot be pushed below a Windowing Calculation.
  // Applying the Filter before the Aggregation Function changes
  // the results of the Windowing invocation.
  //
  // When the Filter is on the Partition expression of the Over clause
  // it can be pushed down. For now we don't support this.
  // @todo support this use case.
  boolean projectHasOver(ProjectRelBase projRel) {
    final List<RexOver> overExprInProjection = new ArrayList<RexOver>();

    RexVisitor<Void> checkOver = new RexVisitorImpl<Void>(true) {
      public Void visitOver(RexOver over) {
        overExprInProjection.add(over);
        return null;
      }
    };

    for (RexNode p : projRel.getProjects()) {
      p.accept(checkOver);
      if (overExprInProjection.size() > 0) {
        return true;
      }
    }
    return false;
  }
}

// End PushFilterPastProjectRule.java
