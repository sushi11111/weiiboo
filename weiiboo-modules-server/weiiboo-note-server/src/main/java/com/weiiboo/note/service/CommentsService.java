package com.weiiboo.note.service;

import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.comment.domin.CommentDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.weiiboo.modules.api.comment.vo.CommentVO;

import java.util.List;

/**
* @author subscriber
* @description 针对表【comments】的数据库操作Service
* @createDate 2024-03-23 20:58:17
*/
public interface CommentsService{

    Result<CommentVO> publishComment(CommentDO commentDO);

    Result<Boolean> praiseComment(String commentId, Long userId, Long targetUserId);

    Result<Integer> getCommentCount(Long notesId);

    Result<List<CommentVO>> getCommentFirstList(Long notesId, Integer page, Integer pageSize);

    Result<Boolean> setTopComment(String commentId);

    Result<Boolean> deleteComment(String commentId);

    Result<List<CommentVO>> getCommentSecondList(Long notesId, String parentId, Integer page, Integer pageSize);

    void deleteAllCommentByNotesId(Long notesId);
}
