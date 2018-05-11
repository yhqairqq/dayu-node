package com.alibaba.otter.node.etl.common.mq.dialect;

import com.alibaba.otter.node.etl.common.mq.dialect.kafka.KafkaDialect;
import com.alibaba.otter.node.etl.load.exception.LoadException;
import com.alibaba.otter.shared.common.model.config.data.mq.MqDataMedia;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Created by yanghuanqing@wdai.com on 2018/4/28.
 */
public class MqDialectGenerator {
    protected static final String KAFKA      = "kafka";
    protected static final String ROCKET       = "rocket";

    protected LobHandler defaultLobHandler;
    protected LobHandler          oracleLobHandler;

    protected MqDialect generate(
            MqMediaSource source,MqDataMedia dataMedia
    ) {

        if(source.getType().isMq()){
          return   new KafkaDialect(source.getUrl(),dataMedia.getNamespace(),-1);
        }
        throw new LoadException("MqDialectGenerator 返回产生器无法产生");

    }

    // ======== setter =========
    public void setDefaultLobHandler(LobHandler defaultLobHandler) {
        this.defaultLobHandler = defaultLobHandler;
    }

    public void setOracleLobHandler(LobHandler oracleLobHandler) {
        this.oracleLobHandler = oracleLobHandler;
    }
}
