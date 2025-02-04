package com.weiiboo.modules.api.im.domin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "message")
public class MessageDO implements Serializable {
    /**
     * 发送者id
     */
    private String from;
    /**
     * 接收者id
     */
    private String to;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 发送时间
     */
    private Long time;
    /**
     * 0:不是聊天信息
     * 1:文本信息
     * 2:图片信息
     * 3:文件信息
     * 4:语音信息
     */
    private Integer chatType;
    /**
     * 0:好友消息
     * 1:陌生人消息
     */
    private Integer friendType;
    /**
     * 语音时长，单位秒
     */
    private Integer audioTime;
}
