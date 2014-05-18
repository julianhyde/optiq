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
 * {@code income_band} TPC-DS table. */
public class IncomeBand implements TpcdsEntity {
  // ib_income_band_sk         integer               not null
  public final int incomeBandSk;
  // ib_lower_bound            integer
  public final Integer lowerBound;
  // ib_upper_bound            integer
  public final Integer upperBound;
  // primary key (ib_income_band_sk)

  public IncomeBand(int incomeBandSk, Integer lowerBound, Integer upperBound) {
    this.incomeBandSk = incomeBandSk;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End IncomeBand.java
