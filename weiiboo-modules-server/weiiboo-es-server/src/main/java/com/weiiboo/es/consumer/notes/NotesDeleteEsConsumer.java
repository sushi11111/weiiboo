package com.weiiboo.es.consumer.notes;

import com.weiiboo.common.constant.RocketMQConsumerGroupConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.es.service.NotesSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.NOTES_DELETE_ES_TOPIC,
            consumerGroup = RocketMQConsumerGroupConstant.NOTES_DELETE_ES_CONSUMER_GROUP)
public class NotesDeleteEsConsumer implements RocketMQListener<String> {
    @Resource
    private NotesSearchService notesSearchService;

    @Override
    public void onMessage(String message) {
        log.info("{}收到消息: {}",RocketMQTopicConstant.NOTES_DELETE_ES_TOPIC,message);
        Long notesId = Long.valueOf(message);
        log.info("notesId: {}",notesId);
        notesSearchService.deleteNotes(notesId);
    }
}
