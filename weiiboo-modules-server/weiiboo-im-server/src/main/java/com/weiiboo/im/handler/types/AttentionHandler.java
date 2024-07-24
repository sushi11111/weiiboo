package com.weiiboo.im.handler.types;

import com.weiiboo.modules.api.im.vo.MessageVO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.weiiboo.im.handler.IMServerHandler.USER_CHANNEL_MAP;

@Component
@Slf4j
public class AttentionHandler {
    @Resource
    private ChatHandler chatHandler;

    public void execute(MessageVO messageVO) {
        Channel channel = USER_CHANNEL_MAP.get(messageVO.getTo());
        chatHandler.sendMessage(channel, messageVO);
    }
}
