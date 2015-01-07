package org.apache.samza.sql.operators.relation;

import java.util.List;

import org.apache.samza.sql.api.data.Relation;
import org.apache.samza.sql.api.data.RelationSpec;
import org.apache.samza.sql.api.operators.RelationOperator;
import org.apache.samza.sql.api.operators.routing.OperatorRoutingContext;
import org.apache.samza.sql.api.task.InitSystemContext;


public class Join implements RelationOperator {

  private final JoinSpec spec;

  private List<Relation> inputs = null;

  private Relation output = null;

  public Join(JoinSpec spec) {
    // TODO Auto-generated constructor stub
    this.spec = spec;
  }

  private boolean hasPendingChanges() {
    // TODO Auto-generated method stub
    return getPendingChanges() != null;
  }

  private Relation getPendingChanges() {
    // TODO Auto-generated method stub
    // return any pending changes that have not been processed yet
    return null;
  }

  private Relation getOutputChanges() {
    // TODO Auto-generated method stub
    return null;
  }

  private boolean hasOutputChanges() {
    // TODO Auto-generated method stub
    return getOutputChanges() != null;
  }

  private void join(Relation deltaRelation) {
    // TODO Auto-generated method stub
    // implement the join logic
    // 1. calculate the delta changes in <code>output</code>
    // 2. check output condition to see whether the current input should trigger an output
    // 3. set the output changes and pending changes
  }

  @Override
  public void init(InitSystemContext initContext) throws Exception {
    // TODO Auto-generated method stub
    for (RelationSpec relationSpec : this.spec.getInputSpecs()) {
      inputs.add(initContext.getRelation(relationSpec));
    }
    this.output = initContext.getRelation(this.spec.getJoinRelation());
  }

  @Override
  public void timeout(long currentSystemNano, OperatorRoutingContext context) throws Exception {
    // TODO Auto-generated method stub
    if (hasPendingChanges()) {
      context.sendToNextRelationOperator(this.spec.getId(), getPendingChanges());
    }
    context.sendToNextTimeoutOperator(this.spec.getId(), currentSystemNano);
  }

  @Override
  public void process(Relation deltaRelation, OperatorRoutingContext context) throws Exception {
    // TODO Auto-generated method stub
    // calculate join based on the input <code>deltaRelation</code>
    join(deltaRelation);
    if (hasOutputChanges()) {
      context.sendToNextRelationOperator(this.spec.getId(), getOutputChanges());
    }
  }

  @Override
  public String getId() {
    // TODO Auto-generated method stub
    return this.spec.getId();
  }

  @Override
  public List<RelationSpec> getInputRelations() {
    // TODO Auto-generated method stub
    return this.spec.getInputSpecs();
  }
}
