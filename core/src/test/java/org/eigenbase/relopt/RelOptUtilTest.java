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
package org.eigenbase.relopt;

import java.util.List;

import org.eigenbase.rel.ProjectRel;
import org.eigenbase.rel.ProjectRelBase;
import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.ValuesRel;
import org.eigenbase.reltype.*;
import org.eigenbase.rex.RexBuilder;
import org.eigenbase.rex.RexLiteral;
import org.eigenbase.rex.RexNode;
import org.eigenbase.sql.type.*;
import org.eigenbase.util.*;
import org.eigenbase.util.mapping.Mapping;

import net.hydromatic.optiq.SchemaPlus;
import net.hydromatic.optiq.tools.Frameworks;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

/**
 * Unit test for {@link RelOptUtil} and other classes in this package.
 */
public class RelOptUtilTest {
  //~ Constructors -----------------------------------------------------------

  public RelOptUtilTest() {
  }

  //~ Methods ----------------------------------------------------------------

  @Test public void testTypeDump() {
    RelDataTypeFactory typeFactory = new SqlTypeFactoryImpl();
    RelDataType t1 =
        typeFactory.builder()
            .add("f0", SqlTypeName.DECIMAL, 5, 2)
            .add("f1", SqlTypeName.VARCHAR, 10)
            .build();
    TestUtil.assertEqualsVerbose(
        TestUtil.fold(
            "f0 DECIMAL(5, 2) NOT NULL,",
            "f1 VARCHAR(10) CHARACTER SET \"ISO-8859-1\" COLLATE \"ISO-8859-1$en_US$primary\" NOT NULL"),
        Util.toLinux(RelOptUtil.dumpType(t1) + "\n"));

    RelDataType t2 =
        typeFactory.builder()
            .add("f0", t1)
            .add("f1", typeFactory.createMultisetType(t1, -1))
            .build();
    TestUtil.assertEqualsVerbose(
        TestUtil.fold(
            "f0 RECORD (",
            "  f0 DECIMAL(5, 2) NOT NULL,",
            "  f1 VARCHAR(10) CHARACTER SET \"ISO-8859-1\" COLLATE \"ISO-8859-1$en_US$primary\" NOT NULL) NOT NULL,",
            "f1 RECORD (",
            "  f0 DECIMAL(5, 2) NOT NULL,",
            "  f1 VARCHAR(10) CHARACTER SET \"ISO-8859-1\" COLLATE \"ISO-8859-1$en_US$primary\" NOT NULL) NOT NULL MULTISET NOT NULL"),
        Util.toLinux(RelOptUtil.dumpType(t2) + "\n"));
  }

  /**
   * Tests the rules for how we name rules.
   */
  @Test public void testRuleGuessDescription() {
    assertEquals("Bar", RelOptRule.guessDescription("com.foo.Bar"));
    assertEquals("Baz", RelOptRule.guessDescription("com.flatten.Bar$Baz"));

    // yields "1" (which as an integer is an invalid
    try {
      Util.discard(RelOptRule.guessDescription("com.foo.Bar$1"));
      fail("expected exception");
    } catch (RuntimeException e) {
      assertEquals(
          "Derived description of rule class com.foo.Bar$1 is an "
          + "integer, not valid. Supply a description manually.",
          e.getMessage());
    }
  }

  /** Tests {@link RelOptUtil#splitProject(org.eigenbase.rel.RelNode)}. */
  @Test public void testSplitProject() {
    Frameworks.withPlanner(
        new Frameworks.PlannerAction<Object>() {
          public Object apply(RelOptCluster cluster, RelOptSchema relOptSchema,
              SchemaPlus rootSchema) {
            final RexBuilder rexBuilder = cluster.getRexBuilder();
            final RelDataTypeFactory typeFactory = cluster.getTypeFactory();
            final RelDataType rowType = typeFactory.builder()
                .add("i", SqlTypeName.INTEGER)
                .add("j", SqlTypeName.BOOLEAN)
                .add("k", SqlTypeName.DOUBLE)
                .build();
            final ValuesRel values =
                new ValuesRel(cluster, rowType,
                    ImmutableList.<List<RexLiteral>>of());
            final ProjectRel project =
                new ProjectRel(cluster, values,
                    ImmutableList.<RexNode>of(
                        rexBuilder.makeInputRef(values, 2),
                        rexBuilder.makeInputRef(values, 0)),
                    null, ProjectRelBase.Flags.BOXED);
            final Pair<RelNode, Mapping> pair =
                RelOptUtil.splitProject(project);
            assertThat(pair, notNullValue());
            assertThat(pair.left, equalTo((RelNode) values));
            assertThat(pair.right.toString(), equalTo("[2:0,0:1]"));

            // split project-on-project returns a composite mapping
            final ProjectRel project2 =
                new ProjectRel(cluster, project,
                    ImmutableList.<RexNode>of(
                        rexBuilder.makeInputRef(project, 1),
                        rexBuilder.makeInputRef(project, 0)),
                    null, ProjectRelBase.Flags.BOXED);
            final Pair<RelNode, Mapping> pair2 =
                RelOptUtil.splitProject(project2);
            assertThat(pair2, notNullValue());
            assertThat(pair2.left, equalTo((RelNode) values));
            assertThat(pair2.right.toString(),
                equalTo(
                    "[size=2, sourceCount=3, targetCount=2, elements=[0:0, 2:1]]"));
            return null;
          }
        });
  }
}

// End RelOptUtilTest.java
