package com.weiiboo.third.service;

import com.weiiboo.common.domin.Result;

public interface AliyunSmsService {
    Result<?> sendBindPhoneSms(String phoneNumber);

    Result<?> sendResetPhoneSms(String phoneNumber);

    Result<?> sendRegisterPhoneSms(String phoneNumber);

    Result<Boolean> checkResetSmsCode(String phoneNumber, String smsCode);

    Result<Boolean> checkBindSmsCode(String phoneNumber, String smsCode);
}
