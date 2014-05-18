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
 * {@code store} TPC-DS table. */
public class Store implements TpcdsEntity {
  // s_store_sk                integer               not null
  public final int storeSk;
  // s_store_id                char(16)              not null
  public final String storeId;
  // s_rec_start_date          date
  public final Date recStartDate;
  // s_rec_end_date            date
  public final Date recEndDate;
  // s_closed_date_sk          integer
  public final Integer closedDateSk;
  // s_store_name              varchar(50)
  public final String storeName;
  // s_number_employees        integer
  public final Integer numberEmployees;
  // s_floor_space             integer
  public final Integer floorSpace;
  // s_hours                   char(20)
  public final String hours;
  // s_manager                 varchar(40)
  public final String manager;
  // s_market_id               integer
  public final Integer marketId;
  // s_geography_class         varchar(100)
  public final String geographyClass;
  // s_market_desc             varchar(100)
  public final String marketDesc;
  // s_market_manager          varchar(40)
  public final String marketManager;
  // s_division_id             integer
  public final Integer divisionId;
  // s_division_name           varchar(50)
  public final String divisionName;
  // s_company_id              integer
  public final Integer companyId;
  // s_company_name            varchar(50)
  public final String companyName;
  // s_street_number           varchar(10)
  public final String streetNumber;
  // s_street_name             varchar(60)
  public final String streetName;
  // s_street_type             char(15)
  public final String streetType;
  // s_suite_number            char(10)
  public final String suiteNumber;
  // s_city                    varchar(60)
  public final String city;
  // s_county                  varchar(30)
  public final String county;
  // s_state                   char(2)
  public final String state;
  // s_zip                     char(10)
  public final String zip;
  // s_country                 varchar(20)
  public final String country;
  // s_gmt_offset              decimal(5,2)
  public final Float gmtOffset;
  // s_tax_precentage          decimal(5,2)
  public final Float taxPrecentage;
  // primary key (s_store_sk)


  public Store(int storeSk, String storeId, Date recStartDate, Date recEndDate,
      Integer closedDateSk, String storeName, Integer numberEmployees,
      Integer floorSpace, String hours, String manager, Integer marketId,
      String geographyClass, String marketDesc, String marketManager,
      Integer divisionId, String divisionName, Integer companyId,
      String companyName, String streetNumber, String streetName,
      String streetType, String suiteNumber, String city, String county,
      String state, String zip, String country, Float gmtOffset,
      Float taxPrecentage) {
    this.storeSk = storeSk;
    this.storeId = storeId;
    this.recStartDate = recStartDate;
    this.recEndDate = recEndDate;
    this.closedDateSk = closedDateSk;
    this.storeName = storeName;
    this.numberEmployees = numberEmployees;
    this.floorSpace = floorSpace;
    this.hours = hours;
    this.manager = manager;
    this.marketId = marketId;
    this.geographyClass = geographyClass;
    this.marketDesc = marketDesc;
    this.marketManager = marketManager;
    this.divisionId = divisionId;
    this.divisionName = divisionName;
    this.companyId = companyId;
    this.companyName = companyName;
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
    this.taxPrecentage = taxPrecentage;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End Store.java
