package com.weiiboo.es.consumer.notes;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.constant.RocketMQConsumerGroupConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.es.service.NotesSearchService;
import com.weiiboo.modules.api.notes.domin.NotesDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.NOTES_UPDATE_ES_TOPIC,
                            consumerGroup = RocketMQConsumerGroupConstant.NOTES_UPDATE_ES_CONSUMER_GROUP)
public class NotesUpdateEsConsumer implements RocketMQListener<String> {
    @Resource
    private NotesSearchService notesSearchService;

    @Override
    public void onMessage(String message) {
        log.info("{}收到消息: {}",RocketMQTopicConstant.NOTES_UPDATE_ES_TOPIC,message);
        NotesDO notesDO = JSON.parseObject(message,NotesDO.class);
        log.info("notesDO: {}",notesDO);
        notesSearchService.updateNotes(notesDO);
    }
}
