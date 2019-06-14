/**
 *
 * Copyright From 2015, 微贷（杭州）金融信息服务有限公司. All rights reserved.
 *
 * MetaDataConfig.java
 *
 */
package com.alibaba.otter.node.etl.common.datasource;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 表 : meta_table_config的 model 类
 *
 * @author 	Yanghq
 * @date 	2019年06月06日
 */
public class MetaDataConfig implements Serializable {
    /**  类的 seri version id */
    private static final long serialVersionUID = 1L;

    /** 字段:id */
    private Integer id;

    /** 字段:db_type，数据库类型 */
    private String dbType;

    /** 字段:ip，ip地址 */
    private String ip;

    /** 字段:schema_name，数据库名称 */
    private String schemaName;

    /** 字段:table_name，表名 */
    private String tableName;

    /** 字段:status，状态 */
    private Integer status;

    /** 字段:gmt_time，创建时间 */
    private Date gmtTime;

    /** 字段:modify_time，修改时间 */
    private Date modifyTime;

    /** 字段:catlog */
    private String catlog;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType == null ? null : dbType.trim();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName == null ? null : schemaName.trim();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName == null ? null : tableName.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getGmtTime() {
        return gmtTime;
    }

    public void setGmtTime(Date gmtTime) {
        this.gmtTime = gmtTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getCatlog() {
        return catlog;
    }

    public void setCatlog(String catlog) {
        this.catlog = catlog == null ? null : catlog.trim();
    }
}
