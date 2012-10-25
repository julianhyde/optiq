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
package net.hydromatic.optiq.prepare;

import net.hydromatic.linq4j.Enumerable;
import net.hydromatic.linq4j.Queryable;
import net.hydromatic.linq4j.expressions.*;
import net.hydromatic.optiq.*;
import net.hydromatic.optiq.impl.java.JavaTypeFactory;
import net.hydromatic.optiq.jdbc.Helper;
import net.hydromatic.optiq.jdbc.OptiqPrepare;
import net.hydromatic.optiq.rules.java.EnumerableRel;
import net.hydromatic.optiq.rules.java.EnumerableRelImplementor;
import net.hydromatic.optiq.rules.java.JavaRules;
import net.hydromatic.optiq.rules.java.RexToLixTranslator;
import net.hydromatic.optiq.runtime.Executable;
import openjava.ptree.ClassDeclaration;
import org.codehaus.janino.ExpressionEvaluator;
import org.eigenbase.oj.stmt.OJPreparingStmt;
import org.eigenbase.oj.stmt.PreparedExecution;
import org.eigenbase.oj.stmt.PreparedResult;
import org.eigenbase.rel.RelCollation;
import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.rules.TableAccessRule;
import org.eigenbase.relopt.*;
import org.eigenbase.relopt.volcano.VolcanoPlanner;
import org.eigenbase.reltype.RelDataType;
import org.eigenbase.reltype.RelDataTypeFactory;
import org.eigenbase.reltype.RelDataTypeField;
import org.eigenbase.rex.RexBuilder;
import org.eigenbase.rex.RexNode;
import org.eigenbase.sql.*;
import org.eigenbase.sql.fun.SqlStdOperatorTable;
import org.eigenbase.sql.parser.SqlParseException;
import org.eigenbase.sql.parser.SqlParser;
import org.eigenbase.sql.type.*;
import org.eigenbase.sql.util.ChainedSqlOperatorTable;
import org.eigenbase.sql.validate.*;
import org.eigenbase.sql2rel.SqlToRelConverter;
import org.eigenbase.util.Pair;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

/**
 * Shit just got real.
 *
 * @author jhyde
 */
class OptiqPrepareImpl implements OptiqPrepare {

    public ParseResult parse(
        Context context, String sql)
    {
        final JavaTypeFactory typeFactory = context.getTypeFactory();
        OptiqCatalogReader catalogReader =
            new OptiqCatalogReader(
                context.getRootSchema(),
                typeFactory);
        final OptiqPreparingStmt preparingStmt =
            new OptiqPreparingStmt(
                catalogReader,
                typeFactory,
                context.getRootSchema());
        preparingStmt.setResultCallingConvention(CallingConvention.ENUMERABLE);

        SqlParser parser = new SqlParser(sql);
        SqlNode sqlNode;
        try {
            sqlNode = parser.parseQuery();
        } catch (SqlParseException e) {
            throw new RuntimeException("parse failed", e);
        }
        SqlValidator validator =
            new SqlValidatorImpl(
                SqlStdOperatorTable.instance(), catalogReader, typeFactory,
                SqlConformance.Default) { };
        SqlNode sqlNode1 = validator.validate(sqlNode);
        return new ParseResult(
            sql, sqlNode1, validator.getValidatedNodeType(sqlNode1));
    }

    public <T> PrepareResult<T> prepareQueryable(
        Context context,
        Queryable<T> queryable)
    {
        return prepare_(context, null, queryable, queryable.getElementType());
    }

    public <T> PrepareResult<T> prepareSql(
        Context context,
        String sql,
        Queryable<T> expression,
        Type elementType)
    {
        return prepare_(context, sql, expression, elementType);
    }

    <T> PrepareResult<T> prepare_(
        Context context,
        String sql,
        Queryable<T> queryable,
        Type elementType)
    {
        final JavaTypeFactory typeFactory = context.getTypeFactory();
        OptiqCatalogReader catalogReader =
            new OptiqCatalogReader(
                context.getRootSchema(),
                typeFactory);
        final OptiqPreparingStmt preparingStmt =
            new OptiqPreparingStmt(
                catalogReader,
                typeFactory,
                context.getRootSchema());
        preparingStmt.setResultCallingConvention(CallingConvention.ENUMERABLE);

        final RelDataType x;
        final PreparedResult preparedResult;
        if (sql != null) {
            assert queryable == null;
            SqlParser parser = new SqlParser(sql);
            SqlNode sqlNode;
            try {
                sqlNode = parser.parseQuery();
            } catch (SqlParseException e) {
                throw new RuntimeException("parse failed", e);
            }
            final Schema rootSchema = context.getRootSchema();
            SqlValidator validator =
                new SqlValidatorImpl(
                    new ChainedSqlOperatorTable(
                        Arrays.<SqlOperatorTable>asList(
                            SqlStdOperatorTable.instance(),
                            new MySqlOperatorTable(rootSchema, typeFactory))),
                    catalogReader,
                    typeFactory,
                    SqlConformance.Default) { };
            preparedResult = preparingStmt.prepareSql(
                sqlNode, Object.class, validator, true);
            x = validator.getValidatedNodeType(sqlNode);
        } else {
            assert queryable != null;
            x = context.getTypeFactory().createType(elementType);
            preparedResult =
                preparingStmt.prepareQueryable(queryable, x);
        }

        // TODO: parameters
        final List<Parameter> parameters = Collections.emptyList();
        // TODO: column meta data
        final List<ColumnMetaData> columns =
            new ArrayList<ColumnMetaData>();
        RelDataType jdbcType = makeStruct(typeFactory, x);
        for (RelDataTypeField field : jdbcType.getFields()) {
            RelDataType type = field.getType();
            SqlTypeName sqlTypeName = type.getSqlTypeName();
            columns.add(
                new ColumnMetaData(
                    columns.size(),
                    false,
                    true,
                    false,
                    false,
                    type.isNullable() ? 1 : 0,
                    true,
                    0,
                    field.getName(),
                    field.getName(),
                    null,
                    sqlTypeName.allowsPrec() && false
                        ? type.getPrecision()
                        : -1,
                    sqlTypeName.allowsScale() ? type.getScale() : -1,
                    null,
                    null,
                    sqlTypeName.getJdbcOrdinal(),
                    sqlTypeName.getName(),
                    true,
                    false,
                    false,
                    null));
        }
        return new PrepareResult<T>(
            sql,
            parameters,
            columns,
            (Enumerable<T>) preparedResult.execute());
    }

    private static RelDataType makeStruct(
        RelDataTypeFactory typeFactory,
        RelDataType type)
    {
        if (type.isStruct()) {
            return type;
        }
        return typeFactory.createStructType(
            RelDataTypeFactory.FieldInfoBuilder.of("$0", type));
    }

    private static class OptiqPreparingStmt extends OJPreparingStmt {
        private final RelOptPlanner planner;
        private final RexBuilder rexBuilder;
        private final Schema schema;
        private int expansionDepth;
        private SqlValidator sqlValidator;

        public OptiqPreparingStmt(
            CatalogReader catalogReader,
            RelDataTypeFactory typeFactory,
            Schema schema)
        {
            super(catalogReader);
            this.schema = schema;
            planner = new VolcanoPlanner();
            planner.addRelTraitDef(CallingConventionTraitDef.instance);
            RelOptUtil.registerAbstractRels(planner);
            planner.addRule(JavaRules.ENUMERABLE_JOIN_RULE);
            planner.addRule(JavaRules.ENUMERABLE_CALC_RULE);
            planner.addRule(JavaRules.ENUMERABLE_AGGREGATE_RULE);
            planner.addRule(JavaRules.ENUMERABLE_SORT_RULE);
            planner.addRule(JavaRules.ENUMERABLE_UNION_RULE);
            planner.addRule(JavaRules.ENUMERABLE_INTERSECT_RULE);
            planner.addRule(JavaRules.ENUMERABLE_MINUS_RULE);
            planner.addRule(TableAccessRule.instance);

            rexBuilder = new RexBuilder(typeFactory);
        }

        public PreparedResult prepareQueryable(
            Queryable queryable,
            RelDataType resultType)
        {
            queryString = null;
            Class runtimeContextClass = Object.class;
            final Argument [] arguments = {
                new Argument(
                    connectionVariable,
                    runtimeContextClass,
                    null)
            };
            ClassDeclaration decl = init(arguments);

            final RelOptQuery query = new RelOptQuery(planner);
            final RelOptCluster cluster =
                query.createCluster(
                    env, rexBuilder.getTypeFactory(), rexBuilder);

            RelNode rootRel =
                new LixToRelTranslator(cluster)
                    .translate(queryable);

            if (timingTracer != null) {
                timingTracer.traceTime("end sql2rel");
            }

            final RelDataType jdbcType =
                makeStruct(rexBuilder.getTypeFactory(), resultType);
            fieldOrigins = Collections.nCopies(jdbcType.getFieldCount(), null);

            // Structured type flattening, view expansion, and plugging in
            // physical storage.
            rootRel = flattenTypes(rootRel, true);

            // Trim unused fields.
            rootRel = trimUnusedFields(rootRel);

            rootRel = optimize(resultType, rootRel);
            containsJava = treeContainsJava(rootRel);

            if (timingTracer != null) {
                timingTracer.traceTime("end optimization");
            }

            return implement(
                resultType,
                rootRel,
                SqlKind.SELECT,
                decl,
                arguments);
        }

        @Override
        protected SqlToRelConverter getSqlToRelConverter(
            SqlValidator validator,
            CatalogReader catalogReader)
        {
            SqlToRelConverter sqlToRelConverter =
                new SqlToRelConverter(
                    this, validator, catalogReader, env, planner, rexBuilder);
            sqlToRelConverter.setTrimUnusedFields(false);
            return sqlToRelConverter;
        }

        @Override
        protected EnumerableRelImplementor getRelImplementor(
            RexBuilder rexBuilder)
        {
            return new EnumerableRelImplementor(rexBuilder);
        }

        @Override
        protected String getClassRoot() {
            return null;
        }

        @Override
        protected String getCompilerClassName() {
            return "org.eigenbase.javac.JaninoCompiler";
        }

        @Override
        protected String getJavaRoot() {
            return null;
        }

        @Override
        protected String getTempPackageName() {
            return "foo";
        }

        @Override
        protected String getTempMethodName() {
            return null;
        }

        @Override
        protected String getTempClassName() {
            return "Foo";
        }

        @Override
        protected boolean shouldAlwaysWriteJavaFile() {
            return false;
        }

        @Override
        protected boolean shouldSetConnectionInfo() {
            return false;
        }

        private SqlToRelConverter getSqlToRelConverter() {
            return getSqlToRelConverter(getSqlValidator(), catalogReader);
        }

        @Override
        public RelNode flattenTypes(
            RelNode rootRel,
            boolean restructure)
        {
            return rootRel;
        }

        @Override
        protected RelNode decorrelate(SqlNode query, RelNode rootRel) {
            return rootRel;
        }

        @Override
        protected RelNode trimUnusedFields(RelNode rootRel) {
            return getSqlToRelConverter().trimUnusedFields(rootRel);
        }

        @Override
        public RelNode expandView(RelDataType rowType, String queryString) {
            expansionDepth++;

            SqlParser parser = new SqlParser(queryString);
            SqlNode sqlNode;
            try {
                sqlNode = parser.parseQuery();
            } catch (SqlParseException e) {
                throw new RuntimeException("parse failed", e);
            }
            SqlValidator validator = getSqlValidator();
            SqlNode sqlNode1 = validator.validate(sqlNode);

            SqlToRelConverter sqlToRelConverter =
                getSqlToRelConverter(validator, catalogReader);
            RelNode relNode =
                sqlToRelConverter.convertQuery(sqlNode1, true, false);

            --expansionDepth;
            return relNode;
        }

        private SqlValidator getSqlValidator() {
            if (sqlValidator == null) {
                sqlValidator = new SqlValidatorImpl(
                    SqlStdOperatorTable.instance(), catalogReader,
                    rexBuilder.getTypeFactory(),
                    SqlConformance.Default) { };
            }
            return sqlValidator;
        }

        @Override
        protected PreparedExecution implement(
            RelDataType rowType,
            RelNode rootRel,
            SqlKind sqlKind,
            ClassDeclaration decl,
            Argument[] args)
        {
            RelDataType resultType = rootRel.getRowType();
            boolean isDml = sqlKind.belongsTo(SqlKind.DML);
            javaCompiler = createCompiler();
            EnumerableRelImplementor relImplementor =
                getRelImplementor(rootRel.getCluster().getRexBuilder());
            BlockExpression expr =
                relImplementor.implementRoot((EnumerableRel) rootRel);
            ParameterExpression root0 =
                Expressions.parameter(DataContext.class, "root0");
            String s = Expressions.toString(
                Blocks.create(
                    Expressions.declare(
                        Modifier.FINAL,
                        (ParameterExpression) schema.getExpression(),
                        root0),
                    expr),
                false);

            final Executable executable;
            try {
                executable = (Executable)
                    ExpressionEvaluator.createFastScriptEvaluator(
                        s, Executable.class, new String[]{root0.name});
            } catch (Exception e) {
                throw Helper.INSTANCE.wrap(
                    "Error while compiling generated Java code:\n" + s, e);
            }

            if (timingTracer != null) {
                timingTracer.traceTime("end codegen");
            }

            if (timingTracer != null) {
                timingTracer.traceTime("end compilation");
            }

            return new PreparedExecution(
                null,
                rootRel,
                resultType,
                isDml,
                mapTableModOp(isDml, sqlKind),
                null)
            {
                public Object execute() {
                    return executable.execute(schema);
                }
            };
        }
    }

    static class RelOptTableImpl
        implements OJPreparingStmt.PreparingTable
    {
        private final RelOptSchema schema;
        private final RelDataType rowType;
        private final String[] names;
        private final Table table;
        private final Expression expression;

        RelOptTableImpl(
            RelOptSchema schema,
            RelDataType rowType,
            String[] names,
            Table table)
        {
            this(schema, rowType, names, table, table.getExpression());
        }

        RelOptTableImpl(
            RelOptSchema schema,
            RelDataType rowType,
            String[] names,
            Expression expression)
        {
            this(schema, rowType, names, null, expression);
        }

        private RelOptTableImpl(
            RelOptSchema schema,
            RelDataType rowType,
            String[] names,
            Table table,
            Expression expression)
        {
            this.schema = schema;
            this.rowType = rowType;
            this.names = names;
            this.table = table;
            this.expression = expression;
            assert expression != null;
        }

        public double getRowCount() {
            return 100;
        }

        public RelOptSchema getRelOptSchema() {
            return schema;
        }

        public RelNode toRel(ToRelContext context)
        {
            if (table instanceof TranslatableTable) {
                return ((TranslatableTable) table).toRel(context, this);
            }
            return new JavaRules.EnumerableTableAccessRel(
                context.getCluster(), this, expression);
        }

        public List<RelCollation> getCollationList() {
            return Collections.emptyList();
        }

        public RelDataType getRowType() {
            return rowType;
        }

        public String[] getQualifiedName() {
            return names;
        }

        public SqlMonotonicity getMonotonicity(String columnName) {
            return SqlMonotonicity.NotMonotonic;
        }

        public SqlAccessType getAllowedAccess() {
            return SqlAccessType.READ_ONLY;
        }
    }

    private static class OptiqCatalogReader
        implements OJPreparingStmt.CatalogReader
    {
        private final Schema schema;
        private final JavaTypeFactory typeFactory;

        public OptiqCatalogReader(
            Schema schema,
            JavaTypeFactory typeFactory)
        {
            super();
            this.schema = schema;
            this.typeFactory = typeFactory;
        }

        public RelOptTableImpl getTable(final String[] names) {
            List<Pair<String, Object>> pairs =
                new ArrayList<Pair<String, Object>>();
            Schema schema2 = schema;
            for (int i = 0; i < names.length; i++) {
                final String name = names[i];
                Schema subSchema = schema2.getSubSchema(name);
                if (subSchema != null) {
                    pairs.add(Pair.<String, Object>of(name, subSchema));
                    schema2 = subSchema;
                    continue;
                }
                final Table table = schema2.getTable(name);
                if (table != null) {
                    pairs.add(Pair.<String, Object>of(name, table));
                    if (i != names.length - 1) {
                        // not enough objects to match all names
                        return null;
                    }
                    return new RelOptTableImpl(
                        this,
                        typeFactory.createType(table.getElementType()),
                        names,
                        table);
                }
                return null;
            }
            return null;
        }

        public RelDataType getNamedType(SqlIdentifier typeName) {
            return null;
        }

        public List<SqlMoniker> getAllSchemaObjectNames(List<String> names) {
            return null;
        }

        public String getSchemaName() {
            return null;
        }

        public RelOptTableImpl getTableForMember(String[] names) {
            return getTable(names);
        }

        public RelDataTypeFactory getTypeFactory() {
            return typeFactory;
        }

        public void registerRules(RelOptPlanner planner) throws Exception {
        }
    }

    interface ScalarTranslator {
        RexNode toRex(BlockExpression expression);
        List<RexNode> toRexList(BlockExpression expression);
        RexNode toRex(Expression expression);
        ScalarTranslator bind(
            List<ParameterExpression> parameterList, List<RexNode> values);
    }

    static class EmptyScalarTranslator implements ScalarTranslator {
        private final RexBuilder rexBuilder;

        public EmptyScalarTranslator(RexBuilder rexBuilder) {
            this.rexBuilder = rexBuilder;
        }

        public static ScalarTranslator empty(RexBuilder builder) {
            return new EmptyScalarTranslator(builder);
        }

        public List<RexNode> toRexList(BlockExpression expression) {
            final List<Expression> simpleList = simpleList(expression);
            final List<RexNode> list = new ArrayList<RexNode>();
            for (Expression expression1 : simpleList) {
                list.add(toRex(expression1));
            }
            return list;
        }

        public RexNode toRex(BlockExpression expression) {
            return toRex(Blocks.simple(expression));
        }

        private static List<Expression> simpleList(BlockExpression expression) {
            Expression simple = Blocks.simple(expression);
            if (simple instanceof NewExpression) {
                NewExpression newExpression = (NewExpression) simple;
                return newExpression.arguments;
            } else {
                return Collections.singletonList(simple);
            }
        }

        public RexNode toRex(Expression expression) {
            switch (expression.getNodeType()) {
            case MemberAccess:
                return rexBuilder.makeFieldAccess(
                    toRex(
                        ((MemberExpression) expression).expression),
                    ((MemberExpression) expression).field.getName());
            case GreaterThan:
                return binary(
                    expression, SqlStdOperatorTable.greaterThanOperator);
            case LessThan:
                return binary(expression, SqlStdOperatorTable.lessThanOperator);
            case Parameter:
                return parameter((ParameterExpression) expression);
            case Call:
                MethodCallExpression call = (MethodCallExpression) expression;
                SqlOperator operator =
                    RexToLixTranslator.JAVA_TO_SQL_METHOD_MAP.get(call.method);
                if (operator != null) {
                    return rexBuilder.makeCall(
                        operator,
                        toRex(
                            Expressions.<Expression>list()
                                .appendIfNotNull(call.targetExpression)
                                .appendAll(call.expressions)));
                }
                throw new RuntimeException(
                    "Could translate call to method " + call.method);
            case Constant:
                final ConstantExpression constant =
                    (ConstantExpression) expression;
                Object value = constant.value;
                if (value instanceof Number) {
                    Number number = (Number) value;
                    if (value instanceof Double || value instanceof Float) {
                        return rexBuilder.makeApproxLiteral(
                            BigDecimal.valueOf(number.doubleValue()));
                    } else if (value instanceof BigDecimal) {
                        return rexBuilder.makeExactLiteral((BigDecimal) value);
                    } else {
                        return rexBuilder.makeExactLiteral(
                            BigDecimal.valueOf(number.longValue()));
                    }
                } else if (value instanceof Boolean) {
                    return rexBuilder.makeLiteral((Boolean) value);
                } else {
                    return rexBuilder.makeLiteral(constant.toString());
                }
            default:
                throw new UnsupportedOperationException(
                    "unknown expression type " + expression.getNodeType() + " "
                    + expression);
            }
        }

        private RexNode binary(Expression expression, SqlBinaryOperator op) {
            BinaryExpression call = (BinaryExpression) expression;
            return rexBuilder.makeCall(
                op, toRex(Arrays.asList(call.expression0, call.expression1)));
        }

        private List<RexNode> toRex(List<Expression> expressions) {
            ArrayList<RexNode> list = new ArrayList<RexNode>();
            for (Expression expression : expressions) {
                list.add(toRex(expression));
            }
            return list;
        }

        public ScalarTranslator bind(
            List<ParameterExpression> parameterList, List<RexNode> values)
        {
            return new LambdaScalarTranslator(
                rexBuilder, parameterList, values);
        }

        public RexNode parameter(ParameterExpression param) {
            throw new RuntimeException("unknown parameter " + param);
        }
    }

    private static class LambdaScalarTranslator extends EmptyScalarTranslator {
        private final List<ParameterExpression> parameterList;
        private final List<RexNode> values;

        public LambdaScalarTranslator(
            RexBuilder rexBuilder,
            List<ParameterExpression> parameterList,
            List<RexNode> values)
        {
            super(rexBuilder);
            this.parameterList = parameterList;
            this.values = values;
        }

        public RexNode parameter(ParameterExpression param) {
            int i = parameterList.indexOf(param);
            if (i >= 0) {
                return values.get(i);
            }
            throw new RuntimeException("unknown parameter " + param);
        }
    }

    private static class MySqlOperatorTable implements SqlOperatorTable {
        private final Schema rootSchema;
        private final JavaTypeFactory typeFactory;

        public MySqlOperatorTable(
            Schema rootSchema,
            JavaTypeFactory typeFactory)
        {
            this.rootSchema = rootSchema;
            this.typeFactory = typeFactory;
        }

        public List<SqlOperator> lookupOperatorOverloads(
            SqlIdentifier opName,
            SqlFunctionCategory category,
            SqlSyntax syntax)
        {
            if (syntax != SqlSyntax.Function) {
                return Collections.emptyList();
            }
            // FIXME: ignoring prefix of opName
            String name = opName.names[opName.names.length - 1];
            List<TableFunction> tableFunctions =
                rootSchema.getTableFunctions(name);
            if (tableFunctions.isEmpty()) {
                return Collections.emptyList();
            }
            return toOps(name, tableFunctions);
        }

        private List<SqlOperator> toOps(
            final String name,
            final List<TableFunction> tableFunctions)
        {
            return new AbstractList<SqlOperator>() {
                public SqlOperator get(int index) {
                    return toOp(name, tableFunctions.get(index));
                }

                public int size() {
                    return tableFunctions.size();
                }
            };
        }

        private SqlOperator toOp(String name, TableFunction fun) {
            List<RelDataType> argTypes = new ArrayList<RelDataType>();
            List<SqlTypeFamily> typeFamilies = new ArrayList<SqlTypeFamily>();
            Parameter p;
            for (net.hydromatic.optiq.Parameter o
                : (List< net.hydromatic.optiq.Parameter>) fun.getParameters())
            {
                argTypes.add(o.getType());
                typeFamilies.add(SqlTypeFamily.ANY);
            }
            return new SqlFunction(
                name,
                SqlKind.OTHER_FUNCTION,
                new ExplicitReturnTypeInference(
                    typeFactory.createType(fun.getElementType())),
                new ExplicitOperandTypeInference(
                    argTypes.toArray(new RelDataType[argTypes.size()])),
                new FamilyOperandTypeChecker(
                    typeFamilies.toArray(
                        new SqlTypeFamily[typeFamilies.size()])),
                null);
        }

        public List<SqlOperator> getOperatorList() {
            return null;
        }
    }
}

// End OptiqPrepareImpl.java
