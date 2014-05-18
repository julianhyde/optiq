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

import java.sql.Date;

import static java.util.Locale.ENGLISH;

/** Entity corresponding to a row in the
 * {@code item} TPC-DS table. */
public class Item implements TpcdsEntity {
  // i_item_sk                 integer               not null
  public final int itemSk;
  // i_item_id                 char(16)              not null
  public final String itemId;
  // i_rec_start_date          date
  public final Date recStartDate;
  // i_rec_end_date            date
  public final Date recEndDate;
  // i_item_desc               varchar(200)
  public final String itemDesc;
  // i_current_price           decimal(7,2)
  public final Float currentPrice;
  // i_wholesale_cost          decimal(7,2)
  public final Float wholesaleCost;
  // i_brand_id                integer
  public final Integer brandId;
  // i_brand                   char(50)
  public final String brand;
  // i_class_id                integer
  public final Integer classId;
  // i_class                   char(50)
  public final String itemClass;
  // i_category_id             integer
  public final Integer categoryId;
  // i_category                char(50)
  public final String category;
  // i_manufact_id             integer
  public final Integer manufactId;
  // i_manufact                char(50)
  public final String manufact;
  // i_size                    char(20)
  public final String size;
  // i_formulation             char(20)
  public final String formulation;
  // i_color                   char(20)
  public final String color;
  // i_units                   char(10)
  public final String units;
  // i_container               char(10)
  public final String container;
  // i_manager_id              integer
  public final Integer managerId;
  // i_product_name            char(50)
  public final String productName;
  // primary key (i_item_sk)

  public Item(int itemSk, String itemId, Date recStartDate, Date recEndDate,
      String itemDesc, Float currentPrice, Float wholesaleCost, Integer brandId,
      String brand, Integer classId, String itemClass, Integer categoryId,
      String category, Integer manufactId, String manufact, String size,
      String formulation, String color, String units, String container,
      Integer managerId, String productName) {
    this.itemSk = itemSk;
    this.itemId = itemId;
    this.recStartDate = recStartDate;
    this.recEndDate = recEndDate;
    this.itemDesc = itemDesc;
    this.currentPrice = currentPrice;
    this.wholesaleCost = wholesaleCost;
    this.brandId = brandId;
    this.brand = brand;
    this.classId = classId;
    this.itemClass = itemClass;
    this.categoryId = categoryId;
    this.category = category;
    this.manufactId = manufactId;
    this.manufact = manufact;
    this.size = size;
    this.formulation = formulation;
    this.color = color;
    this.units = units;
    this.container = container;
    this.managerId = managerId;
    this.productName = productName;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End Item.java
