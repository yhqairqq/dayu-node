package com.alibaba.otter.node.etl.load.loader.mq.model;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanghuanqing@wdai.com on 2018/4/26.
 */
public class MqEvent implements Serializable {
    private static final long serialVersionUID = -7055276959998404945L;


    private EventType eventType;

    /**
     * 变更数据的业务时间.
     */
    private long executeTime;

    /**
     * 发送kafka时间
     */
    private long sendTime;

    /**
     * 变更前的主键值,如果是insert/delete变更前和变更后的主键值是一样的.
     */
    private List<EventColumn> oldKeys = new ArrayList<EventColumn>();

    /**
     * 变更后的主键值,如果是insert/delete变更前和变更后的主键值是一样的.
     */
    private List<EventColumn> keys = new ArrayList<EventColumn>();

    /**
     * 非主键的其他字段
     */
    private List<EventColumn> columns = new ArrayList<EventColumn>();

    // ====================== 运行过程中对数据的附加属性 =============================
    /**
     * 预计的size大小，基于binlog event的推算
     */
    private long size = 1024;


    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public List<EventColumn> getOldKeys() {
        return oldKeys;
    }

    public void setOldKeys(List<EventColumn> oldKeys) {
        this.oldKeys = oldKeys;
    }

    public List<EventColumn> getKeys() {
        return keys;
    }

    public void setKeys(List<EventColumn> keys) {
        this.keys = keys;
    }

    public List<EventColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<EventColumn> columns) {
        this.columns = columns;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
