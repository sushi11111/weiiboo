package com.weiiboo.note.service.impl;


import com.mongodb.client.result.DeleteResult;
import com.weiiboo.common.Utils.ResultUtil;
import com.weiiboo.common.constant.BaseConstant;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.common.redis.utils.RedisKey;
import com.weiiboo.common.web.utils.JWTUtil;
import com.weiiboo.note.feign.UserFeign;
import com.weiiboo.note.service.NotesService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weiiboo.common.domin.Result;
import com.weiiboo.common.web.utils.IPUtils;
import com.weiiboo.modules.api.comment.domin.CommentDO;
import com.weiiboo.modules.api.comment.vo.CommentVO;
import com.weiiboo.note.service.CommentsService;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
* @author subscriber
* @description 针对表【comments】的数据库操作Service实现
* @createDate 2024-03-23 20:58:17
*/
@Service
public class CommentsServiceImpl implements CommentsService {
    @Resource
    private HttpServletRequest request;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private UserFeign userFeign;
    @Resource
    private RedisCache redisCache;
    @Resource
    private Executor asyncThreadExecutor;
    @Resource
    private NotesService notesService;

    @Override
    public Result<CommentVO> publishComment(CommentDO commentDO) {
        // 进行初始化
        commentDO.setCommentLikeNum(0);
        commentDO.setIsHot(false);
        commentDO.setIsTop(false);
        commentDO.setCreateTime(System.currentTimeMillis());

        String ipAddr = IPUtils.getRealIpAddr(request);
        String addr = IPUtils.getAddrByIp(ipAddr);
        String address = IPUtils.provinceAddress(addr);

        if (StringUtils.hasText(address)) {
            commentDO.setProvince(address);
        }else {
            commentDO.setProvince("未知");
        }

        // 处理回复内容
        if (commentDO.getReplyUserId() != 0) {
            //<a href="#{&quot;userId&quot;:&quot;1675532564583455936&quot;}" rel="noopener noreferrer" target="_blank" style="color: rgb(69, 105, 215); text-decoration: none;">@测试用户1</a>
            String prefix = "回复 <a href=\"#{" +
                    "&quot;userId&quot;:&quot;" + commentDO.getReplyUserId() + "&quot;" +
                    "}\" rel=\"noopener noreferrer\" target=\"_blank\" style=\"color: #7d7d7d; text-decoration: none;\">" + commentDO.getReplyUserName() + "</a>：";
            // 添加到content的<p>的最前面
            if (commentDO.getContent().startsWith("<p>")) {
                commentDO.setContent("<p>" + prefix + commentDO.getContent().substring(3));
            } else {
                commentDO.setContent("<p>" + prefix + commentDO.getContent());
            }
        }
        // 插入mongodb
        mongoTemplate.insert(commentDO);

        // 返回评论信息
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(commentDO,commentVO);
        // 获取评论的用户信息
        Result<?> result = userFeign.getUserInfo(commentDO.getCommentUserId());
        if (result.getCode() == 20010) {
            Map<String, Object> userInfo = (Map<String, Object>) result.getData();
            commentVO.setCommentUserName((String) userInfo.get("nickname"));
            commentVO.setCommentUserAvatar((String) userInfo.get("avatarUrl"));
        } else {
            commentVO.setCommentUserName("用户已注销");
            commentVO.setCommentUserAvatar(BaseConstant.DEFAULT_AVATAR_URL);
        }
        commentVO.setCommentReplyNum(0);
        commentVO.setIsLike(false);
        return ResultUtil.successPost(commentVO);
    }

    @Override
    public Result<Boolean> praiseComment(String commentId, Long userId, Long targetUserId) {
        String key = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_LIKE,commentId);
        String count = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_LIKE,commentId);
        if (redisCache.sHasKey(key, userId)) {
            redisCache.setRemove(key, userId);
            redisCache.decr(count, 1);
        } else {
            // 设置过期时间为7天，7天后自动删除，评论点赞集合并没有什么价值，不把它放到数据库中，用户一般不会回头看自己的评论点赞记录
            redisCache.sSetAndTime(key, 60 * 60 * 24 * 7, userId);
            redisCache.incr(count, 1);
        }
        // TODO rocketMQ异步通知targetUserId用户
        return ResultUtil.successPost(true);
    }

    @Override
    public Result<Integer> getCommentCount(Long notesId) {
        Query query = new Query();
        query.addCriteria(new Criteria("noteId").is(notesId));
        long count  = mongoTemplate.count(query,CommentDO.class);
        return ResultUtil.successGet((int)count);
    }

    @Override
    public Result<List<CommentVO>> getCommentFirstList(Long notesId, Integer page, Integer pageSize) {
        List<CommentDO> commentDOList = new ArrayList<>();
        // 第一页评论，需要找置顶评论
        if(page == 1){
            Query query = new Query();
            query.addCriteria(new Criteria("noteId").is(notesId));
            query.addCriteria(new Criteria("isTop").is(true));
            query.addCriteria(new Criteria("parentId").is("0"));
            CommentDO topComment = mongoTemplate.findOne(query,CommentDO.class);
            if(topComment!=null){
                commentDOList.add(topComment);
            }
            // 找非置顶评论，但是是热门评论
            Query query1 = new Query();
            query1.addCriteria(new Criteria("noteId").is(notesId));
            query1.addCriteria(new Criteria("parentId").is("0"));
            query1.addCriteria(new Criteria("isTop").is(false));
            query1.addCriteria(new Criteria("isHot").is(true));
            // 排序，点赞量从高到低
            query1.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
            List<CommentDO> hotComment = mongoTemplate.find(query1, CommentDO.class);
            if(!hotComment.isEmpty()){
                commentDOList.addAll(hotComment);
            }
            // 如果redis中有已经更新过热门的评论标识，有就不用更新，没有就更新
            boolean b = redisCache.sHasKey(RedisConstant.REDIS_KEY_NOTES_COMMENT_HOT,notesId.toString());
            if(!b){
                // 6小时更新一次
                Long l = redisCache.sSetAndTime(RedisConstant.REDIS_KEY_NOTES_COMMENT_HOT, 60 * 60 * 6, notesId.toString());
                // 若l为1，说明redis中没有该key，需要更新，防止并发，只有一个线程更新
                if (l == 1) {
                    // 线程池异步更新热门评论
                    asyncThreadExecutor.execute(() -> {
                        // 先将所有热门评论标识置为false
                        Query query2 = new Query();
                        query2.addCriteria(new Criteria("notesId").is(notesId));
                        query2.addCriteria(new Criteria("parentId").is("0"));
                        query2.addCriteria(new Criteria("isHot").is(true));
                        List<CommentDO> hot = mongoTemplate.find(query2, CommentDO.class);
                        if (!hot.isEmpty()) {
                            hot.forEach(commentDO -> {
                                commentDO.setIsHot(false);
                                mongoTemplate.save(commentDO);
                            });
                        }
                        // 再将commentLikeNum最高的8条评论置为热门评论
                        Query query3 = new Query();
                        query3.addCriteria(new Criteria("notesId").is(notesId));
                        query3.addCriteria(new Criteria("parentId").is("0"));
                        query3.addCriteria(new Criteria("isTop").is(false));
                        query3.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
                        query3.limit(8);
                        List<CommentDO> hot1 = mongoTemplate.find(query3, CommentDO.class);
                        if (!hot1.isEmpty()) {
                            hot1.forEach(commentDO -> {
                                commentDO.setIsHot(true);
                                mongoTemplate.save(commentDO);
                            });
                        }
                    });
                }
            }
        }
        // page不为1
        Query query = new Query();
        query.addCriteria(new Criteria("noteId").is(notesId));
        query.addCriteria(new Criteria("parentId").is("0"));
        query.addCriteria(new Criteria("isTop").is(false));
        query.addCriteria(new Criteria("isHot").is(false));
        query.skip((long) (page - 1) * pageSize);
        query.limit(pageSize);
        // 按commentLikeNum降序排序，若commentLikeNum相同则按createTime降序排序
        query.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
        commentDOList.addAll(mongoTemplate.find(query, CommentDO.class));
        List<CommentVO> commentVOList = commentDOList.stream().map(commentDO -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(commentDO, commentVO);
            Query query1 = new Query();
            query1.addCriteria(new Criteria("parentId").is(commentDO.getId()));
            query1.addCriteria(new Criteria("notesId").is(notesId));
            long count = mongoTemplate.count(query1, CommentDO.class);
            commentVO.setCommentReplyNum((int) count);
            Result<?> result = userFeign.getUserInfo(commentDO.getCommentUserId());
            if (result.getCode() == 20010) {
                Map<String, Object> userInfo = (Map<String, Object>) result.getData();
                commentVO.setCommentUserName((String) userInfo.get("nickname"));
                commentVO.setCommentUserAvatar((String) userInfo.get("avatarUrl"));
            } else {
                commentVO.setCommentUserName("用户已注销");
                commentVO.setCommentUserAvatar(BaseConstant.DEFAULT_AVATAR_URL);
            }
            // 添加进redis
            String key = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_LIKE, commentDO.getId());
            String countKey = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_COUNT, commentDO.getId());
            commentVO.setIsLike(redisCache.sHasKey(key, commentDO.getCommentUserId()));
            if (redisCache.get(countKey) == null) {
                redisCache.set(countKey, commentDO.getCommentLikeNum().longValue());
            } else {
                commentVO.setCommentLikeNum(Integer.parseInt(redisCache.get(countKey).toString()));
            }
            return commentVO;
        }).collect(Collectors.toList());
        return ResultUtil.successGet(commentVOList);
    }

    @Override
    public Result<List<CommentVO>> getCommentSecondList(Long notesId, String parentId, Integer page, Integer pageSize) {
        List<CommentDO> commentDOList = new ArrayList<>();
        if (page == 1) {
            Query query = new Query();
            query.addCriteria(new Criteria("noteId").is(notesId));
            query.addCriteria(new Criteria("parentId").is(parentId));
            query.addCriteria(new Criteria("isHot").is(true));
            query.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
            List<CommentDO> hotComment = mongoTemplate.find(query, CommentDO.class);
            if (!hotComment.isEmpty()) {
                commentDOList.addAll(hotComment);
            }
        }
        Query query = new Query();
        query.addCriteria(new Criteria("parentId").is(parentId));
        query.addCriteria(new Criteria("noteId").is(notesId));
        query.addCriteria(new Criteria("isHot").is(false));
        query.skip((long) (page - 1) * pageSize);
        query.limit(pageSize);
        query.with(Sort.by(Sort.Order.desc("commentLikeNum"), Sort.Order.desc("createTime")));
        commentDOList.addAll(mongoTemplate.find(query, CommentDO.class));
        // 对每个评论的用户补全信息
        List<CommentVO> commentVOList = commentDOList.stream().map(commentDO -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(commentDO, commentVO);
            Result<?> result = userFeign.getUserInfo(commentDO.getCommentUserId());
            if (result.getCode() == 20010) {
                Map<String, Object> userInfo = (Map<String, Object>) result.getData();
                commentVO.setCommentUserName((String) userInfo.get("nickname"));
                commentVO.setCommentUserAvatar((String) userInfo.get("avatarUrl"));
            } else {
                commentVO.setCommentUserName("用户已注销");
                commentVO.setCommentUserAvatar(BaseConstant.DEFAULT_AVATAR_URL);
            }
            String key = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_LIKE, commentDO.getId());
            String countKey = RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_COUNT, commentDO.getId());
            commentVO.setIsLike(redisCache.sHasKey(key, commentDO.getCommentUserId()));
            if (redisCache.get(countKey) == null) {
                redisCache.set(countKey, commentDO.getCommentLikeNum().longValue());
            } else {
                commentVO.setCommentLikeNum(Integer.parseInt(redisCache.get(countKey).toString()));
            }
            return commentVO;
        }).collect(Collectors.toList());
        return ResultUtil.successGet(commentVOList);
    }

    /**
     * 删除笔记下的所有评论
     * @param notesId 笔记id
     */
    @Override
    public void deleteAllCommentByNotesId(Long notesId) {
        Query query = new Query();
        query.addCriteria(new Criteria("notesId").is(notesId));
        mongoTemplate.remove(query, CommentDO.class);
    }

    @Override
    public Result<Boolean> setTopComment(String commentId) {
        ObjectId objectId = new ObjectId(commentId);
        Query query = new Query(Criteria.where("_id").is(objectId));
        CommentDO commentDO = mongoTemplate.findOne(query, CommentDO.class);
        if (commentDO == null) {
            return ResultUtil.errorPost("评论不存在");
        }
        if(!Objects.equals(commentDO.getParentId(), "0")){
            return ResultUtil.errorPost("不是主评论");
        }
        Map<String, Object> map = JWTUtil.parseToken(request.getHeader("token"));
        Long userId = Long.parseLong(map.get("userId").toString());
        Long notesId = commentDO.getNoteId();
        // 获取笔记作者
        Long notesBelongUser = notesService.getBelongUserId(notesId);
        // 只有作者才能置顶
        if (!userId.equals(notesBelongUser)) {
            return ResultUtil.errorPost("无权限");
        }
        commentDO.setIsTop(!commentDO.getIsTop());
        mongoTemplate.save(commentDO);
        // 其他置顶评论取消置顶
        if (commentDO.getIsTop()) {
            Query query1 = new Query();
            query1.addCriteria(new Criteria("noteId").is(notesId));
            query1.addCriteria(new Criteria("parentId").is("0"));
            query1.addCriteria(new Criteria("isTop").is(true));
            query1.addCriteria(new Criteria("_id").ne(objectId));
            List<CommentDO> commentDOList = mongoTemplate.find(query1, CommentDO.class);
            if (!commentDOList.isEmpty()) {
                commentDOList.forEach(commentDO1 -> {
                    commentDO1.setIsTop(false);
                    mongoTemplate.save(commentDO1);
                });
            }
        }
        return ResultUtil.successPost(true);
    }

    @Override
    public Result<Boolean> deleteComment(String commentId) {
        ObjectId objectId = new ObjectId(commentId);
        Query query = new Query(Criteria.where("_id").is(objectId));
        CommentDO commentDO = mongoTemplate.findOne(query, CommentDO.class);
        Map<String, Object> map = JWTUtil.parseToken(request.getHeader("token"));
        Long userId = Long.parseLong(map.get("userId").toString());
        if (commentDO == null) {
            return ResultUtil.errorPost("评论不存在");
        }
        // 获取笔记所属用户Id
        Long notesBelongUser = notesService.getBelongUserId(commentDO.getNoteId());
        if((!Objects.equals(commentDO.getCommentUserId(), userId)) && (!Objects.equals(notesBelongUser, userId))){
            return ResultUtil.errorPost("无权限");
        }
        DeleteResult remove = mongoTemplate.remove(query, CommentDO.class);
        if (remove.getDeletedCount() == 0) {
            return ResultUtil.errorPost("删除失败");
        }
        // 该评论是一级评论（还有二级评论）
        if(Objects.equals(commentDO.getParentId(), "0")){
            // 删除所有二级评论
            Query query1 = new Query();
            query1.addCriteria(new Criteria("parentId").is(commentId));
            mongoTemplate.remove(query1, CommentDO.class);
        }
        redisCache.del(RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_LIKE, commentId));
        redisCache.del(RedisKey.build(RedisConstant.REDIS_KEY_COMMENT_COUNT, commentId));
        return ResultUtil.successPost(true);
    }
}
