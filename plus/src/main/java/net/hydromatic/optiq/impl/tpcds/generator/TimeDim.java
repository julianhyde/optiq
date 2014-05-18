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
public class TimeDim implements TpcdsEntity {
  // t_time_sk                 integer               not null
  public final int timeSk;
  // t_time_id                 char(16)              not null
  public final String timeId;
  // t_time                    integer
  public final Integer time;
  // t_hour                    integer
  public final Integer hour;
  // t_minute                  integer
  public final Integer minute;
  // t_second                  integer
  public final Integer second;
  // t_am_pm                   char(2)
  public final String amPm;
  // t_shift                   char(20)
  public final String shift;
  // t_sub_shift               char(20)
  public final String subShift;
  // t_meal_time               char(20)
  public final String mealTime;
  // primary key (t_time_sk)

  public TimeDim(int timeSk, String timeId, Integer time, Integer hour,
      Integer minute, Integer second, String amPm, String shift,
      String subShift, String mealTime) {
    this.timeSk = timeSk;
    this.timeId = timeId;
    this.time = time;
    this.hour = hour;
    this.minute = minute;
    this.second = second;
    this.amPm = amPm;
    this.shift = shift;
    this.subShift = subShift;
    this.mealTime = mealTime;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End TimeDim.java
