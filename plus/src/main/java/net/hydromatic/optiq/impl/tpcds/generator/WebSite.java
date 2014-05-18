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
 * {@code web_site} TPC-DS table. */
public class WebSite implements TpcdsEntity {
  // web_site_sk               integer               not null
  public final int siteSk;
  // web_site_id               char(16)              not null
  public final String siteId;
  // web_rec_start_date        date
  public final Date recStartDate;
  // web_rec_end_date          date
  public final Date recEndDate;
  // web_name                  varchar(50)
  public final String name;
  // web_open_date_sk          integer
  public final Integer openDateSk;
  // web_close_date_sk         integer
  public final Integer closeDateSk;
  // web_class                 varchar(50)
  public final String webClass;
  // web_manager               varchar(40)
  public final String manager;
  // web_mkt_id                integer
  public final Integer mktId;
  // web_mkt_class             varchar(50)
  public final String mktClass;
  // web_mkt_desc              varchar(100)
  public final String mktDesc;
  // web_market_manager        varchar(40)
  public final String marketManager;
  // web_company_id            integer
  public final Integer companyId;
  // web_company_name          char(50)
  public final String companyName;
  // web_street_number         char(10)
  public final String streetNumber;
  // web_street_name           varchar(60)
  public final String streetName;
  // web_street_type           char(15)
  public final String streetType;
  // web_suite_number          char(10)
  public final String suiteNumber;
  // web_city                  varchar(60)
  public final String city;
  // web_county                varchar(30)
  public final String county;
  // web_state                 char(2)
  public final String state;
  // web_zip                   char(10)
  public final String zip;
  // web_country               varchar(20)
  public final String country;
  // web_gmt_offset            decimal(5,2)
  public final Float gmtOffset;
  // web_tax_percentage        decimal(5,2)
  public final Float taxPercentage;
  // primary key (web_site_sk)

  public WebSite(int siteSk, String siteId, Date recStartDate, Date recEndDate,
      String name, Integer openDateSk, Integer closeDateSk, String webClass,
      String manager, Integer mktId, String mktClass, String mktDesc,
      String marketManager, Integer companyId, String companyName,
      String streetNumber, String streetName, String streetType,
      String suiteNumber, String city, String county, String state, String zip,
      String country, Float gmtOffset, Float taxPercentage) {
    this.siteSk = siteSk;
    this.siteId = siteId;
    this.recStartDate = recStartDate;
    this.recEndDate = recEndDate;
    this.name = name;
    this.openDateSk = openDateSk;
    this.closeDateSk = closeDateSk;
    this.webClass = webClass;
    this.manager = manager;
    this.mktId = mktId;
    this.mktClass = mktClass;
    this.mktDesc = mktDesc;
    this.marketManager = marketManager;
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
    this.taxPercentage = taxPercentage;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End WebSite.java
