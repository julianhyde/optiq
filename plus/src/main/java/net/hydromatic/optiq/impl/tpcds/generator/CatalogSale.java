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
 * {@code catalog_sales} TPC-DS table. */
public class CatalogSale implements TpcdsEntity {
  // cs_sold_date_sk           integer
  public final Integer soldDateSk;
  // cs_sold_time_sk           integer
  public final Integer soldTimeSk;
  // cs_ship_date_sk           integer
  public final Integer shipDateSk;
  // cs_bill_customer_sk       integer
  public final Integer billCustomerSk;
  // cs_bill_cdemo_sk          integer
  public final Integer billCdemoSk;
  // cs_bill_hdemo_sk          integer
  public final Integer billHdemoSk;
  // cs_bill_addr_sk           integer
  public final Integer billAddrSk;
  // cs_ship_customer_sk       integer
  public final Integer shipCustomerSk;
  // cs_ship_cdemo_sk          integer
  public final Integer shipCdemoSk;
  // cs_ship_hdemo_sk          integer
  public final Integer shipHdemoSk;
  // cs_ship_addr_sk           integer
  public final Integer shipAddrSk;
  // cs_call_center_sk         integer
  public final Integer callCenterSk;
  // cs_catalog_page_sk        integer
  public final Integer catalogPageSk;
  // cs_ship_mode_sk           integer
  public final Integer shipModeSk;
  // cs_warehouse_sk           integer
  public final Integer warehouseSk;
  // cs_item_sk                integer               not null
  public final int itemSk;
  // cs_promo_sk               integer
  public final Integer promoSk;
  // cs_order_number           integer               not null
  public final int orderNumber;
  // cs_quantity               integer
  public final Integer quantity;
  // cs_wholesale_cost         decimal(7,2)
  public final Float wholesaleCost;
  // cs_list_price             decimal(7,2)
  public final Float listPrice;
  // cs_sales_price            decimal(7,2)
  public final Float salesPrice;
  // cs_ext_discount_amt       decimal(7,2)
  public final Float extDiscountAmt;
  // cs_ext_sales_price        decimal(7,2)
  public final Float extSalesPrice;
  // cs_ext_wholesale_cost     decimal(7,2)
  public final Float extWholesaleCost;
  // cs_ext_list_price         decimal(7,2)
  public final Float extListPrice;
  // cs_ext_tax                decimal(7,2)
  public final Float extTax;
  // cs_coupon_amt             decimal(7,2)
  public final Float couponAmt;
  // cs_ext_ship_cost          decimal(7,2)
  public final Float extShipCost;
  // cs_net_paid               decimal(7,2)
  public final Float netPaid;
  // cs_net_paid_inc_tax       decimal(7,2)
  public final Float netPaidIncTax;
  // cs_net_paid_inc_ship      decimal(7,2)
  public final Float netPaidIncShip;
  // cs_net_paid_inc_ship_tax  decimal(7,2)
  public final Float netPaidIncShipTax;
  // cs_net_profit             decimal(7,2)
  public final Float netProfit;
  // primary key (cs_item_sk cs_order_number)

  public CatalogSale(Integer soldDateSk, Integer soldTimeSk, Integer shipDateSk,
      Integer billCustomerSk, Integer billCdemoSk, Integer billHdemoSk,
      Integer billAddrSk, Integer shipCustomerSk, Integer shipCdemoSk,
      Integer shipHdemoSk, Integer shipAddrSk, Integer callCenterSk,
      Integer catalogPageSk, Integer shipModeSk, Integer warehouseSk,
      int itemSk, Integer promoSk, int orderNumber, Integer quantity,
      Float wholesaleCost, Float listPrice, Float salesPrice,
      Float extDiscountAmt, Float extSalesPrice, Float extWholesaleCost,
      Float extListPrice, Float extTax, Float couponAmt, Float extShipCost,
      Float netPaid, Float netPaidIncTax, Float netPaidIncShip,
      Float netPaidIncShipTax, Float netProfit) {
    this.soldDateSk = soldDateSk;
    this.soldTimeSk = soldTimeSk;
    this.shipDateSk = shipDateSk;
    this.billCustomerSk = billCustomerSk;
    this.billCdemoSk = billCdemoSk;
    this.billHdemoSk = billHdemoSk;
    this.billAddrSk = billAddrSk;
    this.shipCustomerSk = shipCustomerSk;
    this.shipCdemoSk = shipCdemoSk;
    this.shipHdemoSk = shipHdemoSk;
    this.shipAddrSk = shipAddrSk;
    this.callCenterSk = callCenterSk;
    this.catalogPageSk = catalogPageSk;
    this.shipModeSk = shipModeSk;
    this.warehouseSk = warehouseSk;
    this.itemSk = itemSk;
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

// End CatalogSale.java
