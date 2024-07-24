package com.weiiboo.job.mapper.user;

import com.weiiboo.modules.api.user.domin.UserDO;

import java.util.List;

public interface UserMapper {
    /**
     * 更新用户信息
     * @param user userDO
     */
    void updateUser(UserDO user);

    /**
     * 获取所有UserId
     */
    List<Long> getAllUserId();
}
