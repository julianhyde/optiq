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
package net.hydromatic.optiq.impl.mongodb;

import org.eigenbase.rel.*;
import org.eigenbase.rel.convert.ConverterRule;
import org.eigenbase.relopt.*;
import org.eigenbase.reltype.RelDataType;
import org.eigenbase.reltype.RelDataTypeFactory;
import org.eigenbase.rex.*;
import org.eigenbase.sql.fun.SqlStdOperatorTable;
import org.eigenbase.sql.type.SqlTypeName;
import org.eigenbase.util.Pair;
import org.eigenbase.util.Util;

import java.util.*;

/**
 * Rules and relational operators for
 * {@link MongoRel#CONVENTION MONGO}
 * calling convention.
 */
public class MongoRules {
  private MongoRules() {}

  public static final RelOptRule[] RULES = {
    new PushProjectOntoMongoRule(),
    new MongoSortRule(),
    new MongoFilterRule(),
  };

  /** Rule that combines a {@link ProjectRel} with a {@link MongoTableScan},
   * creating a new table scan with a list of columns to be projected. */
  private static class PushProjectOntoMongoRule extends RelOptRule {
    private PushProjectOntoMongoRule() {
      super(
          operand(
              ProjectRel.class,
              operand(MongoTableScan.class, none())));
    }

    @Override
    public void onMatch(RelOptRuleCall call) {
      final ProjectRel project = call.rel(0);
      final MongoTableScan table = call.rel(1);
      if (!table.ops.isEmpty()) {
        return;
      }
      final RelOptCluster cluster = table.getCluster();
      final RelDataTypeFactory typeFactory = cluster.getTypeFactory();
      final ItemFinder itemFinder = new ItemFinder(typeFactory);
      final List<RexNode> newProjects = new ArrayList<RexNode>();
      for (RexNode rex : project.getProjects()) {
        final RexNode rex2 = rex.accept(itemFinder);
        final RexNode rex3 =
            cluster.getRexBuilder().ensureType(rex.getType(), rex2, true);
        newProjects.add(rex3);
      }

      final List<Pair<String, String>> ops =
          new ArrayList<Pair<String, String>>(table.ops);
      final String findString =
          Util.toString(itemFinder.items, "{", ", ", "}");
      final String aggregateString = "{$project: " + findString + "}";
      ops.add(Pair.of(findString, aggregateString));
      final RelDataType rowType = itemFinder.builder.build();
      final MongoTableScan newTable =
          new MongoTableScan(cluster, table.getTraitSet(), table.getTable(),
              table.mongoTable, rowType, ops);
      final ProjectRel newProject =
          new ProjectRel(cluster, cluster.traitSetOf(RelCollationImpl.EMPTY),
              newTable, newProjects,
              project.getRowType(), ProjectRel.Flags.BOXED);
      call.transformTo(newProject);
    }
  }

  private static String parseFieldAccess(RexNode rex) {
    if (rex instanceof RexCall) {
      final RexCall call = (RexCall) rex;
      if (call.getOperator() == SqlStdOperatorTable.ITEM
          && call.getOperands().size() == 2
          && call.getOperands().get(0) instanceof RexInputRef
          && ((RexInputRef) call.getOperands().get(0)).getIndex() == 0
          && call.getOperands().get(1) instanceof RexLiteral) {
        RexLiteral arg = (RexLiteral) call.getOperands().get(1);
        if (arg.getTypeName() == SqlTypeName.CHAR) {
          return (String) arg.getValue2();
        }
      }
    }
    return null;
  }

  private static RelDataType parseCast(RexNode rex) {
    if (rex instanceof RexCall) {
      final RexCall call = (RexCall) rex;
      if (call.getOperator() == SqlStdOperatorTable.CAST) {
        assert call.getOperands().size() == 1;
        return call.getType();
      }
    }
    return null;
  }

  // Not currently used. Keep it around for a while. It may evolve into
  // something that can handle complex expressions.
  private static class ItemFinder extends RexShuttle {
    private final Map<String, RexInputRef> map =
        new LinkedHashMap<String, RexInputRef>();
    private final RelDataTypeFactory.FieldInfoBuilder builder;
    public List<String> items = new ArrayList<String>();

    ItemFinder(RelDataTypeFactory typeFactory) {
      builder = typeFactory.builder();
    }

    @Override
    public RexNode visitCall(RexCall call) {
      String fieldName = parseFieldAccess(call);
      if (fieldName != null) {
        return registerField(fieldName, call.getType());
      }
      RelDataType type = parseCast(call);
      if (type != null) {
        final RexNode operand = call.getOperands().get(0);
        fieldName = parseFieldAccess(operand);
        if (fieldName != null) {
          return registerField(fieldName, call.getType());
        }
        // just ignore the cast
        return operand.accept(this);
      }
      return super.visitCall(call);
    }

    private RexNode registerField(String fieldName, RelDataType type) {
      RexInputRef x = map.get(fieldName);
      if (x == null) {
        x = new RexInputRef(map.size(), type);
        map.put(fieldName, x);
        builder.add(fieldName, type);
        items.add(fieldName + ": 1");
      }
      return x;
    }
  }


  abstract static class MongoConverterRule extends ConverterRule {
    protected final Convention out;
    public MongoConverterRule(
        Class<? extends RelNode> clazz,
        RelTrait in,
        Convention out,
        String description) {
      super(clazz, in, out, description);
      this.out = out;
    }
  }

  /**
   * Rule to convert a {@link org.eigenbase.rel.SortRel} to a
   * {@link MongoSortRel}.
   */
  private static class MongoSortRule
      extends MongoConverterRule {
    private MongoSortRule() {
      super(
          SortRel.class,
          Convention.NONE,
          MongoRel.CONVENTION,
          "MongoSortRule");
    }

    public RelNode convert(RelNode rel) {
      final SortRel sort = (SortRel) rel;
      final RelTraitSet traitSet =
          sort.getTraitSet().replace(out)
              .replace(sort.getCollation());
      return new MongoSortRel(
          rel.getCluster(),
          traitSet,
          convert(sort.getChild(), traitSet.replace(RelCollationImpl.EMPTY)),
          sort.getCollation());
    }
  }

  /**
   * Rule to convert a {@link org.eigenbase.rel.FilterRel} to a
   * {@link MongoFilterRel}.
   */
  private static class MongoFilterRule extends MongoConverterRule {
    private MongoFilterRule() {
      super(
          FilterRel.class,
          Convention.NONE,
          MongoRel.CONVENTION,
          "MongoFilterRule");
    }

    public RelNode convert(RelNode rel) {
      final FilterRel filter = (FilterRel) rel;
      final RelTraitSet traitSet = filter.getTraitSet().replace(out);
      return new MongoFilterRel(
          rel.getCluster(),
          traitSet,
          convert(filter.getChild(), traitSet),
          filter.getCondition());
    }
  }

/*

  /**
   * Rule to convert a {@link CalcRel} to an
   * {@link MongoCalcRel}.
   o/
  private static class MongoCalcRule
      extends MongoConverterRule {
    private MongoCalcRule(MongoConvention out) {
      super(
          CalcRel.class,
          Convention.NONE,
          out,
          "MongoCalcRule");
    }

    public RelNode convert(RelNode rel) {
      final CalcRel calc = (CalcRel) rel;

      // If there's a multiset, let FarragoMultisetSplitter work on it
      // first.
      if (RexMultisetUtil.containsMultiset(calc.getProgram())) {
        return null;
      }

      return new MongoCalcRel(
          rel.getCluster(),
          rel.getTraitSet().replace(out),
          convert(
              calc.getChild(),
              calc.getTraitSet().replace(out)),
          calc.getProgram(),
          ProjectRelBase.Flags.Boxed);
    }
  }

  public static class MongoCalcRel extends SingleRel implements MongoRel {
    private final RexProgram program;

    /**
     * Values defined in {@link org.eigenbase.rel.ProjectRelBase.Flags}.
     o/
    protected int flags;

    public MongoCalcRel(
        RelOptCluster cluster,
        RelTraitSet traitSet,
        RelNode child,
        RexProgram program,
        int flags) {
      super(cluster, traitSet, child);
      assert getConvention() instanceof MongoConvention;
      this.flags = flags;
      this.program = program;
      this.rowType = program.getOutputRowType();
    }

    public RelOptPlanWriter explainTerms(RelOptPlanWriter pw) {
      return program.explainCalc(super.explainTerms(pw));
    }

    public double getRows() {
      return FilterRel.estimateFilteredRows(
          getChild(), program);
    }

    public RelOptCost computeSelfCost(RelOptPlanner planner) {
      double dRows = RelMetadataQuery.getRowCount(this);
      double dCpu =
          RelMetadataQuery.getRowCount(getChild())
              * program.getExprCount();
      double dIo = 0;
      return planner.makeCost(dRows, dCpu, dIo);
    }

    public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
      return new MongoCalcRel(
          getCluster(),
          traitSet,
          sole(inputs),
          program.copy(),
          getFlags());
    }

    public int getFlags() {
      return flags;
    }

    public RexProgram getProgram() {
      return program;
    }

    public SqlString implement(MongoImplementor implementor) {
      final SqlBuilder buf = new SqlBuilder(implementor.dialect);
      buf.append("SELECT ");
      if (isStar(program)) {
        buf.append("*");
      } else {
        for (Ord<RexLocalRef> ref : Ord.zip(program.getProjectList())) {
          buf.append(ref.i == 0 ? "" : ", ");
          expr(buf, program, ref.e);
          alias(buf, null, getRowType().getFieldNames().get(ref.i));
        }
      }
      implementor.newline(buf)
          .append("FROM ");
      implementor.subquery(buf, 0, getChild(), "t");
      if (program.getCondition() != null) {
        implementor.newline(buf);
        buf.append("WHERE ");
        expr(buf, program, program.getCondition());
      }
      return buf.toSqlString();
    }

    private static boolean isStar(RexProgram program) {
      int i = 0;
      for (RexLocalRef ref : program.getProjectList()) {
        if (ref.getIndex() != i++) {
          return false;
        }
      }
      return i == program.getInputRowType().getFieldCount();
    }

    private static void expr(
        SqlBuilder buf, RexProgram program, RexNode rex) {
      if (rex instanceof RexLocalRef) {
        final int index = ((RexLocalRef) rex).getIndex();
        expr(buf, program, program.getExprList().get(index));
      } else if (rex instanceof RexInputRef) {
        buf.identifier(
            program.getInputRowType().getFieldNames().get(
                ((RexInputRef) rex).getIndex()));
      } else if (rex instanceof RexLiteral) {
        toSql(buf, (RexLiteral) rex);
      } else if (rex instanceof RexCall) {
        final RexCall call = (RexCall) rex;
        switch (call.getOperator().getSyntax()) {
        case Binary:
          expr(buf, program, call.getOperands().get(0));
          buf.append(' ')
              .append(call.getOperator().toString())
              .append(' ');
          expr(buf, program, call.getOperands().get(1));
          break;
        default:
          throw new AssertionError(call.getOperator());
        }
      } else {
        throw new AssertionError(rex);
      }
    }
  }

  private static SqlBuilder toSql(SqlBuilder buf, RexLiteral rex) {
    switch (rex.getTypeName()) {
    case CHAR:
    case VARCHAR:
      return buf.append(
          new NlsString(rex.getValue2().toString(), null, null)
              .asSql(false, false));
    default:
      return buf.append(rex.getValue2().toString());
    }
  }

  /**
   * Rule to convert an {@link org.eigenbase.rel.AggregateRel} to an
   * {@link MongoAggregateRel}.
   o/
  private static class MongoAggregateRule extends MongoConverterRule {
    private MongoAggregateRule(MongoConvention out) {
      super(
          AggregateRel.class,
          Convention.NONE,
          out,
          "MongoAggregateRule");
    }

    public RelNode convert(RelNode rel) {
      final AggregateRel agg = (AggregateRel) rel;
      final RelTraitSet traitSet =
          agg.getTraitSet().replace(out);
      try {
        return new MongoAggregateRel(
            rel.getCluster(),
            traitSet,
            convert(agg.getChild(), traitSet),
            agg.getGroupSet(),
            agg.getAggCallList());
      } catch (InvalidRelException e) {
        tracer.warning(e.toString());
        return null;
      }
    }
  }

  public static class MongoAggregateRel
      extends AggregateRelBase
      implements MongoRel {
    public MongoAggregateRel(
        RelOptCluster cluster,
        RelTraitSet traitSet,
        RelNode child,
        BitSet groupSet,
        List<AggregateCall> aggCalls)
        throws InvalidRelException {
      super(cluster, traitSet, child, groupSet, aggCalls);
      assert getConvention() instanceof MongoConvention;

      for (AggregateCall aggCall : aggCalls) {
        if (aggCall.isDistinct()) {
          throw new InvalidRelException(
              "distinct aggregation not supported");
        }
      }
    }

    @Override
    public MongoAggregateRel copy(
        RelTraitSet traitSet, List<RelNode> inputs) {
      try {
        return new MongoAggregateRel(
            getCluster(),
            traitSet,
            sole(inputs),
            groupSet,
            aggCalls);
      } catch (InvalidRelException e) {
        // Semantic error not possible. Must be a bug. Convert to
        // internal error.
        throw new AssertionError(e);
      }
    }

    public SqlString implement(MongoImplementor implementor) {
      // "select a, b, sum(x) from ( ... ) group by a, b"
      final SqlBuilder buf = new SqlBuilder(implementor.dialect);
      final List<String> inFields =
          getChild().getRowType().getFieldNames();
      final List<String> fields = getRowType().getFieldNames();
      buf.append("SELECT ");
      int i = 0;
      for (int group : Util.toIter(groupSet)) {
        buf.append(i > 0 ? ", " : "");
        final String inField = inFields.get(group);
        buf.identifier(inField);
        alias(buf, inField, fields.get(i));
        i++;
      }
      for (AggregateCall aggCall : aggCalls) {
        buf.append(i > 0 ? ", " : "");
        buf.append(aggCall.getAggregation().getName());
        buf.append("(");
        if (aggCall.getArgList().isEmpty()) {
          buf.append("*");
        } else {
          for (Ord<Integer> call : Ord.zip(aggCall.getArgList())) {
            buf.append(call.i > 0 ? ", " : "");
            buf.append(inFields.get(call.e));
          }
        }
        buf.append(")");
        alias(buf, null, fields.get(i));
        i++;
      }
      implementor.newline(buf)
          .append(" FROM ");
      implementor.subquery(buf, 0, getChild(), "t");
      if (!groupSet.isEmpty()) {
        implementor.newline(buf)
            .append("GROUP BY ");
        i = 0;
        for (int group : Util.toIter(groupSet)) {
          buf.append(i > 0 ? ", " : "");
          final String inField = inFields.get(group);
          buf.identifier(inField);
          i++;
        }
      }
      return buf.toSqlString();
    }
  }

  /**
   * Rule to convert an {@link org.eigenbase.rel.UnionRel} to a
   * {@link MongoUnionRel}.
   o/
  private static class MongoUnionRule
      extends MongoConverterRule {
    private MongoUnionRule(MongoConvention out) {
      super(
          UnionRel.class,
          Convention.NONE,
          out,
          "MongoUnionRule");
    }

    public RelNode convert(RelNode rel) {
      final UnionRel union = (UnionRel) rel;
      final RelTraitSet traitSet =
          union.getTraitSet().replace(out);
      return new MongoUnionRel(
          rel.getCluster(),
          traitSet,
          convertList(union.getInputs(), traitSet),
          union.all);
    }
  }

  public static class MongoUnionRel
      extends UnionRelBase
      implements MongoRel {
    public MongoUnionRel(
        RelOptCluster cluster,
        RelTraitSet traitSet,
        List<RelNode> inputs,
        boolean all) {
      super(cluster, traitSet, inputs, all);
    }

    public MongoUnionRel copy(
        RelTraitSet traitSet, List<RelNode> inputs, boolean all) {
      return new MongoUnionRel(getCluster(), traitSet, inputs, all);
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner) {
      return super.computeSelfCost(planner).multiplyBy(.1);
    }

    public SqlString implement(MongoImplementor implementor) {
      return setOpSql(this, implementor, "UNION");
    }
  }

  private static SqlString setOpSql(
      SetOpRel setOpRel, MongoImplementor implementor, String op) {
    final SqlBuilder buf = new SqlBuilder(implementor.dialect);
    for (Ord<RelNode> input : Ord.zip(setOpRel.getInputs())) {
      if (input.i > 0) {
        implementor.newline(buf)
            .append(op + (setOpRel.all ? " ALL " : ""));
        implementor.newline(buf);
      }
      buf.append(implementor.visitChild(input.i, input.e));
    }
    return buf.toSqlString();
  }

  /**
   * Rule to convert an {@link org.eigenbase.rel.IntersectRel} to an
   * {@link MongoIntersectRel}.
   o/
  private static class MongoIntersectRule
      extends MongoConverterRule {
    private MongoIntersectRule(MongoConvention out) {
      super(
          IntersectRel.class,
          Convention.NONE,
          out,
          "MongoIntersectRule");
    }

    public RelNode convert(RelNode rel) {
      final IntersectRel intersect = (IntersectRel) rel;
      if (intersect.all) {
        return null; // INTERSECT ALL not implemented
      }
      final RelTraitSet traitSet =
          intersect.getTraitSet().replace(out);
      return new MongoIntersectRel(
          rel.getCluster(),
          traitSet,
          convertList(intersect.getInputs(), traitSet),
          intersect.all);
    }
  }

  public static class MongoIntersectRel
      extends IntersectRelBase
      implements MongoRel {
    public MongoIntersectRel(
        RelOptCluster cluster,
        RelTraitSet traitSet,
        List<RelNode> inputs,
        boolean all) {
      super(cluster, traitSet, inputs, all);
      assert !all;
    }

    public MongoIntersectRel copy(
        RelTraitSet traitSet, List<RelNode> inputs, boolean all) {
      return new MongoIntersectRel(getCluster(), traitSet, inputs, all);
    }

    public SqlString implement(MongoImplementor implementor) {
      return setOpSql(this, implementor, " intersect ");
    }
  }

  /**
   * Rule to convert an {@link org.eigenbase.rel.MinusRel} to an
   * {@link MongoMinusRel}.
   o/
  private static class MongoMinusRule
      extends MongoConverterRule {
    private MongoMinusRule(MongoConvention out) {
      super(
          MinusRel.class,
          Convention.NONE,
          out,
          "MongoMinusRule");
    }

    public RelNode convert(RelNode rel) {
      final MinusRel minus = (MinusRel) rel;
      if (minus.all) {
        return null; // EXCEPT ALL not implemented
      }
      final RelTraitSet traitSet =
          rel.getTraitSet().replace(out);
      return new MongoMinusRel(
          rel.getCluster(),
          traitSet,
          convertList(minus.getInputs(), traitSet),
          minus.all);
    }
  }

  public static class MongoMinusRel
      extends MinusRelBase
      implements MongoRel {
    public MongoMinusRel(
        RelOptCluster cluster,
        RelTraitSet traitSet,
        List<RelNode> inputs,
        boolean all) {
      super(cluster, traitSet, inputs, all);
      assert !all;
    }

    public MongoMinusRel copy(
        RelTraitSet traitSet, List<RelNode> inputs, boolean all) {
      return new MongoMinusRel(getCluster(), traitSet, inputs, all);
    }

    public SqlString implement(MongoImplementor implementor) {
      return setOpSql(this, implementor, " minus ");
    }
  }

  public static class MongoValuesRule extends MongoConverterRule {
    private MongoValuesRule(MongoConvention out) {
      super(
          ValuesRel.class,
          Convention.NONE,
          out,
          "MongoValuesRule");
    }

    @Override
    public RelNode convert(RelNode rel) {
      ValuesRel valuesRel = (ValuesRel) rel;
      return new MongoValuesRel(
          valuesRel.getCluster(),
          valuesRel.getRowType(),
          valuesRel.getTuples(),
          valuesRel.getTraitSet().plus(out));
    }
  }

  public static class MongoValuesRel
      extends ValuesRelBase
      implements MongoRel {
    MongoValuesRel(
        RelOptCluster cluster,
        RelDataType rowType,
        List<List<RexLiteral>> tuples,
        RelTraitSet traitSet) {
      super(cluster, rowType, tuples, traitSet);
    }

    @Override
    public RelNode copy(
        RelTraitSet traitSet, List<RelNode> inputs) {
      assert inputs.isEmpty();
      return new MongoValuesRel(
          getCluster(), rowType, tuples, traitSet);
    }

    public SqlString implement(MongoImplementor implementor) {
      throw new AssertionError(); // TODO:
    }
  }
*/
}

// End MongoRules.java
