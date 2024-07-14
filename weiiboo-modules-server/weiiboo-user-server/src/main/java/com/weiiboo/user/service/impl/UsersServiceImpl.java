package com.weiiboo.user.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weiiboo.common.Utils.*;
import com.weiiboo.common.constant.BaseConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.common.domin.Result;
import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.common.web.exception.SystemException;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.common.redis.utils.RedisKey;
import com.weiiboo.common.web.properties.JwtProperties;
import com.weiiboo.common.web.utils.IPUtils;
import com.weiiboo.common.web.utils.JWTUtil;
import com.weiiboo.modules.api.user.domin.UserAttentionDO;
import com.weiiboo.modules.api.user.domin.UserDO;
import com.weiiboo.modules.api.user.vo.PasswordVO;
import com.weiiboo.modules.api.user.vo.RegisterInfoVO;
import com.weiiboo.modules.api.user.vo.UserVO;
import com.weiiboo.modules.api.user.vo.ViewUserVO;
import com.weiiboo.user.mapper.UserAttentionMapper;
import com.weiiboo.user.mapper.UserFansMapper;
import com.weiiboo.user.mapper.UsersMapper;
import com.weiiboo.user.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.weiiboo.common.constant.RocketMQTopicConstant.USER_ADD_ES_TOPIC;

/**
* @author subscriber
* @description 针对表【users】的数据库操作Service实现
* @createDate 2024-03-03 16:47:03
*/
@Service
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, UserDO>
    implements UsersService {

    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private HttpServletRequest request;
    @Resource
    private RedisCache redisCache;
    @Resource
    private UserAttentionMapper userAttentionMapper;
    @Resource
    private UserFansMapper userFansMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 用户注册
     *
     * @param phoneNumber 用户手机号
     * @param password 用户密码
     * @return UserVO
     */
    @Override
    public Result<UserVO> loginByPhoneNumber(String phoneNumber, String password) {
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        String SHA256Password = SHA256Utils.getSHA256(password);
        queryWrapper.lambda().eq(UserDO::getPhoneNumber,phoneNumber).eq(UserDO::getPassword,SHA256Password);
        log.info(SHA256Password);
        UserDO userDO = this.getOne(queryWrapper);
        // 查询不到该用户
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.PASSWORD_ERROR);
        }
        return generateUserVO(userDO);
    }

    /**
     * 用户注册
     *
     * @param registerInfoVO 用户注册信息
     * @return UserVO
     */
    @Override
    public Result<?> register(RegisterInfoVO registerInfoVO) {
        // 检验验证码是否正确
        boolean b = checkSmsCode(
                RedisKey.build(RedisConstant.REDIS_KEY_SMS_REGISTER_PHONE_CODE, registerInfoVO.getPhoneNumber()),
                registerInfoVO.getSmsCode());
        if (!b) {
            throw new BusinessException(ExceptionMsgEnum.SMS_CODE_ERROR);
        }
        QueryWrapper<UserDO>queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getPhoneNumber,registerInfoVO.getPhoneNumber());
        UserDO userDO = this.getOne(queryWrapper);
        // 手机号已存在
        if(Objects.nonNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.PHONE_NUMBER_ALREADY_REGISTER);
        }
        // 进行账号初始化
        UserDO newUserDO = initAccount(registerInfoVO);
        try{
            // 保存到mysql
            this.save(newUserDO);
            // 保存到ES
            rocketMQTemplate.asyncSend(USER_ADD_ES_TOPIC, JSON.toJSONString(newUserDO),new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("发送成功");
                }

                @Override
                public void onException(Throwable e) {
                    log.error("发送失败",e);
                }
            });
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.DB_ERROR, e);
        }
        return ResultUtil.successPost("注册成功", null);
    }

    /**
     * 用户退出登录
     *
     * @param userId 用户id
     * @return UserVO
     */
    @Override
    public Result<?> logout(Long userId) {
        // 检查userId与token是否匹配
        Map<String,Object>token = JWTUtil.parseToken(request.getHeader("token"));
        Long id = Long.valueOf(String.valueOf(token.get("userId")));
        if(!id.equals(userId)){
            throw new BusinessException(ExceptionMsgEnum.TOKEN_INVALID);
        }
        // 判断是否需要更新用户信息
        if(redisCache.sHasKey(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST,userId)){
            Map<String, Object> map = redisCache.hmget(
                    RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userId)));
            UserVO userVO = new UserVO(map);
            UserDO userDO = new UserDO();
            // 对信息进行更新
            userDO.setBirthday(Date.valueOf(userVO.getBirthday()));
            userDO.setArea(userVO.getArea());
            userDO.setAvatarUrl(userVO.getAvatarUrl());
            userDO.setPhoneNumber(userVO.getPhoneNumber());
            userDO.setSex(userVO.getSex());
            userDO.setSelfIntroduction(userVO.getSelfIntroduction());
            userDO.setAge(userVO.getAge());
            userDO.setNickname(userVO.getNickname());
            userDO.setHomePageBackground(userVO.getHomePageBackground());
            this.updateById(userDO);
            // 删除缓存
            redisCache.del(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userId)));
            redisCache.setRemove(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST, userId);
            redisCache.del(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_EXPIRE, String.valueOf(userId)));
            redisCache.del(RedisKey.build(RedisConstant.REDIS_KEY_USER_ONLINE, String.valueOf(userId)));
        }
        return  ResultUtil.successPost("退出登录成功",null);
    }

    /**
     * 生成UserVO
     *
     * @param userId 用户id
     * @return UserVO
     */
    @Override
    public Result<UserVO> getUserInfo(Long userId) {
        log.info("getUserInfo： {}",userId);
        // userId为空
        if(Objects.isNull(userId)){
            throw  new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        // 根据ip地址获取地理信息
        String ipAddr = IPUtils.getRealIpAddr(request);
        String addr = IPUtils.getAddrByIp(ipAddr);
        String address = IPUtils.splitAddress(addr);
        log.info("ip地址： {}",address);
        // 缓存中存在数据
        if(redisCache.hasKey(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO,String.valueOf(userId)))){
            Map<String,Object>map =redisCache.hmget(
                    RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO,String.valueOf(userId)));
            UserVO userVO = new UserVO(map);
            //设置地址
            userVO.setIpAddr(address);
            redisCache.hset(RedisKey.build(
                    RedisConstant.REDIS_KEY_USER_LOGIN_INFO,String.valueOf(userId)),"ipAddr",address);
            return ResultUtil.successGet("获取用户信息成功",userVO);
        }
        // 缓存中没有数据
        // 数据库中根据id查询
        UserDO userDO = this.getById(userId);
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.TOKEN_INVALID);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO,userVO);
        // 设置ip归属地
        userVO.setIpAddr(address);
        // 存到redis中
        redisCache.hmset(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO,
                        String.valueOf(userDO.getId())), UserVO.toMap(userVO));
        return ResultUtil.successGet("获取用户信息成功",userVO);
    }

    /**
     * 重置密码
     * @param phoneNumber 手机号
     * @param password 密码
     * @param smsCode 短信验证码
     * @return 重置结果
     */
    @Override
    public Result<?> resetPassword(String phoneNumber, String password, String smsCode) {
        boolean b = checkSmsCode(RedisKey.build(RedisConstant.REDIS_KEY_SMS_RESET_PASSWORD_PHONE_CODE,
                phoneNumber),smsCode);
        // 验证码错误
        if(!b){
            throw new BusinessException(ExceptionMsgEnum.SMS_CODE_ERROR);
        }
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getPhoneNumber,phoneNumber);
        UserDO userDO = this.getOne(queryWrapper);
        // 手机号未注册
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.PHONE_NUMBER_NOT_REGISTER);
        }
        // 使用SHA256密码加密 通过手机号给密码加盐
        String md5 = SHA256Utils.getSHA256(phoneNumber + password);
        userDO.setPassword(md5);
        try{
            this.updateById(userDO);
        }catch (Exception e){
            throw new SystemException(ExceptionMsgEnum.DB_ERROR,e);
        }
        return ResultUtil.successPost("重置密码成功",null);
    }

    /**
     * 查看用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    @Override
    public Result<ViewUserVO> viewUserInfo(Long userId) {
        if(Objects.isNull(userId)){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        Map<String,Object> token = JWTUtil.parseToken(request.getHeader("token"));
        Long id = Long.valueOf(String.valueOf(token.get("userId")));
        UserAttentionDO exist = userAttentionMapper.getExist(id,userId);
        // 缓存中存在
        if(redisCache.hasKey(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO,String.valueOf(userId)))){
            Map<String,Object> map = redisCache.hmget(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO,String.valueOf(userId)));
            UserVO userVO = new UserVO(map);
            ViewUserVO viewUserVO = new ViewUserVO();
            BeanUtils.copyProperties(userVO,viewUserVO);
            if(Objects.nonNull(exist)){
                viewUserVO.setAttentionStatus(1);
            }else{
                viewUserVO.setAttentionStatus(0);
            }
            return ResultUtil.successGet("获取用户信息成功",viewUserVO);
        }

        // 缓存中不存在
        UserDO userDO = this.getById(userId);
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);

        Integer attentionNum = userAttentionMapper.getCountById(userDO.getId());
        Integer fansNum = userFansMapper.getCountById(userDO.getId());
        if(attentionNum==null){
            attentionNum=0;
        }
        if(fansNum==null){
            fansNum=0;
        }
        userVO.setAttentionNum(attentionNum);
        userVO.setFansNum(fansNum);
        // 放入缓存
        redisCache.hmset(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO,
                String.valueOf(userDO.getId())),UserVO.toMap(userVO));
        ViewUserVO viewUserVO = new ViewUserVO();
        BeanUtils.copyProperties(userVO,viewUserVO);
        if(Objects.nonNull(exist)){
            viewUserVO.setAttentionStatus(1);
        }else{
            viewUserVO.setAttentionStatus(0);
        }
        return ResultUtil.successGet("获取用户信息成功",viewUserVO);
    }

    /**
     * 换绑手机号
     * @param phoneNumber 老手机号
     * @param newPhoneNumber 新手机号
     * @param smsCode 短信验证码
     * @return 用户信息
     */
    @Override
    public Result<Boolean> updatePhoneNumber(String phoneNumber, String newPhoneNumber,String smsCode) {
        Long userId = JWTUtil.getCurrentUserId(request.getHeader("token"));
        boolean b = checkSmsCode(
                RedisKey.build(RedisConstant.REDIS_KEY_SMS_BIND_PHONE_CODE,phoneNumber),smsCode);
        if(!b){
            throw new BusinessException(ExceptionMsgEnum.SMS_CODE_ERROR);
        }
        QueryWrapper<UserDO>queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserDO::getPhoneNumber,phoneNumber);
        UserDO userDO = this.getOne(queryWrapper);
        if (Objects.nonNull(userDO)) {
            throw new BusinessException(ExceptionMsgEnum.PHONE_NUMBER_EXIST);
        }
        UserDO user = new UserDO();
        user.setId(userId);
        user.setPhoneNumber(newPhoneNumber);
        this.updateById(user);
        redisCache.hset(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO,String.valueOf(userId)),"phoneNumber",newPhoneNumber);
        return ResultUtil.successPost("修改手机号成功",true);
    }

    @Override
    public Result<?> resetPasswordByOldPassword(PasswordVO passwordVO) {
        if(!FieldValidationUtil.isPassword(passwordVO.getOldPassword())||
                !FieldValidationUtil.isPassword(passwordVO.getNewPassword())){
            return ResultUtil.errorPost("密码格式不正确");
        }
        Long userId = JWTUtil.getCurrentUserId(request.getHeader("token"));
        UserDO userDO = this.getById(userId);
        String md5 = SHA256Utils.getSHA256(passwordVO.getOldPassword());
        if(!md5.equals(userDO.getPassword())){
            throw new BusinessException((ExceptionMsgEnum.PASSWORD_ERROR));
        }
        String md5New = SHA256Utils.getSHA256(passwordVO.getNewPassword());
        userDO.setPassword(md5New);
        try{
            this.updateById(userDO);
        }catch (Exception e){
            throw new SystemException(ExceptionMsgEnum.DB_ERROR,e);
        }
        return ResultUtil.successPost("修改密码成功",null);
    }

    @Override
    public Result<?> updateAvatarUrl(UserVO userVO) {
        checkField(userVO.getId(),userVO.getAvatarUrl());
        // 更新redis中info信息
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userVO.getId())),
                "avatarUrl",
                userVO.getAvatarUrl());
        // 更新es
        rocketMQTemplate.asyncSend(RocketMQTopicConstant.USER_UPDATE_ES_TOPIC, JSON.toJSONString(userVO), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送成功");
            }

            @Override
            public void onException(Throwable e) {
                log.info("发送失败");
            }
        });
        redisCache.sSet(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST,userVO.getId());
        return ResultUtil.successPost("修改头像成功",null);
    }

    @Override
    public Result<?> updateBackgroundImage(UserVO userVO) {
        checkField(userVO.getId(),userVO.getHomePageBackground());
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userVO.getId())),
                "avatarUrl",
                userVO.getAvatarUrl());
        redisCache.sSet(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST,userVO.getId());
        return ResultUtil.successPost("修改背景图成功",null);
    }

    @Override
    public Result<?> updateNickname(UserVO userVO) {
        checkField(userVO.getId(),userVO.getNickname());
        if (userVO.getNickname().length() > 12 || userVO.getNickname().length() < 2) {
            return ResultUtil.errorPost("昵称长度为2-12位");
        }
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userVO.getId())),
                "nickname",
                userVO.getNickname()
        );
        redisCache.sSet(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST,userVO.getId());
        rocketMQTemplate.asyncSend(RocketMQTopicConstant.USER_UPDATE_ES_TOPIC, JSON.toJSONString(userVO),new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("发送成功");
            }
            @Override
            public void onException(Throwable e) {
                log.error("发送失败",e);
            }
        });
        return ResultUtil.successPost("修改昵称成功", null);
    }

    @Override
    public Result<?> updateIntroduction(UserVO userVO) {
        checkField(userVO.getId(),userVO.getSelfIntroduction());
        if(userVO.getSelfIntroduction().length() > 100){
            return ResultUtil.errorPost("简介长度不能超过100");
        }
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userVO.getId())),
                "selfIntroduction",
                userVO.getSelfIntroduction()
        );
        redisCache.sSet(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST, userVO.getId());
        return ResultUtil.successPost("修改简介成功", null);
    }

    @Override
    public Result<?> updateSex(UserVO userVO) {
        checkField(userVO.getId(),String.valueOf(userVO.getSex()));
        if (userVO.getSex() < 0 || userVO.getSex() > 1) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userVO.getId())),
                "sex",
                userVO.getSex()
        );
        redisCache.sSet(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST, userVO.getId());
        return ResultUtil.successPost("修改性别成功", null);
    }

    @Override
    public Result<Integer> updateBirthday(UserVO userVO) {
        checkField(userVO.getId(),userVO.getBirthday());
        Date date = Date.valueOf(userVO.getBirthday());
        // 判断生日是否合法，不能大于当前时间
        long currentTimeMillis = System.currentTimeMillis();
        if (date.getTime() > currentTimeMillis) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        // 年龄同步更新
        int age = TimeUtil.calculateAge(date.toLocalDate());
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userVO.getId())),
                "birthday",
                userVO.getBirthday()
        );
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userVO.getId())),
                "age",
                age
        );
        redisCache.sSet(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST, userVO.getId());
        return ResultUtil.successPost("修改生日成功", age);
    }

    @Override
    public Result<?> updateArea(UserVO userVO) {
        checkField(userVO.getId(),userVO.getArea());
        // 判断地区是否合法，如果不合法则抛出异常，格式为：省 市 区
        String[] split = userVO.getArea().split(" ");
        if (split.length != 3) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        String area;
        if (split[0].equals(split[1])) {
            area = split[0] + " " + split[2];
        } else {
            area = userVO.getArea();
        }
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userVO.getId())),
                "area",
                area
        );
        redisCache.sSet(RedisConstant.REDIS_KEY_USER_INFO_UPDATE_LIST, userVO.getId());
        return ResultUtil.successPost("修改地区成功", null);
    }

    @Override
    public Result<?> logoff(Long userId) {
        return null;
    }

    @Override
    public Result<UserVO> loginByUid(String uid, String password) {
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        String SHA256Password = SHA256Utils.getSHA256(password);
        queryWrapper.lambda().eq(UserDO::getUid,uid).eq(UserDO::getPassword,SHA256Password);
        log.info(SHA256Password);
        UserDO userDO = this.getOne(queryWrapper);
        // 查询不到该用户
        if(Objects.isNull(userDO)){
            throw new BusinessException(ExceptionMsgEnum.PASSWORD_ERROR);
        }
        return generateUserVO(userDO);
    }

    /**
     * 生成UserVO
     *
     * @param userDO 用户信息
     * @return UserVO
     */
    private Result<UserVO> generateUserVO(UserDO userDO) {
        // 查询缓存
        boolean flag = redisCache.hasKey(RedisConstant.REDIS_KEY_USER_LOGIN_INFO + userDO.getId());
        UserVO userVO;
        // 缓存存在
        if(flag){
            Map<String,Object> map= redisCache.hmget(RedisConstant.REDIS_KEY_USER_LOGIN_INFO + userDO.getId());
            userVO = new UserVO(map);
        }
        // 缓存中不存在
        else {
            userVO = new UserVO();
            BeanUtils.copyProperties(userDO,userVO);
            if(Objects.nonNull(userDO.getBirthday())){
                userVO.setBirthday(userDO.getBirthday().toString());
            }
            Integer attentionNum = userAttentionMapper.getCountById(userDO.getId());
            Integer fansNum = userFansMapper.getCountById(userDO.getId());
            if(attentionNum==null){
                attentionNum=0;
            }
            if(fansNum==null){
                fansNum=0;
            }
            userVO.setAttentionNum(attentionNum);
            userVO.setFansNum(fansNum);
            // 放入缓存
            redisCache.hmset(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userDO.getId())),
                    UserVO.toMap(userVO));
        }
        // 生成token
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",userDO.getId());
        claims.put("currwntTimeMillis",System.currentTimeMillis());
        String token = JWTUtil.createToken(claims);
        // 用redis设置token过期时间 过期时间为登录时间+1天
        redisCache.set(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_EXPIRE,String.valueOf(userDO.getId())),
                        System.currentTimeMillis() + jwtProperties.getExpireTime());
        // 更新redis里的token 防止用户在其他设备登录
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userDO.getId())),
                "token", token);
        log.info("token:{}", token);
        userVO.setToken(token);
        String ipAddr = IPUtils.getRealIpAddr(request);
        String addr = IPUtils.getAddrByIp(ipAddr);
        String address = IPUtils.splitAddress(addr);
        if (StringUtils.hasText(address)) {
            // 设置ip归属地
            userVO.setIpAddr(IPUtils.splitAddress(address));
        }else {
            userVO.setIpAddr("未知");
        }
        redisCache.hset(
                RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(userDO.getId())),
                "ipAddr", address);
        return ResultUtil.successPost(userVO);
    }

    /**
     * 检验验证码是否正确
     *
     * @param key     redis key
     * @param smsCode 验证码
     */
    private boolean checkSmsCode(String key, String smsCode) {
        String s = (String) redisCache.get(key);
        return !Objects.isNull(s) && s.equals(smsCode);
    }

    /**
     * 账号初始化
     * @param registerInfoVO 注册信息
     * @return UserDO 用户信息
     */
    private UserDO initAccount(RegisterInfoVO registerInfoVO){
        UserDO userDO = new UserDO();
        userDO.setPhoneNumber(registerInfoVO.getPhoneNumber());
        userDO.setPassword(SHA256Utils.getSHA256(registerInfoVO.getPassword()));
        // 设置默认头像和昵称
        userDO.setAvatarUrl(BaseConstant.DEFAULT_AVATAR_URL);
        userDO.setNickname(BaseConstant.DEFAULT_NICKNAME_PREFIX + CodeUtil.createNickname());
        // 创建一个uid
        String uid = CodeUtil.createUid(registerInfoVO.getPhoneNumber());
        userDO.setUid(uid);
        // 设置默认性别
        userDO.setSex(2);
        userDO.setHomePageBackground(BaseConstant.DEFAULT_HOME_PAGE_BACKGROUD);
        return userDO;
    }

    /**
     * 检查字段是否为空
     * @param id 用户id
     * @param field 字段
     */
    private void checkField(Long id,String field) {
        if(!StringUtils.hasText(String.valueOf(id))){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        if(!StringUtils.hasText(field)){
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        boolean b = redisCache.hasKey(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(id)));
        if(!b){
            throw new BusinessException(ExceptionMsgEnum.ACCOUNT_EXCEPTION);
        }
    }
}