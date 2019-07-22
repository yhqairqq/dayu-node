/*
 Navicat Premium Data Transfer

 Source Server         : otter-src
 Source Server Type    : MySQL
 Source Server Version : 50670
 Source Host           : 10.111.14.140
 Source Database       : otter

 Target Server Type    : MySQL
 Target Server Version : 50670
 File Encoding         : utf-8

 Date: 06/11/2019 14:55:06 PM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `alarm_rule`
-- ----------------------------
DROP TABLE IF EXISTS `alarm_rule`;
CREATE TABLE `alarm_rule` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `MONITOR_NAME` varchar(1024) DEFAULT NULL,
  `RECEIVER_KEY` varchar(1024) DEFAULT NULL,
  `STATUS` varchar(32) DEFAULT NULL,
  `PIPELINE_ID` bigint(20) NOT NULL,
  `DESCRIPTION` varchar(256) DEFAULT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `MATCH_VALUE` varchar(1024) DEFAULT NULL,
  `PARAMETERS` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `autokeeper_cluster`
-- ----------------------------
DROP TABLE IF EXISTS `autokeeper_cluster`;
CREATE TABLE `autokeeper_cluster` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CLUSTER_NAME` varchar(200) NOT NULL,
  `SERVER_LIST` varchar(1024) NOT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `canal`
-- ----------------------------
DROP TABLE IF EXISTS `canal`;
CREATE TABLE `canal` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `NAME` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `PARAMETERS` text,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `CANALUNIQUE` (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `channel`
-- ----------------------------
DROP TABLE IF EXISTS `channel`;
CREATE TABLE `channel` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `PARAMETERS` text,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `CHANNELUNIQUE` (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `column_pair`
-- ----------------------------
DROP TABLE IF EXISTS `column_pair`;
CREATE TABLE `column_pair` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `SOURCE_COLUMN` varchar(200) DEFAULT NULL,
  `TARGET_COLUMN` varchar(200) DEFAULT NULL,
  `DATA_MEDIA_PAIR_ID` bigint(20) NOT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `idx_DATA_MEDIA_PAIR_ID` (`DATA_MEDIA_PAIR_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `column_pair_group`
-- ----------------------------
DROP TABLE IF EXISTS `column_pair_group`;
CREATE TABLE `column_pair_group` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DATA_MEDIA_PAIR_ID` bigint(20) NOT NULL,
  `COLUMN_PAIR_CONTENT` text,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `idx_DATA_MEDIA_PAIR_ID` (`DATA_MEDIA_PAIR_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `data_matrix`
-- ----------------------------
DROP TABLE IF EXISTS `data_matrix`;
CREATE TABLE `data_matrix` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `GROUP_KEY` varchar(200) DEFAULT NULL,
  `MASTER` varchar(200) DEFAULT NULL,
  `SLAVE` varchar(200) DEFAULT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `GROUPKEY` (`GROUP_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `data_media`
-- ----------------------------
DROP TABLE IF EXISTS `data_media`;
CREATE TABLE `data_media` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` text NOT NULL,
  --`NAME` varchar(200) NOT NULL,
  `NAMESPACE` varchar(200) NOT NULL,
  `PROPERTIES` text NOT NULL,
  -- `PROPERTIES` varchar(1000) NOT NULL,
  `DATA_MEDIA_SOURCE_ID` bigint(20) NOT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `DATAMEDIAUNIQUE` (NAMESPACE`,`DATA_MEDIA_SOURCE_ID`)
  --UNIQUE KEY `DATAMEDIAUNIQUE` (`NAME`,`NAMESPACE`,`DATA_MEDIA_SOURCE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `data_media_pair`
-- ----------------------------
DROP TABLE IF EXISTS `data_media_pair`;
CREATE TABLE `data_media_pair` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `PULLWEIGHT` bigint(20) DEFAULT NULL,
  `PUSHWEIGHT` bigint(20) DEFAULT NULL,
  `RESOLVER` text,
  `FILTER` text,
  `SOURCE_DATA_MEDIA_ID` bigint(20) DEFAULT NULL,
  `TARGET_DATA_MEDIA_ID` bigint(20) DEFAULT NULL,
  `PIPELINE_ID` bigint(20) NOT NULL,
  `COLUMN_PAIR_MODE` varchar(20) DEFAULT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `idx_PipelineID` (`PIPELINE_ID`,`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `data_media_source`
-- ----------------------------
DROP TABLE IF EXISTS `data_media_source`;
CREATE TABLE `data_media_source` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(200) NOT NULL,
  `TYPE` varchar(20) NOT NULL,
  `PROPERTIES` varchar(1000) NOT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `DATAMEDIASOURCEUNIQUE` (`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `delay_stat`
-- ----------------------------
DROP TABLE IF EXISTS `delay_stat`;
CREATE TABLE `delay_stat` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DELAY_TIME` bigint(20) NOT NULL,
  `DELAY_NUMBER` bigint(20) NOT NULL,
  `PIPELINE_ID` bigint(20) NOT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `idx_PipelineID_GmtModified_ID` (`PIPELINE_ID`,`GMT_MODIFIED`,`ID`),
  KEY `idx_Pipeline_GmtCreate` (`PIPELINE_ID`,`GMT_CREATE`),
  KEY `idx_GmtCreate_id` (`GMT_CREATE`,`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `log_record`
-- ----------------------------
DROP TABLE IF EXISTS `log_record`;
CREATE TABLE `log_record` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NID` varchar(200) DEFAULT NULL,
  `CHANNEL_ID` varchar(200) NOT NULL,
  `PIPELINE_ID` varchar(200) NOT NULL,
  `TITLE` varchar(1000) DEFAULT NULL,
  `MESSAGE` text,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `logRecord_pipelineId` (`PIPELINE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `meta_history`
-- ----------------------------
DROP TABLE IF EXISTS `meta_history`;
CREATE TABLE `meta_history` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `destination` varchar(128) DEFAULT NULL COMMENT '通道名称',
  `binlog_file` varchar(64) DEFAULT NULL COMMENT 'binlog文件名',
  `binlog_offest` bigint(20) DEFAULT NULL COMMENT 'binlog偏移量',
  `binlog_master_id` varchar(64) DEFAULT NULL COMMENT 'binlog节点id',
  `binlog_timestamp` bigint(20) DEFAULT NULL COMMENT 'binlog应用的时间戳',
  `use_schema` varchar(1024) DEFAULT NULL COMMENT '执行sql时对应的schema',
  `sql_schema` varchar(1024) DEFAULT NULL COMMENT '对应的schema',
  `sql_table` varchar(1024) DEFAULT NULL COMMENT '对应的table',
  `sql_text` longtext COMMENT '执行的sql',
  `sql_type` varchar(256) DEFAULT NULL COMMENT 'sql类型',
  `extra` text COMMENT '额外的扩展信息',
  PRIMARY KEY (`id`),
  UNIQUE KEY `binlog_file_offest` (`destination`,`binlog_master_id`,`binlog_file`,`binlog_offest`),
  KEY `destination` (`destination`),
  KEY `destination_timestamp` (`destination`,`binlog_timestamp`),
  KEY `gmt_modified` (`gmt_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='表结构变化明细表';

-- ----------------------------
--  Table structure for `meta_snapshot`
-- ----------------------------
DROP TABLE IF EXISTS `meta_snapshot`;
CREATE TABLE `meta_snapshot` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `destination` varchar(128) DEFAULT NULL COMMENT '通道名称',
  `binlog_file` varchar(64) DEFAULT NULL COMMENT 'binlog文件名',
  `binlog_offest` bigint(20) DEFAULT NULL COMMENT 'binlog偏移量',
  `binlog_master_id` varchar(64) DEFAULT NULL COMMENT 'binlog节点id',
  `binlog_timestamp` bigint(20) DEFAULT NULL COMMENT 'binlog应用的时间戳',
  `data` longtext COMMENT '表结构数据',
  `extra` text COMMENT '额外的扩展信息',
  PRIMARY KEY (`id`),
  UNIQUE KEY `binlog_file_offest` (`destination`,`binlog_master_id`,`binlog_file`,`binlog_offest`),
  KEY `destination` (`destination`),
  KEY `destination_timestamp` (`destination`,`binlog_timestamp`),
  KEY `gmt_modified` (`gmt_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='表结构记录表快照表';

-- ----------------------------
--  Table structure for `node`
-- ----------------------------
DROP TABLE IF EXISTS `node`;
CREATE TABLE `node` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(200) NOT NULL,
  `IP` varchar(200) NOT NULL,
  `PORT` bigint(20) NOT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `PARAMETERS` text,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NODEUNIQUE` (`NAME`,`IP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `pipeline`
-- ----------------------------
DROP TABLE IF EXISTS `pipeline`;
CREATE TABLE `pipeline` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(200) DEFAULT NULL,
  `PARAMETERS` text,
  `CHANNEL_ID` bigint(20) NOT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `PIPELINEUNIQUE` (`NAME`,`CHANNEL_ID`),
  KEY `idx_ChannelID` (`CHANNEL_ID`,`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `pipeline_node_relation`
-- ----------------------------
DROP TABLE IF EXISTS `pipeline_node_relation`;
CREATE TABLE `pipeline_node_relation` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `NODE_ID` bigint(20) NOT NULL,
  `PIPELINE_ID` bigint(20) NOT NULL,
  `LOCATION` varchar(20) NOT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `idx_PipelineID` (`PIPELINE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `system_parameter`
-- ----------------------------
DROP TABLE IF EXISTS `system_parameter`;
CREATE TABLE `system_parameter` (
  `ID` bigint(20) unsigned NOT NULL,
  `VALUE` text,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_history_stat`
-- ----------------------------
DROP TABLE IF EXISTS `table_history_stat`;
CREATE TABLE `table_history_stat` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `FILE_SIZE` bigint(20) DEFAULT NULL,
  `FILE_COUNT` bigint(20) DEFAULT NULL,
  `INSERT_COUNT` bigint(20) DEFAULT NULL,
  `UPDATE_COUNT` bigint(20) DEFAULT NULL,
  `DELETE_COUNT` bigint(20) DEFAULT NULL,
  `DATA_MEDIA_PAIR_ID` bigint(20) DEFAULT NULL,
  `PIPELINE_ID` bigint(20) DEFAULT NULL,
  `START_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `END_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `idx_DATA_MEDIA_PAIR_ID_END_TIME` (`DATA_MEDIA_PAIR_ID`,`END_TIME`),
  KEY `idx_GmtCreate_id` (`GMT_CREATE`,`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_stat`
-- ----------------------------
DROP TABLE IF EXISTS `table_stat`;
CREATE TABLE `table_stat` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `FILE_SIZE` bigint(20) NOT NULL,
  `FILE_COUNT` bigint(20) NOT NULL,
  `INSERT_COUNT` bigint(20) NOT NULL,
  `UPDATE_COUNT` bigint(20) NOT NULL,
  `DELETE_COUNT` bigint(20) NOT NULL,
  `DATA_MEDIA_PAIR_ID` bigint(20) NOT NULL,
  `PIPELINE_ID` bigint(20) NOT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `idx_PipelineID_DataMediaPairID` (`PIPELINE_ID`,`DATA_MEDIA_PAIR_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `throughput_stat`
-- ----------------------------
DROP TABLE IF EXISTS `throughput_stat`;
CREATE TABLE `throughput_stat` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `TYPE` varchar(20) NOT NULL,
  `NUMBER` bigint(20) NOT NULL,
  `SIZE` bigint(20) NOT NULL,
  `PIPELINE_ID` bigint(20) NOT NULL,
  `START_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `END_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  KEY `idx_PipelineID_Type_GmtCreate_ID` (`PIPELINE_ID`,`TYPE`,`GMT_CREATE`,`ID`),
  KEY `idx_PipelineID_Type_EndTime_ID` (`PIPELINE_ID`,`TYPE`,`END_TIME`,`ID`),
  KEY `idx_GmtCreate_id` (`GMT_CREATE`,`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USERNAME` varchar(20) NOT NULL,
  `PASSWORD` varchar(20) NOT NULL,
  `AUTHORIZETYPE` varchar(20) NOT NULL,
  `DEPARTMENT` varchar(20) NOT NULL,
  `REALNAME` varchar(20) NOT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `USERUNIQUE` (`USERNAME`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;



CREATE TABLE `retl_buffer` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `TABLE_ID` int(11) NOT NULL,
  `FULL_NAME` varchar(512) DEFAULT NULL,
  `TYPE` char(1) NOT NULL,
  `PK_DATA` varchar(256) NOT NULL,
  `GMT_CREATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `GMT_MODIFIED` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

-- ----------------------------
--  Records of `user`
-- ----------------------------
BEGIN;
INSERT INTO `user` VALUES ('1', 'admin', '801fc357a5a74743894a', 'ADMIN', 'admin', 'admin', '2019-05-15 14:10:51', '2019-05-15 14:10:51'), ('2', 'guest', '471e02a154a2121dc577', 'OPERATOR', 'guest', 'guest', '2019-05-15 14:10:51', '2019-05-15 14:10:51');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
