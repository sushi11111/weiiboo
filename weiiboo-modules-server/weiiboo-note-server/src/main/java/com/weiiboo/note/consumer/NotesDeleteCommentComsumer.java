package com.weiiboo.note.consumer;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.constant.RocketMQConsumerGroupConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.modules.api.notes.vo.NotePublishVO;
import com.weiiboo.note.service.CommentsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 删除笔记时 mongodb异步删除该笔记下的所有评论
 */
@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.NOTES_DELETE_COMMENT_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.NOTES_DELETE_COMMENT_CONSUMER_GROUP)
public class NotesDeleteCommentComsumer implements RocketMQListener<String> {
    @Resource
    private CommentsService commentsService;
    public void onMessage(String message){
        log.info("{}receive: {}",RocketMQTopicConstant.NOTES_DELETE_COMMENT_TOPIC,message);
        Long notesId = Long.valueOf(message);
        log.info("noteId:{}",notesId);
        commentsService.deleteAllCommentByNotesId(notesId);
    }
}