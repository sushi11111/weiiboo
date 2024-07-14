package com.weiiboo.third.controller;

import com.weiiboo.common.Utils.FieldValidationUtil;
import com.weiiboo.common.Utils.ResultUtil;
import com.weiiboo.common.domin.Result;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.third.service.AliyunSmsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/third")
public class SMSController {
    @Resource
    private AliyunSmsService aliyunSmsService;

    /**
     * 发送绑定手机号验证码
     * @param phoneNumber 手机号
     * @return 发送结果
     */
    @GetMapping("/sendBindPhoneSms")
    public Result<?> sendBindPhoneSms(String phoneNumber) {
        if(!FieldValidationUtil.phoneNumberNotNull(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_IS_NULL);
        }
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_INVALID);
        }
        return aliyunSmsService.sendBindPhoneSms(phoneNumber);
    }

    /**
     * 发送换绑手机号验证码
     * @param phoneNumber 手机号
     * @return 发送结果
     */
    @GetMapping("/sendResetPhoneSms")
    public Result<?> sendResetPhoneSms(String phoneNumber) {
        if(!FieldValidationUtil.phoneNumberNotNull(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_IS_NULL);
        }
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_INVALID);
        }
        return aliyunSmsService.sendResetPhoneSms(phoneNumber);
    }

    /**
     * 发送注册手机号验证码
     * @param phoneNumber 手机号
     * @return 发送结果
     */
    @GetMapping("/sendRegisterPhoneSms")
    public Result<?> sendRegisterPhoneSms(String phoneNumber) {
        if(!FieldValidationUtil.phoneNumberNotNull(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_IS_NULL);
        }
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_INVALID);
        }
        return aliyunSmsService.sendRegisterPhoneSms(phoneNumber);
    }

    /**
     * 验证绑定手机号短信验证码
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     * @return 验证结果
     */
    @PostMapping("/checkBindSmsCode")
    public Result<Boolean> checkBindSmsCode(String phoneNumber, String smsCode) {
        if(!FieldValidationUtil.phoneNumberNotNull(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_IS_NULL.getMsg());
        }
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_INVALID.getMsg());
        }
        if(!FieldValidationUtil.smsCodeNotNull(smsCode)){
            return ResultUtil.errorPost(ExceptionMsgEnum.SMS_CODE_IS_NULL.getMsg());
        }
        if(!FieldValidationUtil.isSmsCode(smsCode)){
            return ResultUtil.errorPost(ExceptionMsgEnum.SMS_CODE_INVALID.getMsg());
        }
        return aliyunSmsService.checkBindSmsCode(phoneNumber, smsCode);
    }

    /**
     * 验证重置密码(换绑手机)短信验证码
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     * @return 验证结果
     */
    @PostMapping("/checkResetSmsCode")
    public Result<Boolean> checkResetSmsCode(String phoneNumber, String smsCode) {
        if(!FieldValidationUtil.phoneNumberNotNull(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_IS_NULL.getMsg());
        }
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_INVALID.getMsg());
        }
        if(!FieldValidationUtil.smsCodeNotNull(smsCode)){
            return ResultUtil.errorPost(ExceptionMsgEnum.SMS_CODE_IS_NULL.getMsg());
        }
        if(!FieldValidationUtil.isSmsCode(smsCode)){
            return ResultUtil.errorPost(ExceptionMsgEnum.SMS_CODE_INVALID.getMsg());
        }
        return aliyunSmsService.checkResetSmsCode(phoneNumber, smsCode);
    }
}
