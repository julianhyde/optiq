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
package net.hydromatic.optiq.test;

import net.hydromatic.linq4j.QueryProvider;
import net.hydromatic.linq4j.function.Function1;

import net.hydromatic.optiq.MutableSchema;
import net.hydromatic.optiq.impl.clone.CloneSchema;
import net.hydromatic.optiq.impl.java.ReflectiveSchema;
import net.hydromatic.optiq.impl.jdbc.JdbcQueryProvider;
import net.hydromatic.optiq.impl.jdbc.JdbcSchema;
import net.hydromatic.optiq.jdbc.OptiqConnection;
import net.hydromatic.optiq.runtime.Hook;

import org.apache.commons.dbcp.BasicDataSource;

import org.eigenbase.util.Pair;
import org.eigenbase.util.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Fluid DSL for testing Optiq connections and queries.
 */
public class OptiqAssert {
  private static final DateFormat UTC_DATE_FORMAT;
  private static final DateFormat UTC_TIME_FORMAT;
  private static final DateFormat UTC_TIMESTAMP_FORMAT;
  static {
    final TimeZone utc = TimeZone.getTimeZone("UTC");
    UTC_DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd");
    UTC_DATE_FORMAT.setTimeZone(utc);
    UTC_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    UTC_TIME_FORMAT.setTimeZone(utc);
    UTC_TIMESTAMP_FORMAT = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss'Z'");
    UTC_TIMESTAMP_FORMAT.setTimeZone(utc);
  }

  /** Implementation of {@link AssertThat} that does nothing. */
  private static final AssertThat DISABLED =
      new AssertThat((Config) null) {
        @Override
        public AssertThat with(Config config) {
          return this;
        }

        @Override
        public AssertThat with(ConnectionFactory connectionFactory) {
          return this;
        }

        @Override
        public AssertThat with(String name, Object schema) {
          return this;
        }

        @Override
        public AssertThat withModel(String model) {
          return this;
        }

        @Override
        public AssertQuery query(String sql) {
          return ASD(sql);
        }

        @Override
        public void connectThrows(String message) {
          // nothing
        }

        @Override
        public void connectThrows(Function1<Throwable, Void> exceptionChecker) {
          // nothing
        }

        @Override
        public <T> AssertThat doWithConnection(Function1<OptiqConnection, T> fn)
            throws Exception {
          return this;
        }

        @Override
        public AssertThat withSchema(String schema) {
          return this;
        }

        @Override
        public AssertThat enable(boolean enabled) {
          return this;
        }
      };

  /** Returns an implementation of {@link AssertQuery} that does nothing. */
  private static AssertQuery ASD(final String sql) {
    return new AssertQuery(null, sql) {
      @Override
      protected Connection createConnection() throws Exception {
        throw new AssertionError("disabled");
      }

      @Override
      public AssertQuery returns(String expected) {
        return this;
      }

      @Override
      public AssertQuery returns(Function1<ResultSet, Void> checker) {
        return this;
      }

      @Override
      public AssertQuery throws_(String message) {
        return this;
      }

      @Override
      public AssertQuery runs() {
        return this;
      }

      @Override
      public AssertQuery explainContains(String expected) {
        return this;
      }

      @Override
      public AssertQuery planContains(String expected) {
        return this;
      }

      @Override
      public AssertQuery planHasSql(String expected) {
        return this;
      }
    };
  }

  public static AssertThat assertThat() {
    return new AssertThat(Config.REGULAR);
  }

  static Function1<Throwable, Void> checkException(
      final String expected) {
    return new Function1<Throwable, Void>() {
      public Void apply(Throwable p0) {
        assertNotNull(
            "expected exception but none was thrown", p0);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        p0.printStackTrace(printWriter);
        printWriter.flush();
        String stack = stringWriter.toString();
        assertTrue(stack, stack.contains(expected));
        return null;
      }
    };
  }

  static Function1<ResultSet, Void> checkResult(final String expected) {
    return new Function1<ResultSet, Void>() {
      public Void apply(ResultSet resultSet) {
        try {
          final String resultString = OptiqAssert.toString(resultSet);
          assertEquals(expected, resultString);
          return null;
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  /** Checks that the result of the second and subsequent executions is the same
   * as the first.
   *
   * @param ordered Whether order should be the same both times
   */
  static Function1<ResultSet, Void> consistentResult(final boolean ordered) {
    return new Function1<ResultSet, Void>() {
      int executeCount = 0;
      Collection expected;

      public Void apply(ResultSet resultSet) {
        ++executeCount;
        try {
          final Collection result =
              OptiqAssert.toStringList(
                  resultSet,
                  ordered ? new ArrayList<String>() : new TreeSet<String>());
          if (executeCount == 1) {
            expected = result;
          } else {
            if (!expected.equals(result)) {
              // compare strings to get better error message
              assertEquals(newlineList(expected), newlineList(result));
              fail("oops");
            }
          }
          return null;
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  static String newlineList(Collection collection) {
    final StringBuilder buf = new StringBuilder();
    for (Object o : collection) {
      buf.append(o).append('\n');
    }
    return buf.toString();
  }

  static Function1<ResultSet, Void> checkResultUnordered(
      final String... lines) {
    return new Function1<ResultSet, Void>() {
      public Void apply(ResultSet resultSet) {
        try {
          final Collection<String> actualSet = new TreeSet<String>();
          OptiqAssert.toStringList(resultSet, actualSet);
          final TreeSet<String> expectedSet =
              new TreeSet<String>(Arrays.asList(lines));
          assertEquals(expectedSet, actualSet);
          return null;
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public static Function1<ResultSet, Void> checkResultContains(
      final String expected) {
    return new Function1<ResultSet, Void>() {
      public Void apply(ResultSet s) {
        try {
          final String actual = OptiqAssert.toString(s);
          if (!actual.contains(expected)) {
            assertEquals("contains", expected, actual);
          }
          return null;
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public static Function1<ResultSet, Void> checkResultType(
      final String expected) {
    return new Function1<ResultSet, Void>() {
      public Void apply(ResultSet s) {
        try {
          final String actual = typeString(s.getMetaData());
          assertEquals(expected, actual);
          return null;
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private static String typeString(ResultSetMetaData metaData)
      throws SQLException {
    final List<String> list = new ArrayList<String>();
    for (int i = 0; i < metaData.getColumnCount(); i++) {
      list.add(
          metaData.getColumnName(i + 1)
              + " "
              + metaData.getColumnTypeName(i + 1)
              + (metaData.isNullable(i + 1) == ResultSetMetaData.columnNoNulls
              ? " NOT NULL"
              : ""));
    }
    return list.toString();
  }

  static void assertQuery(
      Connection connection,
      String sql,
      int limit,
      boolean materializationsEnabled,
      Function1<ResultSet, Void> resultChecker,
      Function1<Throwable, Void> exceptionChecker)
      throws Exception {
    final String message =
        "With materializationsEnabled=" + materializationsEnabled
        + ", limit=" + limit;
    try {
      ((OptiqConnection) connection).getProperties().setProperty(
          "materializationsEnabled", Boolean.toString(materializationsEnabled));
      Statement statement = connection.createStatement();
      statement.setMaxRows(limit <= 0 ? limit : Math.max(limit, 1));
      ResultSet resultSet;
      try {
        resultSet = statement.executeQuery(sql);
        if (exceptionChecker != null) {
          exceptionChecker.apply(null);
          return;
        }
      } catch (Exception e) {
        if (exceptionChecker != null) {
          exceptionChecker.apply(e);
          return;
        }
        throw e;
      } catch (Error e) {
        if (exceptionChecker != null) {
          exceptionChecker.apply(e);
          return;
        }
        throw e;
      }
      if (resultChecker != null) {
        resultChecker.apply(resultSet);
      }
      resultSet.close();
      statement.close();
      connection.close();
    } catch (Throwable e) {
      throw new RuntimeException(message, e);
    }
  }

  static String toString(ResultSet resultSet) throws SQLException {
    final StringBuilder buf = new StringBuilder();
    final ResultSetMetaData metaData = resultSet.getMetaData();
    while (resultSet.next()) {
      int n = metaData.getColumnCount();
      if (n > 0) {
        for (int i = 1;; i++) {
          buf.append(metaData.getColumnLabel(i))
              .append("=")
              .append(resultSet.getString(i));
          if (i == n) {
            break;
          }
          buf.append("; ");
        }
      }
      buf.append("\n");
    }
    return buf.toString();
  }

  static Collection<String> toStringList(ResultSet resultSet,
      Collection<String> list) throws SQLException {
    final StringBuilder buf = new StringBuilder();
    while (resultSet.next()) {
      int n = resultSet.getMetaData().getColumnCount();
      if (n > 0) {
        for (int i = 1;; i++) {
          buf.append(resultSet.getMetaData().getColumnLabel(i))
              .append("=")
              .append(resultSet.getString(i));
          if (i == n) {
            break;
          }
          buf.append("; ");
        }
      }
      list.add(buf.toString());
      buf.setLength(0);
    }
    return list;
  }

  private static String str(ResultSet resultSet, int i) throws SQLException {
    final int columnType = resultSet.getMetaData().getColumnType(i);
    switch (columnType) {
    case Types.DATE:
      final Date date = resultSet.getDate(i, null);
      return date == null ? "null" : UTC_DATE_FORMAT.format(date);
    case Types.TIME:
      final Time time = resultSet.getTime(i, null);
      return time == null ? "null" : UTC_TIME_FORMAT.format(time);
    case Types.TIMESTAMP:
      final Timestamp timestamp = resultSet.getTimestamp(i, null);
      return timestamp == null
          ? "null" : UTC_TIMESTAMP_FORMAT.format(timestamp);
    default:
      return String.valueOf(resultSet.getObject(i));
    }
  }

  /** Calls a non-static method via reflection. Useful for testing methods that
   * don't exist in certain versions of the JDK. */
  static Object call(Object o, String methodName, Object... args)
      throws NoSuchMethodException, InvocationTargetException,
      IllegalAccessException {
    return method(o, methodName, args).invoke(o, args);
  }

  /** Finds a non-static method based on its target, name and arguments.
   * Throws if not found. */
  static Method method(Object o, String methodName, Object[] args) {
    for (Class<?> aClass = o.getClass();;) {
      loop:
      for (Method method1 : aClass.getMethods()) {
        if (method1.getName().equals(methodName)
            && method1.getParameterTypes().length == args.length
            && Modifier.isPublic(method1.getDeclaringClass().getModifiers())) {
          for (Pair<Object, Class<?>> pair
              : Pair.zip(args, method1.getParameterTypes())) {
            if (!pair.right.isInstance(pair.left)) {
              continue loop;
            }
          }
          return method1;
        }
      }
      if (aClass.getSuperclass() != null
          && aClass.getSuperclass() != Object.class) {
        aClass = aClass.getSuperclass();
      } else {
        final Class<?>[] interfaces = aClass.getInterfaces();
        if (interfaces.length > 0) {
          aClass = interfaces[0];
        } else {
          break;
        }
      }
    }
    throw new AssertionError("method " + methodName + " not found");
  }

  static OptiqConnection getConnection(String... schema)
      throws ClassNotFoundException, SQLException {
    Class.forName("net.hydromatic.optiq.jdbc.Driver");
    Connection connection =
        DriverManager.getConnection("jdbc:optiq:");
    OptiqConnection optiqConnection =
        connection.unwrap(OptiqConnection.class);
    MutableSchema rootSchema = optiqConnection.getRootSchema();
    final List<String> schemaList = Arrays.asList(schema);
    if (schemaList.contains("hr")) {
      ReflectiveSchema.create(rootSchema, "hr", new JdbcTest.HrSchema());
    }
    if (schemaList.contains("foodmart")) {
      ReflectiveSchema.create(
          rootSchema, "foodmart", new JdbcTest.FoodmartSchema());
    }
    if (schemaList.contains("lingual")) {
      ReflectiveSchema.create(
          rootSchema, "SALES", new JdbcTest.LingualSchema());
    }
    if (schemaList.contains("metadata")) {
      // always present
    }
    return optiqConnection;
  }

  /**
   * Creates a connection with a given query provider. If provider is null,
   * uses the connection as its own provider. The connection contains a
   * schema called "foodmart" backed by a JDBC connection to MySQL.
   *
   * @param queryProvider Query provider
   * @param withClone Whether to create a "foodmart2" schema as in-memory
   *     clone
   * @return Connection
   * @throws ClassNotFoundException
   * @throws java.sql.SQLException
   */
  static OptiqConnection getConnection(
      QueryProvider queryProvider,
      boolean withClone)
      throws ClassNotFoundException, SQLException {
    Class.forName("net.hydromatic.optiq.jdbc.Driver");
    Class.forName("com.mysql.jdbc.Driver");
    Connection connection = DriverManager.getConnection("jdbc:optiq:");
    OptiqConnection optiqConnection =
        connection.unwrap(OptiqConnection.class);
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setUrl("jdbc:mysql://localhost");
    dataSource.setUsername("foodmart");
    dataSource.setPassword("foodmart");

    JdbcSchema foodmart =
        JdbcSchema.create(
            optiqConnection.getRootSchema(),
            dataSource,
            "foodmart",
            null,
            "foodmart");
    if (withClone) {
      CloneSchema.create(
          optiqConnection.getRootSchema(), "foodmart2", foodmart);
    }
    optiqConnection.setSchema("foodmart2");
    return optiqConnection;
  }

  /**
   * Result of calling {@link OptiqAssert#assertThat}.
   */
  public static class AssertThat {
    private final ConnectionFactory connectionFactory;

    private AssertThat(Config config) {
      this(new ConfigConnectionFactory(config));
    }

    private AssertThat(ConnectionFactory connectionFactory) {
      this.connectionFactory = connectionFactory;
    }

    public AssertThat with(Config config) {
      return new AssertThat(config);
    }

    public AssertThat with(ConnectionFactory connectionFactory) {
      return new AssertThat(connectionFactory);
    }

    /** Sets the default schema to a reflective schema based on a given
     * object. */
    public AssertThat with(final String name, final Object schema) {
      return with(
          new OptiqAssert.ConnectionFactory() {
            public OptiqConnection createConnection() throws Exception {
              Class.forName("net.hydromatic.optiq.jdbc.Driver");
              Connection connection =
                  DriverManager.getConnection("jdbc:optiq:");
              OptiqConnection optiqConnection =
                  connection.unwrap(OptiqConnection.class);
              MutableSchema rootSchema =
                  optiqConnection.getRootSchema();
              ReflectiveSchema.create(rootSchema, name, schema);
              optiqConnection.setSchema(name);
              return optiqConnection;
            }
          });
    }

    public AssertThat withModel(final String model) {
      return new AssertThat(
          new OptiqAssert.ConnectionFactory() {
            public OptiqConnection createConnection() throws Exception {
              Class.forName("net.hydromatic.optiq.jdbc.Driver");
              final Properties info = new Properties();
              info.setProperty("model", "inline:" + model);
              return (OptiqConnection) DriverManager.getConnection(
                  "jdbc:optiq:", info);
            }
          });
    }

    /** Adds materializations to the schema. */
    public AssertThat withMaterializations(
        String model, String... materializations) {
      assert materializations.length % 2 == 0;
      final StringBuilder buf = new StringBuilder("materializations: [\n");
      for (int i = 0; i < materializations.length; i++) {
        String table = materializations[i++];
        buf.append("    {\n")
            .append("      table: '").append(table).append("',\n")
            .append("      view: '").append(table).append("v',\n");
        String sql = materializations[i];
        final String sql2 = sql
            .replaceAll("`", "\"")
            .replaceAll("'", "''");
        buf.append("      sql: '").append(sql2)
            .append("'\n")
            .append(i >= materializations.length - 1 ? "}\n" : "},\n");
      }
      buf.append("  ],\n");
      final String model2;
      if (model.contains("jdbcSchema: ")) {
        model2 = model.replace("jdbcSchema: ", buf + "jdbcSchema: ");
      } else if (model.contains("type: ")) {
        model2 = model.replace("type: ", buf + "type: ");
      } else {
        throw new AssertionError("do not know where to splice");
      }
      System.out.println(model2);
      return withModel(model2);
    }

    public AssertQuery query(String sql) {
      System.out.println(sql);
      return new AssertQuery(connectionFactory, sql);
    }

    /** Asserts that there is an exception with the given message while
     * creating a connection. */
    public void connectThrows(String message) {
      connectThrows(checkException(message));
    }

    /** Asserts that there is an exception that matches the given predicate
     * while creating a connection. */
    public void connectThrows(
        Function1<Throwable, Void> exceptionChecker) {
      Throwable throwable;
      try {
        Connection x = connectionFactory.createConnection();
        try {
          x.close();
        } catch (SQLException e) {
          // ignore
        }
        throwable = null;
      } catch (Throwable e) {
        throwable = e;
      }
      exceptionChecker.apply(throwable);
    }

    /** Creates a connection and executes a callback. */
    public <T> AssertThat doWithConnection(Function1<OptiqConnection, T> fn)
        throws Exception {
      Connection connection = connectionFactory.createConnection();
      try {
        T t = fn.apply((OptiqConnection) connection);
        Util.discard(t);
        return AssertThat.this;
      } finally {
        connection.close();
      }
    }

    public AssertThat withSchema(String schema) {
      return new AssertThat(
          new SchemaConnectionFactory(connectionFactory, schema));
    }

    public AssertThat enable(boolean enabled) {
      return enabled ? this : DISABLED;
    }
  }

  public interface ConnectionFactory {
    OptiqConnection createConnection() throws Exception;
  }

  private static class ConfigConnectionFactory implements ConnectionFactory {
    private final Config config;

    public ConfigConnectionFactory(Config config) {
      this.config = config;
    }

    public OptiqConnection createConnection() throws Exception {
      switch (config) {
      case REGULAR:
        return getConnection("hr", "foodmart");
      case REGULAR_PLUS_METADATA:
        return getConnection("hr", "foodmart", "metadata");
      case LINGUAL:
        return getConnection("lingual");
      case JDBC_FOODMART2:
        return getConnection(null, false);
      case JDBC_FOODMART:
        return getConnection(
            JdbcQueryProvider.INSTANCE, false);
      case FOODMART_CLONE:
        return getConnection(JdbcQueryProvider.INSTANCE, true);
      default:
        throw Util.unexpected(config);
      }
    }
  }

  private static class DelegatingConnectionFactory
      implements ConnectionFactory {
    private final ConnectionFactory factory;

    public DelegatingConnectionFactory(ConnectionFactory factory) {
      this.factory = factory;
    }

    public OptiqConnection createConnection() throws Exception {
      return factory.createConnection();
    }
  }

  private static class SchemaConnectionFactory
      extends DelegatingConnectionFactory {
    private final String schema;

    public SchemaConnectionFactory(ConnectionFactory factory, String schema) {
      super(factory);
      this.schema = schema;
    }

    @Override
    public OptiqConnection createConnection() throws Exception {
      OptiqConnection connection = super.createConnection();
      connection.setSchema(schema);
      return connection;
    }
  }

  public static class AssertQuery {
    private final String sql;
    private ConnectionFactory connectionFactory;
    private String plan;
    private int limit;
    private boolean materializationsEnabled = false;

    private AssertQuery(ConnectionFactory connectionFactory, String sql) {
      this.sql = sql;
      this.connectionFactory = connectionFactory;
    }

    protected Connection createConnection() throws Exception {
      return connectionFactory.createConnection();
    }

    public AssertQuery returns(String expected) {
      return returns(checkResult(expected));
    }

    public AssertQuery returns(Function1<ResultSet, Void> checker) {
      try {
        assertQuery(createConnection(), sql, limit, materializationsEnabled,
            checker, null);
        return this;
      } catch (Exception e) {
        throw new RuntimeException(
            "exception while executing [" + sql + "]", e);
      }
    }

    public AssertQuery returnsUnordered(String... lines) {
      return returns(checkResultUnordered(lines));
    }

    public AssertQuery throws_(String message) {
      try {
        assertQuery(createConnection(), sql, limit, materializationsEnabled,
            null, checkException(message));
        return this;
      } catch (Exception e) {
        throw new RuntimeException(
            "exception while executing [" + sql + "]", e);
      }
    }

    public AssertQuery runs() {
      try {
        assertQuery(createConnection(), sql, limit, materializationsEnabled,
            null, null);
        return this;
      } catch (Exception e) {
        throw new RuntimeException(
            "exception while executing [" + sql + "]", e);
      }
    }

    public AssertQuery typeIs(String expected) {
      try {
        assertQuery(
            createConnection(), sql, limit, false,
            checkResultType(expected), null);
        return this;
      } catch (Exception e) {
        throw new RuntimeException(
            "exception while executing [" + sql + "]", e);
      }
    }

    public AssertQuery explainContains(String expected) {
      String explainSql = "explain plan for " + sql;
      try {
        assertQuery(
            createConnection(), explainSql, limit, materializationsEnabled,
            checkResultContains(expected), null);
        return this;
      } catch (Exception e) {
        throw new RuntimeException(
            "exception while executing [" + explainSql + "]", e);
      }
    }

    public AssertQuery planContains(String expected) {
      ensurePlan();
      assertTrue(
          "Plan [" + plan + "] contains [" + expected + "]",
          plan.contains(expected));
      return this;
    }

    public AssertQuery planHasSql(String expected) {
      return planContains(
          "getDataSource(), \"" + expected.replaceAll("\n", "\\\\n")
              + "\"");
    }

    private void ensurePlan() {
      if (plan != null) {
        return;
      }
      final Hook.Closeable hook = Hook.JAVA_PLAN.add(
          new Function1<Object, Object>() {
            public Object apply(Object a0) {
              plan = (String) a0;
              return null;
            }
          });
      try {
        assertQuery(createConnection(), sql, limit, materializationsEnabled,
            null, null);
        assertNotNull(plan);
      } catch (Exception e) {
        throw new RuntimeException(
            "exception while executing [" + sql + "]", e);
      } finally {
        hook.close();
      }
    }

    /** Sets a limit on the number of rows returned. -1 means no limit. */
    public AssertQuery limit(int limit) {
      this.limit = limit;
      return this;
    }

    public void sameResultWithMaterializationsDisabled() {
      boolean save = materializationsEnabled;
      try {
        materializationsEnabled = false;
        final boolean ordered = sql.toUpperCase().contains("ORDER BY");
        final Function1<ResultSet, Void> checker = consistentResult(ordered);
        returns(checker);
        materializationsEnabled = true;
        returns(checker);
      } finally {
        materializationsEnabled = save;
      }
    }

    public AssertQuery enableMaterializations(boolean enable) {
      this.materializationsEnabled = enable;
      return this;
    }
  }

  public enum Config {
    /**
     * Configuration that creates a connection with two in-memory data sets:
     * {@link net.hydromatic.optiq.test.JdbcTest.HrSchema} and
     * {@link net.hydromatic.optiq.test.JdbcTest.FoodmartSchema}.
     */
    REGULAR,

    /**
     * Configuration that creates a connection with an in-memory data set
     * similar to the smoke test in Cascading Lingual.
     */
    LINGUAL,

    /**
     * Configuration that creates a connection to a MySQL server. Tables
     * such as "customer" and "sales_fact_1997" are available. Queries
     * are processed by generating Java that calls linq4j operators
     * such as
     * {@link net.hydromatic.linq4j.Enumerable#where(net.hydromatic.linq4j.function.Predicate1)}.
     */
    JDBC_FOODMART,
    JDBC_FOODMART2,

    /** Configuration that contains an in-memory clone of the FoodMart
     * database. */
    FOODMART_CLONE,

    /** Configuration that includes the metadata schema. */
    REGULAR_PLUS_METADATA,
  }
}

// End OptiqAssert.java
