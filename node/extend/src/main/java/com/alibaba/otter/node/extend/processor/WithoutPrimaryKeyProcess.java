package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class WithoutPrimaryKeyProcess extends AbstractEventProcessor {
    public boolean process(EventData eventData) {
        List<EventColumn> keys =  eventData.getKeys();
        List<EventColumn> columns = eventData.getColumns();
        if(!CollectionUtils.isEmpty(keys)){
            return true;
        }
        if(eventData.getEventType().isUpdate() || eventData.getEventType().isDelete() || eventData.getEventType().isInsert()){
            EventColumn column = null;
            EventColumn removeColumn = null;
            for(EventColumn eventColumn:columns){
                if(eventColumn.getColumnName().toLowerCase().equals("id")){
                    column = eventColumn.clone();
                    removeColumn = eventColumn;
                    break;
                }
            }

            if(column != null){
                columns.remove(removeColumn);
                column.setKey(true);
                column.setIndex(0);
                column.setUpdate(true);
                column.setNull(false);
                keys.add(column);
            }

        }

        return true;
    }


}
