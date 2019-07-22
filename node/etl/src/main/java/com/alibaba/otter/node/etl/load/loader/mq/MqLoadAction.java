package com.alibaba.otter.node.etl.load.loader.mq;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectFactory;
import com.alibaba.otter.node.etl.common.mq.dialect.MqDialect;
import com.alibaba.otter.node.etl.common.mq.dialect.MqDialectFactory;
import com.alibaba.otter.node.etl.common.mq.dialect.kafka.KafkaDialect;
import com.alibaba.otter.node.etl.common.mq.dialect.kafka.RocketMqDialect;
import com.alibaba.otter.node.etl.load.exception.LoadException;
import com.alibaba.otter.node.etl.load.loader.LoadContext;
import com.alibaba.otter.node.etl.load.loader.LoadStatsTracker;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadData;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadDumper;
import com.alibaba.otter.node.etl.load.loader.interceptor.LoadInterceptor;
import com.alibaba.otter.node.etl.load.loader.mq.context.MqLoadContext;
import com.alibaba.otter.node.etl.load.loader.weight.WeightBuckets;
import com.alibaba.otter.node.etl.load.loader.weight.WeightController;
import com.alibaba.otter.shared.arbitrate.impl.zookeeper.ZooKeeperClient;
import com.alibaba.otter.shared.common.model.config.ConfigHelper;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMedia;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.mq.MqMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.thread.NamedThreadFactory;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import com.alibaba.otter.shared.etl.model.Identity;
import com.alibaba.otter.shared.etl.model.RowBatch;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yanghuanqing@wdai.com on 2018/4/26.
 */
public class MqLoadAction implements InitializingBean, DisposableBean {


    private static final Logger logger = LoggerFactory.getLogger(MqLoadAction.class);
    private static final String WORKER_NAME = "MqLoadAction";
    private static final String WORKER_NAME_FORMAT = "pipelineId = %s , pipelineName = %s , " + WORKER_NAME;
    private static final int DEFAULT_POOL_SIZE = 5;
    private int poolSize = DEFAULT_POOL_SIZE;
    private int retry = 3;
    private int retryWait = 3000;
    private LoadInterceptor interceptor;
    private ExecutorService executor;
    private ConfigClientService configClientService;
    private DbDialectFactory dbDialectFactory;
    private MqDialectFactory mqDialectFactory;
    private int batchSize = 50;
    private boolean useBatch = true;
    private LoadStatsTracker loadStatsTracker;
    private ZooKeeperClient zooKeeperClient;


    /**
     * 返回结果为已处理成功的记录
     */
    public MqLoadContext load(RowBatch rowBatch, WeightController controller) {
        Assert.notNull(rowBatch);
        Identity identity = rowBatch.getIdentity();
        MqLoadContext context = buildContext(identity);

        try {
            List<EventData> datas = rowBatch.getDatas();
            context.setPrepareDatas(datas);
            // 执行重复录入数据过滤
            datas = context.getPrepareDatas();
            if (datas == null || datas.size() == 0) {
                logger.info("##no eventdata for load, return");
                return context;
            }

            // 因为所有的数据在DbBatchLoader已按照DateMediaSource进行归好类，不同数据源介质会有不同的DbLoadAction进行处理
            // 设置media source时，只需要取第一节点的source即可
            context.setDataMediaSource(ConfigHelper.findDataMedia(context.getPipeline(), datas.get(0).getTableId())
                    .getSource());
            interceptor.prepare(context);
            // 执行重复录入数据过滤
            datas = context.getPrepareDatas();
            // 处理下ddl语句，ddl/dml语句不可能是在同一个batch中，由canal进行控制
            // 主要考虑ddl的幂等性问题，尽可能一个ddl一个batch，失败或者回滚都只针对这条sql
            if (isDdlDatas(datas)) {
                MqLoadAction.MqLoadWorker worker = new MqLoadAction.MqLoadWorker(context, datas, false);
                Exception exception = worker.call();
            } else {
                WeightBuckets<EventData> buckets = buildWeightBuckets(context, datas);
                List<Long> weights = buckets.weights();
                controller.start(weights);// weights可能为空，也得调用start方法
                if (CollectionUtils.isEmpty(datas)) {
                    logger.info("##no eventdata for load");
                }
                adjustPoolSize(context); // 根据manager配置调整线程池
                adjustConfig(context); // 调整一下运行参数
                // 按权重构建数据对象
                // 处理数据
                for (int i = 0; i < weights.size(); i++) {
                    Long weight = weights.get(i);
                    controller.await(weight.intValue());
                    // 处理同一个weight下的数据
                    List<EventData> items = buckets.getItems(weight);
                    logger.debug("##start load for weight:" + weight);
                    // 预处理下数据

                    // 进行一次数据合并，合并相同pk的多次I/U/D操作
//                    items = DbLoadMerger.merge(items);
                    // 按I/U/D进行归并处理
                    DbLoadData loadData = new DbLoadData();

                    doBefore(items, context, loadData);
                    // 执行load操作
                    doLoad(context, loadData);
                    controller.single(weight.intValue());
                    logger.debug("##end load for weight:" + weight);
                }
            }
            interceptor.commit(context);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            interceptor.error(context);
        } catch (Exception e) {
            e.printStackTrace();
            interceptor.error(context);
            throw new LoadException(e);
        }

        return context;// 返回处理成功的记录
    }

    private MqLoadContext buildContext(Identity identity) {
        MqLoadContext context = new MqLoadContext();
        context.setIdentity(identity);
        Channel channel = configClientService.findChannel(identity.getChannelId());
        Pipeline pipeline = configClientService.findPipeline(identity.getPipelineId());
        context.setChannel(channel);
        context.setPipeline(pipeline);
        return context;
    }

    /**
     * 分析整个数据，将datas划分为多个批次. ddl sql前的DML并发执行，然后串行执行ddl后，再并发执行DML
     *
     * @return
     */
    private boolean isDdlDatas(List<EventData> eventDatas) {
        boolean result = false;
        for (EventData eventData : eventDatas) {
            result |= eventData.getEventType().isDdl();
            if (result && !eventData.getEventType().isDdl()) {
                throw new LoadException("ddl/dml can't be in one batch, it's may be a bug , pls submit issues.",
                        DbLoadDumper.dumpEventDatas(eventDatas));
            }
        }

        return result;
    }

    /**
     * 构建基于weight权重分组的item集合列表
     */
    private WeightBuckets<EventData> buildWeightBuckets(MqLoadContext context, List<EventData> datas) {
        WeightBuckets<EventData> buckets = new WeightBuckets<EventData>();
        for (EventData data : datas) {
            // 获取对应的weight
            DataMediaPair pair = ConfigHelper.findDataMediaPair(context.getPipeline(), data.getPairId());
            buckets.addItem(pair.getPushWeight(), data);
        }

        return buckets;
    }

    /**
     * 执行数据处理，比如数据冲突检测
     */
    private void doBefore(List<EventData> items, final LoadContext context, final DbLoadData loadData) {

        for (final EventData item : items) {

            boolean filter = interceptor.before(context, item);
            if (!filter) {
                loadData.merge(item);// 进行分类
            }
        }
    }

    private void doLoad(final MqLoadContext context, DbLoadData loadData) {
        // 优先处理delete,可以利用batch优化
        List<List<EventData>> batchDatas = new ArrayList<List<EventData>>();
        for (DbLoadData.TableLoadData tableData : loadData.getTables()) {
            if (useBatch) {
                // 优先执行delete语句，针对uniqe更新，一般会进行delete + insert的处理模式，避免并发更新
                batchDatas.addAll(split(tableData.getDeleteDatas()));
            } else {
                // 如果不可以执行batch，则按照单条数据进行并行提交
                // 优先执行delete语句，针对uniqe更新，一般会进行delete + insert的处理模式，避免并发更新
                for (EventData data : tableData.getDeleteDatas()) {
                    batchDatas.add(Arrays.asList(data));
                }
            }
        }
        if (context.getPipeline().getParameters().isDryRun()) {
            doDryRun(context, batchDatas, true);
        } else {
            doTwoPhase(context, batchDatas, true);
        }
        batchDatas.clear();

        // 处理下insert/update
        for (DbLoadData.TableLoadData tableData : loadData.getTables()) {
            if (useBatch) {
                // 执行insert + update语句
                batchDatas.addAll(split(tableData.getInsertDatas()));
                batchDatas.addAll(split(tableData.getUpadateDatas()));// 每条记录分为一组，并行加载
            } else {
                // 执行insert + update语句
                for (EventData data : tableData.getInsertDatas()) {
                    batchDatas.add(Arrays.asList(data));
                }
                for (EventData data : tableData.getUpadateDatas()) {
                    batchDatas.add(Arrays.asList(data));
                }
            }
        }

        if (context.getPipeline().getParameters().isDryRun()) {
            doDryRun(context, batchDatas, true);
        } else {
            doTwoPhase(context, batchDatas, true);
        }
        batchDatas.clear();
    }

    /**
     * 将对应的数据按照sql相同进行batch组合
     */
    private List<List<EventData>> split(List<EventData> datas) {
        List<List<EventData>> result = new ArrayList<List<EventData>>();
        if (datas == null || datas.size() == 0) {
            return result;
        } else {
            int[] bits = new int[datas.size()];// 初始化一个标记，用于标明对应的记录是否已分入某个batch
            for (int i = 0; i < bits.length; i++) {
                // 跳过已经被分入batch的
                while (i < bits.length && bits[i] == 1) {
                    i++;
                }

                if (i >= bits.length) { // 已处理完成，退出
                    break;
                }

                // 开始添加batch，最大只加入batchSize个数的对象
                List<EventData> batch = new ArrayList<EventData>();
                bits[i] = 1;
                batch.add(datas.get(i));
                for (int j = i + 1; j < bits.length && batch.size() < batchSize; j++) {
                    if (bits[j] == 0 && canBatch(datas.get(i), datas.get(j))) {
                        batch.add(datas.get(j));
                        bits[j] = 1;// 修改为已加入
                    }
                }
                result.add(batch);
            }

            return result;
        }
    }

    /**
     * 判断两条记录是否可以作为一个batch提交，主要判断sql是否相等. 可优先通过schemaName进行判断
     */
    private boolean canBatch(EventData source, EventData target) {
        // return StringUtils.equals(source.getSchemaName(),
        // target.getSchemaName())
        // && StringUtils.equals(source.getTableName(), target.getTableName())
        // && StringUtils.equals(source.getSql(), target.getSql());
        // return StringUtils.equals(source.getSql(), target.getSql());

        // 因为sqlTemplate构造sql时用了String.intern()的操作，保证相同字符串的引用是同一个，所以可以直接使用==进行判断，提升效率
        return source.getSql() == target.getSql();
    }

    private void doDryRun(MqLoadContext context, List<List<EventData>> totalRows, boolean canBatch) {
        for (List<EventData> rows : totalRows) {
            if (CollectionUtils.isEmpty(rows)) {
                continue; // 过滤空记录
            }

            for (EventData row : rows) {
                processStat(row, context);// 直接记录成功状态
            }

            context.getProcessedDatas().addAll(rows);
        }
    }


    /**
     * 首先进行并行执行，出错后转为串行执行
     */
    private void doTwoPhase(MqLoadContext context, List<List<EventData>> totalRows, boolean canBatch) {
        // 预处理下数据
        List<Future<Exception>> results = new ArrayList<Future<Exception>>();
        for (List<EventData> rows : totalRows) {
            if (CollectionUtils.isEmpty(rows)) {
                continue; // 过滤空记录
            }
            results.add(executor.submit(new MqLoadAction.MqLoadWorker(context, rows, canBatch)));
        }

        boolean partFailed = false;
        for (int i = 0; i < results.size(); i++) {
            Future<Exception> result = results.get(i);
            Exception ex = null;
            try {
                ex = result.get();
                for (EventData data : totalRows.get(i)) {
                    interceptor.after(context, data);// 通知加载完成
                }
            } catch (Exception e) {
                ex = e;
            }

            if (ex != null) {
                logger.warn("##load phase one failed!", ex);
                partFailed = true;
            }
        }

        if (true == partFailed) {
            // if (CollectionUtils.isEmpty(context.getFailedDatas())) {
            // logger.error("##load phase one failed but failedDatas is empty!");
            // return;
            // }

            // 尝试的内容换成phase one跑的所有数据，避免因failed datas计算错误而导致丢数据
            List<EventData> retryEventDatas = new ArrayList<EventData>();
            for (List<EventData> rows : totalRows) {
                retryEventDatas.addAll(rows);
            }

            context.getFailedDatas().clear(); // 清理failed data数据

            // 可能为null，manager老版本数据序列化传输时，因为数据库中没有skipLoadException变量配置
            Boolean skipLoadException = context.getPipeline().getParameters().getSkipLoadException();
            if (skipLoadException != null && skipLoadException) {// 如果设置为允许跳过单条异常，则一条条执行数据load，准确过滤掉出错的记录，并进行日志记录
                for (EventData retryEventData : retryEventDatas) {
                    MqLoadAction.MqLoadWorker worker = new MqLoadAction.MqLoadWorker(context, Arrays.asList(retryEventData), false);// 强制设置batch为false
                    try {
                        Exception ex = worker.call();
                        if (ex != null) {
                            // do skip
                            logger.warn("skip exception for data : {} , caused by {}",
                                    retryEventData,
                                    ExceptionUtils.getFullStackTrace(ex));
                        }
                    } catch (Exception ex) {
                        // do skip
                        logger.warn("skip exception for data : {} , caused by {}",
                                retryEventData,
                                ExceptionUtils.getFullStackTrace(ex));
                    }
                }
            } else {
                // 直接一批进行处理，减少线程调度
                MqLoadAction.MqLoadWorker worker = new MqLoadAction.MqLoadWorker(context, retryEventDatas, false);// 强制设置batch为false
                try {
                    Exception ex = worker.call();
                    if (ex != null) {
                        throw ex; // 自己抛自己接
                    }
                } catch (Exception ex) {
                    logger.error("##load phase two failed!", ex);
                    throw new LoadException(ex);
                }
            }

            // 清理failed data数据
            for (EventData data : retryEventDatas) {
                interceptor.after(context, data);// 通知加载完成
            }
        }

    }

    // 调整一下线程池
    private void adjustPoolSize(MqLoadContext context) {
        Pipeline pipeline = context.getPipeline();
        int newPoolSize = pipeline.getParameters().getLoadPoolSize();
        if (newPoolSize != poolSize) {
            poolSize = newPoolSize;
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
                pool.setCorePoolSize(newPoolSize);
                pool.setMaximumPoolSize(newPoolSize);
            }
        }
    }

    private void adjustConfig(MqLoadContext context) {
        Pipeline pipeline = context.getPipeline();
        this.useBatch = pipeline.getParameters().isUseBatch();
    }

    public void afterPropertiesSet() throws Exception {
        executor = new ThreadPoolExecutor(poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(poolSize * 4),
                new NamedThreadFactory(WORKER_NAME),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void destroy() throws Exception {
        executor.shutdownNow();
    }

    enum ExecuteResult {
        SUCCESS, ERROR, RETRY
    }

    class MqLoadWorker implements Callable<Exception> {

        private MqLoadContext context;
        private MqDialect mqDialect;
        private List<EventData> datas;
        private boolean canBatch;
        private List<EventData> allFailedDatas = new ArrayList<EventData>();
        private List<EventData> allProcesedDatas = new ArrayList<EventData>();
        private List<EventData> processedDatas = new ArrayList<EventData>();
        private List<EventData> failedDatas = new ArrayList<EventData>();
        private MqMediaSource source;
        private DataMedia dataMedia;

        public MqLoadWorker(MqLoadContext context, List<EventData> datas, boolean canBatch) {
            this.context = context;
            this.datas = datas;
            this.canBatch = canBatch;
            EventData data = datas.get(0); // eventData为同一数据库的记录，只取第一条即可
//            MqMediaSource dataMediaSource = (MqMediaSource) context.getDataMediaSource();

            dataMedia = ConfigHelper.findDataMedia(context.getPipeline(), data.getTableId());
            source = (MqMediaSource) dataMedia.getSource();
            mqDialect = mqDialectFactory.getMqDialect(context.getIdentity().getPipelineId(),
                    (MqMediaSource) dataMedia.getSource());
        }


        public Exception call() throws Exception {
            try {
                Thread.currentThread().setName(String.format(WORKER_NAME_FORMAT,
                        context.getPipeline().getId(),
                        context.getPipeline().getName()));
                return doCall();
            } finally {
                Thread.currentThread().setName(WORKER_NAME);
            }
        }

        private void doSent(final EventData eventData) throws Exception {
            if (mqDialect instanceof KafkaDialect) {
                Producer producer = (Producer) mqDialect.getProducer();
                send(eventData, producer);
            } else if (mqDialect instanceof RocketMqDialect) {
                DefaultMQProducer producer = (DefaultMQProducer) mqDialect.getProducer();
                send(eventData, producer);
            }
        }

        private Exception doCall() {
            RuntimeException error = null;
            MqLoadAction.ExecuteResult exeResult = null;
            int index = 0;// 记录下处理成功的记录下标
            int retryCount = 0;
            failedDatas.clear(); // 先清理
            processedDatas.clear();
            for (final EventData eventData : datas) {
                try {
                    doSent(eventData);
                } catch (Exception e) {
                    e.printStackTrace();
                    error = new LoadException(ExceptionUtils.getFullStackTrace(e),
                            DbLoadDumper.dumpEventDatas(datas));
                    logger.error(e.getMessage(), e.getCause());
                    logger.error(eventData.toString());
                    processStat(eventData,-1,false);
                }
                index ++;
            }

            if (!CollectionUtils.isEmpty(failedDatas)) {
                allFailedDatas.clear();
                allFailedDatas.addAll(failedDatas);
                failedDatas.clear();
                exeResult = ExecuteResult.RETRY;
            }else{
                for(EventData data:datas){
                    processStat(data,1,false);
                }
                exeResult = ExecuteResult.SUCCESS;
            }
            while (exeResult == ExecuteResult.RETRY && retryCount < 3) {
                exeResult = null;
                int j = 0;
                for (final EventData eventData : datas) {
                    try {
                        doSent(eventData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        exeResult = ExecuteResult.RETRY;
                        logger.error(e.getMessage(), e.getCause());
                        logger.warn(eventData.toString(), "发送错误");
                        retryCount++;
                        break;
                    }
                    j++;
                }

                if(j == datas.size()){
                    exeResult = ExecuteResult.SUCCESS;
                }

                if(exeResult == ExecuteResult.RETRY){
                    retryCount++;
                    continue;
                }else{
                    exeResult = ExecuteResult.SUCCESS;
                    for(EventData data:datas){
                        processStat(data,1,false);
                    }
                    break;
                }
            }
            if(ExecuteResult.RETRY == exeResult){
                processedDatas.clear();
                failedDatas.clear();
                processFailedDatas(index);// 局部处理出错了
                return error;
            }
            allProcesedDatas.addAll(processedDatas);
            // 记录一下当前处理过程中失败的记录,affect = 0的记录
            context.getFailedDatas().addAll(allFailedDatas);
            context.getProcessedDatas().addAll(allProcesedDatas);
            return null;
        }
//        private String parseEventData(EventData eventData) {
//            MessageOutType messageOutType = MessageOutType.JSON;
//            if (mqDialect.getTopic().toLowerCase().endsWith(MessageOutType.CSV.name().toLowerCase())) {
//                messageOutType = MessageOutType.CSV;
//            }
//            Object result = MessageConvertUtils.toParse(eventData, messageOutType);
//            if (result instanceof JsonModel) {
//                ((JsonModel) result).setIp(ip(source.getUrl()));
//            }
//            return JSONObject.toJSONString(result);
//        }

        private void send(EventData eventData, DefaultMQProducer producer) throws UnsupportedEncodingException, RemotingException, MQClientException, InterruptedException, MQBrokerException {
            Message msg = new Message(eventData.getTopic(), eventData.getPartitionTag(),eventData.getKeys().get(0).getColumnValue(), JSONObject.toJSONString(eventData).getBytes(RemotingHelper.DEFAULT_CHARSET)// body
            );
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    int id = Math.abs(String.valueOf(arg).hashCode());
                    int queueIndex = id%mqs.size();
                    return mqs.get(queueIndex);
                }
            },eventData.getKeys().get(0).getColumnValue());
            if(sendResult.getSendStatus() != SendStatus.SEND_OK){
                throw new RemotingException("消息发送不成功,远程rocketMq"+sendResult.getSendStatus().name());
            }
//            doSend(producer, msg, eventData);
        }

        private void send(EventData eventData, Producer producer) throws Exception {
            List<PartitionInfo> partitionInfos = producer.partitionsFor(eventData.getTopic());
            if (CollectionUtils.isEmpty(partitionInfos)) {
                throw new Exception("topic 尚未创建");
            }
            if (partitionInfos.size() == 1) {
                logger.warn("topic " + eventData.getTopic() + " 只有一个分片");
            }
            int targetPartition = generatePartition(partitionInfos.size(), eventData.getPartitionTag());
            ProducerRecord<String, String> record
                    = new ProducerRecord<String, String>(eventData.getTopic(), targetPartition, null, JSONObject.toJSONString(eventData));
//                doSend(producer, record, eventData);
            producer.send(record);
        }


        private int generatePartition(int total, String seed) {
            int hashcode = seed.hashCode();
            hashcode = Math.abs(hashcode);
            return hashcode % (total);
        }

        private void doSend(DefaultMQProducer producer, Message message, final EventData eventData) throws RemotingException, MQClientException, InterruptedException {
            try {
                SendResult sendResult = producer.send(message, 1000);
                logger.info("rocket msg id " + sendResult.getMsgId());
                processStat(eventData, 1, true);
                allProcesedDatas.addAll(processedDatas);
            } catch (MQBrokerException e) {
                processStat(eventData, -1, true);
                e.printStackTrace();
                logger.error(e.getMessage());
            }
//            producer.send(message, new SendCallback() {
//                @Override
//                public void onSuccess(SendResult sendResult) {
//                    processStat(eventData, 1, false);
//                    allProcesedDatas.addAll(processedDatas);
//
//                }
//
//                @Override
//                public void onException(Throwable e) {
//                    processStat(eventData, -1, false);
//                    allProcesedDatas.addAll(processedDatas);
//                    e.printStackTrace();
//                    logger.error(e.getMessage());
//                }
//            });
        }

        private void doSend(Producer producer, ProducerRecord<String, String> record, final EventData eventData) {
//            System.out.println(JSONObject.toJSONString(eventData));

//                    producer.send(record);
//                    processStat(eventData, 1, true);
//                    allProcesedDatas.addAll(processedDatas);


        }


        private void processStat(EventData data, int affect, boolean batch) {
            if (batch && (affect < 1 && affect != Statement.SUCCESS_NO_INFO)) {
                failedDatas.add(data); // 记录到错误的临时队列，进行重试处理
            } else if (!batch && affect < 1) {
                failedDatas.add(data);// 记录到错误的临时队列，进行重试处理
            } else {
                processedDatas.add(data); // 记录到成功的临时队列，commit也可能会失败。所以这记录也可能需要进行重试
                MqLoadAction.this.processStat(data, context);
            }
        }

        // 出现异常回滚了，记录一下异常记录
        private void processFailedDatas(int index) {
            allFailedDatas.addAll(failedDatas);// 添加失败记录
            context.getFailedDatas().addAll(allFailedDatas);// 添加历史出错记录
            for (; index < datas.size(); index++) { // 记录一下未处理的数据
                context.getFailedDatas().add(datas.get(index));
            }
            // 这里不需要添加当前成功记录，出现异常后会rollback所有的成功记录，比如processDatas有记录，但在commit出现失败
            // (bugfix)
            allProcesedDatas.addAll(processedDatas);
            context.getProcessedDatas().addAll(allProcesedDatas);// 添加历史成功记录
        }

    }

    private void processStat(EventData data, MqLoadContext context) {
        LoadStatsTracker.LoadThroughput throughput = loadStatsTracker.getStat(context.getIdentity());
        LoadStatsTracker.LoadCounter counter = throughput.getStat(data.getPairId());
        EventType type = data.getEventType();
        if (type.isInsert()) {
            counter.getInsertCount().incrementAndGet();
        } else if (type.isUpdate()) {
            counter.getUpdateCount().incrementAndGet();
        } else if (type.isDelete()) {
            counter.getDeleteCount().incrementAndGet();
        }

        counter.getRowCount().incrementAndGet();
        counter.getRowSize().addAndGet(calculateSize(data));
    }

    // 大致估算一下row记录的大小
    private long calculateSize(EventData data) {
        // long size = 0L;
        // size += data.getKeys().toString().getBytes().length - 12 -
        // data.getKeys().size() + 1L;
        // size += data.getColumns().toString().getBytes().length - 12 -
        // data.getKeys().size() + 1L;
        // return size;

        // byte[] bytes = JsonUtils.marshalToByte(data);// 走序列化的方式快速计算一下大小
        // return bytes.length;

        return data.getSize();// 数据不做计算，避免影响性能
    }

    // =============== setter / getter ===============

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setRetryWait(int retryWait) {
        this.retryWait = retryWait;
    }

    public void setInterceptor(LoadInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public MqDialectFactory getMqDialectFactory() {
        return mqDialectFactory;
    }

    public void setMqDialectFactory(MqDialectFactory mqDialectFactory) {
        this.mqDialectFactory = mqDialectFactory;
    }

    public ConfigClientService getConfigClientService() {
        return configClientService;
    }

    public DbDialectFactory getDbDialectFactory() {
        return dbDialectFactory;
    }

    public void setDbDialectFactory(DbDialectFactory dbDialectFactory) {
        this.dbDialectFactory = dbDialectFactory;
    }

    public void setConfigClientService(ConfigClientService configClientService) {
        this.configClientService = configClientService;
    }

    public void setLoadStatsTracker(LoadStatsTracker loadStatsTracker) {
        this.loadStatsTracker = loadStatsTracker;
    }

    public void setUseBatch(boolean useBatch) {
        this.useBatch = useBatch;
    }

    /**
     * 判断是否为合法IP * @return the ip
     */
    public static String ip(String param) {
        String ip = "jdbc:mysql://(?<ip>[\\d*\\.]*):";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(param);
        while (matcher.find()) {
            String result = matcher.group("ip");
            return result;
        }
        return null;

    }
}
