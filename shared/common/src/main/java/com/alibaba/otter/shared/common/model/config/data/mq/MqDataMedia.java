/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.shared.common.model.config.data.mq;

import com.alibaba.otter.shared.common.model.config.data.DataMedia;

/**
 * NapoliSender对象的实现
 * 
 * @author simon 2012-6-19 下午10:49:08
 * @version 4.1.0
 */
public class MqDataMedia extends DataMedia<MqMediaSource> {



    private static final long serialVersionUID = 1347886915428919398L;

    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
