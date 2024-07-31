package com.weiiboo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.weiiboo.common.Utils.ResultUtil;
import com.weiiboo.common.domin.Result;
import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.common.redis.utils.RedisKey;
import com.weiiboo.modules.api.user.domin.UserAttentionDO;
import com.weiiboo.modules.api.user.domin.UserBlackDO;
import com.weiiboo.modules.api.user.domin.UserFansDO;
import com.weiiboo.modules.api.user.vo.UserBlackVO;
import com.weiiboo.modules.api.user.vo.UserRelationVO;
import com.weiiboo.user.mapper.UserAttentionMapper;
import com.weiiboo.user.mapper.UserBlackMapper;
import com.weiiboo.user.mapper.UserFansMapper;
import com.weiiboo.user.service.UserRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class UserRelationServiceImpl implements UserRelationService {
    @Resource
    private UserAttentionMapper userAttentionMapper;
    @Resource
    private UserFansMapper userFansMapper;
    @Resource
    private UserBlackMapper userBlackMapper;
    @Resource
    private RedisCache redisCache;
    @Override
    @Transactional
    public Result<Boolean> attention(Long userId, Long targetUserId) {
        if(userId == null || targetUserId ==null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(userId.equals(targetUserId)){
            return ResultUtil.errorPost("不能关注自己");
        }
        UserAttentionDO userAttentionDO = userAttentionMapper.getExist(userId,targetUserId);
        // 关注过了
        if(Objects.nonNull(userAttentionDO)){
            // 取消关注
            userAttentionMapper.deleteById(userAttentionDO.getId());
            userFansMapper.delete(new QueryWrapper<UserFansDO>().lambda().eq(
                    UserFansDO::getUserId,targetUserId).eq(UserFansDO::getFansId,userId));
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userId)), "attentionNum", -1);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(targetUserId)), "fansNum", -1);
        }else {
            // 新增记录
            userAttentionDO=new UserAttentionDO();
            userAttentionDO.setUserId(userId);
            userAttentionDO.setAttentionId(targetUserId);
            userAttentionDO.setCreateTime(new Date());
            userAttentionMapper.insert(userAttentionDO);
            UserFansDO userFansDO=new UserFansDO();
            userFansDO.setUserId(targetUserId);
            userFansDO.setFansId(userId);
            userFansDO.setCreateTime(new Date());
            userFansMapper.insert(userFansDO);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userId)), "attentionNum", 1);
            redisCache.hincr(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(targetUserId)), "fansNum", 1);
        }
        // 更新redis
        redisCache.del(RedisKey.build(RedisConstant.REDIS_KEY_USER_RELATION_ALLOW_SEND_MESSAGE,targetUserId+":"+userId));
        return ResultUtil.successPost(true);
    }

    @Override
    public Result<Boolean> selectOneByUserIdAndAttentionIdIsExist(Long toId, Long fromId) {
        return ResultUtil.successGet(userAttentionMapper.selectOneByUserIdAndAttentionIdIsExist(toId, fromId));
    }

    @Override
    public Result<Boolean> selectOneByUserIdAndBlackIdIsExist(Long toId, Long fromId) {
        return ResultUtil.successGet(userBlackMapper.selectOneByUserIdAndBlackIdIsExist(toId,fromId));
    }

    @Override
    public Result<List<UserRelationVO>> selectAttentionList(Long userId, Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum < 1){
            pageNum = 1;
        }
        if(pageSize == null || pageSize < 1){
            pageSize = 10;
        }
        if(userId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        Integer offset = (pageNum - 1) * pageSize;
        List<UserRelationVO> userRelationVOList = userAttentionMapper.selectAttentionList(userId,offset,pageSize);
        return ResultUtil.successGet(userRelationVOList);
    }

    @Override
    public Result<List<UserRelationVO>> selectFansList(Long userId, Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum < 1){
            pageNum = 1;
        }
        if(pageSize == null || pageSize < 1){
            pageSize = 10;
        }
        if(userId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        Integer offset = (pageNum - 1) * pageSize;
        List<UserRelationVO> userRelationVOList = userFansMapper.selectFansList(userId,offset,pageSize);
        return ResultUtil.successGet(userRelationVOList);
    }

    @Override
    public Result<Boolean> black(Long userId,Long targetUserId) {
        if(userId == null || targetUserId ==null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        if(userId.equals(targetUserId)){
            return ResultUtil.errorPost("不能拉黑自己");
        }
        UserBlackDO userBlackDO = userBlackMapper.getExist(userId,targetUserId);
        if(Objects.nonNull(userBlackDO)){
            // 取消拉黑
            userBlackMapper.deleteById(userBlackDO.getId());
        }
        else {
            // 新增拉黑记录
            userBlackDO = new UserBlackDO();
            userBlackDO.setUserId(userId);
            userBlackDO.setBlackId(targetUserId);
            userBlackDO.setCreateTime(new Date());
            userBlackMapper.insert(userBlackDO);
        }
        return ResultUtil.successPost(true);
    }

    @Override
    public Result<List<UserBlackVO>> selectBlackList(Long userId, Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if(pageSize == null || pageSize < 1){
            pageSize = 10;
        }
        if(userId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        return ResultUtil.successGet(userBlackMapper.selectBlackList(userId,(pageNum - 1) * pageSize,pageSize));
    }

    @Override
    public Result<?> updateRemarkName(Long userId, Long targetUserId, String remarkName) {
        if(userId == null || targetUserId == null){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        UserAttentionDO userAttentionDO = userAttentionMapper.getExist(userId,targetUserId);
        if(Objects.isNull(userAttentionDO)){
            return ResultUtil.errorPost("用户未关注");
        }
        userAttentionDO.setRemarkName(remarkName);
        userAttentionMapper.updateById(userAttentionDO);
        return ResultUtil.successPost(null);
    }
}
