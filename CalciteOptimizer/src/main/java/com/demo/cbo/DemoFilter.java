package com.demo.cbo;

import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;

public class DemoFilter extends Filter implements DemoRel {

    private RelOptCost cost;

    protected DemoFilter(RelOptCluster cluster, RelTraitSet traits, RelNode child, RexNode condition) {
        super(cluster, traits, child, condition);
    }

    @Override
    public Filter copy(RelTraitSet traitSet, RelNode input, RexNode condition) {
        return null;
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
        if (cost != null) return cost;

        cost = DemoRelMetadataQuery.INSTANCE.getCumulativeCost(this);
        return cost;
    }
}
