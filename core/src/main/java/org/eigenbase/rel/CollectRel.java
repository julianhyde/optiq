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
package org.eigenbase.rel;

import java.util.List;

import org.eigenbase.relopt.*;
import org.eigenbase.reltype.*;
import org.eigenbase.sql.type.*;

/**
 * A relational expression that collapses multiple rows into one.
 *
 * <p>Rules:
 *
 * <ul>
 * <li>{@code net.sf.farrago.fennel.rel.FarragoMultisetSplitterRule}
 * creates a CollectRel from a call to {@link
 * org.eigenbase.sql.fun.SqlMultisetValueConstructor} or to {@link
 * org.eigenbase.sql.fun.SqlMultisetQueryConstructor}.</li>
 * </ul>
 * </p>
 */
public final class CollectRel extends SingleRel {
  //~ Instance fields --------------------------------------------------------

  private final String fieldName;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a CollectRel.
   *
   * @param cluster   Cluster
   * @param child     Child relational expression
   * @param fieldName Name of the sole output field
   */
  public CollectRel(
      RelOptCluster cluster,
      RelNode child,
      String fieldName) {
    super(
        cluster,
        cluster.traitSetOf(Convention.NONE),
        child);
    this.fieldName = fieldName;
  }

  /**
   * Creates a CollectRel by parsing serialized output.
   */
  public CollectRel(RelInput input) {
    this(input.getCluster(), input.getInput(), input.getString("field"));
  }

  //~ Methods ----------------------------------------------------------------

  @Override
  public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
    assert traitSet.comprises(Convention.NONE);
    return new CollectRel(
        getCluster(),
        sole(inputs),
        fieldName);
  }

  @Override
  public RelWriter explainTerms(RelWriter pw) {
    return super.explainTerms(pw)
        .item("field", fieldName);
  }

  /**
   * Returns the name of the sole output field.
   *
   * @return name of the sole output field
   */
  public String getFieldName() {
    return fieldName;
  }

  @Override
  protected RelDataType deriveRowType() {
    return deriveCollectRowType(this, fieldName);
  }

  /**
   * Derives the output type of a collect relational expression.
   *
   * @param rel       relational expression
   * @param fieldName name of sole output field
   * @return output type of a collect relational expression
   */
  public static RelDataType deriveCollectRowType(
      SingleRel rel,
      String fieldName) {
    RelDataType childType = rel.getChild().getRowType();
    assert childType.isStruct();
    RelDataType ret =
        SqlTypeUtil.createMultisetType(
            rel.getCluster().getTypeFactory(),
            childType,
            false);
    ret =
        rel.getCluster().getTypeFactory().createStructType(
            new RelDataType[]{ret},
            new String[]{fieldName});
    return rel.getCluster().getTypeFactory().createTypeWithNullability(
        ret,
        false);
  }
}

// End CollectRel.java
