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
 * {@code web_returns} TPC-DS table. */
public class WebReturn implements TpcdsEntity {
  // wr_returned_date_sk       integer
  public final Integer returnedDateSk;
  // wr_returned_time_sk       integer
  public final Integer returnedTimeSk;
  // wr_item_sk                integer               not null
  public final int itemSk;
  // wr_refunded_customer_sk   integer
  public final Integer refundedCustomerSk;
  // wr_refunded_cdemo_sk      integer
  public final Integer refundedCdemoSk;
  // wr_refunded_hdemo_sk      integer
  public final Integer refundedHdemoSk;
  // wr_refunded_addr_sk       integer
  public final Integer refundedAddrSk;
  // wr_returning_customer_sk  integer
  public final Integer returningCustomerSk;
  // wr_returning_cdemo_sk     integer
  public final Integer returningCdemoSk;
  // wr_returning_hdemo_sk     integer
  public final Integer returningHdemoSk;
  // wr_returning_addr_sk      integer
  public final Integer returningAddrSk;
  // wr_web_page_sk            integer
  public final Integer webPageSk;
  // wr_reason_sk              integer
  public final Integer reasonSk;
  // wr_order_number           integer               not null
  public final int orderNumber;
  // wr_return_quantity        integer
  public final Integer returnQuantity;
  // wr_return_amt             decimal(7,2)
  public final Float returnAmt;
  // wr_return_tax             decimal(7,2)
  public final Float returnTax;
  // wr_return_amt_inc_tax     decimal(7,2)
  public final Float returnAmtIncTax;
  // wr_fee                    decimal(7,2)
  public final Float fee;
  // wr_return_ship_cost       decimal(7,2)
  public final Float returnShipCost;
  // wr_refunded_cash          decimal(7,2)
  public final Float refundedCash;
  // wr_reversed_charge        decimal(7,2)
  public final Float reversedCharge;
  // wr_account_credit         decimal(7,2)
  public final Float accountCredit;
  // wr_net_loss               decimal(7,2)
  public final Float netLoss;
  // primary key (wr_item_sk wr_order_number)


  public WebReturn(Integer returnedDateSk, Integer returnedTimeSk, int itemSk,
      Integer refundedCustomerSk, Integer refundedCdemoSk,
      Integer refundedHdemoSk, Integer refundedAddrSk,
      Integer returningCustomerSk, Integer returningCdemoSk,
      Integer returningHdemoSk, Integer returningAddrSk, Integer webPageSk,
      Integer reasonSk, int orderNumber, Integer returnQuantity,
      Float returnAmt, Float returnTax, Float returnAmtIncTax, Float fee,
      Float returnShipCost, Float refundedCash, Float reversedCharge,
      Float accountCredit, Float netLoss) {
    this.returnedDateSk = returnedDateSk;
    this.returnedTimeSk = returnedTimeSk;
    this.itemSk = itemSk;
    this.refundedCustomerSk = refundedCustomerSk;
    this.refundedCdemoSk = refundedCdemoSk;
    this.refundedHdemoSk = refundedHdemoSk;
    this.refundedAddrSk = refundedAddrSk;
    this.returningCustomerSk = returningCustomerSk;
    this.returningCdemoSk = returningCdemoSk;
    this.returningHdemoSk = returningHdemoSk;
    this.returningAddrSk = returningAddrSk;
    this.webPageSk = webPageSk;
    this.reasonSk = reasonSk;
    this.orderNumber = orderNumber;
    this.returnQuantity = returnQuantity;
    this.returnAmt = returnAmt;
    this.returnTax = returnTax;
    this.returnAmtIncTax = returnAmtIncTax;
    this.fee = fee;
    this.returnShipCost = returnShipCost;
    this.refundedCash = refundedCash;
    this.reversedCharge = reversedCharge;
    this.accountCredit = accountCredit;
    this.netLoss = netLoss;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End WebReturn.java
