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
 * {@code household_demographics} TPC-DS table. */
public class HouseholdDemographic implements TpcdsEntity {
  // hd_demo_sk                integer               not null
  public final int demoSk;
  // hd_income_band_sk         integer
  public final Integer incomeBandSk;
  // hd_buy_potential          char(15)
  public final String buyPotential;
  // hd_dep_count              integer
  public final Integer depCount;
  // hd_vehicle_count          integer
  public final Integer vehicleCount;
  // primary key (hd_demo_sk)

  public HouseholdDemographic(int demoSk, Integer incomeBandSk,
      String buyPotential, Integer depCount, Integer vehicleCount) {
    this.demoSk = demoSk;
    this.incomeBandSk = incomeBandSk;
    this.buyPotential = buyPotential;
    this.depCount = depCount;
    this.vehicleCount = vehicleCount;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End HouseholdDemographic.java
