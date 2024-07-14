package com.weiiboo.modules.api.notes.domin;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("notes_topic_relation")
public class NotesTopicRelationDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 笔记id
     */
    private Long notesId;

    /**
     * 话题id
     */
    private Long topicId;
}
