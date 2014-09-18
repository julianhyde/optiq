/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hydromatic.optiq.impl.interpreter;

import org.eigenbase.rel.RelNode;
import org.eigenbase.rex.*;

import com.google.common.collect.Maps;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Interpreter.
 *
 * <p>Contains the context for interpreting relational expressions. In
 * particular it holds working state while the data flow graph is being
 * assembled.</p>
 */
public class Interpreter {
  private final Map<RelNode, NodeInfo> nodes = Maps.newHashMap();
  private static final Row DUMMY_ROW = new Row(null);

  /** Compiles an expression to an executable form. */
  public Scalar compile(final RexNode node) {
    return new Scalar() {
      public Object execute(Row row) {
        switch (node.getKind()) {
        case LITERAL:
          return ((RexLiteral) node).getValue();
        case INPUT_REF:
          return row.getObject(((RexInputRef) node).getIndex());
        default:
          throw new RuntimeException("unknown expression type " + node);
        }
      }
    };
  }

  public Source source(RelNode rel, int ordinal) {
    final RelNode input = rel.getInput(ordinal);
    final NodeInfo x = nodes.get(input);
    if (x == null) {
      throw new AssertionError("should be registered: " + rel);
    }
    return new SourceImpl(x.sink);
  }

  public Sink sink(RelNode rel) {
    final BlockingQueue<Row> queue = new ArrayBlockingQueue<Row>(1);
    final SinkImpl sink = new SinkImpl(queue);
    final NodeInfo nodeInfo = new NodeInfo(rel, sink);
    nodes.put(rel, nodeInfo);
    return sink;
  }

  /** Information about a node registered in the data flow graph. */
  private static class NodeInfo {
    RelNode rel;
    SinkImpl sink;

    public NodeInfo(RelNode rel, SinkImpl sink) {
      this.rel = rel;
      this.sink = sink;
    }
  }

  /** Implementation of {@link Sink} using a {@link BlockingQueue}. */
  private static class SinkImpl implements Sink {
    final BlockingQueue<Row> queue;

    private SinkImpl(BlockingQueue<Row> queue) {
      this.queue = queue;
    }

    public void send(Row row) throws InterruptedException {
      queue.put(row);
    }

    public void end() throws InterruptedException {
      queue.put(DUMMY_ROW);
    }
  }

  /** Implementation of {@link Source} using a {@link BlockingQueue}. */
  private static class SourceImpl implements Source {
    private final BlockingQueue<Row> queue;

    public SourceImpl(SinkImpl sink) {
      this.queue = sink.queue;
    }

    public Row receive() {
      return queue.remove();
    }
  }
}

// End Interpreter.java
