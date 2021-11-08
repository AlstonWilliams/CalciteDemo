package com.demo;

import lombok.Data;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class DemoTable extends AbstractTable implements Serializable {

    private String name;
    private List<DemoColumn> columnList;

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        List<String> names = new ArrayList<>();
        List<RelDataType> types = new ArrayList<>();

        for (DemoColumn column : columnList) {
            names.add(column.getName());
            RelDataType sqlType = typeFactory.createSqlType(SqlTypeName.get(column.getType().toUpperCase()));
            types.add(sqlType);
        }

        return typeFactory.createStructType(Pair.zip(names, types));
    }
}
