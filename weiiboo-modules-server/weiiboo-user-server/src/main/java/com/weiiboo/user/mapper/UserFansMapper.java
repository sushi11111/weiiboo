package com.weiiboo.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiiboo.modules.api.user.domin.UserFansDO;
import com.weiiboo.modules.api.user.vo.UserRelationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFansMapper extends BaseMapper<UserFansDO> {

    @Select("select count(*) from user_fans where user_id=#{userId}")
    Integer getCountById(Long userId);

    List<UserRelationVO> selectFansList(Long userId, Integer offset, Integer pageSize);
}
