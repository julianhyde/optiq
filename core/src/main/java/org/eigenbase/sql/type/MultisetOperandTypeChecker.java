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
package org.eigenbase.sql.type;

import java.util.Arrays;

import org.eigenbase.reltype.*;
import org.eigenbase.resource.*;
import org.eigenbase.sql.*;

/**
 * Parameter type-checking strategy types must be [nullable] Multiset,
 * [nullable] Multiset and the two types must have the same element type
 *
 * @see MultisetSqlType#getComponentType
 */
public class MultisetOperandTypeChecker implements SqlOperandTypeChecker {
  //~ Methods ----------------------------------------------------------------

  public boolean checkOperandTypes(
      SqlCallBinding callBinding,
      boolean throwOnFailure) {
    SqlCall call = callBinding.getCall();
    SqlNode op0 = call.operands[0];
    if (!OperandTypes.MULTISET.checkSingleOperandType(
        callBinding,
        op0,
        0,
        throwOnFailure)) {
      return false;
    }

    SqlNode op1 = call.operands[1];
    if (!OperandTypes.MULTISET.checkSingleOperandType(
        callBinding,
        op1,
        0,
        throwOnFailure)) {
      return false;
    }

    // TODO: this won't work if element types are of ROW types and there is
    // a mismatch.
    RelDataType biggest =
        callBinding.getTypeFactory().leastRestrictive(
            Arrays.asList(
                callBinding.getValidator()
                    .deriveType(callBinding.getScope(), op0)
                    .getComponentType(),
                callBinding.getValidator()
                    .deriveType(callBinding.getScope(), op1)
                    .getComponentType()));
    if (null == biggest) {
      if (throwOnFailure) {
        throw callBinding.newError(
            EigenbaseResource.instance().TypeNotComparable.ex(
                call.operands[0].getParserPosition().toString(),
                call.operands[1].getParserPosition().toString()));
      }

      return false;
    }
    return true;
  }

  public SqlOperandCountRange getOperandCountRange() {
    return SqlOperandCountRanges.of(2);
  }

  public String getAllowedSignatures(SqlOperator op, String opName) {
    return "<MULTISET> " + opName + " <MULTISET>";
  }
}

// End MultisetOperandTypeChecker.java
