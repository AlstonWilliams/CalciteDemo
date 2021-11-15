package com.demo.cbo;

import com.fasterxml.jackson.core.PrettyPrinter;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.*;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelDataTypeSystemImpl;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.BasicSqlType;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.util.ChainedSqlOperatorTable;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql.validate.SqlValidatorUtil;
import org.apache.calcite.sql2rel.RelDecorrelator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;

import java.util.List;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        try {
            SchemaPlus rootSchema = Frameworks.createRootSchema(true);
            rootSchema.add("USERS", new AbstractTable() {
                public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
                    RelDataTypeFactory.FieldInfoBuilder builder = typeFactory.builder();
                    builder.add("ID", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.INTEGER));
                    builder.add("NAME", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.CHAR));

                    builder.add("TIME_D", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.DATE));
                    return builder.build();
                }
            });

            rootSchema.add("SCORE", new AbstractTable() {
                public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
                    RelDataTypeFactory.FieldInfoBuilder builder = typeFactory.builder();
                    builder.add("ID", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.INTEGER));
                    builder.add("SCORE", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.INTEGER));
                    return builder.build();
                }
            });


            rootSchema.add("TABLE_RESULT", new AbstractTable() {
                public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
                    RelDataTypeFactory.FieldInfoBuilder builder = typeFactory.builder();
                    builder.add("ID", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.INTEGER));
                    builder.add("NAME", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.CHAR));
                    builder.add("SCORE", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.INTEGER));
                    return builder.build();
                }
            });


            rootSchema.add("FINAL_RESULT", new AbstractTable() {
                public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
                    RelDataTypeFactory.FieldInfoBuilder builder = typeFactory.builder();
                    builder.add("ID", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.INTEGER));
                    builder.add("SOURCE_DS", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.VARCHAR));
                    builder.add("DS", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.VARCHAR));
                    return builder.build();
                }
            });

            rootSchema.add("MY_SOURCE", new AbstractTable() {
                public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
                    RelDataTypeFactory.FieldInfoBuilder builder = typeFactory.builder();
                    builder.add("ID", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.INTEGER));
                    builder.add("SOURCE_DS", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.VARCHAR));
                    builder.add("DS", new BasicSqlType(new RelDataTypeSystemImpl() {
                    }, SqlTypeName.VARCHAR));
                    return builder.build();
                }
            });

            FrameworkConfig config = Frameworks.newConfigBuilder()
                    .parserConfig(SqlParser.Config.DEFAULT)
                    .defaultSchema(rootSchema)
                    .traitDefs(ConventionTraitDef.INSTANCE, RelDistributionTraitDef.INSTANCE)
                    .build();

            String sql = "select id, time_d from users where id between id * 5 and id * id";

            RelNode relNode = sqlToRelNode((SchemaPlus)rootSchema, config, sql);

            HepProgramBuilder builder = new HepProgramBuilder();
            builder.addRuleInstance(DemoTableScanConverter.INSTANCE);
            HepPlanner hepPlanner = new HepPlanner(builder.build());
            hepPlanner.addRule(DemoTableScanConverter.INSTANCE);
            hepPlanner.setRoot(relNode);
            relNode = hepPlanner.findBestExp();

            System.out.println("-----------------------------------------------------------");
            System.out.println(RelOptUtil.toString(relNode));
            System.out.println("-----------------------------------------------------------");

            RelOptCluster cluster = relNode.getCluster();
            RelOptPlanner planner = cluster.getPlanner();

            RelTraitSet desiredTraits = cluster.traitSetOf(DemoRel.CONVENTION);
            if (!relNode.getTraitSet().equals(desiredTraits)) {
                relNode = cluster.getPlanner().changeTraits(relNode, desiredTraits);
            }

            //todo add some rule to Volcano
            planner.setRoot(relNode);
            RelNode finalNode = planner.findBestExp();

            System.out.println(RelOptUtil.toString(finalNode, SqlExplainLevel.ALL_ATTRIBUTES));
            RelNode tmp = finalNode;
            System.out.println(tmp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static RelNode sqlToRelNode(SchemaPlus rootSchema, FrameworkConfig config, String sql) {

        try {
            SqlTypeFactoryImpl factory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
            SqlParser parser = SqlParser.create(sql, config.getParserConfig());
            SqlNode sqlNode = parser.parseStmt();

            CalciteCatalogReader calciteCatalogReader = new CalciteCatalogReader(
                    CalciteSchema.from(rootSchema(rootSchema)),
                    CalciteSchema.from(config.getDefaultSchema()).path(null),
                    factory,
                    new CalciteConnectionConfigImpl(new Properties())
            );

            SqlValidator validator = SqlValidatorUtil.newValidator(
                    config.getOperatorTable(),
                    calciteCatalogReader,
                    new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT)
            );

            SqlNode validateSqlNode= validator.validate(sqlNode);

            final RexBuilder rexBuilder = createRexBuilder(factory);
            DemoVolcanoPlanner volcanoPlanner = new DemoVolcanoPlanner();
            volcanoPlanner.clearRelTraitDefs();

            for (RelTraitDef def : config.getTraitDefs()) {
                volcanoPlanner.addRelTraitDef(def);
            }

            final RelOptCluster cluster = RelOptCluster.create(volcanoPlanner, rexBuilder);
            cluster.setMetadataProvider(DemoRelMetaDataProvider.INSTANCE);
            volcanoPlanner.addRule(DemoFilterConverter.INSTANCE);
            volcanoPlanner.addRule(DemoProjectConverter.INSTANCE);
            volcanoPlanner.addRule(DemoTableScanConverter.INSTANCE);

            final SqlToRelConverter.Config config1 = SqlToRelConverter.configBuilder()
                    .withConfig(config.getSqlToRelConverterConfig())
                    .withTrimUnusedFields(false)
                    .build();

            final SqlToRelConverter sqlToRelConverter = new SqlToRelConverter(new DemoView(),
                    validator, calciteCatalogReader, cluster,
                    config.getConvertletTable(), config1);

            RelRoot root = sqlToRelConverter.convertQuery(validateSqlNode, false, true);

            root = root.withRel(sqlToRelConverter.flattenTypes(root.rel, true));

            final RelBuilder relBuilder = config1.getRelBuilderFactory().create(cluster, null);

            root = root.withRel(
                    RelDecorrelator.decorrelateQuery(root.rel, relBuilder)
            );

            return root.rel;

        } catch (SqlParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static RexBuilder createRexBuilder(SqlTypeFactoryImpl factory) {
        return new RexBuilder(factory);
    }

    private static SchemaPlus rootSchema(SchemaPlus rootSchema) {
        for (;;) {
            if (rootSchema.getParentSchema() == null) {
                return rootSchema;
            }
            rootSchema = rootSchema.getParentSchema();
        }
    }

    private static class DemoView implements RelOptTable.ViewExpander {
        @Override
        public RelRoot expandView(RelDataType rowType, String queryString, List<String> schemaPath, List<String> viewPath) {
            return null;
        }
    }
}
