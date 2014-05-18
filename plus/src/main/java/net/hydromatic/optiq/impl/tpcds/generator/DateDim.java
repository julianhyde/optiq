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
 * {@code date_dim} TPC-DS table. */
public class DateDim implements TpcdsEntity {
  // d_date_sk                 integer               not null
  public final int dateSk;
  // d_date_id                 char(16)              not null
  public final String dateId;
  // d_date                    date
  public final Date date;
  // d_month_seq               integer
  public final Integer monthSeq;
  // d_week_seq                integer
  public final Integer weekSeq;
  // d_quarter_seq             integer
  public final Integer quarterSeq;
  // d_year                    integer
  public final Integer year;
  // d_dow                     integer
  public final Integer dow;
  // d_moy                     integer
  public final Integer moy;
  // d_dom                     integer
  public final Integer dom;
  // d_qoy                     integer
  public final Integer qoy;
  // d_fy_year                 integer
  public final Integer fyYear;
  // d_fy_quarter_seq          integer
  public final Integer fyQuarterSeq;
  // d_fy_week_seq             integer
  public final Integer fyWeekSeq;
  // d_day_name                char(9)
  public final String dayName;
  // d_quarter_name            char(6)
  public final String quarterName;
  // d_holiday                 char(1)
  public final String holiday;
  // d_weekend                 char(1)
  public final String weekend;
  // d_following_holiday       char(1)
  public final String followingHoliday;
  // d_first_dom               integer
  public final Integer firstDom;
  // d_last_dom                integer
  public final Integer lastDom;
  // d_same_day_ly             integer
  public final Integer sameDayLy;
  // d_same_day_lq             integer
  public final Integer sameDayLq;
  // d_current_day             char(1)
  public final String currentDay;
  // d_current_week            char(1)
  public final String currentWeek;
  // d_current_month           char(1)
  public final String currentMonth;
  // d_current_quarter         char(1)
  public final String currentQuarter;
  // d_current_year            char(1)
  public final String currentYear;
  // primary key (d_date_sk)

  public DateDim(int dateSk, String dateId, Date date, Integer monthSeq,
       Integer weekSeq, Integer quarterSeq, Integer year, Integer dow,
       Integer moy, Integer dom, Integer qoy, Integer fyYear,
       Integer fyQuarterSeq, Integer fyWeekSeq, String dayName,
       String quarterName, String holiday, String weekend,
       String followingHoliday, Integer firstDom, Integer lastDom,
       Integer sameDayLy, Integer sameDayLq, String currentDay,
       String currentWeek, String currentMonth, String currentQuarter,
       String currentYear) {
    this.dateSk = dateSk;
    this.dateId = dateId;
    this.date = date;
    this.monthSeq = monthSeq;
    this.weekSeq = weekSeq;
    this.quarterSeq = quarterSeq;
    this.year = year;
    this.dow = dow;
    this.moy = moy;
    this.dom = dom;
    this.qoy = qoy;
    this.fyYear = fyYear;
    this.fyQuarterSeq = fyQuarterSeq;
    this.fyWeekSeq = fyWeekSeq;
    this.dayName = dayName;
    this.quarterName = quarterName;
    this.holiday = holiday;
    this.weekend = weekend;
    this.followingHoliday = followingHoliday;
    this.firstDom = firstDom;
    this.lastDom = lastDom;
    this.sameDayLy = sameDayLy;
    this.sameDayLq = sameDayLq;
    this.currentDay = currentDay;
    this.currentWeek = currentWeek;
    this.currentMonth = currentMonth;
    this.currentQuarter = currentQuarter;
    this.currentYear = currentYear;
  }


  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End DateDim.java
