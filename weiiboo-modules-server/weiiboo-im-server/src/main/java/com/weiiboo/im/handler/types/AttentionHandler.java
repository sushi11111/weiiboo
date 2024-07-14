package com.weiiboo.im.handler.types;

import com.weiiboo.modules.api.im.vo.MessageVO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.weiiboo.im.handler.IMServerHandler.USER_CHANNEL_MAP;

@Component
@Slf4j
public class AttentionHandler {
    private final ChatHandler chatHandler;

    public AttentionHandler(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }

    public void execute(MessageVO messageVO) {
        Channel channel = USER_CHANNEL_MAP.get(messageVO.getTo());
        chatHandler.sendMessage(channel, messageVO);
    }
}
