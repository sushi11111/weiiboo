package com.weiiboo.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiiboo.modules.api.user.domin.UserBlackDO;
import com.weiiboo.modules.api.user.vo.UserBlackVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserBlackMapper extends BaseMapper<UserBlackDO> {
    Boolean selectOneByUserIdAndBlackIdIsExist(Long toId, Long fromId);

    @Select("select * from weber.user_black_list where user_id = #{userId} and black_id = #{targetUserId}")
    UserBlackDO getExist(Long userId, Long targetUserId);

    List<UserBlackVO> selectBlackList(Long userId, Integer pageNum, Integer pageSize);
}
