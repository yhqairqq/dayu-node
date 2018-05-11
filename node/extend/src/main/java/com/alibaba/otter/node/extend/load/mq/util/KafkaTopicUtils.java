package com.alibaba.otter.node.extend.load.mq.util;

import kafka.admin.AdminUtils;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import org.apache.kafka.common.security.JaasUtils;

import java.util.Iterator;
import java.util.Properties;

/**
 * Created by yanghuanqing@wdai.com on 2018/5/7.
 */
public class KafkaTopicUtils {

 public boolean topicIsExist(String url,String topic){
    try{
        ZkUtils zkUtils = ZkUtils.apply(url, 30000, 30000, JaasUtils.isZkSecurityEnabled());
        // 获取topic 'test'的topic属性属性
        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), topic);
        if(props == null){
            return false;
        }
        // 查询topic-level属性
        Iterator it = props.entrySet().iterator();
        while(it.hasNext()){
            return true;
        }
        return false;
    }finally {

    }
 }
}
