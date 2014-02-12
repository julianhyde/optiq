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
package org.eigenbase.sql.util;

import java.util.*;

import org.eigenbase.sql.*;

/**
 * Implementation of the {@link SqlOperatorTable} interface by using a list of
 * {@link SqlOperator operators}.
 */
public class ListSqlOperatorTable implements SqlOperatorTable {
  //~ Instance fields --------------------------------------------------------

  private final List<SqlOperator> operatorList;

  //~ Constructors -----------------------------------------------------------

  public ListSqlOperatorTable() {
    this(new ArrayList<SqlOperator>());
  }

  public ListSqlOperatorTable(List<SqlOperator> operatorList) {
    this.operatorList = operatorList;
  }

  //~ Methods ----------------------------------------------------------------

  public void add(SqlOperator op) {
    operatorList.add(op);
  }

  public List<SqlOperator> lookupOperatorOverloads(
      SqlIdentifier opName,
      SqlFunctionCategory category,
      SqlSyntax syntax) {
    final ArrayList<SqlOperator> list = new ArrayList<SqlOperator>();
    for (SqlOperator operator : operatorList) {
      if (operator.getSyntax() != syntax) {
        continue;
      }
      if (!opName.isSimple()
          || !operator.isName(opName.getSimple())) {
        continue;
      }
      SqlFunctionCategory functionCategory;
      if (operator instanceof SqlFunction) {
        functionCategory = ((SqlFunction) operator).getFunctionType();
      } else {
        functionCategory = SqlFunctionCategory.SYSTEM;
      }
      if (category != functionCategory) {
        continue;
      }
      list.add(operator);
    }
    return list;
  }

  public List<SqlOperator> getOperatorList() {
    return operatorList;
  }
}

// End ListSqlOperatorTable.java
