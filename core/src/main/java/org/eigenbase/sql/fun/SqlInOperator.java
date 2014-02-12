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
package org.eigenbase.sql.fun;

import java.util.*;

import org.eigenbase.reltype.*;
import org.eigenbase.resource.*;
import org.eigenbase.sql.*;
import org.eigenbase.sql.type.*;
import org.eigenbase.sql.validate.*;

/**
 * Definition of the SQL <code>IN</code> operator, which tests for a value's
 * membership in a subquery or a list of values.
 */
public class SqlInOperator extends SqlBinaryOperator {
  //~ Instance fields --------------------------------------------------------

  /**
   * If true the call represents 'NOT IN'.
   */
  private final boolean isNotIn;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a SqlInOperator
   *
   * @param isNotIn Whether this is the 'NOT IN' operator
   */
  SqlInOperator(boolean isNotIn) {
    super(
        isNotIn ? "NOT IN" : "IN",
        SqlKind.IN,
        30,
        true,
        ReturnTypes.BOOLEAN_NULLABLE,
        InferTypes.FIRST_KNOWN,
        null);
    this.isNotIn = isNotIn;
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Returns whether this is the 'NOT IN' operator
   *
   * @return whether this is the 'NOT IN' operator
   */
  public boolean isNotIn() {
    return isNotIn;
  }

  public RelDataType deriveType(
      SqlValidator validator,
      SqlValidatorScope scope,
      SqlCall call) {
    final SqlNode[] operands = call.getOperands();
    assert operands.length == 2;

    final RelDataTypeFactory typeFactory = validator.getTypeFactory();
    RelDataType leftType = validator.deriveType(scope, operands[0]);
    RelDataType rightType;

    // Derive type for RHS.
    if (call.operands[1] instanceof SqlNodeList) {
      // Handle the 'IN (expr, ...)' form.
      List<RelDataType> rightTypeList = new ArrayList<RelDataType>();
      SqlNodeList nodeList = (SqlNodeList) call.operands[1];
      for (int i = 0; i < nodeList.size(); i++) {
        SqlNode node = nodeList.get(i);
        RelDataType nodeType = validator.deriveType(scope, node);
        rightTypeList.add(nodeType);
      }
      rightType = typeFactory.leastRestrictive(rightTypeList);

      // First check that the expressions in the IN list are compatible
      // with each other. Same rules as the VALUES operator (per
      // SQL:2003 Part 2 Section 8.4, <in predicate>).
      if (null == rightType) {
        throw validator.newValidationError(
            call.operands[1],
            EigenbaseResource.instance().IncompatibleTypesInList.ex());
      }

      // Record the RHS type for use by SqlToRelConverter.
      validator.setValidatedNodeType(
          nodeList,
          rightType);
    } else {
      // Handle the 'IN (query)' form.
      rightType = validator.deriveType(scope, operands[1]);
    }

    // Now check that the left expression is compatible with the
    // type of the list. Same strategy as the '=' operator.
    // Normalize the types on both sides to be row types
    // for the purposes of compatibility-checking.
    RelDataType leftRowType =
        SqlTypeUtil.promoteToRowType(
            typeFactory,
            leftType,
            null);
    RelDataType rightRowType =
        SqlTypeUtil.promoteToRowType(
            typeFactory,
            rightType,
            null);

    final ComparableOperandTypeChecker checker =
        (ComparableOperandTypeChecker)
            OperandTypes.COMPARABLE_UNORDERED_COMPARABLE_UNORDERED;
    if (!checker.checkOperandTypes(
        new ExplicitOperatorBinding(
            new SqlCallBinding(
                validator,
                scope,
                call),
            Arrays.asList(leftRowType, rightRowType)))) {
      throw validator.newValidationError(
          call,
          EigenbaseResource.instance().IncompatibleValueType.ex(
              SqlStdOperatorTable.IN.getName()));
    }

    // Result is a boolean, nullable if there are any nullable types
    // on either side.
    RelDataType type = typeFactory.createSqlType(SqlTypeName.BOOLEAN);
    if (leftType.isNullable() || rightType.isNullable()) {
      type = typeFactory.createTypeWithNullability(type, true);
    }

    return type;
  }

  public boolean argumentMustBeScalar(int ordinal) {
    // Argument #0 must be scalar, argument #1 can be a list (1, 2) or
    // a query (select deptno from emp). So, only coerce argument #0 into
    // a scalar subquery. For example, in
    //  select * from emp
    //  where (select count(*) from dept) in (select deptno from dept)
    // we should coerce the LHS to a scalar.
    return ordinal == 0;
  }
}

// End SqlInOperator.java
