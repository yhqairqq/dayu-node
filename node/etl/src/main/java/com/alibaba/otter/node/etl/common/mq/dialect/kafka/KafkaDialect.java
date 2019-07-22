package com.alibaba.otter.node.etl.common.mq.dialect.kafka;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.node.etl.common.mq.dialect.MqDialect;
import kafka.admin.AdminUtils;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.security.JaasUtils;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yanghuanqing@wdai.com on 2018/4/26.
 */
public class KafkaDialect implements MqDialect<Producer> {

    private String brokers;

    private  Producer producer;

    Object object = new Object();

    public KafkaDialect(String brokers) {
        this.brokers = brokers;
        getProducer();
    }
//    public boolean hasTopic(String topic){
//        ZkUtils zkUtils = ZkUtils.apply("172.16.20.124:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());
//        //查询topic集合
//        List<String> topics =   scala.collection.JavaConversions.seqAsJavaList(zkUtils.getAllTopics());
//        if( topics.contains(topic)){
//            return true;
//        }
//        return false;
//    }
    private Map<String, Object> createProducerConfig() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 2000);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);//a batch size of zero will disable batching entirely
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1); //send message without delay
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 134217728);
        props.put(ProducerConfig.SEND_BUFFER_CONFIG, 524288);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        //该事务不能保证事务内部的消息是有序的
//        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,"transaction_id1");
//        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,true);
//        props.put(ProducerConfig.TRANSACTION_TIMEOUT_DOC,"16000");

        return props;
    }

    public Producer createProducer() {
        return new KafkaProducer(createProducerConfig());
    }

    @Override
    public Producer getProducer() {
       synchronized (object){
           if(producer == null){
               producer =  createProducer();
               return producer;
           }
       }
        return producer;
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
    @Override
    public void destory() {
    }


    public static void main(String[] args) {
        KafkaDialect kafkaDialect = new KafkaDialect("172.16.20.124:9092");
        Producer producer = kafkaDialect.createProducer();
        producer.initTransactions();
        producer.beginTransaction();
        try{
            producer.send(new ProducerRecord("otter_sink","message1"));
            producer.send(new ProducerRecord("otter_sink","message2"));
            producer.send(new ProducerRecord("otter_sink","message3"));
            producer.send(new ProducerRecord("otter_sink","message4"));
            producer.send(new ProducerRecord("otter_sink","message5"));
            producer.send(new ProducerRecord("otter_sink","message6"));

            producer.commitTransaction();
        }catch (Exception e){
            e.printStackTrace();
            producer.abortTransaction();
        }
    }
    public static void main111(String[] args) {
        KafkaDialect kafkaDialect = new KafkaDialect("172.16.20.124:9092");
        Producer producer = kafkaDialect.createProducer();


        List<PartitionInfo> partitionInfoList = producer.partitionsFor("sink_d");
         Map<MetricName, ? extends Metric>  map = producer.metrics();
        System.out.println(
                map
        );
        System.out.println(partitionInfoList);

        ZkUtils zkUtils = ZkUtils.apply("172.16.20.124:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());

        //查询topic集合
        List<String> topics =   scala.collection.JavaConversions.seqAsJavaList(zkUtils.getAllTopics());

       if( topics.contains("sink_d")){
           System.out.println("sink_d");
       };

        System.out.println(
                topics
        );
// 获取topic 'test'的topic属性属性
        Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), "sink_d");
// 查询topic-level属性
        Iterator it = props.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry=(Map.Entry)it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            System.out.println(key + " = " + value);
        }
        zkUtils.close();
//        producer.initTransactions();
//        producer.tran
//        producer.beginTransaction();
//        producer.close();
//        producer.commitTransaction();

        Map<String,String> before = new HashMap<>();
        before.put("1","name");
        before.put("2","age");
        System.out.println(JSONObject.toJSONString(before));

    }
}
