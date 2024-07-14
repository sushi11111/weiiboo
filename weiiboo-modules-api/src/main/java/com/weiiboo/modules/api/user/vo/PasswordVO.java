package com.weiiboo.modules.api.user.vo;

import lombok.Data;

@Data
public class PasswordVO {
    private String oldPassword;
    private String newPassword;
}