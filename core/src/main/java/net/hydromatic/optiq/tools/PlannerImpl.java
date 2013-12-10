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
import net.hydromatic.optiq.Schemas;
import net.hydromatic.optiq.jdbc.OptiqPrepare;
import net.hydromatic.optiq.prepare.Prepare.PreparedExplain;

import org.eigenbase.rel.RelNode;
import org.eigenbase.relopt.RelTraitSet;
import org.eigenbase.sql.SqlNode;
import org.eigenbase.sql.SqlOperatorTable;
import org.eigenbase.sql.parser.SqlParseException;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;

/** Implementation of {@link Planner}. */
class PlannerImpl extends Planner {
  private final Schema rootSchema;
  private final Schema defaultSchema;
  private final SqlOperatorTable operatorTable;
  private final ImmutableList<RuleSet> ruleSets;
  private State state = State.RESET;

  public PlannerImpl(Schema rootSchema,
      Schema defaultSchema, SqlOperatorTable operatorTable,
      ImmutableList<RuleSet> ruleSets) {
    this.rootSchema = rootSchema;
    this.defaultSchema = defaultSchema;
    this.operatorTable = operatorTable;
    this.ruleSets = ruleSets;
  }

  public SqlNode parse(String sql) throws SqlParseException {
    requireState(State.RESET);
    OptiqPrepare.ParseResult parse = Schemas.parse(defaultSchema, null, sql);
    return parse.sqlNode;
  }

  private void requireState(State states) {
    if (!Arrays.asList(states).contains(state)) {
      throw new IllegalArgumentException("Action not possible in state "
          + state);
    }
  }

  public RelNode toRel(SqlNode sql) throws RelConversionException {
    throw new UnsupportedOperationException();
  }

  public PreparedExplain explain(RelNode rel) {
    throw new UnsupportedOperationException();
  }

  public RelNode transform(int ruleSetIndex, RelTraitSet requiredOutputTraits,
      RelNode rel) throws RelConversionException {
    throw new UnsupportedOperationException();
  }

  public void reset() {
    state = State.RESET;
  }

  public void close() {
    state = State.CLOSED;
  }

  enum State {
    RESET,
    CLOSED,
  }
}

// End PlannerImpl.java
