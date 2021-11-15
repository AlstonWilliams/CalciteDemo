package com.demo.cbo;

import org.apache.calcite.plan.*;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.metadata.RelMetadataQuery;

public class DemoTableScan extends TableScan implements DemoRel {

    private RelOptCost cost;

    protected DemoTableScan(RelOptCluster cluster, RelTraitSet traitSet, RelOptTable table) {
        super(cluster, traitSet, table);
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
        if (cost != null) return cost;

        cost = DemoRelMetadataQuery.INSTANCE.getCumulativeCost(this);
        return cost;
    }
}
