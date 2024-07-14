package com.weiiboo.note.consumer;

import com.weiiboo.common.constant.RocketMQConsumerGroupConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.NOTES_REMOVE_REDIS_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.NOTES_REMOVE_REDIS_CONSUMER_GROUP)
public class NotesRemoveCacheConsumer implements RocketMQListener<String> {

    @Resource
    private RedisCache redisCache;

    @Override
    public void onMessage(String message) {
        log.info("remove cache: {}", message);
        redisCache.delAllPrefix(message);
    }
}