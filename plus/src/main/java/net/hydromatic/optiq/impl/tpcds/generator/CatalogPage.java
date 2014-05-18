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
 * {@code catalog_page} TPC-DS table. */
public class CatalogPage implements TpcdsEntity {
  // cp_catalog_page_sk        integer               not null
  public final int catalogPageSk;
  // cp_catalog_page_id        char(16)              not null
  public final String catalogPageId;
  // cp_start_date_sk          integer
  public final Integer startDateSk;
  // cp_end_date_sk            integer
  public final Integer endDateSk;
  // cp_department             varchar(50)
  public final String department;
  // cp_catalog_number         integer
  public final Integer catalogNumber;
  // cp_catalog_page_number    integer
  public final Integer catalogPageNumber;
  // cp_description            varchar(100)
  public final String description;
  // cp_type                   varchar(100)
  public final String type;
  // primary key (cp_catalog_page_sk)

  public CatalogPage(int catalogPageSk, String catalogPageId,
      Integer startDateSk, Integer endDateSk, String department,
      Integer catalogNumber, Integer catalogPageNumber, String description,
      String type) {
    this.catalogPageSk = catalogPageSk;
    this.catalogPageId = catalogPageId;
    this.startDateSk = startDateSk;
    this.endDateSk = endDateSk;
    this.department = department;
    this.catalogNumber = catalogNumber;
    this.catalogPageNumber = catalogPageNumber;
    this.description = description;
    this.type = type;
  }

  public String toLine() {
    return String.format(ENGLISH,
        "%d|%s|%s|%d|%s|%s|%s|%s|");
  }
}

// End CatalogPage.java
