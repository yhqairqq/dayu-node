/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.node.etl.common.datasource;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.Test;

import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.utils.meta.DdlUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 类TestAbstractDbDialect.java的实现描述：TODO 类实现描述
 *
 * @author xiaoqing.zhouxq 2011-12-9 下午3:03:55
 */
public class AbstractDbDialectTest {

    @Test
    public void testFindTable() throws Exception {
        DataSource dataSource = createDataSource("jdbc:oracle:thin:@127.0.0.1:1521:OINTEST", "otter1", "jonathan",
                                                 "oracle.jdbc.OracleDriver", DataMediaType.ORACLE, "utf-8");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Table table = DdlUtils.findTable(jdbcTemplate, "otter1".toUpperCase(), "otter1".toUpperCase(),
                                         "wytable3".toUpperCase());
        System.out.println("the tablename = " + table.getSchema() + "." + table.getName());
        Column[] columns = table.getColumns();
        for (Column column : columns) {
            System.out.println("columnName = " + column.getName() + ",columnType = " + column.getTypeCode()
                               + ",isPrimary = " + column.isPrimaryKey() + ",nullable = " + column.isRequired());
        }

    }

    public static void main(String[] args) {
        DataSource dataSource0 = createDataSource("jdbc:mysql://172.16.10.68:3306/aky0", "root", "123456",
                "com.mysql.jdbc.Driver", DataMediaType.MYSQL, "utf-8");
        DataSource dataSource1 = createDataSource("jdbc:mysql://172.16.10.68:3306/aky1", "root", "123456",
                "com.mysql.jdbc.Driver", DataMediaType.MYSQL, "utf-8");
        JdbcTemplate jdbcTemplate0 = new JdbcTemplate(dataSource0);
        String sql0 = "insert into `aky0`.`meta_table_config0` ( `status`, `modify_time`, `catlog`, `db_type`, `gmt_time`, `table_name`, `ip`, `schema_name`) values ( ?,?, ?, ?, ?, ?, ?, ?);";
        MetaDataConfig metaDataConfig0 = new MetaDataConfig();
        metaDataConfig0.setStatus(0);
        metaDataConfig0.setGmtTime(new Date());
        metaDataConfig0.setModifyTime(new Date());
        metaDataConfig0.setCatlog("0");
        metaDataConfig0.setDbType("mysql");
        metaDataConfig0.setIp("172.16.10.68");
        metaDataConfig0.setSchemaName("aky");
        metaDataConfig0.setTableName("metadata");
        jdbcTemplate0.update(sql0,metaDataConfig0.getStatus(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                metaDataConfig0.getCatlog(),
                metaDataConfig0.getDbType(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                metaDataConfig0.getTableName(),
                metaDataConfig0.getIp(),
                metaDataConfig0.getSchemaName()
                );

        MetaDataConfig metaDataConfig1 = new MetaDataConfig();
        metaDataConfig1.setStatus(1);
        metaDataConfig1.setGmtTime(new Date());
        metaDataConfig1.setModifyTime(new Date());
        metaDataConfig1.setCatlog("1");
        metaDataConfig1.setDbType("mysql");
        metaDataConfig1.setIp("172.16.10.68");
        metaDataConfig1.setSchemaName("aky");
        metaDataConfig1.setTableName("metadata");
        JdbcTemplate jdbcTemplate1 = new JdbcTemplate(dataSource1);
        for(int i=0;i<10;i++){
            jdbcTemplate0.update(sql0,metaDataConfig0.getStatus(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    metaDataConfig0.getCatlog(),
                    metaDataConfig0.getDbType(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    metaDataConfig0.getTableName(),
                    metaDataConfig0.getIp(),
                    metaDataConfig0.getSchemaName()
            );

            jdbcTemplate1.update(sql0,metaDataConfig1.getStatus(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    metaDataConfig1.getCatlog(),
                    metaDataConfig1.getDbType(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    metaDataConfig1.getTableName(),
                    metaDataConfig1.getIp(),
                    metaDataConfig1.getSchemaName()
            );
        }



        System.out.println(jdbcTemplate0);

        System.out.println(jdbcTemplate1);
    }

    private static DataSource createDataSource(String url, String userName, String password, String driverClassName,
                                               DataMediaType dataMediaType, String encoding) {
        BasicDataSource dbcpDs = new BasicDataSource();

        dbcpDs.setRemoveAbandoned(true);
        dbcpDs.setLogAbandoned(true);
        dbcpDs.setTestOnBorrow(true);
        dbcpDs.setTestWhileIdle(true);

        // 动态的参数
        dbcpDs.setDriverClassName(driverClassName);
        dbcpDs.setUrl(url);
        dbcpDs.setUsername(userName);
        dbcpDs.setPassword(password);

        if (dataMediaType.isOracle()) {
            dbcpDs.addConnectionProperty("restrictGetTables", "true");
            dbcpDs.setValidationQuery("select 1 from dual");
        } else if (dataMediaType.isMysql()) {
            // open the batch mode for mysql since 5.1.8
            dbcpDs.addConnectionProperty("useServerPrepStmts", "true");
            dbcpDs.addConnectionProperty("rewriteBatchedStatements", "true");
            if (StringUtils.isNotEmpty(encoding)) {
                dbcpDs.addConnectionProperty("characterEncoding", encoding);
            }
            dbcpDs.setValidationQuery("select 1");
        }

        return dbcpDs;
    }
}
