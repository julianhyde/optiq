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
import java.sql.Time;

import static java.util.Locale.ENGLISH;

/** Entity corresponding to a row in the
 * {@code dbgen_version} TPC-DS table. */
public class DbgenVersion implements TpcdsEntity {
  // dv_version                varchar(16)
  public final String version;
  // dv_create_date            date
  public final Date createDate;
  // dv_create_time            time
  public final Time createTime;
  // dv_cmdline_args           varchar(200)
  public final String cmdlineArgs;

  public DbgenVersion(String version, Date createDate,
      Time createTime, String cmdlineArgs) {
    this.version = version;
    this.createDate = createDate;
    this.createTime = createTime;
    this.cmdlineArgs = cmdlineArgs;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End DbgenVersion.java
