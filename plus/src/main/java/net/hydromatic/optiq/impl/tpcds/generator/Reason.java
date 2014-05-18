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
 * {@code reason} TPC-DS table. */
public class Reason implements TpcdsEntity {
  // r_reason_sk               integer               not null
  public final int reasonSk;
  // r_reason_id               char(16)              not null
  public final String reasonId;
  // r_reason_desc             char(100)
  public final String reasonDesc;
  // primary key (r_reason_sk)

  public Reason(int reasonSk, String reasonId, String reasonDesc) {
    this.reasonSk = reasonSk;
    this.reasonId = reasonId;
    this.reasonDesc = reasonDesc;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End Reason.java
