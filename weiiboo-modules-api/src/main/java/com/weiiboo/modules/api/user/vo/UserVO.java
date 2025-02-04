package com.weiiboo.modules.api.user.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UserVO {
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
    private String phoneNumber;
    private String token;
    private String ipAddr;
    private Integer attentionNum;
    private Integer fansNum;

    public UserVO() {
    }
    /**
     * 从map中构造UserVO
     * @param map map
     */
    public UserVO(Map<String,Object> map){
        Object o = map.get("id");
        if(o!=null){
            this.id = Long.parseLong(o.toString());
        }
        o = map.get("uid");
        if(o!=null){
            this.uid = o.toString();
        }
        o = map.get("nickname");
        if(o!=null){
            this.nickname = o.toString();
        }
        o = map.get("avatarUrl");
        if(o!=null){
            this.avatarUrl = o.toString();
        }
        o = map.get("age");
        if(o!=null){
            this.age = Integer.parseInt(o.toString());
        }
        o=map.get("sex");
        if(o!=null){
            this.sex=Integer.parseInt(o.toString());
        }
        o=map.get("area");
        if(o!=null){
            this.area=o.toString();
        }
        o=map.get("birthday");
        if(o!=null){
            this.birthday=o.toString();
        }
        o=map.get("selfIntroduction");
        if(o!=null){
            this.selfIntroduction=o.toString();
        }
        o=map.get("homePageBackground");
        if(o!=null){
            this.homePageBackground=o.toString();
        }
        o=map.get("phoneNumber");
        if(o!=null){
            this.phoneNumber=o.toString();
        }
        o=map.get("token");
        if(o!=null){
            this.token=o.toString();
        }
        o=map.get("ipAddr");
        if(o!=null){
            this.ipAddr=o.toString();
        }
        o=map.get("attentionNum");
        if(o!=null){
            this.attentionNum=Integer.parseInt(o.toString());
        }
        o=map.get("fansNum");
        if(o!=null){
            this.fansNum=Integer.parseInt(o.toString());
        }
    }
    /**
     * 将UserVO转换为map
     * @param userVO userVO
     * @return map
     */
    public static Map<String, Object> toMap(UserVO userVO) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", userVO.getId());
        map.put("uid", userVO.getUid());
        map.put("nickname", userVO.getNickname());
        map.put("avatarUrl", userVO.getAvatarUrl());
        map.put("age", userVO.getAge());
        map.put("sex", userVO.getSex());
        map.put("area", userVO.getArea());
        map.put("birthday", userVO.getBirthday());
        map.put("selfIntroduction", userVO.getSelfIntroduction());
        map.put("homePageBackground", userVO.getHomePageBackground());
        map.put("phoneNumber", userVO.getPhoneNumber());
        map.put("token", userVO.getToken());
        map.put("ipAddr", userVO.getIpAddr());
        map.put("attentionNum", userVO.getAttentionNum());
        map.put("fansNum", userVO.getFansNum());
        return map;
    }
}
