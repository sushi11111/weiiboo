package com.weiiboo.es.service;

import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.user.domin.UserEsDO;

import java.util.List;

public interface UserSearchService {
    Result<List<UserEsDO>> getUser(String keyword, Integer page, Integer pageSize);

    void addUser(UserEsDO userEsDO);

    void updateUser(UserEsDO userEsDO);
}
