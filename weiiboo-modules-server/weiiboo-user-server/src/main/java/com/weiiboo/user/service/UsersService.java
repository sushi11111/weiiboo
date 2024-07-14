package com.weiiboo.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.user.domin.UserDO;
import com.weiiboo.modules.api.user.vo.PasswordVO;
import com.weiiboo.modules.api.user.vo.RegisterInfoVO;
import com.weiiboo.modules.api.user.vo.UserVO;
import com.weiiboo.modules.api.user.vo.ViewUserVO;


/**
* @author subscriber
* @description 针对表【users】的数据库操作Service
* @createDate 2024-03-03 16:47:03
*/
public interface UsersService extends IService<UserDO> {
     /**
      * 手机号登录
      * @param phoneNumber 手机号
      * @param password 密码
      * @return 登录结果
      */
     Result<UserVO> loginByPhoneNumber(String phoneNumber, String password);
     /**
      * 手机号注册
      * @param registerInfoVO 注册信息
      * @return 注册结果
      */
     Result<?>register(RegisterInfoVO registerInfoVO);
     /**
      * 退出登录
      * @param userId 用户id
      * @return 退出结果
      */
     Result<?>logout(Long userId);
     /**
      * 获取用户信息
      * @param userId 用户id
      * @return 用户信息
      */
     Result<UserVO>getUserInfo(Long userId);
     /**
      * 重置密码
      * @param phoneNumber 手机号
      * @param password 密码
      * @param smsCode 短信验证码
      * @return 重置结果
      */
     Result<?> resetPassword(String phoneNumber, String password, String smsCode);

     /**
      * 查看用户信息
      * @param userId 用户id
      * @return 用户信息
      */
     Result<ViewUserVO> viewUserInfo(Long userId);
     /**
      * 换绑手机号
      * @param phoneNumber 老手机号
      * @param newPhoneNumber 新手机号
      * @param smsCode 短信验证码
      * @return 用户信息
      */
     Result<Boolean> updatePhoneNumber(String phoneNumber, String newPhoneNumber,String smsCode);

     /**
      * 重置密码
      * @param passwordVO 密码信息
      * @return 重置结果
      */
     Result<?> resetPasswordByOldPassword(PasswordVO passwordVO);

     /**
      * 更新用户头像
      * @param userVO 用户信息
      * @return 更新结果
      */
     Result<?> updateAvatarUrl(UserVO userVO);

     Result<?> logoff(Long userId);

     Result<UserVO> loginByUid(String uid, String password);

     Result<?> updateBackgroundImage(UserVO userVO);

     Result<?> updateNickname(UserVO userVO);

     Result<?> updateIntroduction(UserVO userVO);

     Result<?> updateSex(UserVO userVO);

     Result<Integer> updateBirthday(UserVO userVO);

     Result<?> updateArea(UserVO userVO);
}
