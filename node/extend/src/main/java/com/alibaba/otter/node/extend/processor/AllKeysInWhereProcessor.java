package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import joptsimple.internal.Strings;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by huaseng on 2019/8/12.
 */
public class AllKeysInWhereProcessor extends AbstractEventProcessor {
    public boolean process(EventData eventData) {
        List<EventColumn> afterColumns =  eventData.getColumns();
        List<EventColumn> keys =  eventData.getKeys();
        Map<String,String> beforeColumns =  eventData.getBefore();

        if(!CollectionUtils.isEmpty(keys)){
            if( eventData.getEventType() == EventType.UPDATE || eventData.getEventType() == EventType.DELETE ){
                if(eventData.getEventType() == EventType.UPDATE){
                    for(EventColumn eventColumn:afterColumns ){
                            if(!Strings.isNullOrEmpty(eventColumn.getColumnValue())){
                                String beforeValue = beforeColumns.get(eventColumn.getColumnName());
                                if(!Strings.isNullOrEmpty(beforeValue)){
                                    EventColumn newKey = eventColumn.clone();
                                    newKey.setKey(true);
                                    newKey.setIndex(keys.size());
                                    newKey.setNull(false);
                                    newKey.setUpdate(true);
                                    newKey.setColumnValue(beforeValue);
                                    keys.add(newKey);
                                }
                            }
                    }
                }else{
                    for(EventColumn eventColumn:afterColumns ){
                        if(!Strings.isNullOrEmpty(eventColumn.getColumnValue())){
                            EventColumn newKey = eventColumn.clone();
                            newKey.setKey(true);
                            newKey.setIndex(keys.size());
                            newKey.setNull(false);
                            newKey.setUpdate(true);
                            keys.add(newKey);
                        }
                    }
                }
            }
        }
        return true;

    }
}

