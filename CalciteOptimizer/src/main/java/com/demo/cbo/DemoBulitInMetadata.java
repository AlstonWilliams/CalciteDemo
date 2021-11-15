package com.demo.cbo;

import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.metadata.*;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.util.BuiltInMethod;

public class DemoBulitInMetadata extends BuiltInMetadata {

    public interface TotalCost extends Metadata {

        MetadataDef<DemoBulitInMetadata.TotalCost> DEF = MetadataDef.of(
                DemoBulitInMetadata.TotalCost.class,
                DemoBulitInMetadata.TotalCost.Handler.class,
                BuiltInMethod.SELECTIVITY.method
        );

        RelOptCost getTotalCost(RexNode predicate);

        interface Handler extends MetadataHandler<DemoBulitInMetadata.TotalCost> {
            RelOptCost getTotalCost(RelNode r, RelMetadataQuery mq);
        }

    }

}
