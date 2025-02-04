package com.weiiboo.im.handler;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.myEnum.MessageTypeEnum;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.im.handler.types.AttentionHandler;
import com.weiiboo.im.handler.types.AuthenticationHandler;
import com.weiiboo.im.handler.types.ChatHandler;
import com.weiiboo.im.handler.types.ConnectHandler;
import com.weiiboo.modules.api.im.vo.MessageVO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Component
@Slf4j
// 标记该Handler可以被多个Channel共享
@ChannelHandler.Sharable
public class IMServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    /**
     * 用户通道关系
     */
    public static final Map<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();

    public static IMServerHandler imServerHandler;

    @Resource
    private AttentionHandler attentionHandler;
    @Resource
    private AuthenticationHandler authenticationHandler;
    @Resource
    private ChatHandler chatHandler;
    @Resource
    private ConnectHandler connectHandler;
    @Resource
    private RedisCache redisCache;
    @Resource
    private Executor asyncThreadExecutor;

    public IMServerHandler() {
    }

    // 初始化注入
    @PostConstruct
    public void init() {
        imServerHandler = this;
        imServerHandler.connectHandler = this.connectHandler;
        imServerHandler.authenticationHandler = this.authenticationHandler;
        imServerHandler.chatHandler = this.chatHandler;
        imServerHandler.redisCache = this.redisCache;
        imServerHandler.asyncThreadExecutor = this.asyncThreadExecutor;
        imServerHandler.attentionHandler = this.attentionHandler;
    }

    /**
     * 读取客户端发送的消息
     *
     * @param channelHandlerContext 上下文
     * @param frame                 消息
     * @throws Exception 异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame) throws Exception {
        log.info("接收到消息：{}", frame.text());
        try {
            MessageVO message = JSON.parseObject(frame.text(), MessageVO.class);
            Integer type = message.getMessageType();
            if (Objects.equals(type, MessageTypeEnum.CONNECT_MESSAGE)) {
                connectHandler.execute(channelHandlerContext, message);
            }else if (Objects.equals(type, MessageTypeEnum.HEART_MESSAGE)) {
                // 心跳消息，这里主要是验证token是否过期
                authenticationHandler.execute(channelHandlerContext, message);
            } else if (Objects.equals(type, MessageTypeEnum.SYSTEM_MESSAGE)) {
                // TODO 系统消息
            } else if (Objects.equals(type, MessageTypeEnum.CHAT_MESSAGE)) {
                // 聊天消息
                chatHandler.execute(message);
            } else if (Objects.equals(type, MessageTypeEnum.FOLLOW_MESSAGE)) {
                attentionHandler.execute(message);
            }
        } catch (Exception e) {
            log.error("消息格式不正确", e);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("客户端心跳检测");
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if (Objects.requireNonNull(event.state()) == IdleState.READER_IDLE) {
                log.warn("服务器已超过5秒未收到数据，关闭连接");
                ctx.channel().close();
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        Channel channel = ctx.channel();
        // 从用户通道关系中移除，使用线程池异步执行
        asyncThreadExecutor.execute(() -> {
            for (Map.Entry<String, Channel> entry : USER_CHANNEL_MAP.entrySet()) {
                if (entry.getValue() == channel) {
                    USER_CHANNEL_MAP.remove(entry.getKey());
                    redisCache.setRemove(RedisConstant.REDIS_KEY_USER_ONLINE, entry.getKey());
                    break;
                }
            }
        });
        log.info("客户端断开连接");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        log.info("客户端建立连接");
    }
}