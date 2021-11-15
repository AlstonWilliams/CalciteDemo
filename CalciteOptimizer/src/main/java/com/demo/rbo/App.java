package com.demo.rbo;

import org.apache.calcite.config.Lex;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.rel2sql.SqlImplementor;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws SqlParseException, ValidationException, RelConversionException {
        Planner planner = getPlanner();
        SqlNode sqlNode = planner.parse("SELECT DATE_CD,SUM(IB0002001_CN000) FROM \n" +
                "(SELECT IDX_ID,CUBE2L_IB00040010_CN000.DATE_CD,SUM(CUBE2L_IB00040010_CN000.IDX_VAL) AS IB0002001_CN000 FROM " +
                "CUBE2L_IB00040010_CN000" +
                " GROUP BY  CUBE2L_IB00040010_CN000.DATE_CD,IDX_ID) IB0002001_CN000  WHERE IDX_ID IN ('IB0002001_CN000') AND DATE_CD = '2020-05-31' GROUP BY DATE_CD ");

        System.out.println(sqlNode.toString());

        planner.validate(sqlNode);
        RelRoot relRoot = planner.rel(sqlNode);

        RelNode relNode = relRoot.project();
        System.out.println(RelOptUtil.toString(relNode));

        HepProgramBuilder builder = new HepProgramBuilder();
        builder.addRuleInstance(CoreRules.AGGREGATE_REMOVE);
//        builder.addRuleInstance(CoreRules.PROJECT_FILTER_TRANSPOSE);
        builder.addRuleInstance(CoreRules.FILTER_PROJECT_TRANSPOSE);
        HepPlanner hepPlanner = new HepPlanner(builder.build());
        hepPlanner.setRoot(relNode);
        relNode = hepPlanner.findBestExp();

        System.out.println(RelOptUtil.toString(relNode));

        RelToSqlConverter relToSqlConverter = new RelToSqlConverter(MysqlSqlDialect.DEFAULT);
        SqlImplementor.Result result = relToSqlConverter.visitRoot(relNode);

        System.out.println(result.asSelect().toString());

    }

    private static Planner getPlanner() {
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);

        rootSchema.add("CUBE2L_IB00040010_CN000", new AbstractTable() {
            @Override
            public RelDataType getRowType(RelDataTypeFactory typeFactory) {

                RelDataTypeFactory.Builder builder = typeFactory.builder();
                builder.add("DATE_CD", typeFactory.createTypeWithNullability(typeFactory.createSqlType(SqlTypeName.DATE), true));
                builder.add("IDX_VAL", typeFactory.createTypeWithNullability(typeFactory.createSqlType(SqlTypeName.BIGINT), true));
                builder.add("test", typeFactory.createTypeWithNullability(typeFactory.createSqlType(SqlTypeName.BIGINT), true));
                builder.add("IDX_ID", typeFactory.createTypeWithNullability(typeFactory.createSqlType(SqlTypeName.VARCHAR), true));

                return builder.build();

            }
        });

        SqlParser.ConfigBuilder parserConfig = SqlParser.configBuilder();
        SqlParser.Config build = parserConfig.setCaseSensitive(false).setLex(Lex.MYSQL).build();

        FrameworkConfig frameworkConfig = Frameworks.newConfigBuilder()
                .parserConfig(build)
                .defaultSchema(rootSchema)
                .build();

        Planner planner = Frameworks.getPlanner(frameworkConfig);

        return planner;

    }
}
