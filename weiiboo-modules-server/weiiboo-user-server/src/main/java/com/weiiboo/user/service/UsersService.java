package com.weiiboo.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.weiiboo.common.domin.Result;
import com.weiiboo.modules.api.user.domin.UserDO;
import com.weiiboo.modules.api.user.vo.PasswordVO;
import com.weiiboo.modules.api.user.vo.RegisterInfoVO;
import com.weiiboo.modules.api.user.vo.UserVO;
import com.weiiboo.modules.api.user.vo.ViewUserVO;


public interface UsersService extends IService<UserDO> {

     Result<UserVO> loginByPhoneNumber(String phoneNumber, String password);

     Result<?>register(RegisterInfoVO registerInfoVO);

     Result<?>logout(Long userId);

     Result<UserVO>getUserInfo(Long userId);

     Result<?> resetPasswordBySms(String phoneNumber, String password, String smsCode);

     Result<ViewUserVO> viewUserInfo(Long userId);

     Result<Boolean> updatePhoneNumber(String phoneNumber, String newPhoneNumber,String smsCode);

     Result<?> resetPasswordByOldPassword(PasswordVO passwordVO);

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
