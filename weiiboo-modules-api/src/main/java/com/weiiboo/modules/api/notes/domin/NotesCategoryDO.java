package com.weiiboo.modules.api.notes.domin;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@TableName("categorys")
@Data
public class NotesCategoryDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类id
     */
    @TableId
    private Integer id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 排序，越大越靠前
     */
    private Integer categorySort;

    /**
     * 图标
     */
    private String icon;
}

