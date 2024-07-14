package com.weiiboo.modules.api.comment.domin;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document("comments")
public class CommentDO implements Serializable {
    @Id
    private String id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者id
     */
    private Long commentUserId;

    /**
     * 评论者省份
     */
    private String province;

    /**
     * 父级评论id
     */
    private String parentId;

    /**
     * 评论回复者id
     */
    private Long replyUserId;

    /**
     * 评论回复者名称
     */
    private String replyUserName;

    /**
     * 评论图片
     */
    private String pictureUrl;

    /**
     * 评论点赞数
     */
    private Integer commentLikeNum;

    /**
     * 评论所属笔记
     */
    private Long noteId;

    /**
     * 是否为置顶评论
     */
    private Boolean isTop;

    /**
     * 是否为热门评论，前十条赞数最多的为热门评论
     */
    private Boolean isHot;

    /**
     * 评论时间
     */
    private Long createTime;

    private static final long serialVersionUID = 1L;
}
