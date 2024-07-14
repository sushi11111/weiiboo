package com.weiiboo.modules.api.user.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserBlackVO implements Serializable {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatarUrl;
}
