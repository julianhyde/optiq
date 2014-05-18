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
import java.util.Iterator;

import static java.util.Locale.ENGLISH;

/** Entity corresponding to a row in the
 * {@code call_center} TPC-DS table. */
public class CallCenter implements TpcdsEntity {
  // cc_call_center_sk         integer               not null
  public final int callCenterSk;
  // cc_call_center_id         char(16)              not null
  public final String callCenterId;
  // cc_rec_start_date         date
  public final Date recStartDate;
  // cc_rec_end_date           date
  public final Date recEndDate;
  // cc_closed_date_sk         integer
  public final Integer closedDateSk;
  // cc_open_date_sk           integer
  public final Integer openDateSk;
  // cc_name                   varchar(50)
  public final String name;
  // cc_class                  varchar(50)
  public final String ccClass;
  // cc_employees              integer
  public final Integer employees;
  // cc_sq_ft                  integer
  public final Integer sqFt;
  // cc_hours                  char(20)
  public final String hours;
  // cc_manager                varchar(40)
  public final String manager;
  // cc_mkt_id                 integer
  public final Integer mktId;
  // cc_mkt_class              char(50)
  public final String mktClass;
  // cc_mkt_desc               varchar(100)
  public final String mktDesc;
  // cc_market_manager         varchar(40)
  public final String marketManager;
  // cc_division               integer
  public final Integer division;
  // cc_division_name          varchar(50)
  public final String divisionName;
  // cc_company                integer
  public final Integer company;
  // cc_company_name           char(50)
  public final String companyName;
  // cc_street_number          char(10)
  public final String streetNumber;
  // cc_street_name            varchar(60)
  public final String streetName;
  // cc_street_type            char(15)
  public final String streetType;
  // cc_suite_number           char(10)
  public final String suiteNumber;
  // cc_city                   varchar(60)
  public final String city;
  // cc_county                 varchar(30)
  public final String county;
  // cc_state                  char(2)
  public final String state;
  // cc_zip                    char(10)
  public final String zip;
  // cc_country                varchar(20)
  public final String country;
  // cc_gmt_offset             decimal(5,2)
  public final Float gmtOffset;
  // cc_tax_percentage         decimal(5,2)
  public final Float taxPercentage;
  // primary key (cc_call_center_sk)

  public CallCenter(int callCenterSk, String callCenterId, Date recStartDate,
      Date recEndDate, Integer closedDateSk, Integer openDateSk, String name,
      String aClass, Integer employees, Integer sqFt, String hours,
      String manager,
      Integer mktId, String mktClass, String mktDesc, String marketManager,
      Integer division, String divisionName, Integer company,
      String companyName, String streetNumber,
      String streetName, String streetType, String suiteNumber, String city,
      String county, String state, String zip, String country, Float gmtOffset,
      Float taxPercentage) {
    this.callCenterSk = callCenterSk;
    this.callCenterId = callCenterId;
    this.recStartDate = recStartDate;
    this.recEndDate = recEndDate;
    this.closedDateSk = closedDateSk;
    this.openDateSk = openDateSk;
    this.name = name;
    ccClass = aClass;
    this.employees = employees;
    this.sqFt = sqFt;
    this.hours = hours;
    this.manager = manager;
    this.mktId = mktId;
    this.mktClass = mktClass;
    this.mktDesc = mktDesc;
    this.marketManager = marketManager;
    this.division = division;
    this.divisionName = divisionName;
    this.company = company;
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

  /** Column definition, */
  public enum Column implements TpcdsColumn<CallCenter> {
    X;

    public String getString(CallCenter callCenter) {
      throw new UnsupportedOperationException();
    }

    public double getDouble(CallCenter callCenter) {
      throw new UnsupportedOperationException();
    }

    public long getLong(CallCenter callCenter) {
      throw new UnsupportedOperationException();
    }

    public String getColumnName() {
      throw new UnsupportedOperationException();
    }

    public Class<?> getType() {
      throw new UnsupportedOperationException();
    }
  }

  /** Value generator. */
  public static class Generator implements Iterable<Customer> {
    public Generator(double scaleFactor, int part, int partCount) {
    }

    public Iterator<Customer> iterator() {
      return null;
    }
  }
}

// End CallCenter.java
