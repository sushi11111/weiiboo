package com.weiiboo.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiiboo.modules.api.user.domin.UserAttentionDO;
import com.weiiboo.modules.api.user.vo.UserRelationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserAttentionMapper extends BaseMapper<UserAttentionDO> {
    @Select("select * from user_attention where user_id=#{userId} and attention_id=#{targetUserId}")
    UserAttentionDO getExist(Long userId, Long targetUserId);

    @Select("select count(*) from user_attention where user_id = #{userId}")
    Integer getCountById(Long userId);

    Boolean selectOneByUserIdAndAttentionIdIsExist(Long userId, Long attentionId);

    List<UserRelationVO> selectAttentionList(Long userId, Integer offset, Integer pageSize);
}
