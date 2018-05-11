package com.alibaba.otter.node.etl.common.mq.dialect;

/**
 * Created by yanghuanqing@wdai.com on 2018/4/26.
 */
public interface MqDialect<Producer> {

    String getBrokers();

    String getEncodeSerilizer();

    String getDeCodeSerializer();

    Producer getProducer();

    String getTopic();

}
