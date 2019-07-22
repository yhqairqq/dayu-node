package com.alibaba.otter.node.etl.common.datasource;

import com.alibaba.otter.canal.parse.driver.mysql.packets.server.ResultSetPacket;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlConnection;
import ext.jtester.org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huahua on 2019/7/20.
 */
public class MainTest {
    @Test
    public  static void test1() throws IOException {

        String url = "rm-bp10p2vl5x3c4mum5160.mysql.rds.aliyuncs.com";
        String username = "caicai";
        String passowrd = "123!@#qwe";
//        String url = "rm-bp1e079ew20s5t566.mysql.rds.aliyuncs.com";
//        String username = "caicai";
//        String passowrd = "a0B!98#$";
        InetAddress inetAddress = InetAddress.getByName(url);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress,3306);
        MysqlConnection mysqlConnection = new MysqlConnection(inetSocketAddress,username,passowrd);
        mysqlConnection.connect();
        String schema = "caicai";
        String tables = "tbl_balance_log;tbl_app_user;tbl_receipt_addr;tbl_app_user_level;tbl_app_user_sign;tbl_app_user_vip;tbl_spread_user;act_invite;tbl_points_update_log;uc_user_oauth;tbl_app_user_rebate;tbl_app_user_rebate_detail;tbl_app_user_login_log;tbl_store_sku_diff;tbl_store_sku_forecast;tbl_product_classify;tbl_hq_product;erp_warehouse;erp_warehouse_store;tbl_role;tbl_menu;tbl_role_menu;tbl_user;tbl_user_role;tbl_sys_param;tbl_sys_code;tbl_city;tbl_excel_model;tbl_push_mould;tbl_award_action;tbl_award_cost;tbl_award_log;tbl_prize_param;tbl_recharge_log_bak;tbl_award_info;tbl_tv;tbl_tv_play_parameter;tbl_appversion;tbl_app_patch;tbl_prize_user;tbl_prize;tbl_prize_log;tbl_card;tbl_card_batch;tbl_recharge_log;tbl_recharge_point_discount;tbl_award_black;tbl_award_info;tbl_prize_type;tbl_award_user;tbl_tv_play_material;tbl_auto_reply_text;tbl_prize_black;tbl_city_product_recommend;tbl_product_tax;erp_purchase_order;erp_purchase_order_detail;";
        String[] tableArr = tables.split(";");
        StringBuffer sql = new StringBuffer();
        for(String table:tableArr){
            String fullName = getFullName(schema,table);
            sql.append("show create table " + fullName+";");
        }


        List<ResultSetPacket> resultSetPackets = mysqlConnection.queryMulti(sql.toString());
        String createDDL = null;
        for(ResultSetPacket packet:resultSetPackets){
            if (packet.getFieldValues().size() > 0) {
                createDDL = packet.getFieldValues().get(1);
                if(!createDDL.contains("PRIMARY KEY")){
                    System.out.println(packet.getFieldValues().get(0));
//                    String command = String.format("use %s alter table %s add primary key %s (id)",schema,table,table);
//                    ResultSetPacket packet2 = mysqlConnection.query(command);
//                    System.out.println(packet2);
//                    break;
                }
            }
        }

        mysqlConnection.disconnect();
    }

    //加主键

    public static void test2() throws IOException {
        //alter table tb_name change add add int(10) not null auto_increment primary key;
        InetAddress inetAddress = InetAddress.getByName("rm-bp10p2vl5x3c4mum5160.mysql.rds.aliyuncs.com");
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress,3306);

        MysqlConnection mysqlConnection = new MysqlConnection(inetSocketAddress,"caicai","123!@#qwe");
        mysqlConnection.connect();
        String schema = "caicai";
        String tables = "tbl_balance_log;tbl_app_user;tbl_receipt_addr;tbl_app_user_level;tbl_app_user_sign;tbl_app_user_vip;tbl_spread_user;act_invite;tbl_points_update_log;uc_user_oauth;tbl_app_user_rebate;tbl_app_user_rebate_detail;tbl_app_user_login_log;tbl_store_sku_diff;tbl_store_sku_forecast;tbl_product_classify;tbl_hq_product;erp_warehouse;erp_warehouse_store;tbl_role;tbl_menu;tbl_role_menu;tbl_user;tbl_user_role;tbl_sys_param;tbl_sys_code;tbl_city;tbl_excel_model;tbl_push_mould;tbl_award_action;tbl_award_cost;tbl_award_log;tbl_prize_param;tbl_recharge_log_bak;tbl_award_info;tbl_tv;tbl_tv_play_parameter;tbl_appversion;tbl_app_patch;tbl_prize_user;tbl_prize;tbl_prize_log;tbl_card;tbl_card_batch;tbl_recharge_log;tbl_recharge_point_discount;tbl_award_black;tbl_award_info;tbl_prize_type;tbl_award_user;tbl_tv_play_material;tbl_auto_reply_text;tbl_prize_black;tbl_city_product_recommend;tbl_product_tax;erp_purchase_order;erp_purchase_order_detail;";
        String[] tableArr = tables.split(";");
        for(String table:tableArr){
            String fullName = getFullName(schema,table);
            String command = String.format("alter table %s change add add int(10) not null auto_increment primary key",fullName);
            ResultSetPacket packet = mysqlConnection.query(command);

        }
        mysqlConnection.disconnect();
    }


    public static void test3() throws IOException {
        InetAddress inetAddress = InetAddress.getByName("rm-bp10p2vl5x3c4mum5160.mysql.rds.aliyuncs.com");
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress,3306);

        MysqlConnection mysqlConnection = new MysqlConnection(inetSocketAddress,"caicai","123!@#qwe");
        mysqlConnection.connect();
        ResultSetPacket packet = mysqlConnection.query("\n" +
                "select count(1) from INFORMATION_SCHEMA.KEY_COLUMN_USAGE t where t.TABLE_SCHEMA = 'caicai' and t.TABLE_NAME = 'tbl_app_user' and t.CONSTRAINT_NAME= 'PRIMARY'");
        System.out.println(
                packet
        );
        mysqlConnection.disconnect();
    }
    private static final String CHECK_PRIMARY_KEY = "select count(1) from INFORMATION_SCHEMA.KEY_COLUMN_USAGE t where t.TABLE_SCHEMA = {0} and t.TABLE_NAME = {1} and t.CONSTRAINT_NAME = {2}";
    public static void test4() throws IOException {
//        connection.query("show full tables from `" + schema + "` where Table_type = 'BASE TABLE'");
        InetAddress inetAddress = InetAddress.getByName("rm-bp10p2vl5x3c4mum5160.mysql.rds.aliyuncs.com");
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress,3306);

        MysqlConnection mysqlConnection = new MysqlConnection(inetSocketAddress,"caicai","123!@#qwe");
        mysqlConnection.connect();
        ResultSetPacket packet = mysqlConnection.query("show full tables from `" + "caicai" + "` where Table_type = 'BASE TABLE'");
        List<String> tables = new ArrayList<String>();
        for (String table : packet.getFieldValues()) {
            if ("BASE TABLE".equalsIgnoreCase(table)) {
                continue;
            }
            String fullName = "caicai" + "." + table;
            ResultSetPacket packet2 =  mysqlConnection.query(MessageFormat.format(CHECK_PRIMARY_KEY,"'caicai'","'"+table+"'","'PRIMARY'"));
            if(!org.springframework.util.CollectionUtils.isEmpty(packet2.getFieldValues())){
                if(!"1".equals(packet2.getFieldValues().get(0))){
                    tables.add(fullName);
                }
            }
        }

        System.out.println(
                tables
        );
        mysqlConnection.disconnect();
    }

    public static  String getFullName(String schema, String table) {
        StringBuilder builder = new StringBuilder();
        return builder.append('`')
                .append(schema)
                .append('`')
                .append('.')
                .append('`')
                .append(table)
                .append('`')
                .toString();
    }
    public static void main(String[] args) throws IOException {
        test4();
    }
}
