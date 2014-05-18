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
 * {@code web_page} TPC-DS table. */
public class WebPage implements TpcdsEntity {
  // wp_web_page_sk            integer               not null
  public final int webPageSk;
  // wp_web_page_id            char(16)              not null
  public final String webPageId;
  // wp_rec_start_date         date
  public final Date recStartDate;
  // wp_rec_end_date           date
  public final Date recEndDate;
  // wp_creation_date_sk       integer
  public final Integer creationDateSk;
  // wp_access_date_sk         integer
  public final Integer accessDateSk;
  // wp_autogen_flag           char(1)
  public final String autogenFlag;
  // wp_customer_sk            integer
  public final Integer customerSk;
  // wp_url                    varchar(100)
  public final String url;
  // wp_type                   char(50)
  public final String type;
  // wp_char_count             integer
  public final Integer charCount;
  // wp_link_count             integer
  public final Integer linkCount;
  // wp_image_count            integer
  public final Integer imageCount;
  // wp_max_ad_count           integer
  public final Integer maxAdCount;
  // primary key (wp_web_page_sk)

  public WebPage(int webPageSk, String webPageId, Date recStartDate,
      Date recEndDate, Integer creationDateSk, Integer accessDateSk,
      String autogenFlag, Integer customerSk, String url, String type,
      Integer charCount, Integer linkCount, Integer imageCount,
      Integer maxAdCount) {
    this.webPageSk = webPageSk;
    this.webPageId = webPageId;
    this.recStartDate = recStartDate;
    this.recEndDate = recEndDate;
    this.creationDateSk = creationDateSk;
    this.accessDateSk = accessDateSk;
    this.autogenFlag = autogenFlag;
    this.customerSk = customerSk;
    this.url = url;
    this.type = type;
    this.charCount = charCount;
    this.linkCount = linkCount;
    this.imageCount = imageCount;
    this.maxAdCount = maxAdCount;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}
