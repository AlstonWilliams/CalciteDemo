package com.demo.cbo;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;

public interface DemoRel extends RelNode {

    Convention CONVENTION = new Convention.Impl("Demo", DemoRel.class);

}
