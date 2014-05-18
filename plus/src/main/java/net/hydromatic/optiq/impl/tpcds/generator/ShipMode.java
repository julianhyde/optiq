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
 * {@code } TPC-DS table. */
public class ShipMode implements TpcdsEntity {
  // sm_ship_mode_sk           integer               not null
  public final int shipModeSk;
  // sm_ship_mode_id           char(16)              not null
  public final String shipModeId;
  // sm_type                   char(30)
  public final String type;
  // sm_code                   char(10)
  public final String code;
  // sm_carrier                char(20)
  public final String carrier;
  // sm_contract               char(20)
  public final String contract;
  // primary key (sm_ship_mode_sk)

  public ShipMode(int shipModeSk, String shipModeId, String type, String code,
      String carrier, String contract) {
    this.shipModeSk = shipModeSk;
    this.shipModeId = shipModeId;
    this.type = type;
    this.code = code;
    this.carrier = carrier;
    this.contract = contract;
  }


  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End ShipMode.java
