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
package org.eigenbase.util14;

import java.math.*;

import java.text.*;

/**
 * Utility functions for working with numbers This class is JDK 1.4 compatible.
 */
public class NumberUtil {
  private NumberUtil() {}

  //~ Static fields/initializers ---------------------------------------------

  private static final DecimalFormat FLOAT_FORMATTER;
  private static final DecimalFormat DOUBLE_FORMATTER;
  private static final BigInteger[] BIG_INT_TEN_POW;
  private static final BigInteger[] BIG_INT_MIN_UNSCALED;
  private static final BigInteger[] BIG_INT_MAX_UNSCALED;

  static {
    // TODO: DecimalFormat uses ROUND_HALF_EVEN, not ROUND_HALF_UP
    // Float: precision of 7 (6 digits after .)
    FLOAT_FORMATTER = new DecimalFormat();
    FLOAT_FORMATTER.applyPattern("0.######E0");

    // Double: precision of 16 (15 digits after .)
    DOUBLE_FORMATTER = new DecimalFormat();
    DOUBLE_FORMATTER.applyPattern("0.###############E0");

    BIG_INT_TEN_POW = new BigInteger[20];
    BIG_INT_MIN_UNSCALED = new BigInteger[20];
    BIG_INT_MAX_UNSCALED = new BigInteger[20];

    for (int i = 0; i < BIG_INT_TEN_POW.length; i++) {
      BIG_INT_TEN_POW[i] = BigInteger.TEN.pow(i);
      if (i < 19) {
        BIG_INT_MAX_UNSCALED[i] = BIG_INT_TEN_POW[i].subtract(BigInteger.ONE);
        BIG_INT_MIN_UNSCALED[i] = BIG_INT_MAX_UNSCALED[i].negate();
      } else {
        BIG_INT_MAX_UNSCALED[i] = BigInteger.valueOf(Long.MAX_VALUE);
        BIG_INT_MIN_UNSCALED[i] = BigInteger.valueOf(Long.MIN_VALUE);
      }
    }
  }

  //~ Methods ----------------------------------------------------------------

  public static BigInteger powTen(int exponent) {
    if ((exponent >= 0) && (exponent < BIG_INT_TEN_POW.length)) {
      return BIG_INT_TEN_POW[exponent];
    } else {
      return BigInteger.TEN.pow(exponent);
    }
  }

  public static BigInteger getMaxUnscaled(int precision) {
    return BIG_INT_MAX_UNSCALED[precision];
  }

  public static BigInteger getMinUnscaled(int precision) {
    return BIG_INT_MIN_UNSCALED[precision];
  }

  public static BigDecimal rescaleBigDecimal(BigDecimal bd, int scale) {
    if (bd != null) {
      bd = bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }
    return bd;
  }

  public static BigDecimal toBigDecimal(Number number, int scale) {
    BigDecimal bd = toBigDecimal(number);
    return rescaleBigDecimal(bd, scale);
  }

  public static BigDecimal toBigDecimal(Number number) {
    if (number == null) {
      return null;
    }
    if (number instanceof BigDecimal) {
      return (BigDecimal) number;
    } else if ((number instanceof Double)
        || (number instanceof Float)) {
      // For JDK 1.4 compatibility
      return new BigDecimal(number.doubleValue());
      //return BigDecimal.valueOf(((Number) number).doubleValue());
    } else if (number instanceof BigInteger) {
      return new BigDecimal((BigInteger) number);
    } else {
      return new BigDecimal(number.longValue());
    }
  }

  /**
   * @return whether a BigDecimal is a valid Farrago decimal. If a
   * BigDecimal's unscaled value overflows a long, then it is not a valid
   * Farrago decimal.
   */
  public static boolean isValidDecimal(BigDecimal bd) {
    BigInteger usv = bd.unscaledValue();
    long usvl = usv.longValue();
    return usv.equals(BigInteger.valueOf(usvl));
  }

  public static NumberFormat getApproxFormatter(boolean isFloat) {
    return isFloat ? FLOAT_FORMATTER : DOUBLE_FORMATTER;
  }

  public static long round(double d) {
    if (d < 0) {
      return (long) (d - 0.5);
    } else {
      return (long) (d + 0.5);
    }
  }

  public static Double add(Double a, Double b) {
    if ((a == null) || (b == null)) {
      return null;
    }

    return a + b;
  }

  public static Double divide(Double a, Double b) {
    if ((a == null) || (b == null) || (b == 0D)) {
      return null;
    }

    return a / b;
  }

  public static Double multiply(Double a, Double b) {
    if ((a == null) || (b == null)) {
      return null;
    }

    return a * b;
  }
}

// End NumberUtil.java
