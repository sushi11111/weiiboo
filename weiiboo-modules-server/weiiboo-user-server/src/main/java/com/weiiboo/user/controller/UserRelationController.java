package com.weiiboo.user.controller;

import com.weiiboo.common.domin.Result;
import com.weiiboo.common.redis.constant.BloomFilterMap;
import com.weiiboo.common.web.aop.bloomFilter.BloomFilterProcessing;
import com.weiiboo.common.web.aop.idempotent.Idempotent;
import com.weiiboo.modules.api.user.vo.UserBlackVO;
import com.weiiboo.modules.api.user.vo.UserRelationVO;
import com.weiiboo.user.service.UserRelationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user/relation")
public class UserRelationController {
    @Resource
    private UserRelationService userRelationService;

    /**
     * 拉黑或取消拉黑用户
     * @param userId 要拉黑的用户id
     * @param targetUserId 拉黑用户的id
     * @return 是否拉黑成功
     */
     @PostMapping("/black")
     @Idempotent(value = "/user/relation/black",expireTime = 60000)
     public Result<Boolean> black(Long userId,Long targetUserId){
         return userRelationService.black(userId,targetUserId);
     }

    /**
     * 获取用户黑名单列表
     * @param userId 用户id
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 黑名单列表
     */
    @GetMapping("/blackList")
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
    public Result<List<UserBlackVO>> blackList(Long userId, Integer pageNum, Integer pageSize){
        return userRelationService.selectBlackList(userId,pageNum,pageSize);
    }

    /**
     * 查询是否存在黑名单
     * @param toId 拉黑的用户id
     * @param fromId 被拉黑的用户id
     * @return 是否存在黑名单
     */
    @PostMapping("/isBlack")
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#toId","#fromId"})
    public Result<Boolean> selectOneByUserIdAndBlackIdIsExist(Long toId,Long fromId){
        return userRelationService.selectOneByUserIdAndBlackIdIsExist(toId,fromId);
    }

    /**
     * 查询是否存在关注
     * @param toId 关注id
     * @param fromId 被关注的用户id
     * @return 是否存在关注
     */
    @PostMapping("/isAttention")
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#toId","#fromId"})
    public Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId,Long fromId){
        return userRelationService.selectOneByUserIdAndAttentionIdIsExist(toId,fromId);
    }

    /**
     * 获取用户关注列表
     * @param userId 用户id
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户关注列表
     */
    @GetMapping("/attentionList")
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
    public Result<List<UserRelationVO>> getAttentionList(Long userId, Integer pageNum, Integer pageSize){
        return userRelationService.selectAttentionList(userId,pageNum,pageSize);
    }

    /**
     * 获取用户粉丝列表
     * @param userId 用户id
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户粉丝列表
     */
    @GetMapping("/fansList")
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
    public Result<List<UserRelationVO>> getFansList(Long userId, Integer pageNum, Integer pageSize){
        return userRelationService.selectFansList(userId,pageNum,pageSize);
    }

    /**
     * 关注或取消关注用户
     * @param userId 用户id
     * @param targetUserId 关注的用户id
     * @return 是否关注成功
     */
    @PostMapping("/attention")
    @Idempotent(value = "/user/relation/attention", expireTime = 500)
    @BloomFilterProcessing(map= BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId","#targetUserId"})
    public Result<Boolean> attention(Long userId, Long targetUserId) {
        return userRelationService.attention(userId,targetUserId);
    }

    /**
     * 更新备注名
     * @param userId 用户id
     * @param targetUserId 关注的用户id
     * @param remarkName 备注名
     * @return 是否更新成功
     */
    @PostMapping("/updateRemarkName")
    @Idempotent(value = "/user/relation/updateRemarkName", expireTime = 500)
    @BloomFilterProcessing(map= BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId","#targetUserId"})
    public Result<?> updateRemarkName(Long userId, Long targetUserId, String remarkName) {
        return userRelationService.updateRemarkName(userId,targetUserId,remarkName);
    }
}