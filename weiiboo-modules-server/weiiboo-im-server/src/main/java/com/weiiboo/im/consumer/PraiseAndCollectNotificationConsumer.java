package com.weiiboo.im.consumer;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.constant.RocketMQConsumerGroupConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.common.redis.constant.BloomFilterMap;
import com.weiiboo.common.redis.utils.BloomFilterUtils;
import com.weiiboo.im.handler.types.ChatHandler;
import com.weiiboo.modules.api.im.vo.MessageVO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.weiiboo.im.handler.IMServerHandler.USER_CHANNEL_MAP;

@Component
@Slf4j
@RocketMQMessageListener(
        topic = RocketMQTopicConstant.PRAISE_AND_COLLECT_REMIND_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.PRAISE_AND_COLLECT_REMIND_CONSUMER_GROUP)
public class PraiseAndCollectNotificationConsumer implements RocketMQListener<MessageExt> {
    @Resource
    private ChatHandler chatHandler;
    @Resource
    private BloomFilterUtils bloomFilterUtils;

    @Override
    public void onMessage(MessageExt messageExt) {
        if (bloomFilterUtils.mightContainBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, messageExt.getMsgId())) {
            log.info("消息已经消费过：{}", messageExt.getMsgId());
            return;
        }
        String s = new String(messageExt.getBody());
        log.info("收到消息：{}", s);
        MessageVO message = JSON.parseObject(s, MessageVO.class);
        Channel channel = USER_CHANNEL_MAP.get(message.getTo());
        chatHandler.sendMessage(channel, message);
        bloomFilterUtils.addBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, messageExt.getMsgId());
    }
}