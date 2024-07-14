package com.weiiboo.modules.api.comment.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentVO implements Serializable {
    private String id;
    private String content;
    private String province;
    private Long commentUserId;
    private String commentUserName;
    private String commentUserAvatar;
    private String parentId;
    private Long replyUserId;
    private String replyUserName;
    private String pictureUrl;
    private Integer commentLikeNum;
    // 评论回复数，只在为一级评论时有效
    private Integer commentReplyNum;
    private Long notesId;
    private Boolean isTop;
    private Boolean isLike;
    private Long createTime;
}
