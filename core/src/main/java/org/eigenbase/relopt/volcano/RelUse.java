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
package org.eigenbase.relopt.volcano;

import org.eigenbase.rel.AbstractRelNode;
import org.eigenbase.rel.RelNode;
import org.eigenbase.relopt.RelOptCluster;
import org.eigenbase.relopt.RelTraitSet;
import org.eigenbase.util.mapping.Surjection;

/**
 * Use of a registered relational expression.
 *
 * <p>A registered relational expression is a {@link RelSubset}. Accordingly,
 * the two sub-types of {@code RelUse} are {@link RelSubset} (an unordered
 * subset) and {@link RelPerm} (a subset with column order changed).</p>
 */
public abstract class RelUse extends AbstractRelNode {
  public RelUse(RelOptCluster cluster, RelTraitSet traitSet) {
    super(cluster, traitSet);
  }

  /** Returns an equivalent relational expression. */
  abstract RelNode toRel();

  /** Returns the underlying subset. */
  abstract RelSubset getSubset();

  /** Returns the set that this use belongs to. */
  abstract RelSet getSet();

  /** Returns the mapping onto the columns of the set. */
  abstract Surjection mapping();
}

// End RelUse.java
