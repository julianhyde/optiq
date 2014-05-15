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
package net.hydromatic.optiq.tools;


import net.hydromatic.optiq.SchemaPlus;
import net.hydromatic.optiq.config.Lex;

import org.eigenbase.relopt.RelOptCostFactory;
import org.eigenbase.relopt.RelTraitDef;
import org.eigenbase.sql.SqlOperatorTable;
import org.eigenbase.sql.fun.SqlStdOperatorTable;
import org.eigenbase.sql.parser.SqlParserImplFactory;
import org.eigenbase.sql.parser.impl.SqlParserImpl;
import org.eigenbase.sql2rel.SqlRexConvertletTable;
import org.eigenbase.sql2rel.StandardConvertletTable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * An implementation of {@link FrameworkConfig} that uses standard Optiq
 * classes to provide basic planner functionality.
 */
public class StdFrameworkConfig implements FrameworkConfig {

  private final FrameworkContext context;
  private final SqlRexConvertletTable convertletTable;
  private final SqlOperatorTable operatorTable;
  private final ImmutableList<RuleSet> ruleSets;
  private final ImmutableList<RelTraitDef> traitDefs;
  private final Lex lex;
  private final SchemaPlus defaultSchema;
  private final RelOptCostFactory costFactory;
  private final SqlParserImplFactory parserFactory;


  public StdFrameworkConfig(FrameworkContext context, //
      SqlRexConvertletTable convertletTable, //
      SqlOperatorTable operatorTable, //
      ImmutableList<RuleSet> ruleSets, //
      ImmutableList<RelTraitDef> traitDefs,
      Lex lex, //
      SchemaPlus defaultSchema, //
      RelOptCostFactory costFactory, //
      SqlParserImplFactory parserFactory) {
    super();
    this.context = context;
    this.convertletTable = convertletTable;
    this.operatorTable = operatorTable;
    this.ruleSets = ruleSets;
    this.traitDefs = traitDefs;
    this.lex = lex;
    this.defaultSchema = defaultSchema;
    this.costFactory = costFactory;
    this.parserFactory = parserFactory;
  }

  public Lex getLex() {
    return lex;
  }

  public SqlParserImplFactory getParserFactory() {
    return parserFactory;
  }

  public SchemaPlus getDefaultSchema() {
    return defaultSchema;
  }

  public ImmutableList<RuleSet> getRuleSets() {
    return ruleSets;
  }

  public RelOptCostFactory getCostFactory() {
    return costFactory;
  }

  public ImmutableList<RelTraitDef> getTraitDefs() {
    return traitDefs;
  }

  public SqlRexConvertletTable getConvertletTable() {
    return convertletTable;
  }

  public FrameworkContext getFrameworkContext() {
    return context;
  }

  public SqlOperatorTable getOperatorTable() {
    return operatorTable;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * A builder class to help you build a StdFrameworkConfig using defaults
   * where values aren't required.
   */
  public static class Builder {
    private SqlRexConvertletTable convertletTable =
        StandardConvertletTable.INSTANCE;
    private SqlOperatorTable operatorTable = SqlStdOperatorTable.instance();
    private ImmutableList<RuleSet> ruleSets = ImmutableList.of();
    private FrameworkContext context;
    private ImmutableList<RelTraitDef> traitDefs;
    private Lex lex = Lex.ORACLE;
    private SchemaPlus defaultSchema;
    private RelOptCostFactory costFactory;
    private SqlParserImplFactory parserFactory = SqlParserImpl.FACTORY;

    private Builder() {}

    public StdFrameworkConfig build() {
      return new StdFrameworkConfig(context, convertletTable, operatorTable,
          ruleSets, traitDefs, lex, defaultSchema, costFactory, //
          parserFactory);
    }

    public Builder context(FrameworkContext c) {
      Preconditions.checkNotNull(c);
      this.context = c;
      return this;
    };

    public Builder convertletTable(SqlRexConvertletTable table) {
      Preconditions.checkNotNull(table);
      this.convertletTable = table;
      return this;
    }

    public Builder operatorTable(SqlOperatorTable table) {
      Preconditions.checkNotNull(table);
      this.operatorTable = table;
      return this;
    }

    public Builder traitDefs(List<RelTraitDef> traitDefs) {
      if (traitDefs == null) {
        this.traitDefs = null;
      } else {
        this.traitDefs = ImmutableList.copyOf(traitDefs);
      }
      return this;
    }

    public Builder traitDefs(RelTraitDef... traitDefs) {
      this.traitDefs = ImmutableList.copyOf(traitDefs);
      return this;
    }

    public Builder lex(Lex lex) {
      Preconditions.checkNotNull(lex);
      this.lex = lex;
      return this;
    }

    public Builder defaultSchema(SchemaPlus defaultSchema) {
      this.defaultSchema = defaultSchema;
      return this;
    }

    public Builder costFactory(RelOptCostFactory costFactory) {
      this.costFactory = costFactory;
      return this;
    }

    public Builder ruleSets(List<RuleSet> ruleSets) {
      Preconditions.checkNotNull(ruleSets);
      this.ruleSets = ImmutableList.copyOf(ruleSets);
      return this;
    }

    public Builder ruleSets(RuleSet... ruleSets) {
      this.ruleSets = ImmutableList.copyOf(ruleSets);
      return this;
    }

    public Builder parserFactory(SqlParserImplFactory parserFactory) {
      Preconditions.checkNotNull(parserFactory);
      this.parserFactory = parserFactory;
      return this;
    }
  }
}
