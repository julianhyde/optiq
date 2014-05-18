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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Definition of a TPC-DS table.
 *
 * @param <E> Element type
 */
public class TpcdsTable<E> {
  public static final TpcdsTable<Customer> CALL_CENTER =
      new TpcdsTable<Customer>("call_center", "cc", CallCenter.Column.values()) {
      public Iterable<Customer> createGenerator(double scaleFactor, int part,
          int partCount) {
        return new CallCenter.Generator(scaleFactor, part, partCount);
      }
    };

  public final String name;
  public final String prefix;
  public final ImmutableList<TpcdsColumn<E>> columns;

  private static final List<TpcdsTable<?>> TABLES;
  private static final Map<String, TpcdsTable<?>> TABLES_BY_NAME;

  static {
    TABLES = ImmutableList.<TpcdsTable<?>>of(
        CALL_CENTER);
    TABLES_BY_NAME = Maps.uniqueIndex(TABLES, tableNameGetter());
  }

  public TpcdsTable(String name, String prefix, TpcdsColumn[] columns) {
    this.name = checkNotNull(name);
    this.prefix = checkNotNull(prefix);
    //noinspection unchecked
    this.columns = ImmutableList.<TpcdsColumn<E>>copyOf(columns);
  }

  public static TpcdsTable[] getTables() {
    return TABLES.toArray(new TpcdsTable[TABLES.size()]);
  }

  public String getTableName() {
    return name;
  }

  public Iterable<E> createGenerator(double scaleFactor, int part,
      int partCount) {
    return null;
  }

  public List<TpcdsColumn<E>> getColumns() {
    return columns;
  }

  public static Function<TpcdsTable<?>, String> tableNameGetter() {
    return new Function<TpcdsTable<?>, String>() {
      public String apply(TpcdsTable<?> table) {
        return table.getTableName();
      }
    };
  }
}

// End TpcdsTable.java
