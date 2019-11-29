package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import joptsimple.internal.Strings;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by huaseng on 2019/8/12.
 */
public class Columns2PrimaryKeyProcessor extends AbstractEventProcessor {

    public boolean process(EventData eventData) {
        List<EventColumn> eventColumns =  eventData.getColumns();
        List<EventColumn> keys =  eventData.getKeys();
        if(!CollectionUtils.isEmpty(keys)){

            if( eventData.getEventType() == EventType.UPDATE || eventData.getEventType() == EventType.DELETE ){

                //转变为主键属性
                for(EventColumn eventColumn:eventColumns ){
                    if(!eventColumn.isUpdate() || eventData.getEventType() == EventType.DELETE){
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
                //在普通列中删除
            }
       }

        return true;
    }

}
