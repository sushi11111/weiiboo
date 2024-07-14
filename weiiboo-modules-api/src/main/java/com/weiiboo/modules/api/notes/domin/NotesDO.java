package com.weiiboo.modules.api.notes.domin;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName notes
 */
@Data
@TableName("notes")
public class NotesDO implements Serializable {
    /**
     * 笔记id
     */
    @TableId
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 所属用户id
     */
    private Long belongUserId;

    /**
     * 所属分类
     */
    private Integer belongCategory;

    /**
     * 0为图片，1为视频，3为文字
     */
    private Integer type;

    /**
     * 视频/图片资源，图片最多九张，视频只能一个
     */
    private String notesResources;

    /**
     * 封面图片url
     */
    private String coverPicture;

    /**
     * 点赞数
     */
    private Integer notesLikeNum;

    /**
     * 收藏数
     */
    private Integer notesCollectNum;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 浏览量
     */
    private Integer notesViewNum;

    /**
     * 地点
     */
    private String address;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 省份，中国显示省份，国外显示国家
     */
    private String province;

    /**
     * 是否公开，0为公开，1为私密
     */
    private Integer authority;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private static final long serialVersionUID = 1L;

}