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
package net.hydromatic.optiq.rules.java;

import net.hydromatic.linq4j.Ord;
import net.hydromatic.linq4j.expressions.*;

import net.hydromatic.optiq.BuiltinMethod;
import net.hydromatic.optiq.TableFunction;
import net.hydromatic.optiq.impl.ScalarFunctionImpl;
import net.hydromatic.optiq.runtime.SqlFunctions;

import org.eigenbase.rel.Aggregation;
import org.eigenbase.reltype.*;
import org.eigenbase.rex.*;
import org.eigenbase.sql.*;
import org.eigenbase.sql.fun.SqlStdOperatorTable;
import org.eigenbase.sql.fun.SqlTrimFunction;
import org.eigenbase.sql.type.SqlTypeName;
import org.eigenbase.sql.validate.SqlUserDefinedFunction;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static net.hydromatic.linq4j.expressions.ExpressionType.*;

import static net.hydromatic.optiq.DataContext.ROOT;

import static org.eigenbase.sql.fun.SqlStdOperatorTable.*;

/**
 * Contains implementations of Rex operators as Java code.
 */
public class RexImpTable {
  public static final ConstantExpression NULL_EXPR =
      Expressions.constant(null);
  public static final ConstantExpression FALSE_EXPR =
      Expressions.constant(false);
  public static final ConstantExpression TRUE_EXPR =
      Expressions.constant(true);
  public static final MemberExpression BOXED_FALSE_EXPR =
      Expressions.field(null, Boolean.class, "FALSE");
  public static final MemberExpression BOXED_TRUE_EXPR =
      Expressions.field(null, Boolean.class, "TRUE");

  private static final CallImplementor UDF_IMPLEMENTOR =
      createImplementor(
          new NotNullImplementor() {
            public Expression implement(RexToLixTranslator translator,
                RexCall call, List<Expression> translatedOperands) {
              TableFunction x =
                  ((SqlUserDefinedFunction) call.getOperator()).tableFunction;
              final Method method = ((ScalarFunctionImpl) x).method;
              if ((method.getModifiers() & Modifier.STATIC) != 0) {
                return Expressions.call(method, translatedOperands);
              } else {
                // The UDF class must have a public zero-args constructor.
                // Assume that the validator checked already.
                final NewExpression target =
                    Expressions.new_(method.getDeclaringClass());
                return Expressions.call(target, method, translatedOperands);
              }
            }
          },
          NullPolicy.ANY, false);

  private final Map<SqlOperator, CallImplementor> map =
      new HashMap<SqlOperator, CallImplementor>();
  final Map<Aggregation, AggregateImplementor> aggMap =
      new HashMap<Aggregation, AggregateImplementor>();
  private final Map<Aggregation, AggImplementor2> agg2Map =
      new HashMap<Aggregation, AggImplementor2>();

  RexImpTable() {
    defineMethod(UPPER, BuiltinMethod.UPPER.method, NullPolicy.STRICT);
    defineMethod(LOWER, BuiltinMethod.LOWER.method, NullPolicy.STRICT);
    defineMethod(INITCAP,  BuiltinMethod.INITCAP.method, NullPolicy.STRICT);
    defineMethod(SUBSTRING, BuiltinMethod.SUBSTRING.method, NullPolicy.STRICT);
    defineMethod(CHARACTER_LENGTH, BuiltinMethod.CHAR_LENGTH.method,
        NullPolicy.STRICT);
    defineMethod(CHAR_LENGTH, BuiltinMethod.CHAR_LENGTH.method,
        NullPolicy.STRICT);
    defineMethod(CONCAT, BuiltinMethod.STRING_CONCAT.method,
        NullPolicy.STRICT);
    defineMethod(OVERLAY, BuiltinMethod.OVERLAY.method, NullPolicy.STRICT);
    defineMethod(POSITION, BuiltinMethod.POSITION.method, NullPolicy.STRICT);

    final TrimImplementor trimImplementor = new TrimImplementor();
    defineImplementor(TRIM, NullPolicy.STRICT, trimImplementor, false);

    // logical
    defineBinary(AND, AndAlso, NullPolicy.AND, null);
    defineBinary(OR, OrElse, NullPolicy.OR, null);
    defineUnary(NOT, Not, NullPolicy.NOT);

    // comparisons
    defineBinary(LESS_THAN, LessThan, NullPolicy.STRICT, "lt");
    defineBinary(LESS_THAN_OR_EQUAL, LessThanOrEqual, NullPolicy.STRICT, "le");
    defineBinary(GREATER_THAN, GreaterThan, NullPolicy.STRICT, "gt");
    defineBinary(GREATER_THAN_OR_EQUAL, GreaterThanOrEqual, NullPolicy.STRICT,
        "ge");
    defineBinary(EQUALS, Equal, NullPolicy.STRICT, "eq");
    defineBinary(NOT_EQUALS, NotEqual, NullPolicy.STRICT, "ne");

    // arithmetic
    defineBinary(PLUS, Add, NullPolicy.STRICT, "plus");
    defineBinary(MINUS, Subtract, NullPolicy.STRICT, "minus");
    defineBinary(MULTIPLY, Multiply, NullPolicy.STRICT, "multiply");
    defineBinary(DIVIDE, Divide, NullPolicy.STRICT, "divide");
    defineBinary(DIVIDE_INTEGER, Divide, NullPolicy.STRICT, "divide");
    defineUnary(UNARY_MINUS, Negate, NullPolicy.STRICT);
    defineUnary(UNARY_PLUS, UnaryPlus, NullPolicy.STRICT);

    defineMethod(MOD, "mod", NullPolicy.STRICT);
    defineMethod(EXP, "exp", NullPolicy.STRICT);
    defineMethod(POWER, "power", NullPolicy.STRICT);
    defineMethod(LN, "ln", NullPolicy.STRICT);
    defineMethod(LOG10, "log10", NullPolicy.STRICT);
    defineMethod(ABS, "abs", NullPolicy.STRICT);
    defineMethod(CEIL, "ceil", NullPolicy.STRICT);
    defineMethod(FLOOR, "floor", NullPolicy.STRICT);

    map.put(IS_NULL, new IsXxxImplementor(null, false));
    map.put(IS_NOT_NULL, new IsXxxImplementor(null, true));
    map.put(IS_TRUE, new IsXxxImplementor(true, false));
    map.put(IS_NOT_TRUE, new IsXxxImplementor(true, true));
    map.put(IS_FALSE, new IsXxxImplementor(false, false));
    map.put(IS_NOT_FALSE, new IsXxxImplementor(false, true));

    // LIKE and SIMILAR
    final MethodImplementor likeImplementor =
        new MethodImplementor(BuiltinMethod.LIKE.method);
    defineImplementor(LIKE, NullPolicy.STRICT, likeImplementor, false);
    defineImplementor(NOT_LIKE, NullPolicy.STRICT,
        NotImplementor.of(likeImplementor), false);
    final MethodImplementor similarImplementor =
        new MethodImplementor(BuiltinMethod.SIMILAR.method);
    defineImplementor(SIMILAR_TO, NullPolicy.STRICT, similarImplementor, false);
    defineImplementor(NOT_SIMILAR_TO, NullPolicy.STRICT,
        NotImplementor.of(similarImplementor), false);

    map.put(CASE, new CaseImplementor());

    defineImplementor(CAST, NullPolicy.STRICT, new CastImplementor(),
        false);
    defineImplementor(REINTERPRET, NullPolicy.STRICT,
        new ReinterpretImplementor(), false);

    final CallImplementor value = new ValueConstructorImplementor();
    map.put(MAP_VALUE_CONSTRUCTOR, value);
    map.put(ARRAY_VALUE_CONSTRUCTOR, value);
    map.put(ITEM, new ItemImplementor());

    // System functions
    final SystemFunctionImplementor systemFunctionImplementor =
        new SystemFunctionImplementor();
    map.put(USER, systemFunctionImplementor);
    map.put(CURRENT_USER, systemFunctionImplementor);
    map.put(SESSION_USER, systemFunctionImplementor);
    map.put(SYSTEM_USER, systemFunctionImplementor);
    map.put(CURRENT_PATH, systemFunctionImplementor);
    map.put(CURRENT_ROLE, systemFunctionImplementor);

    // Current time functions
    map.put(CURRENT_TIME, systemFunctionImplementor);
    map.put(CURRENT_TIMESTAMP, systemFunctionImplementor);
    map.put(CURRENT_DATE, systemFunctionImplementor);
    map.put(LOCALTIME, systemFunctionImplementor);
    map.put(LOCALTIMESTAMP, systemFunctionImplementor);

    aggMap.put(COUNT, new BuiltinAggregateImplementor("longCount"));
    aggMap.put(SUM, new BuiltinAggregateImplementor("sum"));
    aggMap.put(MIN, new BuiltinAggregateImplementor("min"));
    aggMap.put(MAX, new BuiltinAggregateImplementor("max"));

    agg2Map.put(COUNT, new CountImplementor2());
    agg2Map.put(SUM, new SumImplementor2());
    agg2Map.put(SUM0, new SumImplementor2());
    final MinMaxImplementor2 minMax =
        new MinMaxImplementor2();
    agg2Map.put(MIN, minMax);
    agg2Map.put(MAX, minMax);
    agg2Map.put(RANK, new RankImplementor2());
  }

  private void defineImplementor(
      SqlOperator operator,
      NullPolicy nullPolicy,
      NotNullImplementor implementor,
      boolean harmonize) {
    CallImplementor callImplementor =
        createImplementor(implementor, nullPolicy, harmonize);
    map.put(operator, callImplementor);
  }

  private static RexCall call2(
      boolean harmonize,
      RexToLixTranslator translator,
      RexCall call) {
    if (!harmonize) {
      return call;
    }
    final List<RexNode> operands2 =
        harmonize(translator, call.getOperands());
    if (operands2.equals(call.getOperands())) {
      return call;
    }
    return call.clone(call.getType(), operands2);
  }

  private static CallImplementor createImplementor(
      final NotNullImplementor implementor,
      final NullPolicy nullPolicy,
      final boolean harmonize) {
    switch (nullPolicy) {
    case ANY:
    case STRICT:
      return new CallImplementor() {
        public Expression implement(
            RexToLixTranslator translator, RexCall call, NullAs nullAs) {
          return implementNullSemantics0(
              translator, call, nullAs, nullPolicy, harmonize,
              implementor);
        }
      };
    case AND:
/* TODO:
            if (nullAs == NullAs.FALSE) {
                nullPolicy2 = NullPolicy.ANY;
            }
*/
      // If any of the arguments are false, result is false;
      // else if any arguments are null, result is null;
      // else true.
      //
      // b0 == null ? (b1 == null || b1 ? null : Boolean.FALSE)
      //   : b0 ? b1
      //   : Boolean.FALSE;
      return new CallImplementor() {
        public Expression implement(
            RexToLixTranslator translator, RexCall call, NullAs nullAs) {
          final RexCall call2 = call2(false, translator, call);
          final NullAs nullAs2 = nullAs == NullAs.TRUE ? NullAs.NULL : nullAs;
          final List<Expression> expressions =
              translator.translateList(call2.getOperands(), nullAs2);
          switch (nullAs) {
          case NOT_POSSIBLE:
          case TRUE:
            return Expressions.foldAnd(expressions);
          }
          return Expressions.foldAnd(Lists.transform(expressions,
              new Function<Expression, Expression>() {
                public Expression apply(Expression e) {
                  return nullAs2.handle(e);
                }
              }));
        }
      };
    case OR:
      // If any of the arguments are true, result is true;
      // else if any arguments are null, result is null;
      // else false.
      //
      // b0 == null ? (b1 == null || !b1 ? null : Boolean.TRUE)
      //   : !b0 ? b1
      //   : Boolean.TRUE;
      return new CallImplementor() {
        public Expression implement(
            RexToLixTranslator translator, RexCall call, NullAs nullAs) {
          final RexCall call2 = call2(harmonize, translator, call);
          final NullAs nullAs2 = nullAs == NullAs.TRUE ? NullAs.NULL : nullAs;
          final List<Expression> expressions =
              translator.translateList(call2.getOperands(), nullAs2);
          switch (nullAs) {
          case NOT_POSSIBLE:
          case FALSE:
            return Expressions.foldOr(expressions);
          }
          final Expression t0 = expressions.get(0);
          final Expression t1 = expressions.get(1);
          if (!nullable(call2, 0) && !nullable(call2, 1)) {
            return Expressions.orElse(t0, t1);
          }
          return optimize(
              Expressions.condition(
                  Expressions.equal(t0, NULL_EXPR),
                  Expressions.condition(
                      Expressions.orElse(
                          Expressions.equal(t1, NULL_EXPR),
                          Expressions.not(t1)),
                      NULL_EXPR,
                      BOXED_TRUE_EXPR),
                  Expressions.condition(
                      Expressions.not(t0),
                      t1,
                      BOXED_TRUE_EXPR)));
        }
      };
    case NOT:
      // If any of the arguments are false, result is true;
      // else if any arguments are null, result is null;
      // else false.
      return new CallImplementor() {
        public Expression implement(
            RexToLixTranslator translator, RexCall call, NullAs nullAs) {
          NullAs nullAs2;
          switch (nullAs) {
          case FALSE:
            nullAs2 = NullAs.TRUE;
            break;
          case TRUE:
            nullAs2 = NullAs.FALSE;
            break;
          default:
            nullAs2 = nullAs;
          }
          return implementNullSemantics0(
              translator, call, nullAs2, nullPolicy, harmonize, implementor);
        }
      };
    case NONE:
      return new CallImplementor() {
        public Expression implement(
            RexToLixTranslator translator, RexCall call, NullAs nullAs) {
          final RexCall call2 = call2(false, translator, call);
          return implementCall(
              translator, call2, implementor, nullAs);
        }
      };
    default:
      throw new AssertionError(nullPolicy);
    }
  }

  private void defineMethod(
      SqlOperator operator, String functionName, NullPolicy nullPolicy) {
    defineImplementor(
        operator,
        nullPolicy,
        new MethodNameImplementor(functionName),
        false);
  }

  private void defineMethod(
      SqlOperator operator, Method method, NullPolicy nullPolicy) {
    defineImplementor(
        operator, nullPolicy, new MethodImplementor(method), false);
  }

  private void defineUnary(
      SqlOperator operator, ExpressionType expressionType,
      NullPolicy nullPolicy) {
    defineImplementor(
        operator,
        nullPolicy,
        new UnaryImplementor(expressionType), false);
  }

  private void defineBinary(
      SqlOperator operator,
      ExpressionType expressionType,
      NullPolicy nullPolicy,
      String backupMethodName) {
    defineImplementor(
        operator,
        nullPolicy,
        new BinaryImplementor(expressionType, backupMethodName),
        true);
  }

  public static final RexImpTable INSTANCE = new RexImpTable();

  public CallImplementor get(final SqlOperator operator) {
    if (operator instanceof SqlUserDefinedFunction) {
      return UDF_IMPLEMENTOR;
    }
    return map.get(operator);
  }

  public AggregateImplementor get(final Aggregation aggregation) {
    return aggMap.get(aggregation);
  }

  public AggImplementor2 get2(final Aggregation aggregation) {
    return agg2Map.get(aggregation);
  }

  static Expression maybeNegate(boolean negate, Expression expression) {
    if (!negate) {
      return expression;
    } else {
      return Expressions.not(expression);
    }
  }

  static Expression optimize(Expression expression) {
    return expression.accept(new OptimizeVisitor());
  }

  static Expression optimize2(Expression operand, Expression expression) {
    if (Primitive.is(operand.getType())) {
      // Primitive values cannot be null
      return optimize(expression);
    } else {
      return optimize(
          Expressions.condition(
              Expressions.equal(
                  operand,
                  NULL_EXPR),
              NULL_EXPR,
              expression));
    }
  }

  private static boolean nullable(RexCall call, int i) {
    return call.getOperands().get(i).getType().isNullable();
  }

  /** Ensures that operands have identical type. */
  private static List<RexNode> harmonize(
      final RexToLixTranslator translator, final List<RexNode> operands) {
    int nullCount = 0;
    final List<RelDataType> types = new ArrayList<RelDataType>();
    final RelDataTypeFactory typeFactory =
        translator.builder.getTypeFactory();
    for (RexNode operand : operands) {
      RelDataType type = operand.getType();
      type = toSql(typeFactory, type);
      if (translator.isNullable(operand)) {
        ++nullCount;
      } else {
        type = typeFactory.createTypeWithNullability(type, false);
      }
      types.add(type);
    }
    if (allSame(types)) {
      // Operands have the same nullability and type. Return them
      // unchanged.
      return operands;
    }
    final RelDataType type = typeFactory.leastRestrictive(types);
    if (type == null) {
      // There is no common type. Presumably this is a binary operator with
      // asymmetric arguments (e.g. interval / integer) which is not intended
      // to be harmonized.
      return operands;
    }
    assert (nullCount > 0) == type.isNullable();
    final List<RexNode> list = new ArrayList<RexNode>();
    for (RexNode operand : operands) {
      list.add(
          translator.builder.ensureType(type, operand, false));
    }
    return list;
  }

  private static RelDataType toSql(RelDataTypeFactory typeFactory,
      RelDataType type) {
    if (type instanceof RelDataTypeFactoryImpl.JavaType) {
      final SqlTypeName typeName = type.getSqlTypeName();
      if (typeName != null && typeName != SqlTypeName.OTHER) {
        return typeFactory.createTypeWithNullability(
            typeFactory.createSqlType(typeName),
            type.isNullable());
      }
    }
    return type;
  }

  private static <E> boolean allSame(List<E> list) {
    E prev = null;
    for (E e : list) {
      if (prev != null && !prev.equals(e)) {
        return false;
      }
      prev = e;
    }
    return true;
  }

  private static Expression implementNullSemantics0(
      RexToLixTranslator translator,
      RexCall call,
      NullAs nullAs,
      NullPolicy nullPolicy,
      boolean harmonize,
      NotNullImplementor implementor) {
    switch (nullAs) {
    case IS_NOT_NULL:
      // If "f" is strict, then "f(a0, a1) IS NOT NULL" is
      // equivalent to "a0 IS NOT NULL AND a1 IS NOT NULL".
      if (nullPolicy == NullPolicy.STRICT) {
        return Expressions.foldAnd(
            translator.translateList(
                call.getOperands(), nullAs));
      }
      break;
    case IS_NULL:
      // If "f" is strict, then "f(a0, a1) IS NULL" is
      // equivalent to "a0 IS NULL OR a1 IS NULL".
      if (nullPolicy == NullPolicy.STRICT) {
        return Expressions.foldOr(
            translator.translateList(
                call.getOperands(), nullAs));
      }
      break;
    }
    final RexCall call2 = call2(harmonize, translator, call);
    try {
      return implementNullSemantics(
          translator, call2, nullAs, implementor);
    } catch (RexToLixTranslator.AlwaysNull e) {
      switch (nullAs) {
      case NOT_POSSIBLE:
        throw e;
      case FALSE:
        return FALSE_EXPR;
      case TRUE:
        return TRUE_EXPR;
      default:
        return NULL_EXPR;
      }
    }
  }

  private static Expression implementNullSemantics(
      RexToLixTranslator translator,
      RexCall call,
      NullAs nullAs,
      NotNullImplementor implementor) {
    final List<Expression> list = new ArrayList<Expression>();
    switch (nullAs) {
    case NULL:
      // v0 == null || v1 == null ? null : f(v0, v1)
      for (Ord<RexNode> operand : Ord.zip(call.getOperands())) {
        if (translator.isNullable(operand.e)) {
          list.add(
              translator.translate(
                  operand.e, NullAs.IS_NULL));
          translator = translator.setNullable(operand.e, false);
        }
      }
      final Expression box =
          Expressions.box(
              implementCall(translator, call, implementor, nullAs));
      return optimize(
          Expressions.condition(
              Expressions.foldOr(list),
              Types.castIfNecessary(box.getType(), NULL_EXPR),
              box));
    case FALSE:
      // v0 != null && v1 != null && f(v0, v1)
      for (Ord<RexNode> operand : Ord.zip(call.getOperands())) {
        if (translator.isNullable(operand.e)) {
          list.add(
              translator.translate(
                  operand.e, NullAs.IS_NOT_NULL));
          translator = translator.setNullable(operand.e, false);
        }
      }
      list.add(implementCall(translator, call, implementor, nullAs));
      return Expressions.foldAnd(list);
    case NOT_POSSIBLE:
      // Need to transmit to the implementor the fact that call cannot
      // return null. In particular, it should return a primitive (e.g.
      // int) rather than a box type (Integer).
      translator = translator.setNullable(call, false);
      // fall through
    default:
      return implementCall(translator, call, implementor, nullAs);
    }
  }

  private static Expression implementCall(
      RexToLixTranslator translator,
      RexCall call,
      NotNullImplementor implementor,
      NullAs nullAs) {
    final List<Expression> translatedOperands =
        translator.translateList(call.getOperands());
    switch (nullAs) {
    case NOT_POSSIBLE:
    case NULL:
      for (Expression translatedOperand : translatedOperands) {
        if (Expressions.isConstantNull(translatedOperand)) {
          return NULL_EXPR;
        }
      }
    }
    return implementor.implement(translator, call, translatedOperands);
  }

  /** Implements an aggregate function by generating a call to a method that
   * takes an enumeration and an accessor function. */
  interface AggregateImplementor {
    Expression implementAggregate(
        Expression grouping, Expression accessor);
  }

  /** Implements an aggregate function by generating expressions to
   * initialize, add to, and get a result from, an accumulator. */
  interface AggImplementor2 {
    /** Whether "add" code is called if any of the arguments are null. If
     * false, the container will ensure that the "add" arguments are always
     * not-null. If true, the container code must handle null values
     * appropriately. */
    boolean callOnNull();
    Expression implementInit(
        Aggregation aggregation,
        Type returnType,
        List<Type> parameterTypes);
    Expression implementAdd(
        Aggregation aggregation,
        Expression accumulator,
        List<Expression> arguments);
    Expression implementResult(
        Aggregation aggregation, Expression accumulator);
  }

  /** Implements a windowed aggregate function. */
  interface WinAggImplementor extends AggImplementor2 {
    Expression implementResultPlus(
        Aggregation aggregation, Expression accumulator, Expression start,
        Expression end, Expression rows, Expression current);
  }

  public enum NullAs {
    NULL,
    FALSE,
    TRUE,
    NOT_POSSIBLE,
    /** Return false if result is not null, true if result is null. */
    IS_NULL,
    /** Return true if result is not null, false if result is null. */
    IS_NOT_NULL;

    public static NullAs of(boolean nullable) {
      return nullable ? NULL : NOT_POSSIBLE;
    }

    /** Adapts an expression with "normal" result to one that adheres to
     * this particular policy. */
    public Expression handle(Expression x) {
      switch (Primitive.flavor(x.getType())) {
      case PRIMITIVE:
        // Expression cannot be null. We can skip any runtime checks.
        switch (this) {
        case NULL:
        case NOT_POSSIBLE:
        case FALSE:
        case TRUE:
          return x;
        case IS_NULL:
          return FALSE_EXPR;
        case IS_NOT_NULL:
          return TRUE_EXPR;
        default:
          throw new AssertionError();
        }
      case BOX:
        switch (this) {
        case NOT_POSSIBLE:
          return RexToLixTranslator.convert(
              x,
              Primitive.ofBox(x.getType()).primitiveClass);
        }
        // fall through
      }
      switch (this) {
      case NULL:
      case NOT_POSSIBLE:
        return x;
      case FALSE:
        return Expressions.call(
            BuiltinMethod.IS_TRUE.method,
            x);
      case TRUE:
        return Expressions.call(
            BuiltinMethod.IS_NOT_FALSE.method,
            x);
      case IS_NULL:
        return Expressions.equal(x, NULL_EXPR);
      case IS_NOT_NULL:
        return Expressions.notEqual(x, NULL_EXPR);
      default:
        throw new AssertionError();
      }
    }
  }

  interface CallImplementor {
    /** Implements a call. */
    Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        NullAs nullAs);
  }

  abstract static class AbstractCallImplementor implements CallImplementor {
    /** Implements a call with "normal" {@link NullAs} semantics. */
    abstract Expression implement(
        RexToLixTranslator translator,
        RexCall call);

    public final Expression implement(
        RexToLixTranslator translator, RexCall call, NullAs nullAs) {
      // Convert "normal" NullAs semantics to those asked for.
      return nullAs.handle(implement(translator, call));
    }
  }

  /** Simplified version of {@link CallImplementor} that does not know about
   * null semantics. */
  interface NotNullImplementor {
    Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        List<Expression> translatedOperands);
  }

  static class BuiltinAggregateImplementor
      implements AggregateImplementor {
    private final String methodName;

    public BuiltinAggregateImplementor(String methodName) {
      this.methodName = methodName;
    }

    public Expression implementAggregate(
        Expression grouping, Expression accessor) {
      return accessor == null
          ?  Expressions.call(grouping, methodName)
          :  Expressions.call(grouping, methodName, accessor);
    }
  }

  static class CountImplementor2 implements AggImplementor2 {
    public boolean callOnNull() {
      return false;
    }

    public Expression implementInit(
        Aggregation aggregation,
        Type returnType,
        List<Type> parameterTypes) {
      return Expressions.constant(0, returnType);
    }

    public Expression implementAdd(
        Aggregation aggregation,
        Expression accumulator,
        List<Expression> arguments) {
      // We don't need to check whether the argument is NULL. callOnNull()
      // returned false, so that container has checked for us.
      return Expressions.add(
          accumulator, Expressions.constant(1, accumulator.type));
    }

    public Expression implementResult(
        Aggregation aggregation, Expression accumulator) {
      return accumulator;
    }
  }

  static class SumImplementor2 implements AggImplementor2 {
    public boolean callOnNull() {
      return false;
    }

    public Expression implementInit(
        Aggregation aggregation,
        Type returnType,
        List<Type> parameterTypes) {
      final Primitive primitive = choosePrimitive(returnType);
      assert primitive != null;
      return Expressions.constant(primitive.number(0), returnType);
    }

    private Primitive choosePrimitive(Type returnType) {
      switch (Primitive.flavor(returnType)) {
      case PRIMITIVE:
        return Primitive.of(returnType);
      case BOX:
        return Primitive.ofBox(returnType);
      default:
        assert returnType == BigDecimal.class
            : "expected primitive or boxed primitive, got "
            + returnType;
        return Primitive.INT;
      }
    }

    public Expression implementAdd(
        Aggregation aggregation,
        Expression accumulator,
        List<Expression> arguments) {
      assert arguments.size() == 1;
      if (accumulator.type == BigDecimal.class
          || accumulator.type == BigInteger.class) {
        return Expressions.call(
            accumulator,
            "add",
            arguments.get(0));
      }
      return Types.castIfNecessary(
          accumulator.type,
          Expressions.add(
              accumulator,
              Types.castIfNecessary(accumulator.type, arguments.get(0))));
    }

    public Expression implementResult(
        Aggregation aggregation, Expression accumulator) {
      return accumulator;
    }
  }

  static class MinMaxImplementor2 implements AggImplementor2 {
    public boolean callOnNull() {
      return false;
    }

    public Expression implementInit(
        Aggregation aggregation,
        Type returnType,
        List<Type> parameterTypes) {
      final Primitive primitive = Primitive.of(returnType);
      if (primitive != null) {
        // allow nulls even if input does not
        returnType = primitive.boxClass;
      }
      return Types.castIfNecessary(returnType, NULL_EXPR);
    }

    public Expression implementAdd(
        Aggregation aggregation,
        Expression accumulator,
        List<Expression> arguments) {
      // Need to check for null accumulator (e.g. first call to "add"
      // after "init") but because callWithNull() returned false, the
      // container has ensured that argument is not null.
      //
      // acc = acc == null
      //   ? arg
      //   : lesser(acc, arg)
      assert arguments.size() == 1;
      final Expression arg = arguments.get(0);
      return optimize(
          Expressions.condition(
              Expressions.foldOr(
                  Expressions.<Expression>list(
                      Expressions.equal(accumulator, NULL_EXPR))
                      .appendIf(
                          !Primitive.is(arg.type),
                          Expressions.equal(arg, NULL_EXPR))),
              arg,
              Expressions.convert_(
                  Expressions.call(
                      SqlFunctions.class,
                      aggregation == MIN ? "lesser" : "greater",
                      Expressions.unbox(accumulator),
                      Expressions.unbox(arg)),
                  arg.getType())));
    }

    public Expression implementResult(
        Aggregation aggregation, Expression accumulator) {
      return accumulator;
    }
  }

  static class RankImplementor2 implements WinAggImplementor {
    public boolean callOnNull() {
      return false;
    }

    public Expression implementInit(
        Aggregation aggregation,
        Type returnType,
        List<Type> parameterTypes) {
      return Expressions.constant(0, returnType);
    }

    public Expression implementAdd(
        Aggregation aggregation,
        Expression accumulator,
        List<Expression> arguments) {
      return accumulator;
    }

    public Expression implementResult(
        Aggregation aggregation, Expression accumulator) {
      return accumulator;
    }

    public Expression implementResultPlus(Aggregation aggregation,
        Expression accumulator, Expression start, Expression end,
        Expression rows, Expression current) {
      // Rank is 1-based
      return Expressions.add(current, Expressions.constant(1));
    }
  }

  private static class TrimImplementor implements NotNullImplementor {
    public Expression implement(RexToLixTranslator translator, RexCall call,
        List<Expression> translatedOperands) {
      final Object value =
          ((ConstantExpression) translatedOperands.get(0)).value;
      SqlTrimFunction.Flag flag = (SqlTrimFunction.Flag) value;
      return Expressions.call(
          BuiltinMethod.TRIM.method,
          Expressions.constant(
              flag == SqlTrimFunction.Flag.BOTH
              || flag == SqlTrimFunction.Flag.LEADING),
          Expressions.constant(
              flag == SqlTrimFunction.Flag.BOTH
              || flag == SqlTrimFunction.Flag.TRAILING),
          translatedOperands.get(1),
          translatedOperands.get(2));
    }
  }

  private static class MethodImplementor implements NotNullImplementor {
    private final Method method;

    MethodImplementor(Method method) {
      this.method = method;
    }

    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        List<Expression> translatedOperands) {
      return Expressions.call(
          method,
          translatedOperands);
    }
  }

  private static class MethodNameImplementor implements NotNullImplementor {
    private final String methodName;

    MethodNameImplementor(String methodName) {
      this.methodName = methodName;
    }

    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        List<Expression> translatedOperands) {
      return Expressions.call(
          SqlFunctions.class,
          methodName,
          translatedOperands);
    }
  }

  private static class BinaryImplementor implements NotNullImplementor {
    /** Types that can be arguments to comparison operators such as
     * {@code <}. */
    private static final List<Primitive> COMP_OP_TYPES =
        Arrays.asList(
            Primitive.BYTE,
            Primitive.CHAR,
            Primitive.SHORT,
            Primitive.INT,
            Primitive.LONG,
            Primitive.FLOAT,
            Primitive.DOUBLE);

    private static final List<SqlBinaryOperator> COMPARISON_OPERATORS =
        Arrays.asList(
            SqlStdOperatorTable.LESS_THAN,
            SqlStdOperatorTable.LESS_THAN_OR_EQUAL,
            SqlStdOperatorTable.GREATER_THAN,
            SqlStdOperatorTable.GREATER_THAN_OR_EQUAL);

    private final ExpressionType expressionType;
    private final String backupMethodName;

    BinaryImplementor(
        ExpressionType expressionType,
        String backupMethodName) {
      this.expressionType = expressionType;
      this.backupMethodName = backupMethodName;
    }

    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        List<Expression> expressions) {
      // neither nullable:
      //   return x OP y
      // x nullable
      //   null_returns_null
      //     return x == null ? null : x OP y
      //   ignore_null
      //     return x == null ? null : y
      // x, y both nullable
      //   null_returns_null
      //     return x == null || y == null ? null : x OP y
      //   ignore_null
      //     return x == null ? y : y == null ? x : x OP y
      if (backupMethodName != null) {
        final Primitive primitive =
            Primitive.ofBoxOr(expressions.get(0).getType());
        final SqlBinaryOperator op = (SqlBinaryOperator) call.getOperator();
        if (primitive == null
            || COMPARISON_OPERATORS.contains(op)
            && !COMP_OP_TYPES.contains(primitive)) {
          return Expressions.call(
              SqlFunctions.class,
              backupMethodName,
              expressions);
        }
      }
      return Expressions.makeBinary(
          expressionType, expressions.get(0), expressions.get(1));
    }
  }

  private static class UnaryImplementor implements NotNullImplementor {
    private final ExpressionType expressionType;

    UnaryImplementor(ExpressionType expressionType) {
      this.expressionType = expressionType;
    }

    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        List<Expression> translatedOperands) {
      return Expressions.makeUnary(
          expressionType,
          translatedOperands.get(0));
    }
  }

  /** Describes when a function/operator will return null.
   *
   * <p>STRICT and ANY are similar. STRICT says f(a0, a1) will NEVER return
   * null if a0 and a1 are not null. This means that we can check whether f
   * returns null just by checking its arguments. Use STRICT in preference to
   * ANY whenever possible.</p>
   */
  enum NullPolicy {
    /** Returns null if and only if one of the arguments are null. */
    STRICT,
    /** If any of the arguments are null, return null. */
    ANY,
    /** If any of the arguments are false, result is false; else if any
     * arguments are null, result is null; else true. */
    AND,
    /** If any of the arguments are true, result is true; else if any
     * arguments are null, result is null; else false. */
    OR,
    /** If any argument is true, result is false; else if any argument is null,
     * result is null; else true. */
    NOT,
    NONE
  }

  /** Visitor that optimizes expressions.
   *
   * <p>The optimizations are essential, not mere tweaks. Without
   * optimization, expressions such as {@code false == null} will be left in,
   * which are invalid to Janino (because it does not automatically box
   * primitives).</p>
   */
  static class OptimizeVisitor extends Visitor {
    @Override
    public Expression visit(
        TernaryExpression ternaryExpression,
        Expression expression0,
        Expression expression1,
        Expression expression2) {
      final TernaryExpression ternary = (TernaryExpression) super.visit(
          ternaryExpression, expression0, expression1, expression2);
      switch (ternary.getNodeType()) {
      case Conditional:
        Boolean always = always(ternary.expression0);
        if (always != null) {
          // true ? y : z  ===  y
          // false ? y : z  === z
          return always
              ? ternary.expression1
              : ternary.expression2;
        }
        if (ternary.expression1.equals(ternary.expression2)) {
          // a ? b : b   ===   b
          return ternary.expression1;
        }
      }
      return ternary;
    }

    @Override
    public Expression visit(
        BinaryExpression binaryExpression,
        Expression expression0,
        Expression expression1) {
      final BinaryExpression binary = (BinaryExpression) super.visit(
          binaryExpression, expression0, expression1);
      Boolean always;
      switch (binary.getNodeType()) {
      case AndAlso:
        always = always(binary.expression0);
        if (always != null) {
          return always
              ? binary.expression1
              : FALSE_EXPR;
        }
        always = always(binary.expression1);
        if (always != null) {
          return always
              ? binary.expression0
              : FALSE_EXPR;
        }
        break;
      case OrElse:
        always = always(binary.expression0);
        if (always != null) {
          // true or x  --> true
          // null or x  --> x
          // false or x --> x
          return !always
              ? binary.expression1
              : TRUE_EXPR;
        }
        always = always(binary.expression1);
        if (always != null) {
          return !always
              ? binary.expression0
              : TRUE_EXPR;
        }
        break;
      case Equal:
        if (binary.expression0 instanceof ConstantExpression
            && binary.expression1 instanceof ConstantExpression) {
          return binary.expression0.equals(binary.expression1)
              ? TRUE_EXPR : FALSE_EXPR;
        }
        if (isConstantNull(binary.expression1)
            && Primitive.is(binary.expression0.getType())) {
          return FALSE_EXPR;
        }
        if (isConstantNull(binary.expression0)
            && Primitive.is(binary.expression1.getType())) {
          return FALSE_EXPR;
        }
        break;
      case NotEqual:
        if (binary.expression0 instanceof ConstantExpression
            && binary.expression1 instanceof ConstantExpression) {
          return !binary.expression0.equals(binary.expression1)
              ? TRUE_EXPR : FALSE_EXPR;
        }
        if (isConstantNull(binary.expression1)
            && Primitive.is(binary.expression0.getType())) {
          return TRUE_EXPR;
        }
        if (isConstantNull(binary.expression0)
            && Primitive.is(binary.expression1.getType())) {
          return TRUE_EXPR;
        }
        break;
      }
      return binary;
    }

    private boolean isConstantNull(Expression expression) {
      return expression instanceof ConstantExpression
          && ((ConstantExpression) expression).value == null;
    }

    /** Returns whether an expression always evaluates to true or false.
     * Assumes that expression has already been optimized. */
    private static Boolean always(Expression x) {
      if (x.equals(FALSE_EXPR) || x.equals(BOXED_FALSE_EXPR)) {
        return Boolean.FALSE;
      }
      if (x.equals(TRUE_EXPR) || x.equals(BOXED_TRUE_EXPR)) {
        return Boolean.TRUE;
      }
      return null;
    }
  }

  private static class CaseImplementor extends AbstractCallImplementor {
    public Expression implement(
        RexToLixTranslator translator,
        RexCall call) {
      return implementRecurse(translator, call, 0);
    }

    private Expression implementRecurse(
        RexToLixTranslator translator, RexCall call, int i) {
      List<RexNode> operands = call.getOperands();
      if (i == operands.size() - 1) {
        // the "else" clause
        return translator.translate(
            translator.builder.ensureType(
                call.getType(), operands.get(i), false));
      } else {
        return Expressions.condition(
            translator.translate(operands.get(i), NullAs.FALSE),
            translator.translate(
                translator.builder.ensureType(call.getType(),
                    operands.get(i + 1),
                    false)),
            implementRecurse(translator, call, i + 2));
      }
    }
  }

  private static class CastImplementor implements NotNullImplementor {
    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        List<Expression> translatedOperands) {
      assert call.getOperands().size() == 1;
      final RelDataType sourceType = call.getOperands().get(0).getType();
      // It's only possible for the result to be null if both expression
      // and target type are nullable. We assume that the caller did not
      // make a mistake. If expression looks nullable, caller WILL have
      // checked that expression is not null before calling us.
      final boolean nullable =
          translator.isNullable(call)
              && sourceType.isNullable()
              && !Primitive.is(translatedOperands.get(0).getType());
      final RelDataType targetType =
          translator.nullifyType(call.getType(), nullable);
      return translator.translateCast(
          sourceType, targetType, translatedOperands.get(0));
    }
  }

  private static class ReinterpretImplementor implements NotNullImplementor {
    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        List<Expression> translatedOperands) {
      assert call.getOperands().size() == 1;
      return translatedOperands.get(0);
    }
  }

  private static class ValueConstructorImplementor
      implements CallImplementor {
    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        NullAs nullAs) {
      return translator.translateConstructor(
          call.getOperands(),
          call.getOperator().getKind());
    }
  }

  private static class ItemImplementor
      implements CallImplementor {
    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        NullAs nullAs) {
      final MethodImplementor implementor =
          getImplementor(
              call.getOperands().get(0).getType().getSqlTypeName());
      return implementNullSemantics0(
          translator, call, nullAs, NullPolicy.STRICT, false,
          implementor);
    }

    private MethodImplementor getImplementor(SqlTypeName sqlTypeName) {
      switch (sqlTypeName) {
      case ARRAY:
        return new MethodImplementor(BuiltinMethod.ARRAY_ITEM.method);
      case MAP:
        return new MethodImplementor(BuiltinMethod.MAP_ITEM.method);
      default:
        return new MethodImplementor(BuiltinMethod.ANY_ITEM.method);
      }
    }
  }

  private static class SystemFunctionImplementor
      implements CallImplementor {
    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        NullAs nullAs) {
      switch (nullAs) {
      case IS_NULL:
        return Expressions.constant(false);
      case IS_NOT_NULL:
        return Expressions.constant(true);
      }
      final SqlOperator op = call.getOperator();
      if (op == CURRENT_USER
          || op == SESSION_USER
          || op == USER) {
        return Expressions.constant("sa");
      } else if (op == SYSTEM_USER) {
        return Expressions.constant(System.getProperty("user.name"));
      } else if (op == CURRENT_PATH
          || op == CURRENT_ROLE) {
        // By default, the CURRENT_ROLE function returns
        // the empty string because a role has to be set explicitly.
        return Expressions.constant("");
      } else if (op == CURRENT_TIMESTAMP) {
        return Expressions.call(BuiltinMethod.CURRENT_TIMESTAMP.method, ROOT);
      } else if (op == CURRENT_TIME) {
        return Expressions.call(BuiltinMethod.CURRENT_TIME.method, ROOT);
      } else if (op == CURRENT_DATE) {
        return Expressions.call(BuiltinMethod.CURRENT_DATE.method, ROOT);
      } else if (op == LOCALTIMESTAMP) {
        return Expressions.call(BuiltinMethod.LOCAL_TIMESTAMP.method, ROOT);
      } else if (op == LOCALTIME) {
        return Expressions.call(BuiltinMethod.LOCAL_TIME.method, ROOT);
      } else {
        throw new AssertionError("unknown function " + op);
      }
    }
  }

  /** Implements "IS XXX" operations such as "IS NULL"
   * or "IS NOT TRUE".
   *
   * <p>What these operators have in common:</p>
   * 1. They return TRUE or FALSE, never NULL.
   * 2. Of the 3 input values (TRUE, FALSE, NULL) they return TRUE for 1 or 2,
   *    FALSE for the other 2 or 1.
   */
  private static class IsXxxImplementor
      implements CallImplementor {
    private final Boolean seek;
    private final boolean negate;

    public IsXxxImplementor(Boolean seek, boolean negate) {
      this.seek = seek;
      this.negate = negate;
    }

    public Expression implement(
        RexToLixTranslator translator, RexCall call, NullAs nullAs) {
      List<RexNode> operands = call.getOperands();
      assert operands.size() == 1;
      if (seek == null) {
        return translator.translate(
            operands.get(0),
            negate ? NullAs.IS_NOT_NULL : NullAs.IS_NULL);
      } else {
        return maybeNegate(
            negate == seek,
            translator.translate(
                operands.get(0),
                seek ? NullAs.FALSE : NullAs.TRUE));
      }
    }
  }

  private static class NotImplementor implements NotNullImplementor {
    private final NotNullImplementor implementor;

    public NotImplementor(NotNullImplementor implementor) {
      this.implementor = implementor;
    }

    private static NotNullImplementor of(NotNullImplementor implementor) {
      return new NotImplementor(implementor);
    }

    public Expression implement(
        RexToLixTranslator translator,
        RexCall call,
        List<Expression> translatedOperands) {
      final Expression expression =
          implementor.implement(translator, call, translatedOperands);
      return Expressions.not(expression);
    }
  }
}

// End RexImpTable.java
