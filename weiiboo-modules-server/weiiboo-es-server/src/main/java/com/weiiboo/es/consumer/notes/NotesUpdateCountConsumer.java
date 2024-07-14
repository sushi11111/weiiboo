package com.weiiboo.es.consumer.notes;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.constant.RocketMQConsumerGroupConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.es.service.NotesSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.NOTES_UPDATE_COUNT_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.NOTES_UPDATE_COUNT_CONSUMER_GROUP)
public class NotesUpdateCountConsumer implements RocketMQListener<String> {
    private final NotesSearchService notesSearchService;

    public NotesUpdateCountConsumer(NotesSearchService notesSearchService) {
        this.notesSearchService = notesSearchService;
    }

    @Override
    public void onMessage(String message) {
        log.info("{}收到消息: {}",RocketMQTopicConstant.NOTES_UPDATE_COUNT_TOPIC,message);
        Map<String,String> map = JSON.parseObject(message, Map.class);
        log.info("notesId:{},notesLikeNum:{},type:{}", map.get("notesId"), map.get("notesLikeNum"), map.get("type"));
        notesSearchService.updateCount(map);
    }
}
