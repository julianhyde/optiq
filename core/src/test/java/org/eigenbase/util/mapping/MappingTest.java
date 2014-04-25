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
package org.eigenbase.util.mapping;

import net.hydromatic.linq4j.function.Function1;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for mappings.
 *
 * @see Mapping
 * @see Mappings
 */
public class MappingTest {
  public static final Function1<Integer, Integer> DOUBLE =
      new Function1<Integer, Integer>() {
        public Integer apply(Integer integer) {
          return integer * 2;
        }
      };

  public static final Function1<Integer, Integer> PLUS_ONE =
      new Function1<Integer, Integer>() {
        public Integer apply(Integer integer) {
          return integer + 1;
        }
      };

  public static final Function1<Integer, Integer> MINUS_ONE =
      new Function1<Integer, Integer>() {
        public Integer apply(Integer integer) {
          return integer - 1;
        }
      };

  public MappingTest() {
  }

  @Test public void testMappings() {
    assertTrue(Mappings.isIdentity(Mappings.createIdentity(0)));
    assertTrue(Mappings.isIdentity(Mappings.createIdentity(5)));
    assertFalse(
        Mappings.isIdentity(
            Mappings.create(MappingType.PARTIAL_SURJECTION, 3, 4)));
    assertFalse(
        Mappings.isIdentity(
            Mappings.create(MappingType.PARTIAL_SURJECTION, 3, 3)));
    assertFalse(
        Mappings.isIdentity(
            Mappings.create(MappingType.PARTIAL_SURJECTION, 4, 4)));
  }

  /**
   * Unit test for {@link Mappings#createShiftMapping}.
   */
  @Test public void testMappingsCreateShiftMapping() {
    assertEquals(
        "[size=5, sourceCount=20, targetCount=13, elements=[6:3, 7:4, 15:10, 16:11, 17:12]]",
        Mappings.createShiftMapping(
            20,
            3, 6, 2,
            10, 15, 3).toString());

    // no triples makes for a mapping with 0 targets, 20 sources, but still
    // valid
    Mappings.TargetMapping mapping =
        Mappings.createShiftMapping(
            20);
    assertEquals("[size=0, sourceCount=20, targetCount=0, elements=[]]",
        mapping.toString());
    assertEquals(20, mapping.getSourceCount());
    assertEquals(0, mapping.getTargetCount());
  }

  /**
   * Unit test for {@link Mappings#append}.
   */
  @Test public void testMappingsAppend() {
    assertTrue(
        Mappings.isIdentity(
            Mappings.append(
                Mappings.createIdentity(3),
                Mappings.createIdentity(2))));
    Mapping mapping0 = Mappings.create(MappingType.PARTIAL_SURJECTION, 5, 3);
    mapping0.set(0, 2);
    mapping0.set(3, 1);
    mapping0.set(4, 0);
    assertEquals(
        "[size=5, sourceCount=7, targetCount=5, elements=[0:2, 3:1, 4:0, 5:3, 6:4]]",
        Mappings.append(mapping0, Mappings.createIdentity(2)).toString());
  }

  /**
   * Unit test for {@link Mappings#offsetSource}.
   */
  @Test public void testMappingsOffsetSource() {
    final Mappings.TargetMapping mapping =
        Mappings.target(ImmutableMap.of(0, 5, 1, 7), 2, 8);
    assertEquals(
        "[size=2, sourceCount=2, targetCount=8, elements=[0:5, 1:7]]",
        mapping.toString());
    assertEquals(2, mapping.getSourceCount());
    assertEquals(8, mapping.getTargetCount());

    final Mappings.TargetMapping mapping1 =
        Mappings.offsetSource(mapping, 3, 5);
    assertEquals(
        "[size=2, sourceCount=5, targetCount=8, elements=[3:5, 4:7]]",
        mapping1.toString());
    assertEquals(5, mapping1.getSourceCount());
    assertEquals(8, mapping1.getTargetCount());

    // mapping that extends RHS
    final Mappings.TargetMapping mapping2 =
        Mappings.offsetSource(mapping, 3, 15);
    assertEquals(
        "[size=2, sourceCount=15, targetCount=8, elements=[3:5, 4:7]]",
        mapping2.toString());
    assertEquals(15, mapping2.getSourceCount());
    assertEquals(8, mapping2.getTargetCount());

    try {
      final Mappings.TargetMapping mapping3 =
          Mappings.offsetSource(mapping, 3, 4);
      fail("expected exception, got " + mapping3);
    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  /** Unit test for {@link Mappings#compose}. */
  @Test public void testMappingsCompose() {
    final Mapping mapping1 = Mappings.target(DOUBLE, 4, 8).inverse();
    final Mapping mapping2 = Mappings.target(PLUS_ONE, 8, 9).inverse();
    final Mapping mapping3 = Mappings.compose(mapping2, mapping1);
    assertThat(mapping3.toString(),
        equalTo(
            "[size=4, sourceCount=9, targetCount=4, elements=[1:0, 3:1, 5:2, 7:3]]"));
    assertThat(mapping3.isIdentity(), equalTo(false));

    final Mapping mapping4 = Mappings.target(PLUS_ONE, 4, 5).inverse();
    final Mapping mapping5 = Mappings.target(MINUS_ONE, 5, 4).inverse();
    final Mapping mapping6 = Mappings.compose(mapping5, mapping4);
    assertThat(mapping6.toString(),
        equalTo(
            "[size=4, sourceCount=4, targetCount=4, elements=[0:0, 1:1, 2:2, 3:3]]"));
    assertThat(mapping6.isIdentity(), equalTo(true));

    // anything compose empty --> empty
    final Mapping mapping7 =
        (Mapping) Mappings.target(ImmutableMap.<Integer, Integer>of(), 6, 5);
    final Mapping mapping8 = Mappings.compose(mapping7, mapping4);
    assertThat(mapping8.toString(),
        equalTo("[size=0, sourceCount=6, targetCount=4, elements=[]]"));
    assertThat(mapping8.isIdentity(), equalTo(false));

    // empty compose anything --> empty
    final Mapping mapping9 =
        Mappings.target(ImmutableMap.<Integer, Integer>of(), 10, 4).inverse();
    final Mapping mapping10 = Mappings.compose(mapping4, mapping9);
    assertThat(mapping10.toString(),
        equalTo("[size=0, sourceCount=5, targetCount=10, elements=[]]"));
    assertThat(mapping10.isIdentity(), equalTo(false));

    // target count must match source count
    try {
      final Mapping mapping11 = Mappings.compose(mapping4, mapping1);
      fail("expected error, got " + mapping11);
    } catch (Exception e) {
      assertThat(e.getMessage(),
          equalTo("compose requires left.targetCount = right.sourceCount"));
    }
  }
}

// End MappingTest.java
