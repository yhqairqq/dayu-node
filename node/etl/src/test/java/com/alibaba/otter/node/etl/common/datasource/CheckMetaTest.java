package com.alibaba.otter.node.etl.common.datasource;

import com.alibaba.otter.canal.parse.driver.mysql.packets.server.ResultSetPacket;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlConnection;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by huahua on 2019/7/20.
 */
public class CheckMetaTest {

    //专门检测主键是否存在
    @Test
    public void test3() throws IOException {
        InetAddress inetAddress = InetAddress.getByName("rm-bp1e079ew20s5t566.mysql.rds.aliyuncs.com");
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress,3306);

        MysqlConnection mysqlConnection = new MysqlConnection(inetSocketAddress,"caicai","a0B!98#$");
        mysqlConnection.connect();
        String schema = "feature1";
        String tables = "tbl_app_user;tbl_receipt_addr;tbl_app_user_level;tbl_app_user_sign;tbl_app_user_vip;tbl_spread_user;act_invite;tbl_points_update_log;uc_user_oauth;tbl_app_user_rebate;tbl_app_user_rebate_detail;tbl_app_user_login_log;tbl_store_sku_diff;tbl_store_sku_forecast;tbl_product_classify;tbl_hq_product;erp_warehouse;erp_warehouse_store;tbl_role;tbl_menu;tbl_role_menu;tbl_user;tbl_user_role;tbl_sys_param;tbl_sys_code;tbl_city;tbl_excel_model;tbl_push_mould;tbl_award_action;tbl_award_cost;tbl_award_log;tbl_prize_param;tbl_recharge_log_bak;tbl_award_info;tbl_tv;tbl_tv_play_parameter;tbl_appversion;tbl_app_patch;tbl_prize_user;tbl_prize;tbl_prize_log;tbl_card;tbl_card_batch;tbl_recharge_log;tbl_recharge_point_discount;tbl_award_black;tbl_award_info;tbl_prize_type;tbl_award_user;tbl_tv_play_material;tbl_auto_reply_text;tbl_prize_black;tbl_city_product_recommend;tbl_product_tax;erp_purchase_order;erp_purchase_order_detail;";
        String[] tableArr = tables.split(";");
        for(String table:tableArr){
            String fullName = getFullName(schema,table);
            ResultSetPacket packet = mysqlConnection.query("show create table " + fullName);
            String createDDL = null;
            if (packet.getFieldValues().size() > 0) {
                createDDL = packet.getFieldValues().get(1);
                if(!createDDL.contains("PRIMARY KEY")){
                    System.out.println(fullName);
//                    String command = String.format("use %s alter table %s add primary key %s (id)",schema,table,table);
//                    ResultSetPacket packet2 = mysqlConnection.query(command);
//                    System.out.println(packet2);
//                    break;
                }
            }
        }
        mysqlConnection.disconnect();
    }

    @Test
    public void test1() throws IOException {
        InetAddress inetAddress = InetAddress.getByName("rm-bp10p2vl5x3c4mum5160.mysql.rds.aliyuncs.com");
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress,3306);

        MysqlConnection mysqlConnection = new MysqlConnection(inetSocketAddress,"caicai","123!@#qwe");
        mysqlConnection.connect();
        String schema = "caicai";
        String tables = "tbl_balance_log;tbl_app_user;tbl_receipt_addr;tbl_app_user_level;tbl_app_user_sign;tbl_app_user_vip;tbl_spread_user;act_invite;tbl_points_update_log;uc_user_oauth;tbl_app_user_rebate;tbl_app_user_rebate_detail;tbl_app_user_login_log;tbl_store_sku_diff;tbl_store_sku_forecast;tbl_product_classify;tbl_hq_product;erp_warehouse;erp_warehouse_store;tbl_role;tbl_menu;tbl_role_menu;tbl_user;tbl_user_role;tbl_sys_param;tbl_sys_code;tbl_city;tbl_excel_model;tbl_push_mould;tbl_award_action;tbl_award_cost;tbl_award_log;tbl_prize_param;tbl_recharge_log_bak;tbl_award_info;tbl_tv;tbl_tv_play_parameter;tbl_appversion;tbl_app_patch;tbl_prize_user;tbl_prize;tbl_prize_log;tbl_card;tbl_card_batch;tbl_recharge_log;tbl_recharge_point_discount;tbl_award_black;tbl_award_info;tbl_prize_type;tbl_award_user;tbl_tv_play_material;tbl_auto_reply_text;tbl_prize_black;tbl_city_product_recommend;tbl_product_tax;erp_purchase_order;erp_purchase_order_detail;";
        String[] tableArr = tables.split(";");
        for(String table:tableArr){
            String fullName = getFullName(schema,table);
            ResultSetPacket packet = mysqlConnection.query("show create table " + fullName);
            String createDDL = null;
            if (packet.getFieldValues().size() > 0) {
                createDDL = packet.getFieldValues().get(1);
                if(!createDDL.contains("PRIMARY KEY")){
                    System.out.println(fullName);
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
    @Test
    public void test2() throws IOException {
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

}
