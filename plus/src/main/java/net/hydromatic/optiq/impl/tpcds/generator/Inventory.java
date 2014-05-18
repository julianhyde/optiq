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
package net.hydromatic.optiq.impl.tpcds.generator;

import static java.util.Locale.ENGLISH;

/** Entity corresponding to a row in the
 * {@code inventory} TPC-DS table. */
public class Inventory implements TpcdsEntity {
  // inv_date_sk               integer               not null
  public final int dateSk;
  // inv_item_sk               integer               not null
  public final int itemSk;
  // inv_warehouse_sk          integer               not null
  public final int warehouseSk;
  // inv_quantity_on_hand      integer
  public final Integer quantityOnHand;
  // primary key (inv_date_sk inv_item_sk inv_warehouse_sk)

  public Inventory(int dateSk, int itemSk, int warehouseSk,
      Integer quantityOnHand) {
    this.dateSk = dateSk;
    this.itemSk = itemSk;
    this.warehouseSk = warehouseSk;
    this.quantityOnHand = quantityOnHand;
  }


  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End Inventory.java
