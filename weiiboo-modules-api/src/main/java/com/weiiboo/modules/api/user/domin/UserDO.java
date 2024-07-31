package com.weiiboo.modules.api.user.domin;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.weiiboo.modules.api.user.vo.UserVO;
import lombok.Data;


import java.io.Serializable;
import java.sql.Date;

@Data
@TableName("users")
public class UserDO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId
    private Long id;

    /**
     * 账号的唯一凭证，可以为英文，数字，下划线，6-15位组成，相对于用户id，其不同之处在于用户可以对其进行修改，但是也是唯一的
     */
    private String uid;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户头像地址
     */
    private String avatarUrl;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别，0为女，1为男，默认为0
     */
    private Integer sex;

    /**
     * 地区
     */
    private String area;

    /**
     * 自我介绍
     */
    private String selfIntroduction;

    /**
     * 生日
     */
    private Date birthday;


    /**
     * 主页背景图
     */
    private String homePageBackground;

    /**
     * 职业，可以根据该字段为用户的推荐加一点占比
     */
    private String occupation;

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 密码
     */
    private String password;

    /**
     * 账号状态，0为正常，1为注销，2为封禁
     */
    private Integer accountStatus;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private java.util.Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private java.util.Date updateTime;

    public static UserDO voToDO(UserVO userVO) {
        UserDO userDO = new UserDO();
        userDO.setId(userVO.getId());
        userDO.setNickname(userVO.getNickname());
        userDO.setAge(userVO.getAge());
        userDO.setSex(userVO.getSex());
        userDO.setArea(userVO.getArea());
        userDO.setBirthday(Date.valueOf(userVO.getBirthday()));
        userDO.setHomePageBackground(userVO.getHomePageBackground());
        userDO.setSelfIntroduction(userVO.getSelfIntroduction());
        userDO.setPhoneNumber(userVO.getPhoneNumber());
        userDO.setAvatarUrl(userVO.getAvatarUrl());
        userDO.setUid(userVO.getUid());
        return userDO;
    }
}