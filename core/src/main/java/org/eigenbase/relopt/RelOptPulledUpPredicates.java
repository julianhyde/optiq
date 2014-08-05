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
package org.eigenbase.relopt;

import org.eigenbase.rex.RexNode;

import com.google.common.collect.ImmutableList;

/**
 *
 */
public class RelOptPulledUpPredicates {

  ImmutableList<RexNode> pulledUpPredicates;
  ImmutableList<RexNode> leftInferredPredicates;
  ImmutableList<RexNode> rightInferredPredicates;

  public RelOptPulledUpPredicates(Iterable<RexNode> pulledUpPredicates,
      Iterable<RexNode> leftInferredPredicates,
      Iterable<RexNode> rightInferredPredicates) {
    super();
    this.pulledUpPredicates = ImmutableList.copyOf(pulledUpPredicates);
    this.leftInferredPredicates = ImmutableList.copyOf(leftInferredPredicates);
    this.rightInferredPredicates =
        ImmutableList.copyOf(rightInferredPredicates);
  }

  public ImmutableList<RexNode> getPulledUpPredicates() {
    return pulledUpPredicates;
  }

  public ImmutableList<RexNode> getLeftInferredPredicates() {
    return leftInferredPredicates;
  }

  public ImmutableList<RexNode> getRightInferredPredicates() {
    return rightInferredPredicates;
  }
}
