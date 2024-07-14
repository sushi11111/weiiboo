package com.weiiboo.im.handler.types;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.common.redis.utils.RedisKey;
import com.weiiboo.common.web.utils.JWTUtil;
import com.weiiboo.modules.api.im.vo.MessageVO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@Slf4j
public class AuthenticationHandler {

    private final RedisCache redisCache;

    public AuthenticationHandler(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    public void execute(ChannelHandlerContext channelHandlerContext, MessageVO message) {
        String token = message.getContent();
        if (!StringUtils.hasText(token)) {
            log.warn("token为空");
            String content = ExceptionMsgEnum.NOT_LOGIN.getMsg();
            replyMessage(channelHandlerContext.channel(), content, message.getFrom());
        }
        Map<String, Object> map = null;
        try {
            map = JWTUtil.parseToken(token);
        } catch (Exception e) {
            log.error("token不合法");
            String content = ExceptionMsgEnum.ACCOUNT_OPERATION_ERROR.getMsg();
            replyMessage(channelHandlerContext.channel(), content, message.getFrom());
        }
        if (map == null
                || !StringUtils.hasText(String.valueOf(map.get("userId")))
                || !String.valueOf(map.get("userId")).equals(message.getFrom())) {
            // token不合法
            log.error("token不合法");
            String content = ExceptionMsgEnum.ACCOUNT_OPERATION_ERROR.getMsg();
            replyMessage(channelHandlerContext.channel(), content, message.getFrom());
        }
        // 判断token是否过期
        Long expire = (Long) redisCache.get(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_EXPIRE,
                        String.valueOf(map.get("userId"))));
        if (expire == null || expire < System.currentTimeMillis()) {
            // token过期
            log.warn("token过期");
            String content = ExceptionMsgEnum.TOKEN_EXPIRED.getMsg();
            replyMessage(channelHandlerContext.channel(), content, message.getFrom());
        }
        String currentToken = (String) redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(map.get("userId"))),
                "token");
        if (!token.equals(currentToken)) {
            // 告知用户，您的账号在其他地方登录或者登录信息已过期
            log.warn("您的账号在其他地方登录或者登录信息已过期");
            String content = ExceptionMsgEnum.ACCOUNT_OTHER_LOGIN.getMsg();
            replyMessage(channelHandlerContext.channel(), content, message.getFrom());
        }
    }

    private void replyMessage(Channel channel, String content, String from) {
        MessageVO messageVO = new MessageVO();
        messageVO.setContent(content);
        messageVO.setFrom("SYSTEM");
        messageVO.setTo(from);
        messageVO.setMessageType(6);
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageVO)));
    }
}