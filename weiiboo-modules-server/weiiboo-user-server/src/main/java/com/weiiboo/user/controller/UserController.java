package com.weiiboo.user.controller;


import com.weiiboo.common.Utils.FieldValidationUtil;
import com.weiiboo.common.Utils.ResultUtil;
import com.weiiboo.common.domin.Result;
import com.weiiboo.common.redis.constant.BloomFilterMap;
import com.weiiboo.common.web.aop.bloomFilter.BloomFilterProcessing;
import com.weiiboo.common.web.aop.idempotent.Idempotent;
import com.weiiboo.modules.api.user.vo.PasswordVO;
import com.weiiboo.modules.api.user.vo.UserVO;
import com.weiiboo.modules.api.user.vo.ViewUserVO;
import com.weiiboo.user.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UsersService usersService;

    /**
     * 获取用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
    public Result<UserVO> getUserInfo(@RequestParam Long userId){
        log.info("userId:{}",userId);
        return usersService.getUserInfo(userId);
    }

    /**
     * 重置密码
     * @param phoneNumber 手机号
     * @param password 密码
     * @param smsCode 短信验证码
     * @return 重置结果
     */
    @PostMapping("/resetPassword")
    @Idempotent(value = "/user/resetPassword",expireTime = 60000)
    public Result<?>resetPassword(String phoneNumber,String password,String smsCode){
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost("手机号格式不正确");
        }
        if(!FieldValidationUtil.isPhoneNumber(password)){
            return ResultUtil.errorPost("密码必须包含数字和字母，长度为6-16位");
        }
        if(!FieldValidationUtil.isSmsCode(smsCode)){
            return ResultUtil.errorPost("验证码格式不正确");
        }
        return usersService.resetPassword(phoneNumber,password,smsCode);
    }

    /**
     * 查看对方用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    @GetMapping("/viewUserInfo")
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userId"})
    public Result<ViewUserVO> vieeUserInfo(Long userId){
        return usersService.viewUserInfo(userId);
    }

    /**
     * 更新用户头像
     * @param userVO 用户信息
     * @return 更新结果
     */
    @PostMapping("/updateAvatarUrl")
    @Idempotent(value = "/user/updateAvatarUrl",expireTime = 60000)
    public Result<?> updateAvatarUrl(@RequestBody UserVO userVO){
        return usersService.updateAvatarUrl(userVO);
    }

    /**
     * 更新用户主页背景
     * @param userVO 用户信息
     * @return 更新结果
     */
    @PostMapping("/updateBackgroundImage")
    @Idempotent(value = "/user/updateBackgroundImage",expireTime = 60000)
    public Result<?> updateBackgroundImage(@RequestBody UserVO userVO){
        return usersService.updateBackgroundImage(userVO);
    }

    /**
     * 更新用户昵称
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateNickname")
    @Idempotent(value = "/user/updateNickname", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateNickname(@RequestBody UserVO userVO) {
        return usersService.updateNickname(userVO);
    }

    /**
     * 更新用户简介
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateIntroduction")
    @Idempotent(value = "/user/updateIntroduction", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateIntroduction(@RequestBody UserVO userVO) {
        return usersService.updateIntroduction(userVO);
    }

    /**
     * 更新用户性别
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateSex")
    @Idempotent(value = "/user/updateSex", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateSex(@RequestBody UserVO userVO) {
        return usersService.updateSex(userVO);
    }

    /**
     * 更新用户生日
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateBirthday")
    @Idempotent(value = "/user/updateBirthday", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<Integer> updateBirthday(@RequestBody UserVO userVO) {
        return usersService.updateBirthday(userVO);
    }

    /**
     * 更新用户地区
     * @param userVO 用户信息
     * @return 用户信息
     */
    @PostMapping("/updateArea")
    @Idempotent(value = "/user/updateArea", expireTime = 3000)
    @BloomFilterProcessing(map = BloomFilterMap.USER_ID_BLOOM_FILTER,keys = {"#userVO.id"})
    public Result<?> updateArea(@RequestBody UserVO userVO) {
        return usersService.updateArea(userVO);
    }

    /**
     * 换绑手机号
     * @param phoneNumber 手机号
     * @param smsCode 短信验证码
     * @return 更新结果
     */
    @PostMapping("/updatePhoneNumber")
    @Idempotent(value = "/user/updatePhoneNumber",expireTime = 60000)
    public Result<Boolean> updatePhoneNumber(String phoneNumber,String newPhoneNumber,String smsCode){
        if(!FieldValidationUtil.isPhoneNumber(phoneNumber)){
            return ResultUtil.errorPost("手机号格式不正确");
        }
        if(!FieldValidationUtil.isSmsCode(smsCode)){
            return ResultUtil.errorPost("验证码格式不正确");
        }
        return usersService.updatePhoneNumber(phoneNumber,newPhoneNumber,smsCode);
    }

    /**
     * 通过旧密码重置密码
     * @param passwordVO 密码信息
     * @return 重置结果
     */
    @PostMapping("/resetPasswordByOldPassword")
    @Idempotent(value = "/user/resetPasswordByOldPassword",expireTime = 60000)
    public Result<?>resetPasswordByOldPassword(@RequestBody PasswordVO passwordVO){
        return usersService.resetPasswordByOldPassword(passwordVO);
    }
}