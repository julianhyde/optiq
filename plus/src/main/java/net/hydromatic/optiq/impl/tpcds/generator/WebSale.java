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
 * {@code web_sales} TPC-DS table. */
public class WebSale implements TpcdsEntity {
  // ws_sold_date_sk           integer
  public final Integer soldDateSk;
  // ws_sold_time_sk           integer
  public final Integer soldTimeSk;
  // ws_ship_date_sk           integer
  public final Integer shipDateSk;
  // ws_item_sk                integer               not null
  public final int itemSk;
  // ws_bill_customer_sk       integer
  public final Integer billCustomerSk;
  // ws_bill_cdemo_sk          integer
  public final Integer billCdemoSk;
  // ws_bill_hdemo_sk          integer
  public final Integer billHdemoSk;
  // ws_bill_addr_sk           integer
  public final Integer billAddrSk;
  // ws_ship_customer_sk       integer
  public final Integer shipCustomerSk;
  // ws_ship_cdemo_sk          integer
  public final Integer shipCdemoSk;
  // ws_ship_hdemo_sk          integer
  public final Integer shipHdemoSk;
  // ws_ship_addr_sk           integer
  public final Integer shipAddrSk;
  // ws_web_page_sk            integer
  public final Integer webPageSk;
  // ws_web_site_sk            integer
  public final Integer webSiteSk;
  // ws_ship_mode_sk           integer
  public final Integer shipModeSk;
  // ws_warehouse_sk           integer
  public final Integer warehouseSk;
  // ws_promo_sk               integer
  public final Integer promoSk;
  // ws_order_number           integer               not null
  public final int orderNumber;
  // ws_quantity               integer
  public final Integer quantity;
  // ws_wholesale_cost         decimal(7,2)
  public final Float wholesaleCost;
  // ws_list_price             decimal(7,2)
  public final Float listPrice;
  // ws_sales_price            decimal(7,2)
  public final Float salesPrice;
  // ws_ext_discount_amt       decimal(7,2)
  public final Float extDiscountAmt;
  // ws_ext_sales_price        decimal(7,2)
  public final Float extSalesPrice;
  // ws_ext_wholesale_cost     decimal(7,2)
  public final Float extWholesaleCost;
  // ws_ext_list_price         decimal(7,2)
  public final Float extListPrice;
  // ws_ext_tax                decimal(7,2)
  public final Float extTax;
  // ws_coupon_amt             decimal(7,2)
  public final Float couponAmt;
  // ws_ext_ship_cost          decimal(7,2)
  public final Float extShipCost;
  // ws_net_paid               decimal(7,2)
  public final Float netPaid;
  // ws_net_paid_inc_tax       decimal(7,2)
  public final Float netPaidIncTax;
  // ws_net_paid_inc_ship      decimal(7,2)
  public final Float netPaidIncShip;
  // ws_net_paid_inc_ship_tax  decimal(7,2)
  public final Float netPaidIncShipTax;
  // ws_net_profit             decimal(7,2)
  public final Float netProfit;
  // primary key (ws_item_sk ws_order_number)


  public WebSale(Integer soldDateSk, Integer soldTimeSk, Integer shipDateSk,
      int itemSk, Integer billCustomerSk, Integer billCdemoSk,
      Integer billHdemoSk, Integer billAddrSk, Integer shipCustomerSk,
      Integer shipCdemoSk, Integer shipHdemoSk, Integer shipAddrSk,
      Integer webPageSk, Integer webSiteSk, Integer shipModeSk,
      Integer warehouseSk, Integer promoSk, int orderNumber, Integer quantity,
      Float wholesaleCost, Float listPrice, Float salesPrice,
      Float extDiscountAmt, Float extSalesPrice, Float extWholesaleCost,
      Float extListPrice, Float extTax, Float couponAmt, Float extShipCost,
      Float netPaid, Float netPaidIncTax, Float netPaidIncShip,
      Float netPaidIncShipTax, Float netProfit) {
    this.soldDateSk = soldDateSk;
    this.soldTimeSk = soldTimeSk;
    this.shipDateSk = shipDateSk;
    this.itemSk = itemSk;
    this.billCustomerSk = billCustomerSk;
    this.billCdemoSk = billCdemoSk;
    this.billHdemoSk = billHdemoSk;
    this.billAddrSk = billAddrSk;
    this.shipCustomerSk = shipCustomerSk;
    this.shipCdemoSk = shipCdemoSk;
    this.shipHdemoSk = shipHdemoSk;
    this.shipAddrSk = shipAddrSk;
    this.webPageSk = webPageSk;
    this.webSiteSk = webSiteSk;
    this.shipModeSk = shipModeSk;
    this.warehouseSk = warehouseSk;
    this.promoSk = promoSk;
    this.orderNumber = orderNumber;
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
    this.extShipCost = extShipCost;
    this.netPaid = netPaid;
    this.netPaidIncTax = netPaidIncTax;
    this.netPaidIncShip = netPaidIncShip;
    this.netPaidIncShipTax = netPaidIncShipTax;
    this.netProfit = netProfit;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End WebSale.java
