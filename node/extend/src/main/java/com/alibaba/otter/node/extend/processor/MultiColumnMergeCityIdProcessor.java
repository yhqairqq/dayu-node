package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import org.springframework.util.CollectionUtils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MultiColumnMergeCityIdProcessor extends AbstractEventProcessor {
    public boolean process(EventData eventData) {
        //merge_id = store_id + city_product_id + warehouse_id + id
        List<EventColumn> eventColumns =  eventData.getColumns();
        List<EventColumn> keys =  eventData.getKeys();
        String store_id = "";
        String city_product_id = "";
        String warehouse_id = "";
        for(EventColumn eventColumn:eventColumns ){
            if(eventColumn.getColumnName().equals("store_id")){
                store_id  = store_id + eventColumn.getColumnValue();
            }
            if(eventColumn.getColumnName().equals("city_product_id")){
                city_product_id = city_product_id + eventColumn.getColumnValue();
            }
            if(eventColumn.getColumnName().equals("warehouse_id")){
                warehouse_id = warehouse_id + eventColumn.getColumnValue();
            }
        }
       StringBuffer fixId = new StringBuffer();
        fixId.append(store_id);
        fixId.append(city_product_id);
        fixId.append(warehouse_id);

        if(!CollectionUtils.isEmpty(keys)){
            if(eventData.getEventType() == EventType.INSERT || eventData.getEventType() == EventType.UPDATE || eventData.getEventType() == EventType.DELETE ){
                List<EventColumn> newEventColumns =  new ArrayList<>();
                /**
                 * 通过城市city_id + id  ==> merge_id  Varchar  types.VARCHAR ->12
                 */
                EventColumn orgiKeyColumn = keys.get(0);
                EventColumn newKeyColumn = orgiKeyColumn.clone();
                newKeyColumn.setColumnName("merge_id");
                newKeyColumn.setColumnValue(fixId.toString()+newKeyColumn.getColumnValue());
                newKeyColumn.setColumnType(Types.VARCHAR);
                newKeyColumn.setIndex(0);
                 //清除原来id
                 keys.clear();
                 //更改ID
                 keys.add(newKeyColumn);
                //原有主键变为普通列
                orgiKeyColumn.setKey(false);
                orgiKeyColumn.setIndex(orgiKeyColumn.getIndex()+1);
                newEventColumns.add(orgiKeyColumn);
                for(EventColumn eventColumn:eventColumns){
                    eventColumn.setIndex(eventColumn.getIndex()+1);
                }
                newEventColumns.addAll(eventColumns);

                eventData.setColumns(newEventColumns);

            }
        }


        return true;
    }


}
