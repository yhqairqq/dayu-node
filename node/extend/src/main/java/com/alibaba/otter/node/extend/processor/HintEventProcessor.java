package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;

import java.util.List;

/**
 * 测试下hint
 *
 * @author jianghang 2014-6-11 下午4:20:32
 * @since 5.1.0
 */
public class HintEventProcessor extends AbstractEventProcessor {
    public boolean process(EventData eventData) {
        List<EventColumn> eventColumns =  eventData.getColumns();
        List<EventColumn> keys =  eventData.getKeys();
        List<EventColumn> updateKeys = eventData.getUpdatedKeys();
        String fixId = "";
        for(EventColumn eventColumn:eventColumns ){
            if(eventColumn.getColumnName().equals("catlog")){
                fixId  = fixId + eventColumn.getColumnValue();
            }
        }
        if(eventData.getEventType() == EventType.INSERT){
            if(keys.size() == 1){
                String key =  keys.get(0).getColumnValue();
                keys.get(0).setColumnValue(key+fixId);
            }
        }else if(eventData.getEventType() == EventType.UPDATE){
            if(updateKeys.size() == 1){
                String key =  updateKeys.get(0).getColumnValue();
                updateKeys.get(0).setColumnValue(key+fixId);
            }
        }

        return true;
    }
}