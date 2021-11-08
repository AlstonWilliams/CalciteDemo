package com.demo;

import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class DemoTableSchema extends AbstractSchema implements Serializable {

    private String name;
    private List<DemoTable> tableList;

    public DemoTableSchema(String name, List<DemoTable> tableList) {
        this.name = name;
        this.tableList = tableList;
    }

    @Override
    protected Map<String, Table> getTableMap() {
        Map<String, Table> tableMap = Maps.newHashMap();
        for (DemoTable table : tableList) {
            tableMap.put(table.getName(), table);
        }

        return tableMap;
    }
}
