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
package net.hydromatic.optiq.runtime;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Helper methods to implement SQL functions in generated code.
 *
 * <p>Not present: and, or, not (builtin operators are better, because they
 * use lazy evaluation. Implementations do not check for null values; the
 * calling code must do that.</p>
 *
 * @author jhyde
 */
@SuppressWarnings("UnnecessaryUnboxing")
public class SqlFunctions {

    private static final DecimalFormat DOUBLE_FORMAT =
        new DecimalFormat("0.0E0");

    /** SQL SUBSTRING(string FROM ... FOR ...) function. */
    public static String substring(String s, int from, int for_) {
        return s.substring(from - 1, Math.min(from - 1 + for_, s.length()));
    }

    /** SQL SUBSTRING(string FROM ...) function. */
    public static String substring(String s, int from) {
        return s.substring(from - 1);
    }

    /** SQL UPPER(string) function. */
    public static String upper(String s) {
        if (s == null) {
            return null;
        }
        return s.toUpperCase();
    }

    /** SQL LOWER(string) function. */
    public static String lower(String s) {
        if (s == null) {
            return null;
        }
        return s.toLowerCase();
    }

    /** SQL INITCAP(string) function. */
    public static String initCap(String s) {

        // Assumes Alpha as [A-Za-z0-9]
        // white space is treated as everything else.
        //
        int len = s.length();
        boolean start = true;
        char curCh;
        StringBuilder newS = new StringBuilder();

        for (int i = 0; i<len; i++) {
            curCh = s.charAt(i);
            if (start) {  // curCh is whitespace or first character of word.
                if ( ((int)curCh > 47) && ((int)curCh < 58)) { // 0-9
                    start = false;
                }
                else if (((int)curCh > 64) && ((int)curCh < 91)) {  // A-Z
                    start = false;
                }
                else if (((int)curCh > 96) && ((int)curCh < 123)) {  // a-z
                    start = false;
                    curCh = (char)((int)curCh-32); // Uppercase this character

                }
                // else {} whitespace
            }
            else {  // Inside of a word or white space after end of word.
                if ( ((int)curCh > 47) && ((int)curCh < 58)) { // 0-9
                    // noop
                }
                else if (((int)curCh > 64) && ((int)curCh < 91)) {  // A-Z
                    curCh = (char)((int)curCh+32);  // Lower Case this character
                }
                else if (((int)curCh > 96) && ((int)curCh < 123)) {  // a-z
                    //noop

                }
                else { //whitespace
                    start = true;

                }
            }
            newS.append(curCh);
        } // for each character in s
        return newS.toString();
    }

    /** SQL CHARACTER_LENGTH(string) function. */
    public static Integer charLength(String s) {
        if (s == null) {
            return null;
        }
        return s.length();
    }

    /** SQL {@code string || string} operator. */
    public static String concat(String s0, String s1) {
        if (s0 == null || s1 == null) {
            return null;
        }
        return s0 + s1;
    }

    /** SQL {@code RTRIM} function. */
    public static String rtrim(String s) {
        return trim_(s, false, true);
    }

    /** SQL {@code LTRIM} function. */
    public static String ltrim(String s) {
        return trim_(s, true, false);
    }

    /** SQL {@code TRIM} function. */
    public static String trim(String s) {
        return trim_(s, true, true);
    }

    /** SQL {@code TRIM} function. */
    private static String trim_(String s, boolean left, boolean right) {
        int j = s.length();
        if (right) {
            for (;;) {
                if (j == 0) {
                    return "";
                }
                if (s.charAt(j - 1) != ' ') {
                    break;
                }
                --j;
            }
        }
        int i = 0;
        if (left) {
            for (;;) {
                if (i == j) {
                    return "";
                }
                if (s.charAt(i) != ' ') {
                    break;
                }
                ++i;
            }
        }
        return s.substring(i, j);
    }

    /** SQL {@code OVERLAY} function. */
    public static String overlay(String s, String r, int start) {
        if (s == null || r == null) {
            return null;
        }
        return s.substring(0, start - 1)
            + r
            + s.substring(start - 1 + r.length());
    }

    /** SQL {@code OVERLAY} function. */
    public static String overlay(String s, String r, int start, int length) {
        if (s == null || r == null) {
            return null;
        }
        return s.substring(0, start - 1)
            + r
            + s.substring(start - 1 + length);
    }

    // =

    /** SQL = operator applied to Object values (including String; neither
     * side may be null). */
    public static boolean eq(Object b0, Object b1) {
        return b0.equals(b1);
    }

    /** SQL = operator applied to BigDecimal values (neither may be null). */
    public static boolean eq(BigDecimal b0, BigDecimal b1) {
        return b0.stripTrailingZeros().equals(b1.stripTrailingZeros());
    }

    // <>

    /** SQL &lt;&gt; operator applied to Object values (including String;
     * neither side may be null). */
    public static boolean ne(Object b0, Object b1) {
        return !b0.equals(b1);
    }

    // <

    /** SQL &lt; operator applied to boolean values. */
    public static boolean lt(boolean b0, boolean b1) {
        return compare(b0, b1) < 0;
    }

    /** SQL &lt; operator applied to String values. */
    public static Boolean lt(String b0, String b1) {
        return (b0 == null || b1 == null) ? null : (b0.compareTo(b1) < 0);
    }

    /** SQL &lt; operator applied to BigDecimal values. */
    public static Boolean lt(BigDecimal b0, BigDecimal b1) {
        return (b0 == null || b1 == null) ? null : (b0.compareTo(b1) < 0);
    }

    // <=

    /** SQL &le; operator applied to boolean values. */
    public static boolean le(boolean b0, boolean b1) {
        return compare(b0, b1) <= 0;
    }

    /** SQL &le; operator applied to String values. */
    public static Boolean le(String b0, String b1) {
        return (b0 == null || b1 == null) ? null : (b0.compareTo(b1) <= 0);
    }

    /** SQL &le; operator applied to BigDecimal values. */
    public static Boolean le(BigDecimal b0, BigDecimal b1) {
        return (b0 == null || b1 == null) ? null : (b0.compareTo(b1) <= 0);
    }

    // >

    /** SQL &gt; operator applied to boolean values. */
    public static boolean gt(boolean b0, boolean b1) {
        return compare(b0, b1) > 0;
    }

    /** SQL &gt; operator applied to String values. */
    public static Boolean gt(String b0, String b1) {
        return (b0 == null || b1 == null) ? null : (b0.compareTo(b1) > 0);
    }

    /** SQL &gt; operator applied to BigDecimal values. */
    public static Boolean gt(BigDecimal b0, BigDecimal b1) {
        return (b0 == null || b1 == null) ? null : (b0.compareTo(b1) > 0);
    }

    // >=

    /** SQL &ge; operator applied to boolean values. */
    public static boolean ge(boolean b0, boolean b1) {
        return compare(b0, b1) >= 0;
    }

    /** SQL &ge; operator applied to String values. */
    public static Boolean ge(String b0, String b1) {
        return (b0 == null || b1 == null) ? null : (b0.compareTo(b1) >= 0);
    }

    /** SQL &ge; operator applied to BigDecimal values. */
    public static Boolean ge(BigDecimal b0, BigDecimal b1) {
        return (b0 == null || b1 == null) ? null : (b0.compareTo(b1) >= 0);
    }

    // +

    /** SQL <code>+</code> operator applied to int values. */
    public static int plus(int b0, int b1) {
        return b0 + b1;
    }

    /** SQL <code>+</code> operator applied to int values; left side may be
     * null. */
    public static Integer plus(Integer b0, int b1) {
        return b0 == null ? null : (b0 + b1);
    }

    /** SQL <code>+</code> operator applied to int values; right side may be
     * null. */
    public static Integer plus(int b0, Integer b1) {
        return b1 == null ? null : (b0 + b1);
    }

    /** SQL <code>+</code> operator applied to nullable int values. */
    public static Integer plus(Integer b0, Integer b1) {
        return (b0 == null || b1 == null) ? null : (b0 + b1);
    }

    /** SQL <code>+</code> operator applied to nullable long and int values. */
    public static Long plus(Long b0, Integer b1) {
        return (b0 == null || b1 == null)
            ? null
            : (b0.longValue() + b1.longValue());
    }

    /** SQL <code>+</code> operator applied to nullable int and long values. */
    public static Long plus(Integer b0, Long b1) {
        return (b0 == null || b1 == null)
            ? null
            : (b0.longValue() + b1.longValue());
    }

    /** SQL <code>+</code> operator applied to BigDecimal values. */
    public static BigDecimal plus(BigDecimal b0, BigDecimal b1) {
        return (b0 == null || b1 == null) ? null : b0.add(b1);
    }

    // -

    /** SQL <code>-</code> operator applied to int values. */
    public static int minus(int b0, int b1) {
        return b0 - b1;
    }

    /** SQL <code>-</code> operator applied to int values; left side may be
     * null. */
    public static Integer minus(Integer b0, int b1) {
        return b0 == null ? null : (b0 - b1);
    }

    /** SQL <code>-</code> operator applied to int values; right side may be
     * null. */
    public static Integer minus(int b0, Integer b1) {
        return b1 == null ? null : (b0 - b1);
    }

    /** SQL <code>-</code> operator applied to nullable int values. */
    public static Integer minus(Integer b0, Integer b1) {
        return (b0 == null || b1 == null) ? null : (b0 - b1);
    }

    /** SQL <code>-</code> operator applied to nullable long and int values. */
    public static Long minus(Long b0, Integer b1) {
        return (b0 == null || b1 == null)
            ? null
            : (b0.longValue() - b1.longValue());
    }

    /** SQL <code>-</code> operator applied to nullable int and long values. */
    public static Long minus(Integer b0, Long b1) {
        return (b0 == null || b1 == null)
            ? null
            : (b0.longValue() - b1.longValue());
    }

    /** SQL <code>-</code> operator applied to BigDecimal values. */
    public static BigDecimal minus(BigDecimal b0, BigDecimal b1) {
        return (b0 == null || b1 == null) ? null : b0.subtract(b1);
    }

    // /

    /** SQL <code>/</code> operator applied to int values. */
    public static int divide(int b0, int b1) {
        return b0 / b1;
    }

    /** SQL <code>/</code> operator applied to int values; left side may be
     * null. */
    public static Integer divide(Integer b0, int b1) {
        return b0 == null ? null : (b0 / b1);
    }

    /** SQL <code>/</code> operator applied to int values; right side may be
     * null. */
    public static Integer divide(int b0, Integer b1) {
        return b1 == null ? null : (b0 / b1);
    }

    /** SQL <code>/</code> operator applied to nullable int values. */
    public static Integer divide(Integer b0, Integer b1) {
        return (b0 == null || b1 == null) ? null : (b0 / b1);
    }

    /** SQL <code>/</code> operator applied to nullable long and int values. */
    public static Long divide(Long b0, Integer b1) {
        return (b0 == null || b1 == null)
            ? null
            : (b0.longValue() / b1.longValue());
    }

    /** SQL <code>/</code> operator applied to nullable int and long values. */
    public static Long divide(Integer b0, Long b1) {
        return (b0 == null || b1 == null)
            ? null
            : (b0.longValue() / b1.longValue());
    }

    /** SQL <code>/</code> operator applied to BigDecimal values. */
    public static BigDecimal divide(BigDecimal b0, BigDecimal b1) {
        return (b0 == null || b1 == null) ? null : b0.divide(b1);
    }

    // *

    /** SQL <code>*</code> operator applied to int values. */
    public static int multiply(int b0, int b1) {
        return b0 * b1;
    }

    /** SQL <code>*</code> operator applied to int values; left side may be
     * null. */
    public static Integer multiply(Integer b0, int b1) {
        return b0 == null ? null : (b0 * b1);
    }

    /** SQL <code>*</code> operator applied to int values; right side may be
     * null. */
    public static Integer multiply(int b0, Integer b1) {
        return b1 == null ? null : (b0 * b1);
    }

    /** SQL <code>*</code> operator applied to nullable int values. */
    public static Integer multiply(Integer b0, Integer b1) {
        return (b0 == null || b1 == null) ? null : (b0 * b1);
    }

    /** SQL <code>*</code> operator applied to nullable long and int values. */
    public static Long multiply(Long b0, Integer b1) {
        return (b0 == null || b1 == null)
            ? null
            : (b0.longValue() * b1.longValue());
    }

    /** SQL <code>*</code> operator applied to nullable int and long values. */
    public static Long multiply(Integer b0, Long b1) {
        return (b0 == null || b1 == null)
            ? null
            : (b0.longValue() * b1.longValue());
    }

    /** SQL <code>*</code> operator applied to BigDecimal values. */
    public static BigDecimal multiply(BigDecimal b0, BigDecimal b1) {
        return (b0 == null || b1 == null) ? null : b0.multiply(b1);
    }

    // POWER

    /** SQL <code>POWER</code> operator applied to double values. */
    public static double power(double b0, double b1) {
        return Math.pow(b0, b1);
    }

    // temporary
    public static double power(int b0, BigDecimal b1) {
        return Math.pow(b0, b1.doubleValue());
    }

    /** SQL {@code LN(number)} function applied to double values. */
    public static double ln(double d) {
        return Math.log(d);
    }

    /** SQL {@code LN(number)} function applied to BigDecimal values. */
    public static BigDecimal ln(BigDecimal d) {
        return BigDecimal.valueOf(Math.log(d.doubleValue()));
    }

    // MOD

    /** SQL <code>MOD</code> operator applied to int values. */
    public static int mod(int b0, int b1) {
        return b0 % b1;
    }

    /** SQL <code>MOD</code> operator applied to long values. */
    public static long mod(long b0, long b1) {
        return b0 % b1;
    }

    // temporary
    public static BigDecimal mod(BigDecimal b0, int b1) {
        return mod(b0, new BigDecimal(b1));
    }

    // temporary
    public static int mod(int b0, BigDecimal b1) {
        return mod(b0, b1.intValue());
    }

    public static BigDecimal mod(BigDecimal b0, BigDecimal b1) {
        final BigDecimal[] bigDecimals = b0.divideAndRemainder(b1);
        return bigDecimals[1];
    }

    // Helpers

    /** Helper for implementing MIN. Somewhat similar to LEAST operator. */
    public static <T extends Comparable<T>> T lesser(T b0, T b1) {
        return b0 == null || b0.compareTo(b1) > 0 ? b1 : b0;
    }

    /** LEAST operator. */
    public static <T extends Comparable<T>> T least(T b0, T b1) {
        return b0 == null || b1 != null && b0.compareTo(b1) > 0 ? b1 : b0;
    }

    public static int lesser(int b0, int b1) {
        return b0 > b1 ? b1 : b0;
    }

    /** Helper for implementing MAX. Somewhat similar to GREATEST operator. */
    public static <T extends Comparable<T>> T greater(T b0, T b1) {
        return b0 == null || b0.compareTo(b1) < 0 ? b1 : b0;
    }

    /** GREATEST operator. */
    public static <T extends Comparable<T>> T greatest(T b0, T b1) {
        return b0 == null || b1 != null && b0.compareTo(b1) < 0 ? b1 : b0;
    }

    /** Boolean comparison. */
    public static int compare(boolean x, boolean y) {
        return x == y ? 0 : x ? 1 : -1;
    }

    /** CAST(FLOAT AS VARCHAR). */
    public static String toString(float x) {
        if (x == 0) {
            return "0E0";
        }
        BigDecimal bigDecimal =
            new BigDecimal(x, MathContext.DECIMAL32).stripTrailingZeros();
        final String s = bigDecimal.toString();
        return s.replaceAll("0*E", "E").replace("E+", "E");
    }

    /** CAST(DOUBLE AS VARCHAR). */
    public static String toString(double x) {
        if (x == 0) {
            return "0E0";
        }
        BigDecimal bigDecimal =
            new BigDecimal(x, MathContext.DECIMAL64).stripTrailingZeros();
        final String s = bigDecimal.toString();
        return s.replaceAll("0*E", "E").replace("E+", "E");
    }

    /** CAST(DECIMAL AS VARCHAR). */
    public static String toString(BigDecimal x) {
        final String s = x.toString();
        if (s.startsWith("0")) {
            // we want ".1" not "0.1"
            return s.substring(1);
        } else if (s.startsWith("-0")) {
            // we want "-.1" not "-0.1"
            return "-" + s.substring(2);
        } else {
            return s;
        }
    }

    /** CAST(BOOLEAN AS VARCHAR). */
    public static String toString(boolean x) {
        // Boolean.toString returns lower case -- no good.
        return x ? "TRUE" : "FALSE";
    }

    /** CAST(VARCHAR AS BOOLEAN). */
    public static boolean toBoolean(String s) {
        s = trim(s);
        if (s.equalsIgnoreCase("TRUE")) {
            return true;
        } else if (s.equalsIgnoreCase("FALSE")) {
            return false;
        } else {
            throw new RuntimeException("Invalid character for cast");
        }
    }

    /** Helper for CAST(... AS VARCHAR(maxLength)). */
    public static String truncate(String s, int maxLength) {
        return s.length() > maxLength ? s.substring(0, maxLength) : s;
    }

    /** Cheap, unsafe, long power. power(2, 3) returns 8. */
    public static long power(long a, long b) {
        long x = 1;
        while (b > 0) {
            x *= a;
            --b;
        }
        return x;
    }

    /** Helper for rounding. Truncate(12345, 1000) returns 12000. */
    public static long round(long v, long x) {
        return truncate(v + x / 2, x);
    }

    /** Helper for rounding. Truncate(12345, 1000) returns 12000. */
    public static long truncate(long v, long x) {
        long remainder = v % x;
        if (remainder < 0) {
            remainder += x;
        }
        return v - remainder;
    }

    /** Helper for rounding. Truncate(12345, 1000) returns 12000. */
    public static int round(int v, int x) {
        return truncate(v + x / 2, x);
    }

    /** Helper for rounding. Truncate(12345, 1000) returns 12000. */
    public static int truncate(int v, int x) {
        int remainder = v % x;
        if (remainder < 0) {
            remainder += x;
        }
        return v - remainder;
    }

    /** Helper for CAST({timestamp} AS VARCHAR(n)). */
    public static String unixTimestampToString(long timestamp) {
        final StringBuilder buf = new StringBuilder(17);
        unixDateToString(buf, (int) (timestamp / 86400000));
        buf.append(' ');
        unixTimeToString(buf, (int) (timestamp % 86400000));
        return buf.toString();
    }

    /** Helper for CAST({timestamp} AS VARCHAR(n)). */
    public static String unixTimeToString(int time) {
        final StringBuilder buf = new StringBuilder(8);
        unixTimeToString(buf, time);
        return buf.toString();
    }

    private static void unixTimeToString(StringBuilder buf, int time) {
        int h = time / 3600000;
        int time2 = time % 3600000;
        int m = time2 / 60000;
        int time3 = time2 % 60000;
        int s = time3 / 1000;
        int ms = time3 % 1000;
        int2(buf, h);
        buf.append(':');
        int2(buf, m);
        buf.append(':');
        int2(buf, s);
    }

    private static void int2(StringBuilder buf, int i) {
        buf.append((char) ('0' + (i / 10) % 10));
        buf.append((char) ('0' + i % 10));
    }

    private static void int4(StringBuilder buf, int i) {
        buf.append((char) ('0' + (i / 1000) % 10));
        buf.append((char) ('0' + (i / 100) % 10));
        buf.append((char) ('0' + (i / 10) % 10));
        buf.append((char) ('0' + i % 10));
    }

    /** Helper for CAST({date} AS VARCHAR(n)). */
    public static String unixDateToString(int date) {
        final StringBuilder buf = new StringBuilder(10);
        unixDateToString(buf, date);
        return buf.toString();
    }

    private static void unixDateToString(StringBuilder buf, int date) {
        julianToString(buf, date + 2440588);
    }

    private static void julianToString(StringBuilder buf, int J) {
        // this shifts the epoch back to astronomical year -4800 instead of the
        // start of the Christian era in year AD 1 of the proleptic Gregorian
        // calendar.
        int j = J + 32044;
        int g = j / 146097;
        int dg = j % 146097;
        int c = (dg / 36524 + 1) * 3 / 4;
        int dc = dg - c * 36524;
        int b = dc / 1461;
        int db = dc % 1461;
        int a = (db / 365 + 1) * 3 / 4;
        int da = db - a * 365;

        // integer number of full years elapsed since March 1, 4801 BC
        int y = g * 400 + c * 100 + b * 4 + a;
        // integer number of full months elapsed since the last March 1
        int m = (da * 5 + 308) / 153 - 2;
        // number of days elapsed since day 1 of the month
        int d = da - (m + 4) * 153 / 5 + 122;
        int Y = y - 4800 + (m + 2) / 12;
        int M = (m + 2) % 12 + 1;
        int D = d + 1;
        int4(buf, Y);
        buf.append('-');
        int2(buf, M);
        buf.append('-');
        int2(buf, D);
    }

    public static int ymdToUnixDate(int year, int month, int day) {
        return ymdToJulian(year, month, day) - 2440588 - 13;
    }

    public static int ymdToJulian(int year, int month, int day) {
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + 12 * a - 3;
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083;
    }

    /** Helper for "array element reference". Caller has already ensured that
     * array and index are not null. Index is 1-based, per SQL. */
    public static Object arrayItem(List list, int item) {
        if (item < 1 || item > list.size()) {
            return null;
        }
        return list.get(item - 1);
    }

    /** Helper for "map element reference". Caller has already ensured that
     * array and index are not null. Index is 1-based, per SQL. */
    public static Object mapItem(Map map, Object item) {
        return map.get(item);
    }

    /** Implements the {@code [ ... ]} operator on an object whose type is not
     * known until runtime.
     */
    public static Object item(Object object, Object index) {
        if (object instanceof Map) {
            return ((Map) object).get(index);
        }
        if (object instanceof List && index instanceof Number) {
            List list = (List) object;
            return list.get(((Number) index).intValue());
        }
        return null;
    }

    /** NULL -> FALSE, FALSE -> FALSE, TRUE -> TRUE. */
    public static boolean isTrue(Boolean b) {
        return b != null && b;
    }

    /** NULL -> TRUE, FALSE -> FALSE, TRUE -> TRUE. */
    public static boolean isNotFalse(Boolean b) {
        return b == null || b;
    }
}

// End SqlFunctions.java
