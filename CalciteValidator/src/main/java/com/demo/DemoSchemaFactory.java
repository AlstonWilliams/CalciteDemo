package com.demo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoSchemaFactory implements SchemaFactory {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true);

    @Override
    public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {
        try {
            List<DemoTable> tableList = new ArrayList<>();

            ArrayList tables = (ArrayList) operand.get("tables");

            for (Object table : tables) {
                String ddl = (String) ((HashMap)table).get("ddl");
                DemoTable demoTable = JSON_MAPPER.readValue(new File(ddl), DemoTable.class);
                tableList.add(demoTable);
            }

            return new DemoTableSchema(name, tableList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
