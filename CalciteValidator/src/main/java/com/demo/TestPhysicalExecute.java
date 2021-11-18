package com.demo;

import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.server.CalciteServerStatement;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.*;

import java.sql.*;
import java.util.Properties;

public class TestPhysicalExecute {
    public static void main(String[] args) throws SQLException, SqlParseException, ValidationException, RelConversionException {
        Properties info = new Properties();
        info.put("lex", "mysql");
        String model = "/Users/pangshuyue/projects/CalciteDemo/data/model.json";
        info.put("model", model);

        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        CalciteServerStatement statement = connection.createStatement().unwrap(CalciteServerStatement.class);
        CalcitePrepare.Context prepareContext = statement.createPrepareContext();
        SqlParser.Config mysqlConfig = SqlParser.configBuilder().setLex(Lex.MYSQL).build();

        FrameworkConfig frameworkConfig = Frameworks.newConfigBuilder()
                .parserConfig(mysqlConfig)
                .defaultSchema(prepareContext.getDataContext().getRootSchema())
                .build();
        Planner planner = Frameworks.getPlanner(frameworkConfig);
        SqlNode sqlNode = planner.parse("select name from tutorial.user_info where id = 1 order by id");
        SqlNode validateSqlNode = planner.validate(sqlNode);
        RelRoot relRoot = planner.rel(validateSqlNode);
        RelNode relNode = relRoot.project();

        PreparedStatement run = RelRunners.run(relNode);
        ResultSet resultSet = run.executeQuery();
        System.out.println("Result:");
        while (resultSet.next()) {
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                System.out.print(resultSet.getObject(i)+",");
            }
            System.out.println();
        }

    }
}
