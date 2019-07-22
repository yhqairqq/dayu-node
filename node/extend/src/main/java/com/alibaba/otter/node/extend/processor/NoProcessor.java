package com.alibaba.otter.node.extend.processor;

import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import com.alibaba.otter.shared.etl.model.EventType;
import org.springframework.util.CollectionUtils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class NoProcessor extends AbstractEventProcessor {
    public boolean process(EventData eventData) {
        return true;
    }


}
