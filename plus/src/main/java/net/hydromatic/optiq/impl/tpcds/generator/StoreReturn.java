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
 * {@code store_returns} TPC-DS table. */
public class StoreReturn implements TpcdsEntity {
  // sr_returned_date_sk       integer
  public final Integer returnedDateSk;
  // sr_return_time_sk         integer
  public final Integer returnTimeSk;
  // sr_item_sk                integer               not null
  public final int itemSk;
  // sr_customer_sk            integer
  public final Integer customerSk;
  // sr_cdemo_sk               integer
  public final Integer cdemoSk;
  // sr_hdemo_sk               integer
  public final Integer hdemoSk;
  // sr_addr_sk                integer
  public final Integer addrSk;
  // sr_store_sk               integer
  public final Integer storeSk;
  // sr_reason_sk              integer
  public final Integer reasonSk;
  // sr_ticket_number          integer               not null
  public final int ticketNumber;
  // sr_return_quantity        integer
  public final Integer returnQuantity;
  // sr_return_amt             decimal(7,2)
  public final Float returnAmt;
  // sr_return_tax             decimal(7,2)
  public final Float returnTax;
  // sr_return_amt_inc_tax     decimal(7,2)
  public final Float returnAmtIncTax;
  // sr_fee                    decimal(7,2)
  public final Float fee;
  // sr_return_ship_cost       decimal(7,2)
  public final Float returnShipCost;
  // sr_refunded_cash          decimal(7,2)
  public final Float refundedCash;
  // sr_reversed_charge        decimal(7,2)
  public final Float reversedCharge;
  // sr_store_credit           decimal(7,2)
  public final Float storeCredit;
  // sr_net_loss               decimal(7,2)
  public final Float netLoss;
  // primary key (sr_item_sk sr_ticket_number)


  public StoreReturn(Integer returnedDateSk, Integer returnTimeSk, int itemSk,
      Integer customerSk, Integer cdemoSk, Integer hdemoSk, Integer addrSk,
      Integer storeSk, Integer reasonSk, int ticketNumber,
      Integer returnQuantity, Float returnAmt, Float returnTax,
      Float returnAmtIncTax, Float fee, Float returnShipCost,
      Float refundedCash, Float reversedCharge, Float storeCredit,
      Float netLoss) {
    this.returnedDateSk = returnedDateSk;
    this.returnTimeSk = returnTimeSk;
    this.itemSk = itemSk;
    this.customerSk = customerSk;
    this.cdemoSk = cdemoSk;
    this.hdemoSk = hdemoSk;
    this.addrSk = addrSk;
    this.storeSk = storeSk;
    this.reasonSk = reasonSk;
    this.ticketNumber = ticketNumber;
    this.returnQuantity = returnQuantity;
    this.returnAmt = returnAmt;
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

// End StoreReturn.java
