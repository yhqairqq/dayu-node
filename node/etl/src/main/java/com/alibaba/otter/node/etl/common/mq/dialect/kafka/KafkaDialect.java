package com.alibaba.otter.node.etl.common.mq.dialect.kafka;

import com.alibaba.otter.node.etl.common.mq.dialect.MqDialect;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yanghuanqing@wdai.com on 2018/4/26.
 */
public class KafkaDialect implements MqDialect<Producer> {

    private String brokers;

    private String topic;

    private int partition;

    public KafkaDialect(String brokers, String topic, int partition) {
        this.brokers = brokers;
        this.topic = topic;
        this.partition = partition;
    }

    @Override
    public Producer getProducer(){
        Map<String, Object> props = new HashMap<String,Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);//a batch size of zero will disable batching entirely
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1); //send message without delay
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
       return new KafkaProducer(props);

    }
    @Override
    public String getBrokers() {
        return brokers;
    }

    @Override
    public String getEncodeSerilizer() {
        return null;
    }

    @Override
    public String getDeCodeSerializer() {
        return null;
    }

    public void setBrokers(String brokers) {
        this.brokers = brokers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }
}
