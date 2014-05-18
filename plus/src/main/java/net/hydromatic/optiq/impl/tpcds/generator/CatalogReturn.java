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
public class CatalogReturn implements TpcdsEntity {
  // cr_returned_date_sk       integer
  public final Integer returnedDateSk;
  // cr_returned_time_sk       integer
  public final Integer returnedTimeSk;
  // cr_item_sk                integer               not null
  public final int itemSk;
  // cr_refunded_customer_sk   integer
  public final Integer refundedCustomerSk;
  // cr_refunded_cdemo_sk      integer
  public final Integer refundedCdemoSk;
  // cr_refunded_hdemo_sk      integer
  public final Integer refundedHdemoSk;
  // cr_refunded_addr_sk       integer
  public final Integer refundedAddrSk;
  // cr_returning_customer_sk  integer
  public final Integer returningCustomerSk;
  // cr_returning_cdemo_sk     integer
  public final Integer returningCdemoSk;
  // cr_returning_hdemo_sk     integer
  public final Integer returningHdemoSk;
  // cr_returning_addr_sk      integer
  public final Integer returningAddrSk;
  // cr_call_center_sk         integer
  public final Integer callCenterSk;
  // cr_catalog_page_sk        integer
  public final Integer catalogPageSk;
  // cr_ship_mode_sk           integer
  public final Integer shipModeSk;
  // cr_warehouse_sk           integer
  public final Integer warehouseSk;
  // cr_reason_sk              integer
  public final Integer reasonSk;
  // cr_order_number           integer               not null
  public final int orderNumber;
  // cr_return_quantity        integer
  public final Integer returnQuantity;
  // cr_return_amount          decimal(7,2)
  public final Float returnAmount;
  // cr_return_tax             decimal(7,2)
  public final Float returnTax;
  // cr_return_amt_inc_tax     decimal(7,2)
  public final Float returnAmtIncTax;
  // cr_fee                    decimal(7,2)
  public final Float fee;
  // cr_return_ship_cost       decimal(7,2)
  public final Float returnShipCost;
  // cr_refunded_cash          decimal(7,2)
  public final Float refundedCash;
  // cr_reversed_charge        decimal(7,2)
  public final Float reversedCharge;
  // cr_store_credit           decimal(7,2)
  public final Float storeCredit;
  // cr_net_loss               decimal(7,2)
  public final Float netLoss;
  // primary key (cr_item_sk cr_order_number)

  public CatalogReturn(Integer returnedDateSk, Integer returnedTimeSk,
      int itemSk, Integer refundedCustomerSk, Integer refundedCdemoSk,
      Integer refundedHdemoSk, Integer refundedAddrSk,
      Integer returningCustomerSk, Integer returningCdemoSk,
      Integer returningHdemoSk, Integer returningAddrSk, Integer callCenterSk,
      Integer catalogPageSk, Integer shipModeSk, Integer warehouseSk,
      Integer reasonSk, int orderNumber, Integer returnQuantity,
      Float returnAmount, Float returnTax, Float returnAmtIncTax, Float fee,
      Float returnShipCost, Float refundedCash, Float reversedCharge,
      Float storeCredit, Float netLoss) {
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
    this.callCenterSk = callCenterSk;
    this.catalogPageSk = catalogPageSk;
    this.shipModeSk = shipModeSk;
    this.warehouseSk = warehouseSk;
    this.reasonSk = reasonSk;
    this.orderNumber = orderNumber;
    this.returnQuantity = returnQuantity;
    this.returnAmount = returnAmount;
    this.returnTax = returnTax;
    this.returnAmtIncTax = returnAmtIncTax;
    this.fee = fee;
    this.returnShipCost = returnShipCost;
    this.refundedCash = refundedCash;
    this.reversedCharge = reversedCharge;
    this.storeCredit = storeCredit;
    this.netLoss = netLoss;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End CatalogReturn.java
