package com.weiiboo.user.service;

import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.user.vo.UserBlackVO;
import com.weiiboo.modules.api.user.vo.UserRelationVO;

import java.util.List;

public interface UserRelationService {
    Result<Boolean> attention(Long userId, Long targetUserId);

    Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId, Long fromId);

    Result<Boolean> selectOneByUserIdAndBlackIdIsExist(Long toId, Long fromId);

    Result<List<UserRelationVO>> selectAttentionList(Long userId, Integer pageNum, Integer pageSize);

    Result<List<UserRelationVO>> selectFansList(Long userId, Integer pageNum, Integer pageSize);

    Result<Boolean> black(Long userId,Long targetUserId);

    Result<List<UserBlackVO>> selectBlackList(Long userId, Integer pageNum, Integer pageSize);

    Result<?> updateRemarkName(Long userId, Long targetUserId, String remarkName);
}

