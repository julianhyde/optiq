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
package net.hydromatic.optiq;

import net.hydromatic.linq4j.*;
import net.hydromatic.linq4j.expressions.FunctionExpression;
import net.hydromatic.linq4j.expressions.Primitive;
import net.hydromatic.linq4j.expressions.Types;
import net.hydromatic.linq4j.function.*;
import net.hydromatic.optiq.impl.java.ReflectiveSchema;
import net.hydromatic.optiq.impl.jdbc.JdbcSchema;
import net.hydromatic.optiq.impl.mongodb.MongoTable;
import net.hydromatic.optiq.runtime.*;

import java.lang.reflect.Method;
import java.util.*;
import javax.sql.DataSource;

/**
 * Builtin methods.
 */
public enum BuiltinMethod {
  QUERYABLE_SELECT(Queryable.class, "select", FunctionExpression.class),
  AS_QUERYABLE(Enumerable.class, "asQueryable"),
  INTO(ExtendedEnumerable.class, "into", Collection.class),
  GET_SUB_SCHEMA(DataContext.class, "getSubSchema", String.class),
  GET_TARGET(ReflectiveSchema.class, "getTarget"),
  DATA_CONTEXT_GET_TABLE(DataContext.class, "getTable", String.class,
      Class.class),
  JDBC_SCHEMA_DATA_SOURCE(JdbcSchema.class, "getDataSource"),
  RESULT_SET_ENUMERABLE_OF(ResultSetEnumerable.class, "of", DataSource.class,
      String.class, Primitive[].class),
  MONGO_TABLE_FIND(MongoTable.class, "find", String.class, String.class,
      List.class),
  MONGO_TABLE_AGGREGATE(MongoTable.class, "aggregate", List.class, List.class),
  JOIN(ExtendedEnumerable.class, "join", Enumerable.class, Function1.class,
      Function1.class, Function2.class),
  SELECT(ExtendedEnumerable.class, "select", Function1.class),
  SELECT2(ExtendedEnumerable.class, "select", Function2.class),
  WHERE(ExtendedEnumerable.class, "where", Predicate1.class),
  WHERE2(ExtendedEnumerable.class, "where", Predicate2.class),
  GROUP_BY(ExtendedEnumerable.class, "groupBy", Function1.class),
  GROUP_BY2(ExtendedEnumerable.class, "groupBy", Function1.class,
      Function0.class, Function2.class, Function2.class),
  AGGREGATE(ExtendedEnumerable.class, "aggregate", Object.class,
      Function2.class, Function1.class),
  ORDER_BY(ExtendedEnumerable.class, "orderBy", Function1.class,
      Comparator.class),
  UNION(ExtendedEnumerable.class, "union", Enumerable.class),
  CONCAT(ExtendedEnumerable.class, "concat", Enumerable.class),
  INTERSECT(ExtendedEnumerable.class, "intersect", Enumerable.class),
  EXCEPT(ExtendedEnumerable.class, "except", Enumerable.class),
  SINGLETON_ENUMERABLE(Linq4j.class, "singletonEnumerable", Object.class),
  NULLS_COMPARATOR(Functions.class, "nullsComparator", boolean.class,
      boolean.class),
  ARRAY_COMPARER(Functions.class, "arrayComparer"),
  ARRAYS_AS_LIST(FlatLists.class, "of", Object[].class),
  LIST2(FlatLists.class, "of", Object.class, Object.class),
  LIST3(FlatLists.class, "of", Object.class, Object.class, Object.class),
  IDENTITY_SELECTOR(Functions.class, "identitySelector"),
  AS_ENUMERABLE(Linq4j.class, "asEnumerable", Object[].class),
  AS_ENUMERABLE2(Linq4j.class, "asEnumerable", Iterable.class),
  AS_LIST(Primitive.class, "asList", Object.class),
  ENUMERATOR_CURRENT(Enumerator.class, "current"),
  ENUMERATOR_MOVE_NEXT(Enumerator.class, "moveNext"),
  ENUMERATOR_CLOSE(Enumerator.class, "close"),
  ENUMERATOR_RESET(Enumerator.class, "reset"),
  ENUMERABLE_ENUMERATOR(Enumerable.class, "enumerator"),
  ENUMERABLE_FOREACH(Enumerable.class, "foreach", Function1.class),
  TYPED_GET_ELEMENT_TYPE(Typed.class, "getElementType"),
  EXECUTABLE_EXECUTE(Executable.class, "execute", DataContext.class),
  COMPARATOR_COMPARE(Comparator.class, "compare", Object.class, Object.class),
  COLLECTIONS_REVERSE_ORDER(Collections.class, "reverseOrder"),
  COLLECTION_SIZE(Collection.class, "size"),
  MAP_CLEAR(Map.class, "clear"),
  MAP_GET(Map.class, "get", Object.class),
  MAP_PUT(Map.class, "put", Object.class, Object.class),
  COLLECTION_ADD(Collection.class, "add", Object.class),
  LIST_GET(List.class, "get", int.class),
  ITERATOR_HAS_NEXT(Iterator.class, "hasNext"),
  ITERATOR_NEXT(Iterator.class, "next"),
  MATH_MAX(Math.class, "max", int.class, int.class),
  MATH_MIN(Math.class, "min", int.class, int.class),
  SORTED_MULTI_MAP_PUT_MULTI(SortedMultiMap.class, "putMulti", Object.class,
      Object.class),
  SORTED_MULTI_MAP_ARRAYS(SortedMultiMap.class, "arrays", Comparator.class),
  SORTED_MULTI_MAP_SINGLETON(SortedMultiMap.class, "singletonArrayIterator",
      Comparator.class, List.class),
  ARRAY_ITEM(SqlFunctions.class, "arrayItem", List.class, int.class),
  MAP_ITEM(SqlFunctions.class, "mapItem", Map.class, Object.class),
  ANY_ITEM(SqlFunctions.class, "item", Object.class, Object.class),
  UPPER(SqlFunctions.class, "upper", String.class),
  LOWER(SqlFunctions.class, "lower", String.class),
  INITCAP(SqlFunctions.class, "initcap", String.class),
  SUBSTRING(SqlFunctions.class, "substring", String.class, int.class,
      int.class),
  CHAR_LENGTH(SqlFunctions.class, "charLength", String.class),
  STRING_CONCAT(SqlFunctions.class, "concat", String.class, String.class),
  OVERLAY(SqlFunctions.class, "overlay", String.class, String.class, int.class),
  OVERLAY3(SqlFunctions.class, "overlay", String.class, String.class, int.class,
      int.class),
  TRUNCATE(SqlFunctions.class, "truncate", String.class, int.class),
  TRIM(SqlFunctions.class, "trim", String.class),
  LTRIM(SqlFunctions.class, "ltrim", String.class),
  RTRIM(SqlFunctions.class, "rtrim", String.class),
  LIKE(SqlFunctions.class, "like", String.class, String.class),
  SIMILAR(SqlFunctions.class, "similar", String.class, String.class),
  IS_TRUE(SqlFunctions.class, "isTrue", Boolean.class),
  IS_NOT_FALSE(SqlFunctions.class, "isNotFalse", Boolean.class),
  MODIFIABLE_TABLE_GET_MODIFIABLE_COLLECTION(ModifiableTable.class,
      "getModifiableCollection"),
  STRING_TO_BOOLEAN(SqlFunctions.class, "toBoolean", String.class),
  UNIX_DATE_TO_STRING(SqlFunctions.class, "unixDateToString", int.class),
  UNIX_TIME_TO_STRING(SqlFunctions.class, "unixTimeToString", int.class),
  UNIX_TIMESTAMP_TO_STRING(SqlFunctions.class, "unixTimestampToString",
      long.class),
  BOOLEAN_TO_STRING(SqlFunctions.class, "toString", boolean.class),
  ROUND_LONG(SqlFunctions.class, "round", long.class, long.class),
  ROUND_INT(SqlFunctions.class, "round", int.class, int.class);

  public final Method method;

  private static final HashMap<Method, BuiltinMethod> MAP =
      new HashMap<Method, BuiltinMethod>();

  static {
    for (BuiltinMethod builtinMethod : BuiltinMethod.values()) {
      MAP.put(builtinMethod.method, builtinMethod);
    }
  }

  BuiltinMethod(Class clazz, String methodName, Class... argumentTypes) {
    this.method = Types.lookupMethod(clazz, methodName, argumentTypes);
  }

  public static BuiltinMethod lookup(Method method) {
    return MAP.get(method);
  }
}

// End BuiltinMethod.java
