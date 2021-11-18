package com.demo;

import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.server.CalciteServerStatement;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.validate.*;
import org.apache.calcite.tools.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class App
{
    public static void main( String[] args ) throws SQLException, SqlParseException, RelConversionException {
        Properties info = new Properties();
        info.put("lex", "mysql");
        String model = "/Users/pangshuyue/projects/CalciteDemo/data/model.json";
        info.put("model", model);

        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        CalciteServerStatement statement = connection.createStatement().unwrap(CalciteServerStatement.class);
        CalcitePrepare.Context prepareContext = statement.createPrepareContext();

        SqlParser.Config mysqlConfig = SqlParser.configBuilder().setLex(Lex.MYSQL).build();
        SqlParser parser = SqlParser.create("", mysqlConfig);

        String sql = "select name from tutorial.user_info where id = 1 order by id";
        SqlNode sqlNode = parser.parseQuery(sql);

        // 数据类型校验工厂类
        SqlTypeFactoryImpl factory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
        CalciteCatalogReader calciteCatalogReader = new CalciteCatalogReader(
                prepareContext.getRootSchema(),
                prepareContext.getDefaultSchemaPath(),
                factory,
                new CalciteConnectionConfigImpl(new Properties())
        );

        SqlValidator validator = SqlValidatorUtil.newValidator(SqlStdOperatorTable.instance(),
                calciteCatalogReader, factory);
        SqlNode validateSqlNode = validator.validate(sqlNode);
        SqlValidatorScope selectScope = validator.getSelectScope((SqlSelect) validateSqlNode);
        SqlValidatorNamespace namespace = validator.getNamespace(sqlNode);
        System.out.println(validateSqlNode);
        List<SqlMoniker> sqlMonikerList = new ArrayList<>();
        System.out.println(selectScope);
        for (SqlMoniker sqlMoniker : sqlMonikerList) {
            System.out.println(sqlMoniker.id());
        }

        System.out.println(namespace);
        System.out.println(namespace.fieldExists("nameCC"));




//        Statement statement1 = connection.createStatement();
//        ResultSet resultSet = statement1.executeQuery("select name from tutorial.user_info where id = 1 order by id");
//        while (resultSet.next()) {
//            System.out.println(resultSet.getString("name"));
//        }
    }
}
