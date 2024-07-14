package com.weiiboo.modules.api.user.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String phoneNumber;
    private String password;
    private String smsCode;
}
