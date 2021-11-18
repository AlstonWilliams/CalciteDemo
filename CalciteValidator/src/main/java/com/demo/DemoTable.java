package com.demo;

import lombok.Data;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class DemoTable extends AbstractTable implements Serializable, ScannableTable {

    private String name;
    private List<DemoColumn> columnList;
    private String dataFile;

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

    @Override
    public Enumerable<Object[]> scan(DataContext dataContext) {
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new DemoEnumerator<>(dataFile);
            }
        };
    }
}
