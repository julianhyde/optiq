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
 * {@code customer_address} TPC-DS table. */
public class CustomerAddress implements TpcdsEntity {
  // ca_address_sk             integer               not null
  public final int addressSk;
  // ca_address_id             char(16)              not null
  public final String addressId;
  // ca_street_number          char(10)
  public final String streetNumber;
  // ca_street_name            varchar(60)
  public final String streetName;
  // ca_street_type            char(15)
  public final String streetType;
  // ca_suite_number           char(10)
  public final String suiteNumber;
  // ca_city                   varchar(60)
  public final String city;
  // ca_county                 varchar(30)
  public final String county;
  // ca_state                  char(2)
  public final String state;
  // ca_zip                    char(10)
  public final String zip;
  // ca_country                varchar(20)
  public final String country;
  // ca_gmt_offset             decimal(5,2)
  public final Float gmtOffset;
  // ca_location_type          char(20)
  public final String locationType;
  // primary key (ca_address_sk)

  public CustomerAddress(int addressSk, String addressId, String streetNumber,
      String streetName, String streetType, String suiteNumber, String city,
      String county, String state, String zip, String country, Float gmtOffset,
      String locationType) {
    this.addressSk = addressSk;
    this.addressId = addressId;
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
    this.locationType = locationType;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End CustomerAddress.java
