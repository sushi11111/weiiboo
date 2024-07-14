package com.weiiboo.modules.api.notes.domin;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("user_collect_notes")
public class UserCollectNotesDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 笔记id
     */
    private Long notesId;

    /**
     * 创建时间
     */
    private Date createTime;
}
