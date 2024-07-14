package com.weiiboo.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiiboo.modules.api.user.domin.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
* @author subscriber
* @description 针对表【users】的数据库操作Mapper
* @createDate 2024-03-03 16:47:03
* @Entity example.domain.Users
*/
@Mapper
public interface UsersMapper extends BaseMapper<UserDO> {

}




