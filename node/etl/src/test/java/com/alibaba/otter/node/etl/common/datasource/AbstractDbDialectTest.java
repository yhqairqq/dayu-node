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

import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
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
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * 类TestAbstractDbDialect.java的实现描述：TODO 类实现描述
 *
 * @author xiaoqing.zhouxq 2011-12-9 下午3:03:55
 *
 * mybatis 数据插入例子
 */
public class AbstractDbDialectTest {

     private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
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
        DataSource dataSource0 = createDataSource("jdbc:mysql://10.111.14.140:3307/otter_sink", "re_write", "IXAaaWWiZhn1Nces",
                "com.mysql.jdbc.Driver", DataMediaType.MYSQL, "utf-8");
//        DataSource dataSource1 = createDataSource("jdbc:mysql://172.16.10.68:3306/aky1", "root", "123456",
//                "com.mysql.jdbc.Driver", DataMediaType.MYSQL, "utf-8");
        JdbcTemplate jdbcTemplate0 = new JdbcTemplate(dataSource0);
        String sql0 = "insert into `otter_sink`.`source_a` (  `name`, `city_id`,`f4`, `f5`) values ( ?, ?, ?,?)";
//        ExecutorService executorService = Executors.newFixedThreadPool(4);
        final long[] i = {0};
        executorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                jdbcTemplate0.update(sql0,
                        "name"+i[0]++,
                        i[0],
                        "f4"+i[0],
                        i[0]
                );
            }
        }, 30, 30, TimeUnit.SECONDS);
//        int threadSize = 2;
//        int recordSize = 100000;
//        int unitRecordSize = recordSize/2;
//        for(int i=0;i<threadSize;i++){
//             int finalI = i;
//            executorService.submit(new Runnable() {
//                 @Override
//                 public void run() {
//                     for(int start = finalI *unitRecordSize;start < unitRecordSize*(finalI+1);start++){
//                         // ( `f4`, `name`, `city_id`, `f5`)
//                         jdbcTemplate0.update(sql0,
//                                 "name"+start,
//                                 "name"+start,
//                                 start,
//                                 start,
//                                 start
//                         );
//
////                         System.out.println(Thread.currentThread().getName()+":"+start);
//                     }
//                 }
//             });
//        }
//        System.out.println(jdbcTemplate0);

//        System.out.println(jdbcTemplate1);
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
