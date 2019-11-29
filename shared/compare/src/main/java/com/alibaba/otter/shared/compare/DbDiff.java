//package com.alibaba.otter.shared.compare;
//
//import com.alibaba.otter.canal.parse.driver.mysql.packets.server.FieldPacket;
//import com.alibaba.otter.canal.parse.driver.mysql.packets.server.ResultSetPacket;
//import com.alibaba.otter.canal.parse.inbound.mysql.MysqlConnection;
//import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
//import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
//import com.alibaba.otter.shared.common.model.config.data.DataMedia;
//import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
//import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
//import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
//import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
//import com.alibaba.otter.shared.compare.model.IndexTable;
//
//import javax.sql.DataSource;
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
///**
// * Created by huaseng on 2019/8/5.
// */
//public class DbDiff {
//    private String startTime;
//    private String endTime;
//    private ExecutorService executorService = Executors.newCachedThreadPool(new NamedThreadFactory("DbDiff"));
//    private Map<Long, Map<String, IndexTable>> indexTables;
//    private static final String ESCAPE = "`";
//    /**
//     * 生成计算地图
//     */
//    public void compare(Pipeline pipeline) {
//
//        List<DataMediaPair> dataMediaPairs = pipeline.getPairs();
//        List<Future> resultList = new ArrayList<>();
//        for (DataMediaPair dataMediaPair : dataMediaPairs) {
//            worker(dataMediaPair);
//        }
//    }
//
//    public Exception worker(DataMediaPair dataMediaPair) {
//        DataMedia source = dataMediaPair.getSource();
//        DataMedia target = dataMediaPair.getTarget();
//        String schema = source.getNamespace();
//        String targetSchema = target.getNamespace();
//
//        DataMedia.ModeValue modeValue = source.getNameMode();
//        DataMedia.ModeValue targetModelValue = target.getNameMode();
//        try {
//            if (modeValue.getMode().isSingle()) {
//                String table = modeValue.getSingleValue();
//                if (targetModelValue.getMode().isWildCard()) {
//                    String targetTable = table;
//                    compareRecordCount(schema, table, source, targetSchema, targetTable, target);
//                } else {
//                }
//            } else if (modeValue.getMode().isMulti()) {
//                List<String> tables = modeValue.getMultiValue();
//                for (String table : tables) {
//                    if (targetModelValue.getMode().isWildCard()) {
//                        String targetTable = table;
//                        compareRecordCount(schema, table, source, targetSchema, targetTable, target);
//                    } else {
//                    }
//                }
//            }
//        } catch (Exception e) {
//
//        }
//        return null;
//    }
//
//    public Exception compareRecordCount(String sschema, String stable, DataMedia source,
//                                        String targetSchema, String targetTable, DataMedia target
//    ) {
//        Exception result = null;
//        try {
//            int scount = select(source, sschema, stable);
//            int tcount = select(target, targetSchema, targetTable);
//            if (scount != tcount) {
//                return new Exception(String.format("table[%s] records not same with table[%s]", getFullName(sschema, stable), getFullName(targetSchema, targetTable)));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            result = e;
//        }
//        return result;
//
//    }
//
//    private int select(DataMedia source, String schema, String table) throws Exception {
//        DbDialect dbDialect = dbDialectFactory.getDbDialect(pipeline.getId(), (DbMediaSource) source.getSource());
//        DataSource dataSource = dataSourceCreator.createDataSource(dataMedia.getSource());
//        IndexTable indexTable = findIndexTable(source, schema, table);
//        String timeColumn = null;
//        String primaryKey = null;
//        // update_time的索引
//        if (indexTable.isIndexField("update_time")) {
//            timeColumn = "update_time";
//        } else if (indexTable.isIndexField("create_time")) {
//            timeColumn = "create_time";
//        }
//        if (timeColumn == null) {
//            throw new Exception(String.format("no time index in %s %s", schema, table));
//        }
//        //获取主键名称
//        primaryKey = indexTable.getPrimaryKeyFieldName();
//        if (primaryKey == null) {
//            throw new Exception(String.format("no primary in %s %s", schema, table));
//        }
//        //查询当天的记录
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
//        Calendar now = Calendar.getInstance();
//        Date today = now.getTime();
//        String startTime = format.format(today);
//        now.add(Calendar.DAY_OF_YEAR, 1);
//        Date tomorrow = now.getTime();
//        String endTime = format.format(tomorrow);
//
//        String sql = getSql(primaryKey, timeColumn, schema, table);
//        return dbDialect.getJdbcTemplate().queryForInt(sql, startTime, endTime);
//    }
//
//    private String appendEscape(String columnName) {
//        return ESCAPE + columnName + ESCAPE;
//    }
//
//    private String getSql(String primaryKeyField, String timeFields, String schema, String table) {
//        StringBuilder sql = new StringBuilder("select ");
//        sql.append(" count(").append(appendEscape(primaryKeyField)).append(") ");
//        sql.append(" from ").append(getFullName(schema, table)).append(" where (");
//        sql.append(appendEscape(timeFields)).append(" between ").append("? ").append("and ").append("? ").append(" )");
//        return sql.toString().intern();
//    }
//
//    private IndexTable findIndexTable(DataMedia source, String schema, String table) throws IOException {
//        DbMediaSource dbMediaSource = ((DbMediaSource) source.getSource());
//        String[] arr = ((DbMediaSource) source.getSource()).getUrl().split(":");
//        InetAddress inetAddress = InetAddress.getByName(arr[0]);
//        MysqlConnection mysqlConnection = null;
//        try {
//            InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, Integer.parseInt(arr[1]));
//            mysqlConnection = new MysqlConnection(inetSocketAddress, dbMediaSource.getUsername(), dbMediaSource.getPassword());
//            mysqlConnection.connect();
//            ResultSetPacket packet = mysqlConnection.query("show index from " + getFullName(schema, table));
//            return parseTableMetaByDesc(packet, schema, table);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            mysqlConnection.disconnect();
//        } finally {
//            mysqlConnection.disconnect();
//        }
//        return null;
//
//    }
//
//    private IndexTable parseTableMetaByDesc(ResultSetPacket packet, String schema, String table) {
//        IndexTable indexTable = new IndexTable();
//        int cols = packet.getFieldDescriptors().size();
//        List<String> columns = new ArrayList<>();
//        for (FieldPacket fieldPacket : packet.getFieldDescriptors()) {
//            columns.add(fieldPacket.getName());
//        }
//        int index = 0;
//        List<String> values = null;
//        List<List<String>> data = new ArrayList<>();
//        for (String value : packet.getFieldValues()) {
//            if (index % cols == 0) {
//                values = new ArrayList<>();
//                data.add(values);
//            }
//            values.add(value);
//            index++;
//
//        }
//        indexTable.setColumns(columns);
//        indexTable.setDatas(data);
//        indexTable.setSchema(schema);
//        indexTable.setTable(table);
//        return indexTable;
//    }
//
//
//    private String getFullName(String schema, String table) {
//        StringBuilder builder = new StringBuilder();
//        return builder.append('`')
//                .append(schema)
//                .append('`')
//                .append('.')
//                .append('`')
//                .append(table)
//                .append('`')
//                .toString();
//    }
//
//
//    public Pipeline getPipeline() {
//        return pipeline;
//    }
//
//    public void setPipeline(Pipeline pipeline) {
//        this.pipeline = pipeline;
//    }
//
//
//    public static void main(String[] args) throws IOException {
//        InetAddress inetAddress = InetAddress.getByName("rm-bp10p2vl5x3c4mum5160.mysql.rds.aliyuncs.com");
//        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 3306);
//        MysqlConnection mysqlConnection = new MysqlConnection(inetSocketAddress, "caicai", "123!@#qwe");
//        try {
//            mysqlConnection.connect();
//            ResultSetPacket packet = mysqlConnection.query("show index from caicai.tbl_order");
//            int cols = packet.getFieldDescriptors().size();
//            List<String> columns = new ArrayList<>();
//            for (FieldPacket fieldPacket : packet.getFieldDescriptors()) {
//                columns.add(fieldPacket.getName());
//            }
//            int index = 0;
//            List<String> values = null;
//            List<List<String>> arr = new ArrayList<>();
//            for (String value : packet.getFieldValues()) {
//                if (index % cols == 0) {
//                    values = new ArrayList<>();
//                    arr.add(values);
//                }
//                values.add(value);
//                index++;
//
//            }
//            System.out.println(arr);
//        } catch (Exception e) {
//
//        } finally {
//            mysqlConnection.disconnect();
//        }
//    }
//
//
//    public DbDialectFactory getDbDialectFactory() {
//        return dbDialectFactory;
//    }
//
//    public void setDbDialectFactory(DbDialectFactory dbDialectFactory) {
//        this.dbDialectFactory = dbDialectFactory;
//    }
//}
