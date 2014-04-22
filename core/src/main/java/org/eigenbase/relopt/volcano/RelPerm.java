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

import org.eigenbase.rel.RelNode;
import org.eigenbase.relopt.RelOptUtil;
import org.eigenbase.reltype.RelDataType;
import org.eigenbase.util.mapping.Mapping;
import org.eigenbase.util.mapping.Mappings;
import org.eigenbase.util.mapping.Surjection;

/**
 * A use of a {@link RelSubset} that changes the order of the columns returned.
 */
public class RelPerm extends RelUse {
  public final RelSubset left;
  public final Surjection right;

  public RelPerm(RelSubset left, Surjection right) {
    super(left.getCluster(), left.getTraitSet());
    this.left = left;
    this.right = right;
  }

  public static RelUse of(RelSubset left, Surjection right) {
    if (right == null || Mappings.isIdentity((Mappings.TargetMapping) right)) {
      return left;
    }
    return new RelPerm(left, right);
  }

  @Override
  protected RelDataType deriveRowType() {
    return getCluster().getTypeFactory().createStructType(
        Mappings.apply((Mapping) right, left.getRowType().getFieldList()));
  }

  RelNode toRel() {
    return RelOptUtil.project(left, (Mappings.TargetMapping) right);
  }

  RelSubset getSubset() {
    return left;
  }

  RelSet getSet() {
    return left.getSet();
  }

  Surjection mapping() {
    return right;
  }
}

// End RelPerm.java
