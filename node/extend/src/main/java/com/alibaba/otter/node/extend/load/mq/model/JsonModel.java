package com.alibaba.otter.node.extend.load.mq.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanghuanqing@wdai.com on 2018/5/3.
 */
public class JsonModel implements Serializable {
    private static final long serialVersionUID = -6313346616445741626L;
    private String schema;
    private String table;
    private String eventType;
    private List<String> primaryKeys = new ArrayList<>(0);
    private long executeTime;
    private long sendTime;
    private String sql;
    private Map<String, Object> columns = new HashMap<String, Object>(0);
    private Map<String, Object> columnsType = new HashMap<String, Object>(0);
    private String ip;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<String> primaryKey) {
        this.primaryKeys = primaryKey;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public Map<String, Object> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, Object> columns) {
        this.columns = columns;
    }

    public Map<String, Object> getColumnsType() {
        return columnsType;
    }

    public void setColumnsType(Map<String, Object> columnsType) {
        this.columnsType = columnsType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
