package com.alibaba.otter.shared.compare.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huaseng on 2019/8/6.
 * 表的索引信息
 */
public class IndexTable {
    private String schema;
    private String table;
    private List<String> columns;
    private List<List<String>> datas = new ArrayList<>();

    /**
     * 判断指定列名是否有索引
     * @param value
     * @return
     */
    public boolean isIndexField(String value){
        for(List<String> values:datas){
            if(values.contains(value)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断指定列名是否为主键
     * @param value
     * @return
     */
    public boolean isPrimaryKeyField(String value){
        for(List<String> values:datas){
            if(values.contains(value) && values.contains("PRIMARY")){
                return true;
            }
        }
        return false;
    }

    public String getPrimaryKeyFieldName(){
        /**
         *  Table     | Non_unique | Key_name      | Seq_in_index | Column_name     | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment | Visible |
         +
         */
        int colindex = -1;
        for(int i = 0 ;i < columns.size(); i++){
            if(columns.get(i).equals("Column_name")){
                colindex = i;
                break;
            }
        }
        if(colindex == -1){
            return null;
        }
        for(List<String> values : datas){
            if(values.contains("PRIMARY")){
               return values.get(colindex);
            }
        }
        return null;
    }



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

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<String>> getDatas() {
        return datas;
    }

    public void setDatas(List<List<String>> datas) {
        this.datas = datas;
    }
}
