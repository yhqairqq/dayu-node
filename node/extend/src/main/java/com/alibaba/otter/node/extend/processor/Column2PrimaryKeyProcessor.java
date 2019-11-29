package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import org.springframework.util.CollectionUtils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huaseng on 2019/8/12.
 */
public class Column2PrimaryKeyProcessor extends AbstractEventProcessor {

    public boolean process(EventData eventData) {

        List<EventColumn> eventColumns =  eventData.getColumns();
        List<EventColumn> keys =  eventData.getKeys();
        EventColumn cityEventColumn = null;
        EventColumn removeEventColumn = null;
        for(EventColumn eventColumn:eventColumns ){
            if(eventColumn.getColumnName().equals("city_id")){
                cityEventColumn =  eventColumn.clone();
                removeEventColumn = eventColumn;
            }
        }
       if(cityEventColumn == null){
            return true;
       }
        if(!CollectionUtils.isEmpty(keys)){
            if( eventData.getEventType() == EventType.UPDATE || eventData.getEventType() == EventType.DELETE ){
                //转变为主键属性
                cityEventColumn.setKey(true);
                cityEventColumn.setIndex(keys.size());
                cityEventColumn.setNull(false);
                cityEventColumn.setUpdate(true);
                keys.add(cityEventColumn);
                //在普通列中删除
                eventColumns.remove(removeEventColumn);
            }
       }

        return true;
    }

}
