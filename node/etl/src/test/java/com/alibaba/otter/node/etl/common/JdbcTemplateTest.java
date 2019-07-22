package com.alibaba.otter.node.etl.common;

import com.alibaba.otter.shared.common.model.config.data.DataMediaType;
import com.alibaba.otter.shared.common.utils.meta.DdlUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Table;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huahua on 2019/7/22.
 */
public class JdbcTemplateTest {

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

    public static void main(String[] args) {
        String url = "rm-bp10p2vl5x3c4mum5160.mysql.rds.aliyuncs.com";
        String username = "caicai";
        String passowrd = "123!@#qwe";
        DataSource dataSource0 = createDataSource("jdbc:mysql://10.111.14.140:3307/otter_sink", "re_write", "IXAaaWWiZhn1Nces",
                "com.mysql.jdbc.Driver", DataMediaType.MYSQL, "utf-8");
//        DataSource dataSource1 = createDataSource("jdbc:mysql://172.16.10.68:3306/aky1", "root", "123456",
//                "com.mysql.jdbc.Driver", DataMediaType.MYSQL, "utf-8");
        JdbcTemplate jdbcTemplate0 = new JdbcTemplate(dataSource0);
        String sql0 = "insert into `otter_sink`.`source_a` ( `f4`, `name`, `city_id`, `f5`) values ( ?, ?, ?, ?)";
        ExecutorService executorService = Executors.newFixedThreadPool(4);


    }
}
