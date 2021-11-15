package com.demo.cbo;

import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.rel.metadata.*;
import org.apache.calcite.util.BuiltInMethod;

public class DemoCumulativeCost implements MetadataHandler<BuiltInMetadata.CumulativeCost> {

    public static final RelMetadataProvider SOURCE =
            ReflectiveRelMetadataProvider.reflectiveSource(
                    BuiltInMethod.CUMULATIVE_COST.method, new DemoCumulativeCost());

    public DemoCumulativeCost() {

    }

    @Override
    public MetadataDef<BuiltInMetadata.CumulativeCost> getDef() {
        return BuiltInMetadata.CumulativeCost.DEF;
    }

    public RelOptCost getCumulativeCost(DemoTableScan tableScan, RelMetadataQuery mq) {
        return DemoCostAccumulator.INSTANCE.getAccumulative(tableScan, mq);
    }

    public RelOptCost getCumulativeCost(DemoProject demoProject, RelMetadataQuery mq) {
        return DemoCostAccumulator.INSTANCE.getAccumulative(demoProject, mq);
    }

    public RelOptCost getCumulativeCost(DemoFilter filter, RelMetadataQuery mq) {
        return DemoCostAccumulator.INSTANCE.getAccumulative(filter, mq);
    }
}
