package com.alibaba.otter.node.extend.load.mq;

import com.alibaba.otter.node.extend.load.mq.model.JsonModel;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yanghuanqing@wdai.com on 2018/5/3.
 */
public class MessageConvertUtils {
    public static Object toParse(EventData eventData, MessageOutType messageOutType) {
        if (MessageOutType.CSV.equals(messageOutType)) {
            return convertCsv(eventData);
        } else {
            return convertJson(eventData);
        }
    }

    private static String convertCsv(EventData eventData) {
        /**
         * 格式   UPDATE,1525228546000,id,topic_name,1525229019354,example3
         *        事件类型，执行时间，主键，schema，发送时间，table，[动态列，动态列]
         */
        String spilitFix = ",";
        StringBuffer buffer = new StringBuffer();
        buffer.append(eventData.getEventType().name());
        buffer.append(spilitFix);
        buffer.append(eventData.getExecuteTime());
        buffer.append(spilitFix);
        buffer.append(eventData.getKeys().get(0).getColumnName());
        buffer.append(spilitFix);
        buffer.append(eventData.getSchemaName());
        buffer.append(spilitFix);
        buffer.append(System.currentTimeMillis());
        buffer.append(spilitFix);
        buffer.append(eventData.getTableName());
        buffer.append(spilitFix);
        buffer.append(eventData.getKeys().get(0).getColumnValue());
        buffer.append(spilitFix);
        int index = 0;
        for (EventColumn eventColumn : eventData.getColumns()) {
            buffer.append(eventColumn.getColumnValue());
            if (index++ < eventData.getColumns().size() - 1) {
                buffer.append(spilitFix);
            }
        }
        return buffer.toString();
    }

    private static JsonModel convertJson(EventData eventData) {
        JsonModel jsonModel = new JsonModel();
        jsonModel.setSchema(eventData.getSchemaName());
        jsonModel.setTable(eventData.getTableName());
        jsonModel.setExecuteTime(eventData.getExecuteTime());
        jsonModel.setSendTime(System.currentTimeMillis());
        jsonModel.setEventType(eventData.getEventType().name());
        if(eventData.getEventType().isDdl()){
            jsonModel.setSql(eventData.getSql().replaceAll("[\\r|\\n|\\t]"," "));
            return jsonModel;
        }


        Map<String, Object> columns = new HashMap<String, Object>();
        Map<String, Object> columnsType = new HashMap<String, Object>();
        for (EventColumn eventColumn : eventData.getColumns()) {
            columns.put(eventColumn.getColumnName(), eventColumn.getColumnValue());
            columnsType.put(eventColumn.getColumnName(), JDBCType.valueOf(eventColumn.getColumnType()).getName());
        }
        //添加主键

        for(EventColumn column:eventData.getKeys()){
            jsonModel.getPrimaryKeys().add(column.getColumnName());
            columns.put(column.getColumnName(), column.getColumnValue());
            columnsType.put(column.getColumnName(), JDBCType.valueOf(column.getColumnType()));
        }



        jsonModel.setColumns(columns);
        jsonModel.setColumnsType(columnsType);
        return jsonModel;
    }
}
