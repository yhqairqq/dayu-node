package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import org.springframework.util.CollectionUtils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class IdMergeCityIdProcessor extends AbstractEventProcessor {
    public boolean process(EventData eventData) {

        List<EventColumn> eventColumns =  eventData.getColumns();
        List<EventColumn> keys =  eventData.getKeys();
        String fixId = "";
        for(EventColumn eventColumn:eventColumns ){
            if(eventColumn.getColumnName().equals("city_id")){
                fixId  = fixId + eventColumn.getColumnValue();
            }
        }
        if("".equals(fixId)){
            return true;
        }

        if(!CollectionUtils.isEmpty(keys)){
            if(eventData.getEventType() == EventType.INSERT || eventData.getEventType() == EventType.UPDATE || eventData.getEventType() == EventType.DELETE ){
                List<EventColumn> newEventColumns =  new ArrayList<>();
                /**
                 * 通过城市city_id + id  ==> merge_id  Varchar  types.VARCHAR ->12
                 */
                EventColumn orgiKeyColumn = keys.get(0);
                EventColumn newKeyColumn = orgiKeyColumn.clone();
                newKeyColumn.setColumnName("merge_id");
                newKeyColumn.setColumnValue(fixId+newKeyColumn.getColumnValue());
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
        if(!CollectionUtils.isEmpty(eventData.getOldKeys())){
            List<EventColumn> oldKeys =  eventData.getOldKeys();
            EventColumn column = oldKeys.get(0);
            column.setColumnValue(fixId+column.getColumnValue());
        }

        return true;
    }


}
