package com.weiiboo.im.handler.types;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.common.redis.utils.RedisKey;
import com.weiiboo.modules.api.im.vo.MessageVO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.Executor;

import static com.weiiboo.im.handler.IMServerHandler.USER_CHANNEL_MAP;

@Component
@Slf4j
public class ConnectHandler {
    @Resource
    private RedisCache redisCache;

    @Resource(name = "asyncThreadExecutor")
    private Executor asyncThreadExecutor;

    public void execute(ChannelHandlerContext channelHandlerContext, MessageVO message) {
        log.info("用户{}连接成功", message.getFrom());
        // 用户上线时，都会发送一条连接信息，将用户和channel绑定
        USER_CHANNEL_MAP.putIfAbsent(message.getFrom(), channelHandlerContext.channel());
        // 将在线用户放到redis中维护，存放的是用户id，方便后面观察是否存储离线消息
        redisCache.sSet(RedisConstant.REDIS_KEY_USER_ONLINE, message.getFrom());
        MessageVO response = new MessageVO();
        response.setMessageType(5);
        response.setFrom(message.getFrom());
        response.setTo(message.getTo());
        response.setContent("连接成功");
        response.setTime(System.currentTimeMillis());
        // 拉去离线消息
        Set<String> keys = redisCache.keys(RedisKey.build(RedisConstant.REDIS_KEY_USER_OFFLINE_MESSAGE, message.getFrom()));
        if (keys != null && !keys.isEmpty()) {
            log.info("用户{}上线，有离线消息，进行拉取", message.getFrom());
            keys.forEach(key -> {
                //  异步执行
                asyncThreadExecutor.execute(() -> {
                    // 将离线消息发送给用户
                    redisCache.lGet(key, 0, -1).forEach(o -> {
                        MessageVO messageVO = JSON.parseObject(o.toString(), MessageVO.class);
                        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageVO)));
                    });
                    // 删除离线消息
                    redisCache.del(key);
                });
            });
        }
    }
}