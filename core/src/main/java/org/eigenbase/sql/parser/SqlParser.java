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
package org.eigenbase.sql.parser;

import java.io.*;

import org.eigenbase.sql.*;
import org.eigenbase.sql.parser.SqlAbstractParserImpl.Metadata;
import org.eigenbase.sql.parser.impl.*;
import org.eigenbase.util.*;

import net.hydromatic.avatica.Casing;
import net.hydromatic.avatica.Quoting;

/**
 * A <code>SqlParser</code> parses a SQL statement.
 */
public class SqlParser {
  //~ Instance fields --------------------------------------------------------

  private final SqlAbstractParserImpl parser;
  private String originalInput;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a <code>SqlParser</code> that reads input from a string.
   */
  public SqlParser(String s) {
    this(SqlStdParserImplFactory.FACTORY, s);
  }

  /**
   * Creates a <code>SqlParser</code> that reads input from a string. It gets
   * the parser implementation from given {@link SqlParserImplFactory}.
   *
   * @param parserFactory creates and returns parser implementation.
   * @param s
   */
  public SqlParser(SqlParserImplFactory parserFactory, String s) {
    this(parserFactory, s, Quoting.DOUBLE_QUOTE, Casing.TO_UPPER,
      Casing.UNCHANGED);
  }

  /**
   * Creates a <code>SqlParser</code> that reads input from a string.
   */
  public SqlParser(String s, Quoting quoting, Casing unquotedCasing,
      Casing quotedCasing) {
     this(SqlStdParserImplFactory.FACTORY, s, quoting, unquotedCasing,
       quotedCasing);
  }

  /**
   * Creates a <code>SqlParser</code> that reads input from a string and
   * gets the parser implementation from given {@link SqlParserImplFactory}
   * with given quoting and casing.
   *
   * @param parserFactory
   * @param s
   * @param quoting
   * @param unquotedCasing
   * @param quotedCasing
   */
  public SqlParser(SqlParserImplFactory parserFactory, String s,
      Quoting quoting, Casing unquotedCasing, Casing quotedCasing) {
    parser = parserFactory.getParser(new StringReader(s));
    parser.setTabSize(1);
    parser.setQuotedCasing(quotedCasing);
    parser.setUnquotedCasing(unquotedCasing);
    this.originalInput = s;
    switch (quoting) {
    case DOUBLE_QUOTE:
      parser.switchTo("DQID");
      break;
    case BACK_TICK:
      parser.switchTo("BTID");
      break;
    case BRACKET:
      parser.switchTo("DEFAULT");
      break;
    }
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Parses a SQL expression.
   *
   * @throws SqlParseException if there is a parse error
   */
  public SqlNode parseExpression() throws SqlParseException {
    try {
      return parser.parseSqlExpressionEof();
    } catch (Throwable ex) {
      if ((ex instanceof EigenbaseContextException)
          && (originalInput != null)) {
        ((EigenbaseContextException) ex).setOriginalStatement(
            originalInput);
      }
      throw parser.normalizeException(ex);
    }
  }

  /**
   * Parses a <code>SELECT</code> statement.
   *
   * @return A {@link org.eigenbase.sql.SqlSelect} for a regular <code>
   * SELECT</code> statement; a {@link org.eigenbase.sql.SqlBinaryOperator}
   * for a <code>UNION</code>, <code>INTERSECT</code>, or <code>EXCEPT</code>.
   * @throws SqlParseException if there is a parse error
   */
  public SqlNode parseQuery() throws SqlParseException {
    try {
      return parser.parseSqlStmtEof();
    } catch (Throwable ex) {
      if ((ex instanceof EigenbaseContextException)
          && (originalInput != null)) {
        ((EigenbaseContextException) ex).setOriginalStatement(
            originalInput);
      }
      throw parser.normalizeException(ex);
    }
  }

  /**
   * Parses an SQL statement.
   *
   * @return top-level SqlNode representing stmt
   * @throws SqlParseException if there is a parse error
   */
  public SqlNode parseStmt() throws SqlParseException {
    try {
      return parser.parseSqlStmtEof();
    } catch (Throwable ex) {
      if ((ex instanceof EigenbaseContextException)
          && (originalInput != null)) {
        ((EigenbaseContextException) ex).setOriginalStatement(
            originalInput);
      }
      throw parser.normalizeException(ex);
    }
  }

  /**
   * @Return Returns {@link Metadata} of underlying parser implementation.
   */
  public Metadata getMetadata() {
    return parser.getMetadata();
  }
}

// End SqlParser.java
