package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import joptsimple.internal.Strings;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by huaseng on 2019/8/12.
 */
public class UnixColumns2KeysProcessor extends AbstractEventProcessor {
    private List<String> candidateKeys = Arrays.asList(
            "city_product_id",
            "warehouse_id",
            "store_id",
            "spec_id",
            "classify_id",
            "product_no",
            "order_id",
            "city_id",
            "area_id",
            "classify_no",
            "user_id",
            "role_id",
            "lv1_classify_id",
            "group_id");
    public boolean process(EventData eventData) {
        List<EventColumn> eventColumns =  eventData.getColumns();
        List<EventColumn> keys =  eventData.getKeys();
        if(!CollectionUtils.isEmpty(keys)){
            if( eventData.getEventType() == EventType.UPDATE || eventData.getEventType() == EventType.DELETE ){
                for(EventColumn eventColumn:eventColumns ){
                        if(!Strings.isNullOrEmpty(eventColumn.getColumnValue())){
                          if(candidateKeys.contains(eventColumn.getColumnName()) &&!eventColumn.isUpdate()){
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
