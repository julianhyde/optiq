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
public class StoreSale implements TpcdsEntity {
  // ss_sold_date_sk           integer
  public final Integer soldDateSk;
  // ss_sold_time_sk           integer
  public final Integer soldTimeSk;
  // ss_item_sk                integer               not null
  public final int itemSk;
  // ss_customer_sk            integer
  public final Integer customerSk;
  // ss_cdemo_sk               integer
  public final Integer cdemoSk;
  // ss_hdemo_sk               integer
  public final Integer hdemoSk;
  // ss_addr_sk                integer
  public final Integer addrSk;
  // ss_store_sk               integer
  public final Integer storeSk;
  // ss_promo_sk               integer
  public final Integer promoSk;
  // ss_ticket_number          integer               not null
  public final int ticketNumber;
  // ss_quantity               integer
  public final Integer quantity;
  // ss_wholesale_cost         decimal(7,2)
  public final Float wholesaleCost;
  // ss_list_price             decimal(7,2)
  public final Float listPrice;
  // ss_sales_price            decimal(7,2)
  public final Float salesPrice;
  // ss_ext_discount_amt       decimal(7,2)
  public final Float extDiscountAmt;
  // ss_ext_sales_price        decimal(7,2)
  public final Float extSalesPrice;
  // ss_ext_wholesale_cost     decimal(7,2)
  public final Float extWholesaleCost;
  // ss_ext_list_price         decimal(7,2)
  public final Float extListPrice;
  // ss_ext_tax                decimal(7,2)
  public final Float extTax;
  // ss_coupon_amt             decimal(7,2)
  public final Float couponAmt;
  // ss_net_paid               decimal(7,2)
  public final Float netPaid;
  // ss_net_paid_inc_tax       decimal(7,2)
  public final Float netPaidIncTax;
  // ss_net_profit             decimal(7,2)
  public final Float netProfit;
  // primary key (ss_item_sk ss_ticket_number)


  public StoreSale(Integer soldDateSk, Integer soldTimeSk, int itemSk,
      Integer customerSk, Integer cdemoSk, Integer hdemoSk, Integer addrSk,
      Integer storeSk, Integer promoSk, int ticketNumber, Integer quantity,
      Float wholesaleCost, Float listPrice, Float salesPrice,
      Float extDiscountAmt, Float extSalesPrice, Float extWholesaleCost,
      Float extListPrice, Float extTax, Float couponAmt, Float netPaid,
      Float netPaidIncTax, Float netProfit) {
    this.soldDateSk = soldDateSk;
    this.soldTimeSk = soldTimeSk;
    this.itemSk = itemSk;
    this.customerSk = customerSk;
    this.cdemoSk = cdemoSk;
    this.hdemoSk = hdemoSk;
    this.addrSk = addrSk;
    this.storeSk = storeSk;
    this.promoSk = promoSk;
    this.ticketNumber = ticketNumber;
    this.quantity = quantity;
    this.wholesaleCost = wholesaleCost;
    this.listPrice = listPrice;
    this.salesPrice = salesPrice;
    this.extDiscountAmt = extDiscountAmt;
    this.extSalesPrice = extSalesPrice;
    this.extWholesaleCost = extWholesaleCost;
    this.extListPrice = extListPrice;
    this.extTax = extTax;
    this.couponAmt = couponAmt;
    this.netPaid = netPaid;
    this.netPaidIncTax = netPaidIncTax;
    this.netProfit = netProfit;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End StoreSale.java
