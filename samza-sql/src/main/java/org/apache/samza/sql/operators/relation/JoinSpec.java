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

package org.apache.samza.sql.operators.relation;

import java.util.ArrayList;
import java.util.List;

import org.apache.samza.sql.api.data.RelationSpec;
import org.apache.samza.sql.api.operators.spec.RelationOperatorSpec;


public class JoinSpec implements RelationOperatorSpec {
  private final String id;
  private final List<RelationSpec> inputs = new ArrayList<RelationSpec>();
  private final RelationSpec output;

  public JoinSpec(String id, List<RelationSpec> inputs, RelationSpec output) {
    this.id = id;
    this.inputs.addAll(inputs);
    this.output = output;
  }

  @Override
  public List<RelationSpec> getInputSpecs() {
    // TODO Auto-generated method stub
    return this.inputs;
  }

  @Override
  public String getId() {
    // TODO Auto-generated method stub
    return this.id;
  }

  public RelationSpec getOutputRelation() {
    return this.output;
  }
}
