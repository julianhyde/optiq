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
 * {@code warehouse} TPC-DS table. */
public class Warehouse implements TpcdsEntity {
  // w_warehouse_sk            integer               not null
  public final int warehouseSk;
  // w_warehouse_id            char(16)              not null
  public final String warehouseId;
  // w_warehouse_name          varchar(20)
  public final String warehouseName;
  // w_warehouse_sq_ft         integer
  public final Integer warehouseSqFt;
  // w_street_number           char(10)
  public final String streetNumber;
  // w_street_name             varchar(60)
  public final String streetName;
  // w_street_type             char(15)
  public final String streetType;
  // w_suite_number            char(10)
  public final String suiteNumber;
  // w_city                    varchar(60)
  public final String city;
  // w_county                  varchar(30)
  public final String county;
  // w_state                   char(2)
  public final String state;
  // w_zip                     char(10)
  public final String zip;
  // w_country                 varchar(20)
  public final String country;
  // w_gmt_offset              decimal(5,2)
  public final Float gmtOffset;
  // primary key (w_warehouse_sk)


  public Warehouse(int warehouseSk, String warehouseId, String warehouseName,
      Integer warehouseSqFt, String streetNumber, String streetName,
      String streetType, String suiteNumber, String city, String county,
      String state, String zip, String country, Float gmtOffset) {
    this.warehouseSk = warehouseSk;
    this.warehouseId = warehouseId;
    this.warehouseName = warehouseName;
    this.warehouseSqFt = warehouseSqFt;
    this.streetNumber = streetNumber;
    this.streetName = streetName;
    this.streetType = streetType;
    this.suiteNumber = suiteNumber;
    this.city = city;
    this.county = county;
    this.state = state;
    this.zip = zip;
    this.country = country;
    this.gmtOffset = gmtOffset;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}
