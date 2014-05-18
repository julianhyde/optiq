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
package net.hydromatic.optiq.impl.tpcds;

import net.hydromatic.optiq.test.OptiqAssert;

import org.junit.Test;

/** Unit test for {@link TpcdsSchema}.
 *
 * <p>Only runs if {@code -Doptiq.test.slow=true} is specified on the
 * command-line.
 * (See {@link OptiqAssert#ENABLE_SLOW}.)</p> */
public class TpcdsTest {
  private static String schema(String name, String scaleFactor) {
    return "     {\n"
        + "       type: 'custom',\n"
        + "       name: '" + name + "',\n"
        + "       factory: 'net.hydromatic.optiq.impl.tpcds.TpcdsSchemaFactory',\n"
        + "       operand: {\n"
        + "         columnPrefix: true,\n"
        + "         scale: " + scaleFactor + "\n"
        + "       }\n"
        + "     }";
  }

  public static final String TPCDS_MODEL =
      "{\n"
      + "  version: '1.0',\n"
      + "  defaultSchema: 'TPCDS',\n"
      + "   schemas: [\n"
      + schema("TPCDS", "1.0") + ",\n"
      + schema("TPCDS_01", "0.01") + ",\n"
      + schema("TPCDS_5", "5.0") + "\n"
      + "   ]\n"
      + "}";

  static final String[] QUERIES = {
    // 01
    "values 1",

    // 02
    "value 2",

    // 03
    "select  dt.d_year \n"
      + "       ,item.i_brand_id brand_id \n"
      + "       ,item.i_brand brand\n"
      + "       ,sum(ss_ext_sales_price) sum_agg\n"
      + " from  tpcds.date_dim dt\n"
      + "      ,tpcds.store_sales\n"
      + "      ,tpcds.item\n"
      + " where dt.d_date_sk = store_sales.ss_sold_date_sk\n"
      + "   and store_sales.ss_item_sk = item.i_item_sk\n"
      + "   and item.i_manufact_id = 436\n"
      + "   and dt.d_moy=12\n"
      + "   and (\n"
      + "        ( ss_sold_date between '1998-12-01' and '1998-12-31' ) or\n"
      + "        ( ss_sold_date between '1999-12-01' and '1999-12-31' ) or\n"
      + "        ( ss_sold_date between '2000-12-01' and '2000-12-31' ) or\n"
      + "        ( ss_sold_date between '2001-12-01' and '2001-12-31' ) or\n"
      + "        ( ss_sold_date between '2002-12-01' and '2002-12-31' )\n"
      + "   )\n"
      + " group by dt.d_year\n"
      + "      ,item.i_brand\n"
      + "      ,item.i_brand_id\n"
      + " order by dt.d_year\n"
      + "         ,sum_agg desc\n"
      + "         ,brand_id\n"
      + " limit 100",
  };

  @Test public void testCallCenter() {
    with()
        .query("select * from tpcds.call_center")
        .returnsUnordered(
            "R_REGIONKEY=0; R_NAME=AFRICA; R_COMMENT=lar deposits. blithely final packages cajole. regular waters are final requests. regular accounts are according to ",
            "R_REGIONKEY=1; R_NAME=AMERICA; R_COMMENT=hs use ironic, even requests. s",
            "R_REGIONKEY=2; R_NAME=ASIA; R_COMMENT=ges. thinly even pinto beans ca",
            "R_REGIONKEY=3; R_NAME=EUROPE; R_COMMENT=ly final courts cajole furiously final excuse",
            "R_REGIONKEY=4; R_NAME=MIDDLE EAST; R_COMMENT=uickly special accounts cajole carefully blithely close requests. carefully final asymptotes haggle furiousl");
  }

  private OptiqAssert.AssertThat with() {
    return OptiqAssert.that()
        .withModel(TPCDS_MODEL)
        .enable(OptiqAssert.ENABLE_SLOW);
  }

  @Test public void testQuery03() {
    checkQuery(3);
  }

  private void checkQuery(int i) {
    with()
        .query(QUERIES[i - 1].replaceAll("tpcds\\.", "tpcds_01."))
        .runs();
  }
}

// End TpchTest.java
