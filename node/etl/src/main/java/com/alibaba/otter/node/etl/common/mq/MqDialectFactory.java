package com.alibaba.otter.node.etl.common.mq;

import com.alibaba.otter.node.etl.common.datasource.DataSourceService;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectGenerator;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import com.google.common.collect.OtterMigrateMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by yanghuanqing@wdai.com on 2018/4/26.
 */
public class MqDialectFactory implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(MqDialectFactory.class);

    private DataSourceService dataSourceService;
    private DbDialectGenerator dbDialectGenerator;

    // 第一层pipelineId , 第二层DbMediaSource id
    private Map<Long, Map<DbMediaSource, DbDialect>> dialects;

    public MqDialectFactory(){


    }

    public DbDialect getDbDialect(Long pipelineId, DbMediaSource source) {
        return dialects.get(pipelineId).get(source);
    }

    public void destory(Long pipelineId) {
        Map<DbMediaSource, DbDialect> dialect = dialects.remove(pipelineId);
        if (dialect != null) {
            for (DbDialect dbDialect : dialect.values()) {
                dbDialect.destory();
            }
        }
    }

    public void destroy() throws Exception {
        Set<Long> pipelineIds = new HashSet<Long>(dialects.keySet());
        for (Long pipelineId : pipelineIds) {
            destory(pipelineId);
        }
    }

    // =============== setter / getter =================

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public void setDbDialectGenerator(DbDialectGenerator dbDialectGenerator) {
        this.dbDialectGenerator = dbDialectGenerator;
    }
}
