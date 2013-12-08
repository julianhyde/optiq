package net.hydromatic.optiq.tools;

import org.eigenbase.relopt.RelOptRule;

/**
 * A RuleSet describes a set of rules associated with a particular type of invocation of the PlannerInstance. The
 */
public interface RuleSet extends Iterable<RelOptRule> {}