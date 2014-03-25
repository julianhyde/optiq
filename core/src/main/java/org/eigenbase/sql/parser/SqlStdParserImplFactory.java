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

import java.io.Reader;

import org.eigenbase.sql.parser.impl.SqlParserImpl;

/**
 * Factory for creating Opitq's parser implementation
 * {@link org.eigenbase.sql.parser.impl.SqlParserImpl}
 */
public class SqlStdParserImplFactory implements SqlParserImplFactory {

  public static final SqlStdParserImplFactory FACTORY =
    new SqlStdParserImplFactory();

  /**
   * Returns parser implementation
   * {@link org.eigenbase.sql.parser.impl.SqlParserImpl}
   *
   * @return
   */
  public SqlAbstractParserImpl getParser(Reader stream) {
    return new SqlParserImpl(stream);
  }
}
