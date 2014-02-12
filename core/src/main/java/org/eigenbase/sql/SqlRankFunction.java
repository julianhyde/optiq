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
package org.eigenbase.sql;

import java.util.List;

import org.eigenbase.reltype.*;
import org.eigenbase.resource.*;
import org.eigenbase.sql.parser.*;
import org.eigenbase.sql.type.*;
import org.eigenbase.sql.validate.*;

import com.google.common.collect.ImmutableList;

/**
 * Operator which aggregates sets of values into a result.
 */
public class SqlRankFunction extends SqlAggFunction {
  //~ Instance fields --------------------------------------------------------

  private final RelDataType type = null;

  //~ Constructors -----------------------------------------------------------

  public SqlRankFunction(String name) {
    super(
        name,
        SqlKind.OTHER_FUNCTION,
        ReturnTypes.INTEGER,
        null,
        OperandTypes.NILADIC,
        SqlFunctionCategory.NUMERIC);
  }

  //~ Methods ----------------------------------------------------------------

  public RelDataType getReturnType(RelDataTypeFactory typeFactory) {
    return type;
  }

  public List<RelDataType> getParameterTypes(RelDataTypeFactory typeFactory) {
    return ImmutableList.of(type);
  }

  public boolean isAggregator() {
    return true;
  }

  public void validateCall(
      SqlCall call,
      SqlValidator validator,
      SqlValidatorScope scope,
      SqlValidatorScope operandScope) {
    final SqlParserPos pos = call.getParserPosition();
    throw SqlUtil.newContextException(
        pos,
        EigenbaseResource.instance().FunctionUndefined.ex(
            call.toString()));
  }
}

// End SqlRankFunction.java
