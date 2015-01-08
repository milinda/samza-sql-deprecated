/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.samza.sql.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.samza.config.Config;
import org.apache.samza.sql.api.data.RelationSpec;
import org.apache.samza.sql.api.data.StreamSpec;
import org.apache.samza.sql.api.operators.Operator;
import org.apache.samza.sql.api.operators.TupleOperator;
import org.apache.samza.sql.api.operators.routing.OperatorRoutingContext;
import org.apache.samza.sql.api.task.RuntimeSystemContext;
import org.apache.samza.sql.data.IncomingMessageTuple;
import org.apache.samza.sql.operators.factory.SimpleOperatorFactoryImpl;
import org.apache.samza.sql.operators.output.SystemStreamOp;
import org.apache.samza.sql.operators.output.SystemStreamSpec;
import org.apache.samza.sql.operators.partition.PartitionOp;
import org.apache.samza.sql.operators.partition.PartitionSpec;
import org.apache.samza.sql.operators.relation.Join;
import org.apache.samza.sql.operators.relation.JoinSpec;
import org.apache.samza.sql.operators.routing.AutoRoutingContext;
import org.apache.samza.sql.operators.stream.InsertStream;
import org.apache.samza.sql.operators.stream.InsertStreamSpec;
import org.apache.samza.sql.operators.window.BoundedTimeWindow;
import org.apache.samza.sql.operators.window.WindowSpec;
import org.apache.samza.sql.store.SqlContextManager;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.task.WindowableTask;


public class StreamSqlTask implements StreamTask, InitableTask, WindowableTask {

  private SqlContextManager initCntx;

  private OperatorRoutingContext rteCntx;

  @SuppressWarnings("unchecked")
  @Override
  public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator)
      throws Exception {
    // TODO Auto-generated method stub
    RuntimeSystemContext opCntx = new RuntimeSystemContext() {

      @Override
      public MessageCollector getMessageCollector() {
        // TODO Auto-generated method stub
        return collector;
      }

      @Override
      public TaskCoordinator getTaskCoordinator() {
        // TODO Auto-generated method stub
        return coordinator;
      }

    };

    this.rteCntx.setRuntimeContext(opCntx);

    IncomingMessageTuple ituple = new IncomingMessageTuple(envelope);
    for (Iterator<TupleOperator> iter = this.rteCntx.getSystemInputOps().iterator(ituple.getStreamSpec()); iter
        .hasNext();) {
      iter.next().process(ituple, this.rteCntx);
    }

    this.rteCntx.setRuntimeContext(null);

  }

  @SuppressWarnings("unchecked")
  @Override
  public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
    // TODO Auto-generated method stub
    RuntimeSystemContext opCntx = new RuntimeSystemContext() {
      @Override
      public MessageCollector getMessageCollector() {
        // TODO Auto-generated method stub
        return collector;
      }

      @Override
      public TaskCoordinator getTaskCoordinator() {
        // TODO Auto-generated method stub
        return coordinator;
      }

    };

    this.rteCntx.setRuntimeContext(opCntx);

    long currNano = System.nanoTime();
    for (Iterator<Operator> iter = this.rteCntx.getSystemInputOps().values().iterator(); iter.hasNext();) {
      iter.next().timeout(currNano, this.rteCntx);
    }

    this.rteCntx.setRuntimeContext(null);

  }

  @Override
  public void init(Config config, TaskContext context) throws Exception {
    // create all operators first
    // 1. create two window operators
    SimpleOperatorFactoryImpl operatorFactory = new SimpleOperatorFactoryImpl();
    WindowSpec spec1 = new WindowSpec("fixedWnd1", 10, new StreamSpec() {

      @Override
      public String getName() {
        // TODO Auto-generated method stub
        return "inputStream1";
      }

    }, new RelationSpec() {

      @Override
      public String getPrimaryKey() {
        // TODO Auto-generated method stub
        return "pkey";
      }

      @Override
      public String getName() {
        // TODO Auto-generated method stub
        return "fixedWndOutput1";
      }

    });
    WindowSpec spec2 = new WindowSpec("fixedWnd1", 10, new StreamSpec() {

      @Override
      public String getName() {
        // TODO Auto-generated method stub
        return "inputStream2";
      }

    }, new RelationSpec() {

      @Override
      public String getPrimaryKey() {
        // TODO Auto-generated method stub
        return "pkey";
      }

      @Override
      public String getName() {
        // TODO Auto-generated method stub
        return "fixedWndOutput2";
      }

    });
    BoundedTimeWindow wnd1 = (BoundedTimeWindow) operatorFactory.getTupleOperator(spec1);
    BoundedTimeWindow wnd2 = (BoundedTimeWindow) operatorFactory.getTupleOperator(spec2);
    // 2. create one join operator
    List<RelationSpec> inputRelations = new ArrayList<RelationSpec>();
    inputRelations.add(spec1.getWindowedRelationSpec());
    inputRelations.add(spec2.getWindowedRelationSpec());
    JoinSpec joinSpec = new JoinSpec("joinOp", inputRelations, new RelationSpec() {

      @Override
      public String getPrimaryKey() {
        // TODO Auto-generated method stub
        return "pkey";
      }

      @Override
      public String getName() {
        // TODO Auto-generated method stub
        return "joinOutput";
      }

    });
    Join join = (Join) operatorFactory.getRelationOperator(joinSpec);
    // 3. create one stream operator
    InsertStreamSpec istrmSpec = new InsertStreamSpec("istremOp", joinSpec.getOutputRelation(), new StreamSpec() {

      @Override
      public String getName() {
        // TODO Auto-generated method stub
        return "istrmOutput1";
      }

    });
    InsertStream istream = (InsertStream) operatorFactory.getRelationOperator(istrmSpec);
    // 4. create a re-partition operator
    PartitionSpec parSpec = new PartitionSpec("parOp1", istrmSpec.getOutputStream(), new StreamSpec() {

      @Override
      public String getName() {
        // TODO Auto-generated method stub
        return "parOutputStrm1";
      }

    });
    PartitionOp par = (PartitionOp) operatorFactory.getTupleOperator(parSpec);
    // 5. finally, the system stream output operator
    SystemStreamSpec sstrmSpec =
        new SystemStreamSpec("outputStream", parSpec.getOutputStream(), new SystemStream("kafka", "outputTopic1"));
    SystemStreamOp sysStream = (SystemStreamOp) operatorFactory.getTupleOperator(sstrmSpec);

    // Now initialize all operators
    this.initCntx = new SqlContextManager(context);
    wnd1.init(this.initCntx);
    wnd2.init(this.initCntx);
    join.init(this.initCntx);
    istream.init(this.initCntx);
    par.init(this.initCntx);
    sysStream.init(this.initCntx);

    // Finally, initialize the operator routing context
    this.rteCntx = new AutoRoutingContext();
    // 1. set two system input operators (i.e. two window operators)
    this.rteCntx.setSystemInputOperator(wnd1);
    this.rteCntx.setSystemInputOperator(wnd2);
    // 2. connect join operator to both window operators
    this.rteCntx.setNextRelationOperator(wnd1.getId(), join);
    this.rteCntx.setNextRelationOperator(wnd2.getId(), join);
    // 3. connect stream operator to the join operator
    this.rteCntx.setNextRelationOperator(join.getId(), istream);
    // 4. connect re-partition operator to the stream operator
    this.rteCntx.setNextTupleOperator(istream.getId(), par);
    // 5. finally, connect the system stream output operator to the re-partition operator
    this.rteCntx.setNextTupleOperator(par.getId(), sysStream);
  }
}
