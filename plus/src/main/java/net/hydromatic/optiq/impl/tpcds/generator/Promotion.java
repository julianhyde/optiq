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
 * {@code promotion} TPC-DS table. */
public class Promotion implements TpcdsEntity {
  // p_promo_sk                integer               not null
  public final int promoSk;
  // p_promo_id                char(16)              not null
  public final String promoId;
  // p_start_date_sk           integer
  public final Integer startDateSk;
  // p_end_date_sk             integer
  public final Integer endDateSk;
  // p_item_sk                 integer
  public final Integer itemSk;
  // p_cost                    decimal(152)
  public final Float cost;
  // p_response_target         integer
  public final Integer responseTarget;
  // p_promo_name              char(50)
  public final String promoName;
  // p_channel_dmail           char(1)
  public final String channelDmail;
  // p_channel_email           char(1)
  public final String channelEmail;
  // p_channel_catalog         char(1)
  public final String channelCatalog;
  // p_channel_tv              char(1)
  public final String channelTv;
  // p_channel_radio           char(1)
  public final String channelRadio;
  // p_channel_press           char(1)
  public final String channelPress;
  // p_channel_event           char(1)
  public final String channelEvent;
  // p_channel_demo            char(1)
  public final String channelDemo;
  // p_channel_details         varchar(100)
  public final String channelDetails;
  // p_purpose                 char(15)
  public final String purpose;
  // p_discount_active         char(1)
  public final String discountActive;
  // primary key (p_promo_sk)

  public Promotion(int promoSk, String promoId, Integer startDateSk,
      Integer endDateSk, Integer itemSk, Float cost, Integer responseTarget,
      String promoName, String channelDmail, String channelEmail,
      String channelCatalog, String channelTv, String channelRadio,
      String channelPress, String channelEvent, String channelDemo,
      String channelDetails, String purpose, String discountActive) {
    this.promoSk = promoSk;
    this.promoId = promoId;
    this.startDateSk = startDateSk;
    this.endDateSk = endDateSk;
    this.itemSk = itemSk;
    this.cost = cost;
    this.responseTarget = responseTarget;
    this.promoName = promoName;
    this.channelDmail = channelDmail;
    this.channelEmail = channelEmail;
    this.channelCatalog = channelCatalog;
    this.channelTv = channelTv;
    this.channelRadio = channelRadio;
    this.channelPress = channelPress;
    this.channelEvent = channelEvent;
    this.channelDemo = channelDemo;
    this.channelDetails = channelDetails;
    this.purpose = purpose;
    this.discountActive = discountActive;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End Promotion.java
