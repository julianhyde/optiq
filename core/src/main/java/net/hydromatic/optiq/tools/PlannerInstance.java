package net.hydromatic.optiq.tools;

import net.hydromatic.optiq.Schema;
import net.hydromatic.optiq.prepare.Prepare.PreparedExplain;

import org.eigenbase.rel.RelNode;
import org.eigenbase.relopt.RelTraitSet;
import org.eigenbase.sql.SqlNode;
import org.eigenbase.sql.SqlOperatorTable;
import org.eigenbase.sql.parser.SqlParseException;
import org.eigenbase.sql.validate.SqlValidator;

/**
 * A utility class that allows external frameworks to access internals of the Optiq Planner. PlannerInstance is NOT
 * thread safe. However, it can be reused for different queries. The consumer of this interface is responsible for
 * calling reset() after each use of PlannerInstance that corresponds to a different query.
 */
public abstract class PlannerInstance {

  /**
   * Get a new instance for Query planning purposes
   * 
   * @param rootSchema
   *          The root schema for the query.
   * @param defaultSchema
   *          The default schema first used to resolve references.
   * @param operatorTable
   *          The instance of SqlOperatorTable that be should to resolve Optiq operators.
   * @param ruleSets
   *          An array of one or more rule sets used during the course of query evaluation. The common use case is when
   *          there is a single ruleset and toRel will only be called once. However, consumers may also register
   *          multiple RuleSets and do multiple repetitions of RelToRel planning cycles using different indices. The
   *          order of rule sets provided here determine the zero-index based indices of rulesets elsewhere in this
   *          class.
   * @return The PlannerInstance object.
   */
  public static PlannerInstance getPlanner(Schema rootSchema, Schema defaultSchema, SqlOperatorTable operatorTable,
      RuleSet... ruleSets) {
    throw new UnsupportedOperationException();
  }

  /**
   * Parse a SQL statement.
   * 
   * @param sql
   *          The SQL statement to parse.
   * @return The root node of the SQL parse tree.
   * @throws SqlParseException
   */
  public abstract SqlNode parse(String sql) throws SqlParseException;

  /**
   * Convert a SqlParseResult into a tree of RelNodes
   * @param sql The root node of the SQL parse tree.
   * @return The root node of the newly generated RelNode tree.
   * @throws RelConversionException
   */
  public abstract RelNode toRel(SqlNode sql) throws RelConversionException;

  /**
   * Convert a tree of RelNodes into an explanation of the QueryPlan.
   * @param rel The root of the RelNode tree to convert.
   * @return The prepared explanation.
   */
  public abstract PreparedExplain explain(RelNode rel);

  /**
   * Convert one RelNode tree into another RelNode tree based on a particular RuleSet and requires set of traits.
   * @param ruleSetIndex The RuleSet to use for conversion purposes.  Note that this is zero-indexed and is based on the list and order of RuleSets provided in the construction of this PlannerInstance.
   * @param requiredOutputTraits The set of RelTraits required of the root node at the termination of the planning cycle. 
   * @param rel The root of the RelNode tree to convert.
   * @return The root of the new RelNode tree.
   * @throws RelConversionException
   */
  public abstract RelNode toRel(int ruleSetIndex, RelTraitSet requiredOutputTraits, RelNode rel)
      throws RelConversionException;

  /**
   * Resets this instance of Planner Instance to be used with a new query. This should be called between each new query.
   */
  public abstract void reset();
  
  /**
   * Releases all internal resources utilized while this Planner Instance exists.  Once called, this PlannerInstance object is no longer valid.
   */
  public abstract void close();

}
