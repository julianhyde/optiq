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
public class Customer implements TpcdsEntity {
  // c_customer_sk             integer               not null
  public final int customerSk;
  // c_customer_id             char(16)              not null
  public final String customerId;
  // c_current_cdemo_sk        integer
  public final Integer currentCdemoSk;
  // c_current_hdemo_sk        integer
  public final Integer currentHdemoSk;
  // c_current_addr_sk         integer
  public final Integer currentAddrSk;
  // c_first_shipto_date_sk    integer
  public final Integer firstShiptoDateSk;
  // c_first_sales_date_sk     integer
  public final Integer firstSalesDateSk;
  // c_salutation              char(10)
  public final String salutation;
  // c_first_name              char(20)
  public final String firstName;
  // c_last_name               char(30)
  public final String lastName;
  // c_preferred_cust_flag     char(1)
  public final String preferredCustFlag;
  // c_birth_day               integer
  public final Integer birthDay;
  // c_birth_month             integer
  public final Integer birthMonth;
  // c_birth_year              integer
  public final Integer birthYear;
  // c_birth_country           varchar(20)
  public final String birthCountry;
  // c_login                   char(13)
  public final String login;
  // c_email_address           char(50)
  public final String emailAddress;
  // c_last_review_date        char(10)
  public final String lastReviewDate;
  // primary key (c_customer_sk)

  public Customer(int customerSk, String customerId, Integer currentCdemoSk,
      Integer currentHdemoSk, Integer currentAddrSk, Integer firstShiptoDateSk,
      Integer firstSalesDateSk, String salutation, String firstName,
      String lastName, String preferredCustFlag, Integer birthDay,
      Integer birthMonth, Integer birthYear, String birthCountry, String login,
      String emailAddress, String lastReviewDate) {
    this.customerSk = customerSk;
    this.customerId = customerId;
    this.currentCdemoSk = currentCdemoSk;
    this.currentHdemoSk = currentHdemoSk;
    this.currentAddrSk = currentAddrSk;
    this.firstShiptoDateSk = firstShiptoDateSk;
    this.firstSalesDateSk = firstSalesDateSk;
    this.salutation = salutation;
    this.firstName = firstName;
    this.lastName = lastName;
    this.preferredCustFlag = preferredCustFlag;
    this.birthDay = birthDay;
    this.birthMonth = birthMonth;
    this.birthYear = birthYear;
    this.birthCountry = birthCountry;
    this.login = login;
    this.emailAddress = emailAddress;
    this.lastReviewDate = lastReviewDate;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End Customer.java
