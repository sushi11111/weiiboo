package com.weiiboo.im.handler.types;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.common.domin.Result;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.common.redis.utils.RedisKey;
import com.weiiboo.common.web.exception.OnlyWarnException;
import com.weiiboo.im.feign.UserFeign;
import com.weiiboo.modules.api.im.domin.MessageDO;
import com.weiiboo.modules.api.im.vo.MessageVO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.weiiboo.im.handler.IMServerHandler.USER_CHANNEL_MAP;

@Component
@Slf4j
public class ChatHandler {
    @Resource
    private RedisCache redisCache;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource(name = "asyncThreadExecutor")
    private Executor asyncThreadExecutor;
    @Resource
    private UserFeign userFeign;

    public void execute(MessageVO messageVO) {
        if (messageVO.getFrom().equals(messageVO.getTo())) {
            log.info("不能给自己发送消息");
            // 告知发送者，不能给自己发送消息
            messageVO.setContent("不能给自己发送消息");
            replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), messageVO);
            return;
        }
        Channel channel = USER_CHANNEL_MAP.get(messageVO.getTo());
        // TODO 判断双方是否可以互相聊天
        boolean isExist = redisCache.hasKey(RedisKey.build(RedisConstant.REDIS_KEY_USER_RELATION_ALLOW_SEND_MESSAGE,
                messageVO.getFrom() + ":" + messageVO.getTo()));
        if (isExist) {
            Map<String, Object> hmget = redisCache.hmget(RedisKey.build(RedisConstant.REDIS_KEY_USER_RELATION_ALLOW_SEND_MESSAGE,
                    messageVO.getFrom() + ":" + messageVO.getTo()));
            Boolean isBlacked = (Boolean) hmget.get("isBlacked");
            if (isBlacked) {
                log.info("用户{}被用户{}拉黑，无法发送消息", messageVO.getFrom(), messageVO.getTo());
                // 告知发送者，对方已经将你拉黑
                messageVO.setContent("对方已将你拉黑");
                replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), messageVO);
                return;
            }
            Boolean allowSendMessage = (Boolean) hmget.get("allowSendMessage");
            if (!allowSendMessage) {
                log.info("对方没有关注，用户{}24小时内已经向用户{}发送消息", messageVO.getFrom(), messageVO.getTo());
                // 告知发送者，对方没有关注你，24小时内只能发送一条消息
                messageVO.setContent("对方没有关注你，24小时内只能发送一条消息");
                replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), messageVO);
                return;
            }
            sendMessage(channel, messageVO);
            // 应答消息，告知发送者，消息发送成功
            MessageVO replyMessage = new MessageVO();
            replyMessage.setFrom(messageVO.getFrom());
            replyMessage.setTo(messageVO.getTo());
            replyMessage.setId(messageVO.getId());
            replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), replyMessage);
            return;
        }
        createKeyAndSendMessage(channel, messageVO);
    }

    private void createKeyAndSendMessage(Channel channel, MessageVO messageVO) {
        Map<String, Object> userRelation = new HashMap<>();
        // 判断对方是否拉黑了自己
        String token = (String) redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, messageVO.getFrom()), "token");
        Result<Boolean> a = userFeign.selectOneByUserIdAndBlackIdIsExist
                (Long.valueOf(messageVO.getTo()), Long.valueOf(messageVO.getFrom()), token);
        if (a.getCode() != 20010) {
            log.error("查询用户关系失败");
            throw new OnlyWarnException(ExceptionMsgEnum.SERVER_ERROR);
        }
        Boolean isBlack = a.getData();
        if (isBlack) {
            log.info("用户{}被用户{}拉黑，无法发送消息", messageVO.getFrom(), messageVO.getTo());
            userRelation.putIfAbsent("isBlacked", true);
            userRelation.putIfAbsent("allowSendMessage", false);
            // 告知发送者，对方已经将你拉黑
            messageVO.setContent("对方已将你拉黑");
            replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), messageVO);
        } else {
            // 判断对方是否关注了我
            Result<Boolean> b = userFeign.selectOneByUserIdAndAttentionIdIsExist(
                    Long.valueOf(messageVO.getTo()), Long.valueOf(messageVO.getFrom()), token);
            if (b.getCode() != 20010) {
                log.error("查询用户关系失败");
                throw new OnlyWarnException(ExceptionMsgEnum.SERVER_ERROR);
            }
            Boolean isAttention = b.getData();
            if (isAttention) {
                log.info("对方关注了我，可以发送消息");
                userRelation.putIfAbsent("isBlacked", false);
                userRelation.putIfAbsent("allowSendMessage", true);

            } else {
                log.info("对方没有关注我，用户{}24小时内只能向用户{}发送一条消息", messageVO.getFrom(), messageVO.getTo());
                userRelation.putIfAbsent("isBlacked", false);
                userRelation.putIfAbsent("allowSendMessage", false);
                // 只发送这一次消息，下次只能等到redis中的key过期后才能发送，即24小时
            }
            sendMessage(channel, messageVO);
            // 应答消息，告知发送者，消息发送成功
            MessageVO replyMessage = new MessageVO();
            replyMessage.setFrom(messageVO.getFrom());
            replyMessage.setTo(messageVO.getTo());
            replyMessage.setId(messageVO.getId());
            replyMessage(USER_CHANNEL_MAP.get(messageVO.getFrom()), replyMessage);
        }
        // 将用户关系存储到redis中，key为发送者和接收者的id，value为用户关系
        redisCache.hmset(RedisKey.build(RedisConstant.REDIS_KEY_USER_RELATION_ALLOW_SEND_MESSAGE,
                messageVO.getFrom() + ":" + messageVO.getTo()), userRelation, 24 * 60 * 60);
    }

    public void sendMessage(Channel channel, MessageVO messageVO) {
        messageVO.setTime(System.currentTimeMillis());
        // 消息持久化到mongodb，异步执行
        asyncThreadExecutor.execute(() -> {
            try {
                MessageDO message = new MessageDO();
                BeanUtils.copyProperties(messageVO, message);
                mongoTemplate.insert(message);
            } catch (Exception e) {
                log.error("消息持久化失败", e);
            }
        });
        if (channel != null) {
            log.info("双方在一个服务，直接发送消息");
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageVO)));
            return;
        }
        boolean b = redisCache.sHasKey(RedisConstant.REDIS_KEY_USER_ONLINE, messageVO.getTo());
        if (b) {
            log.info("双方不在一个服务，发送广播消息");
            // 利用rocketmq发送广播消息，让所有的服务都能收到消息，然后再发送给用户
            rocketMQTemplate.convertAndSend(RocketMQTopicConstant.FIND_USER_TOPIC, JSON.toJSONString(messageVO));
            return;
        }
        log.info("对方不在线，发送离线消息");
        // 将离线消息存储到redis中，key为发送者和接收者的id，value为消息
        redisCache.lSet(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_OFFLINE_MESSAGE, messageVO.getTo() + ":" + messageVO.getFrom()),
                JSON.toJSONString(messageVO));
    }

    private void replyMessage(Channel channel, MessageVO messageVO) {
        messageVO.setTime(System.currentTimeMillis());
        messageVO.setChatType(0);
        messageVO.setMessageType(5);
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageVO)));
    }
}