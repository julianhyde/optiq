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
 * {@code customer_demographics} TPC-DS table. */
public class CustomerDemographic implements TpcdsEntity {
  // cd_demo_sk                integer               not null
  public final int demoSk;
  // cd_gender                 char(1)
  public final String gender;
  // cd_marital_status         char(1)
  public final String maritalStatus;
  // cd_education_status       char(20)
  public final String educationStatus;
  // cd_purchase_estimate      integer
  public final Integer purchaseEstimate;
  // cd_credit_rating          char(10)
  public final String creditRating;
  // cd_dep_count              integer
  public final Integer depCount;
  // cd_dep_employed_count     integer
  public final Integer depEmployedCount;
  // cd_dep_college_count      integer
  public final Integer depCollegeCount;
  // primary key (cd_demo_sk)

  public CustomerDemographic(int demoSk, String gender, String maritalStatus,
      String educationStatus, Integer purchaseEstimate, String creditRating,
      Integer depCount, Integer depEmployedCount, Integer depCollegeCount) {
    this.demoSk = demoSk;
    this.gender = gender;
    this.maritalStatus = maritalStatus;
    this.educationStatus = educationStatus;
    this.purchaseEstimate = purchaseEstimate;
    this.creditRating = creditRating;
    this.depCount = depCount;
    this.depEmployedCount = depEmployedCount;
    this.depCollegeCount = depCollegeCount;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End CustomerDemographic.java
