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
package net.hydromatic.optiq.tools;

import net.hydromatic.optiq.Schema;
import net.hydromatic.optiq.impl.java.ReflectiveSchema;

import org.eigenbase.sql.SqlNode;
import org.eigenbase.sql.fun.SqlStdOperatorTable;
import org.eigenbase.sql.parser.SqlParseException;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link net.hydromatic.optiq.tools.Planner}.
 */
public class PlannerTest {
  @Ignore // there are still bugs
  @Test public void testParse() throws SqlParseException {
    Schema schema = ReflectiveSchema.create(null, "hr");
    Planner planner =
        Planner.getPlanner(schema, schema, SqlStdOperatorTable.instance());
    SqlNode parse =
        planner.parse("select * from \"emps\" where name like '%e%'");
    assertEquals("xxx", parse.toString());
  }
}

// End PlannerTest.java
