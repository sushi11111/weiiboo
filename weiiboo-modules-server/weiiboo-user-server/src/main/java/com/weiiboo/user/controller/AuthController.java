package com.weiiboo.user.controller;



import com.weiiboo.common.Utils.FieldValidationUtil;
import com.weiiboo.common.Utils.ResultUtil;
import com.weiiboo.common.domin.Result;
import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.modules.api.user.vo.RegisterInfoVO;
import com.weiiboo.modules.api.user.vo.UserVO;
import com.weiiboo.user.service.UsersService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource
    private UsersService usersService;

    /**
     * 手机号登录
     * @param phoneNumber 手机号
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/loginByPhone")
    public Result<UserVO> loginByPhoneNumber(String phoneNumber, String password){
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_INVALID.getMsg());
        }
        if(!FieldValidationUtil.isPassword(password)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PASSWORD_INVALID.getMsg());
        }
        return usersService.loginByPhoneNumber(phoneNumber,password);
    }

    /**
     * uid登录
     * @param uid uid
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/loginByUid")
    public Result<UserVO> loginByUid(String uid, String password){
        if(!FieldValidationUtil.isUid(uid)){
            return ResultUtil.errorPost(ExceptionMsgEnum.UDI_INVALID.getMsg());
        }
        if(!FieldValidationUtil.isPassword(password)){
            return ResultUtil.errorPost(ExceptionMsgEnum.PASSWORD_INVALID.getMsg());
        }
        return usersService.loginByUid(uid,password);
    }

    /**
     * 手机号注册
     * @param registerInfoVO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<?>register(@RequestBody RegisterInfoVO registerInfoVO){
        if(!FieldValidationUtil.isPhoneNumber(registerInfoVO.getPhoneNumber())){
            return ResultUtil.errorPost(ExceptionMsgEnum.PHONE_NUMBER_INVALID.getMsg());
        }
        if(!FieldValidationUtil.isPassword(registerInfoVO.getPassword())){
            return ResultUtil.errorPost(ExceptionMsgEnum.PASSWORD_INVALID.getMsg());
        }
        if(!FieldValidationUtil.isSmsCode(registerInfoVO.getSmsCode())){
            return ResultUtil.errorPost(ExceptionMsgEnum.SMS_CODE_INVALID.getMsg());
        }
        return usersService.register(registerInfoVO);
    }

    /**
     * 退出登录
     * @param userId 用户id
     * @return 退出结果
     */
    @PostMapping("/logout")
    public Result<?>logout(Long userId){
        if(userId==null){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        return usersService.logout(userId);
    }

    /**
     * 注销
     * @param userId 用户id
     * @return 注销结果
     */
    @DeleteMapping ("/logoff")
    public Result<?>logoff(Long userId){
        if(userId==null){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        return usersService.logoff(userId);
    }
}