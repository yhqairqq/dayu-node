package com.alibaba.otter.node.etl.common.mq.dialect;

import com.alibaba.otter.node.etl.common.datasource.DataSourceService;
import com.alibaba.otter.shared.common.model.config.data.mq.MqDataMedia;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;
import com.google.common.base.Function;
import com.google.common.collect.MigrateMap;
import com.google.common.collect.OtterMigrateMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by yanghuanqing@wdai.com on 2018/4/28.
 */

/**
 * Created by yanghuanqing@wdai.com on 2018/4/26.
 */
public class MqDialectFactory implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(MqDialectFactory.class);

    private DataSourceService dataSourceService;
    private MqDialectGenerator mqDialectGenerator;

    // 第一层pipelineId , MqMediaSource id
    private Map<Long, Map<MqMediaSource, MqDialect>> dialects;

    public MqDialectFactory() {
        dialects = OtterMigrateMap.makeSoftValueComputingMapWithRemoveListenr(new Function<Long, Map<MqMediaSource, MqDialect>>() {

                  public Map<MqMediaSource, MqDialect> apply(final Long pipelineId) {
                      // 构建第二层map
                      return MigrateMap.makeComputingMap(new Function<MqMediaSource, MqDialect>() {

                          public MqDialect apply(final MqMediaSource source) {
                              return mqDialectGenerator.generate(source);
                          }
                      });
                  }
              },
                new OtterMigrateMap.OtterRemovalListener<Long, Map<MqMediaSource, MqDialect>>() {
                    @Override
                    public void onRemoval(Long pipelineId, Map<MqMediaSource, MqDialect> dialect) {
                        if (dialect == null) {
                            return;
                        }
                        for (MqDialect mqDialect : dialect.values()) {
                            mqDialect.destory();
                        }
                    }

                });

    }

    public MqDialect getMqDialect(Long pipelineId, MqMediaSource source) {
        return dialects.get(pipelineId).get(source);
    }

    public void destory(Long pipelineId) {
        Map<MqMediaSource, MqDialect> dialect = dialects.remove(pipelineId);
        if (dialect != null) {
            for (MqDialect mqDialect : dialect.values()) {
                mqDialect.destory();
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

    public void setDbDialectGenerator(MqDialectGenerator mqDialectGenerator) {
        this.mqDialectGenerator = mqDialectGenerator;
    }
}
