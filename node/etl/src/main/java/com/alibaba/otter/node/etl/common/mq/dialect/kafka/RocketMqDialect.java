package com.alibaba.otter.node.etl.common.mq.dialect.kafka;

import com.alibaba.otter.node.etl.common.mq.dialect.MqDialect;
import joptsimple.internal.Strings;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by huahua on 2019/6/25.
 */
public class RocketMqDialect implements MqDialect<DefaultMQProducer> {

    private String groupName;
    private String instanceName;
    public RocketMqDialect(String brokers,String groupName,String instanceName) {
        this.brokers = brokers;
        this.groupName = groupName;
        this.instanceName = instanceName;
        getProducer();
    }

    private DefaultMQProducer producer;
    private String brokers;
    Object object = new Object();

    @Override
    public String getBrokers() {
        return this.brokers;
    }

    @Override
    public String getEncodeSerilizer() {
        return null;
    }

    @Override
    public String getDeCodeSerializer() {
        return null;
    }

    @Override
    public DefaultMQProducer getProducer() {
        synchronized (object) {
            //调用start()方法启动一个producer实例
            try {
                if(producer == null){
                    //声明并初始化一个producer
                    //需要一个producer group名字作为构造方法的参数，这里为producer1
                    String id = UUID.randomUUID().toString();
                    producer = new DefaultMQProducer();
                    if(!Strings.isNullOrEmpty(this.groupName)){
                        producer.setProducerGroup(groupName);
                    }else{
                        producer.setProducerGroup(id);
                    }
                    producer.setInstanceName(id);
                    //设置NameServer地址,此处应改为实际NameServer地址，多个地址之间用；分隔
                    //NameServer的地址必须有，但是也可以通过环境变量的方式设置，不一定非得写死在代码里
                    producer.setNamesrvAddr(this.brokers);

                    producer.start();
                }
                return producer;
            } catch (MQClientException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    @Override
    public void destory() {
    }


    public static void main(String[] args) throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException, MQBrokerException {
        DefaultMQProducer   producer = new DefaultMQProducer(UUID.randomUUID().toString());
        //设置NameServer地址,此处应改为实际NameServer地址，多个地址之间用；分隔
        //NameServer的地址必须有，但是也可以通过环境变量的方式设置，不一定非得写死在代码里
        producer.setProducerGroup("Otter_sendBinlogErp");
        producer.setNamesrvAddr("10.111.27.2:9876");
        producer.setInstanceName("kk");
        producer.start();
        Message msg = new Message("Otter_Erp_N", "sink_d","212121", "你好a啊啊ddd啊".getBytes(RemotingHelper.DEFAULT_CHARSET)// body
        );
        DefaultMQProducer  producer1 = new DefaultMQProducer(UUID.randomUUID().toString());
        //设置NameServer地址,此处应改为实际NameServer地址，多个地址之间用；分隔
        //NameServer的地址必须有，但是也可以通过环境变量的方式设置，不一定非得写死在代码里
        producer1.setProducerGroup("Otter_sendBinlogErp");
        producer1.setNamesrvAddr("10.111.31.224:9876");
        producer1.setInstanceName("kk2");
        producer1.start();
        Message msg1 = new Message("topic2", "sink_d","121212", "你好".getBytes(RemotingHelper.DEFAULT_CHARSET)// body
        );
        Map<String,DefaultMQProducer> producerMap = new HashMap<>();
        producerMap.put("pro",producer);
        producerMap.put("pro1",producer1);
        DefaultMQProducer proInstance2 =   producerMap.get("pro1");

        SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                int id = Math.abs(String.valueOf(arg).hashCode());
                int queueIndex = id%mqs.size();
                return mqs.get(queueIndex);
            }
        },"2445");



        SendResult sendResult1 = producer1.send(msg1, new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                int id = Math.abs(String.valueOf(arg).hashCode());
                int queueIndex = id%mqs.size();
                return mqs.get(queueIndex);
            }
        },"2445");

        System.out.println(sendResult);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    producerMap.get("pro").send(msg);
//                } catch (MQClientException e) {
//                    e.printStackTrace();
//                } catch (RemotingException e) {
//                    e.printStackTrace();
//                } catch (MQBrokerException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }
}
