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
import net.hydromatic.optiq.prepare.Prepare.PreparedExplain;

import org.eigenbase.rel.RelNode;
import org.eigenbase.relopt.RelTraitSet;
import org.eigenbase.sql.SqlNode;
import org.eigenbase.sql.SqlOperatorTable;
import org.eigenbase.sql.parser.SqlParseException;

import com.google.common.collect.ImmutableList;

/**
 * A fa&ccedil;ade that covers Optiq's query planning process: parse SQL,
 * validate the parse tree, convert the parse tree to a relational expression,
 * and optimize the relational expression.
 *
 * <p>Planner is NOT thread safe. However, it can be reused for
 * different queries. The consumer of this interface is responsible for calling
 * reset() after each use of Planner that corresponds to a different
 * query.
 */
public abstract class Planner {

  /**
   * Creates an instance of {@code Planner}.
   *
   * @param rootSchema The root schema for the query.
   * @param defaultSchema The default schema first used to resolve references.
   * @param operatorTable The instance of SqlOperatorTable that be should to
   *     resolve Optiq operators.
   * @param ruleSets An array of one or more rule sets used during the course of
   *                 query evaluation. The common use case is when there is a
   *                 single rule set and {@link #toRel} will only be called
   *                 once. However, consumers may also register multiple
   *                 {@link RuleSet}s and do multiple repetitions of
   *                 {@link #transform} planning cycles using different indices.
   *                 The order of rule sets provided here determines the
   *                 zero-based indices of rule sets elsewhere in this class.
   * @return The Planner object.
   */
  public static Planner getPlanner(Schema rootSchema,
      Schema defaultSchema, SqlOperatorTable operatorTable,
      RuleSet... ruleSets) {
    return new PlannerImpl(rootSchema, defaultSchema, operatorTable,
        ImmutableList.copyOf(ruleSets));
  }

  /**
   * Parses and validates a SQL statement.
   *
   * @param sql The SQL statement to parse.
   * @return The root node of the SQL parse tree.
   * @throws SqlParseException on parse error
   */
  public abstract SqlNode parse(String sql) throws SqlParseException;

  /**
   * Convert a SQL parse tree into a tree of relational expressions.
   *
   * @param sql The root node of the SQL parse tree.
   * @return The root node of the newly generated RelNode tree.
   * @throws RelConversionException
   */
  public abstract RelNode toRel(SqlNode sql) throws RelConversionException;

  /**
   * Converts a tree of relational expressions into an explanation of the
   * QueryPlan.
   *
   * @param rel The root of the RelNode tree to convert.
   * @return The prepared explanation.
   */
  public abstract PreparedExplain explain(RelNode rel);

  /**
   * Converts one relational expression tree into another relational expression
   * based on a particular rule set and requires set of traits.
   *
   * @param ruleSetIndex The RuleSet to use for conversion purposes.  Note that
   *                     this is zero-indexed and is based on the list and order
   *                     of RuleSets provided in the construction of this
   *                     {@link Planner}.
   * @param requiredOutputTraits The set of RelTraits required of the root node
   *                             at the termination of the planning cycle.
   * @param rel The root of the RelNode tree to convert.
   * @return The root of the new RelNode tree.
   * @throws RelConversionException on conversion error
   */
  public abstract RelNode transform(int ruleSetIndex,
      RelTraitSet requiredOutputTraits, RelNode rel)
      throws RelConversionException;

  /**
   * Resets this {@link Planner} to be used with a new query. This
   * should be called between each new query.
   */
  public abstract void reset();

  /**
   * Releases all internal resources utilized while this {@link Planner}
   * exists.  Once called, this Planner object is no longer valid.
   */
  public abstract void close();
}

// End Planner.java
