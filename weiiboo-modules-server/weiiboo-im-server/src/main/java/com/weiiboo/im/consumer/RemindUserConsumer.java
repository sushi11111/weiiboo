package com.weiiboo.im.consumer;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.constant.RocketMQConsumerGroupConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.common.redis.constant.BloomFilterMap;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.BloomFilterUtils;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.im.handler.types.ChatHandler;
import com.weiiboo.modules.api.im.vo.MessageVO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

import static com.weiiboo.im.handler.IMServerHandler.USER_CHANNEL_MAP;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.NOTES_REMIND_TARGET_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.NOTES_REMIND_TARGET_CONSUMER_GROUP)
public class RemindUserConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private ChatHandler chatHandler;
    @Resource
    private RedisCache redisCache;
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
        Map<String,Object> map = JSON.parseObject(s, Map.class);
        String userId = (String) map.get("belongUserId");
        Map<String, Object> userInfo = redisCache.hmget(RedisConstant.REDIS_KEY_USER_LOGIN_INFO + userId);
        if (userInfo == null) {
            log.info("用户未登录");
            return;
        }
        String avatarUrl = (String) map.get("avatarUrl");
        String nickName = (String) map.get("nickName");
        MessageVO messageVO = new MessageVO();
        messageVO.setFrom(userId);
        messageVO.setFromAvatar(avatarUrl);
        messageVO.setFromName(nickName);
        messageVO.setContent((String) map.get("coverPicture"));
        messageVO.setTo((String) map.get("toUserId"));
        messageVO.setTime(System.currentTimeMillis());
        messageVO.setMessageType(7);
        messageVO.setChatType(0);
        Channel channel = USER_CHANNEL_MAP.get(messageVO.getTo());
        chatHandler.sendMessage(channel, messageVO);
        bloomFilterUtils.addBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, messageExt.getMsgId());
    }
}
