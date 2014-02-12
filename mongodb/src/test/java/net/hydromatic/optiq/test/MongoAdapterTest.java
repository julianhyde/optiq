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

import net.hydromatic.linq4j.Ord;
import net.hydromatic.linq4j.function.Function1;

import org.eigenbase.util.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests for the {@code net.hydromatic.optiq.impl.mongodb} package.
 *
 * <p>Before calling this test, you need to populate MongoDB with the "zips"
 * data set (as described in HOWTO.md)
 * and "foodmart" data set, as follows:</p>
 *
 * <blockquote><code>
 * JAR=~/.m2/repository/pentaho/mondrian-data-foodmart-json/
 * 0.3/mondrian-data-foodmart-json-0.3.jar<br>
 * mkdir /tmp/foodmart<br>
 * cd /tmp/foodmart<br>
 * jar xvf $JAR<br>
 * for i in *.json; do<br>
 * &nbsp;&nbsp;mongoimport --db foodmart --collection ${i/.json/} --file $i<br>
 * done<br>
 * </code></blockquote>
 */
public class MongoAdapterTest {
  public static final String MONGO_FOODMART_SCHEMA =
      "     {\n"
      + "       type: 'custom',\n"
      + "       name: '_foodmart',\n"
      + "       factory: 'net.hydromatic.optiq.impl.mongodb.MongoSchemaFactory',\n"
      + "       operand: {\n"
      + "         host: 'localhost',\n"
      + "         database: 'foodmart'\n"
      + "       }\n"
      + "     },\n"
      + "     {\n"
      + "       name: 'foodmart',\n"
      + "       tables: [\n"
      + "         {\n"
      + "           name: 'sales_fact_1997',\n"
      + "           type: 'view',\n"
      + "           sql: 'select cast(_MAP[\\'product_id\\'] AS double) AS \"product_id\" from \"_foodmart\".\"sales_fact_1997\"'\n"
      + "         },\n"
      + "         {\n"
      + "           name: 'sales_fact_1998',\n"
      + "           type: 'view',\n"
      + "           sql: 'select cast(_MAP[\\'product_id\\'] AS double) AS \"product_id\" from \"_foodmart\".\"sales_fact_1998\"'\n"
      + "         },\n"
      + "         {\n"
      + "           name: 'store',\n"
      + "           type: 'view',\n"
      + "           sql: 'select cast(_MAP[\\'store_id\\'] AS double) AS \"store_id\", cast(_MAP[\\'store_name\\'] AS varchar(20)) AS \"store_name\" from \"_foodmart\".\"store\"'\n"
      + "         },\n"
      + "         {\n"
      + "           name: 'warehouse',\n"
      + "           type: 'view',\n"
      + "           sql: 'select cast(_MAP[\\'warehouse_id\\'] AS double) AS \"warehouse_id\", cast(_MAP[\\'warehouse_state_province\\'] AS varchar(20)) AS \"warehouse_state_province\" from \"_foodmart\".\"warehouse\"'\n"
      + "         }\n"
      + "       ]\n"
      + "     }\n";

  public static final String MONGO_FOODMART_MODEL =
      "{\n"
      + "  version: '1.0',\n"
      + "  defaultSchema: 'foodmart',\n"
      + "   schemas: [\n"
      + MONGO_FOODMART_SCHEMA
      + "   ]\n"
      + "}";

  /** Connection factory based on the "mongo-zips" model. */
  public static final ImmutableMap<String, String> ZIPS =
      ImmutableMap.of("model",
          "mongodb/target/test-classes/mongo-zips-model.json");

  /** Connection factory based on the "mongo-zips" model. */
  public static final ImmutableMap<String, String> FOODMART =
      ImmutableMap.of("model",
          "mongodb/target/test-classes/mongo-foodmart-model.json");

  /** Disabled by default, because we do not expect Mongo to be installed and
   * populated with the FoodMart data set. */
  private boolean enabled() {
    return true;
  }

  /** Returns a function that checks that a particular MongoDB pipeline is
   * generated to implement a query. */
  private static Function1<List, Void> mongoChecker(final String... strings) {
    return new Function1<List, Void>() {
      public Void apply(List actual) {
        if (!actual.contains(ImmutableList.copyOf(strings))) {
          Assert.fail("expected MongoDB query not found; actual: " + actual);
        }
        return null;
      }
    };
  }

  @Test public void testSort() {
    OptiqAssert.that()
        .enable(enabled())
        .with(ZIPS)
        .query("select * from zips order by state")
        .returnsCount(29467)
        .explainContains(
            "PLAN=EnumerableCalcRel(expr#0..4=[{inputs}], expr#5=[0], expr#6=[ITEM($t1, $t5)], expr#7=[CAST($t6):FLOAT NOT NULL], expr#8=[1], expr#9=[ITEM($t1, $t8)], expr#10=[CAST($t9):FLOAT NOT NULL], CITY=[$t0], LONGITUDE=[$t7], LATITUDE=[$t10], POP=[$t2], STATE=[$t3], ID=[$t4])\n"
            + "  MongoToEnumerableConverter\n"
            + "    MongoSortRel(sort0=[$3], dir0=[Ascending])\n"
            + "      MongoTableScan(table=[[mongo_raw, zips]], ops=[[<{city: 1, loc: 1, pop: 1, state: 1, _id: 1}, {$project: {city: 1, loc: 1, pop: 1, state: 1, _id: 1}}>]])");
  }

  @Test public void testFilterSort() {
    OptiqAssert.that()
        .enable(enabled())
        .with(ZIPS)
        .query(
            "select * from zips\n"
            + "where city = 'SPRINGFIELD' and id between '20000' and '30000'\n"
            + "order by state")
        .returns(
            "CITY=SPRINGFIELD; LONGITUDE=-81.249855; LATITUDE=33.534264; POP=2184; STATE=SC; ID=29146\n"
            + "CITY=SPRINGFIELD; LONGITUDE=-77.186584; LATITUDE=38.779716; POP=16811; STATE=VA; ID=22150\n"
            + "CITY=SPRINGFIELD; LONGITUDE=-77.23702; LATITUDE=38.744858; POP=32161; STATE=VA; ID=22153\n"
            + "CITY=SPRINGFIELD; LONGITUDE=-78.69502; LATITUDE=39.462997; POP=1321; STATE=WV; ID=26763\n")
        .explainContains(
            "PLAN=EnumerableCalcRel(expr#0..4=[{inputs}], expr#5=[0], expr#6=[ITEM($t1, $t5)], expr#7=[CAST($t6):FLOAT NOT NULL], expr#8=[1], expr#9=[ITEM($t1, $t8)], expr#10=[CAST($t9):FLOAT NOT NULL], CITY=[$t0], LONGITUDE=[$t7], LATITUDE=[$t10], POP=[$t2], STATE=[$t3], ID=[$t4])\n"
            + "  MongoToEnumerableConverter\n"
            + "    MongoSortRel(sort0=[$3], dir0=[Ascending])\n"
            + "      MongoFilterRel(condition=[AND(=($0, 'SPRINGFIELD'), >=($4, '20000'), <=($4, '30000'))])\n"
            + "        MongoTableScan(table=[[mongo_raw, zips]], ops=[[<{city: 1, loc: 1, pop: 1, state: 1, _id: 1}, {$project: {city: 1, loc: 1, pop: 1, state: 1, _id: 1}}>]])");
  }

  @Test public void testUnionPlan() {
    OptiqAssert.that()
        .enable(enabled())
        .withModel(MONGO_FOODMART_MODEL)
        .query(
            "select * from \"sales_fact_1997\"\n"
            + "union all\n"
            + "select * from \"sales_fact_1998\"")
        .explainContains(
            "PLAN=EnumerableUnionRel(all=[true])\n"
            + "  MongoToEnumerableConverter\n"
            + "    MongoTableScan(table=[[_foodmart, sales_fact_1997]], ops=[[<{product_id: 1}, {$project: {product_id: 1}}>]])\n"
            + "  MongoToEnumerableConverter\n"
            + "    MongoTableScan(table=[[_foodmart, sales_fact_1998]], ops=[[<{product_id: 1}, {$project: {product_id: 1}}>]])")
        .limit(2)
        .returns(
            "product_id=337\n"
            + "product_id=1512\n");
  }

  @Test public void testFilterUnionPlan() {
    OptiqAssert.that()
        .enable(enabled())
        .withModel(MONGO_FOODMART_MODEL)
        .query(
            "select * from (\n"
            + "  select * from \"sales_fact_1997\"\n"
            + "  union all\n"
            + "  select * from \"sales_fact_1998\")\n"
            + "where \"product_id\" = 1")
        .runs();
  }

  /** Tests that we don't generate multiple constraints on the same column.
   * MongoDB doesn't like it. If there is an '=', it supersedes all other
   * operators. */
  @Test public void testFilterRedundant() {
    OptiqAssert.that()
        .enable(enabled())
        .with(ZIPS)
        .query(
            "select * from zips where state > 'CA' and state < 'AZ' and state = 'OK'")
        .runs()
        .queryContains(
            mongoChecker(
                "{$project: {city: 1, loc: 1, pop: 1, state: 1, _id: 1}}",
                "{\n"
                + "  $match: {\n"
                + "    state: \"OK\"\n"
                + "  }\n"
                + "}"));
  }

  @Test public void testSelectWhere() {
    OptiqAssert.that()
        .enable(enabled())
        .withModel(MONGO_FOODMART_MODEL)
        .query(
            "select * from \"warehouse\" where \"warehouse_state_province\" = 'CA'")
        .explainContains(
            "PLAN=MongoToEnumerableConverter\n"
            + "  MongoFilterRel(condition=[=($1, 'CA')])\n"
            + "    MongoTableScan(table=[[_foodmart, warehouse]], ops=[[<{warehouse_id: 1, warehouse_state_province: 1}, {$project: {warehouse_id: 1, warehouse_state_province: 1}}>]])")
        .returns(
            "warehouse_id=6; warehouse_state_province=CA\n"
            + "warehouse_id=7; warehouse_state_province=CA\n"
            + "warehouse_id=14; warehouse_state_province=CA\n"
            + "warehouse_id=24; warehouse_state_province=CA\n")
        .queryContains(
            mongoChecker(
                "{$project: {warehouse_id: 1, warehouse_state_province: 1}}",
                "{\n"
                + "  $match: {\n"
                + "    warehouse_state_province: \"CA\"\n"
                + "  }\n"
                + "}"));
  }

  @Test public void testInPlan() {
    OptiqAssert.that()
        .enable(enabled())
        .withModel(MONGO_FOODMART_MODEL)
        .query(
            "select \"store_id\", \"store_name\" from \"store\"\n"
            + "where \"store_name\" in ('Store 1', 'Store 10', 'Store 11', 'Store 15', 'Store 16', 'Store 24', 'Store 3', 'Store 7')")
        .returns(
            "store_id=1; store_name=Store 1\n"
            + "store_id=3; store_name=Store 3\n"
            + "store_id=7; store_name=Store 7\n"
            + "store_id=10; store_name=Store 10\n"
            + "store_id=11; store_name=Store 11\n"
            + "store_id=15; store_name=Store 15\n"
            + "store_id=16; store_name=Store 16\n"
            + "store_id=24; store_name=Store 24\n")
        .queryContains(
            mongoChecker(
                "{$project: {store_id: 1, store_name: 1}}",
                "{\n  $match: {\n    $or: [\n      {\n        store_name: \"Store 1\"\n      },\n      {\n        store_name: \"Store 10\"\n      },\n      {\n        store_name: \"Store 11\"\n      },\n      {\n        store_name: \"Store 15\"\n      },\n      {\n        store_name: \"Store 16\"\n      },\n      {\n        store_name: \"Store 24\"\n      },\n      {\n        store_name: \"Store 3\"\n      },\n      {\n        store_name: \"Store 7\"\n      }\n    ]\n  }\n}"));
  }

  /** Query based on the "mongo-zips" model. */
  @Test public void testZips() {
    OptiqAssert.that()
        .enable(enabled())
        .with(ZIPS)
        .query("select count(*) from zips")
        .returns("EXPR$0=29467\n")
        .explainContains(
            "PLAN=EnumerableAggregateRel(group=[{}], EXPR$0=[COUNT()])\n"
            + "  EnumerableCalcRel(expr#0=[{inputs}], expr#0=[0], DUMMY=[$t0])\n"
            + "    MongoToEnumerableConverter\n"
            + "      MongoTableScan(table=[[mongo_raw, zips]], ops=[[<{}, {$project: {}}>]])");
  }

  @Test public void testProject() {
    OptiqAssert.that()
        .enable(enabled())
        .with(ZIPS)
        .query("select state, city from zips")
        .limit(2)
        .returns(
            "STATE=AL; CITY=ACMAR\n"
            + "STATE=AL; CITY=ADAMSVILLE\n")
        .explainContains(
            "PLAN=EnumerableCalcRel(expr#0..1=[{inputs}], STATE=[$t1], CITY=[$t0])\n"
            + "  MongoToEnumerableConverter\n"
            + "    MongoTableScan(table=[[mongo_raw, zips]], ops=[[<{city: 1, state: 1}, {$project: {city: 1, state: 1}}>]])");
  }

  @Test public void testFilter() {
    OptiqAssert.that()
        .enable(enabled())
        .with(ZIPS)
        .query("select state, city from zips where state = 'CA'")
        .limit(2)
        .returns(
            "STATE=CA; CITY=LOS ANGELES\n"
            + "STATE=CA; CITY=LOS ANGELES\n")
        .explainContains(
            "PLAN=EnumerableCalcRel(expr#0..1=[{inputs}], STATE=[$t1], CITY=[$t0])\n"
            + "  MongoToEnumerableConverter\n"
            + "    MongoFilterRel(condition=[=($1, 'CA')])\n"
            + "      MongoTableScan(table=[[mongo_raw, zips]], ops=[[<{city: 1, state: 1}, {$project: {city: 1, state: 1}}>]])");
  }

  public void _testFoodmartQueries() {
    final List<Pair<String, String>> queries = JdbcTest.getFoodmartQueries();
    for (Ord<Pair<String, String>> query : Ord.zip(queries)) {
//      if (query.i != 29) continue;
      if (query.e.left.contains("agg_")) {
        continue;
      }
      final OptiqAssert.AssertQuery query1 =
          OptiqAssert.that()
              .enable(enabled())
              .with(FOODMART)
              .query(query.e.left);
      if (query.e.right != null) {
        query1.returns(query.e.right);
      } else {
        query1.runs();
      }
    }
  }
}

// End MongoAdapterTest.java
