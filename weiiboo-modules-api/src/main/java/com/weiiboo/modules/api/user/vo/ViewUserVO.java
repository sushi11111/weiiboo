package com.weiiboo.modules.api.user.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ViewUserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String uid;
    private String nickname;
    private String avatarUrl;
    private Integer age;
    private Integer sex;
    private String area;
    private String birthday;
    private String selfIntroduction;
    private String homePageBackground;
    private Integer attentionNum;
    private Integer fansNum;
    private Integer attentionStatus;
}
