package com.weiiboo.im.consumer;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.constant.RocketMQConsumerGroupConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.common.redis.constant.BloomFilterMap;
import com.weiiboo.common.redis.utils.BloomFilterUtils;
import com.weiiboo.im.handler.IMServerHandler;
import com.weiiboo.modules.api.im.vo.MessageVO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(
        topic = RocketMQTopicConstant.FIND_USER_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.FIND_USER_CONSUMER_GROUP,
        messageModel = MessageModel.BROADCASTING)
public class FindUserConsumer implements RocketMQListener<MessageExt> {

    private final BloomFilterUtils bloomFilterUtils;

    public FindUserConsumer(BloomFilterUtils bloomFilterUtils) {
        this.bloomFilterUtils = bloomFilterUtils;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        if (bloomFilterUtils.mightContainBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, messageExt.getMsgId())) {
            log.info("消息已经消费过：{}", messageExt.getMsgId());
            return;
        }
        String s = new String(messageExt.getBody());
        log.info("收到消息：{}",s);
        MessageVO message = JSON.parseObject(s, MessageVO.class);
        String messageTo = message.getTo();
        Channel channel = IMServerHandler.USER_CHANNEL_MAP.get(messageTo);
        if(channel!=null){
            log.info("找到用户：{}，发送消息：{}",messageTo,message);
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(message)));
        }
        bloomFilterUtils.addBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, messageExt.getMsgId());
    }
}
