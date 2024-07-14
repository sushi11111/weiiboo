package com.weiiboo.modules.api.notes.vo;

import com.weiiboo.modules.api.notes.dto.ResourcesDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Date;

@Data
public class NotesVO implements Serializable {
    private Long id;
    private String title;
    private String content;
    private String coverPicture;
    private String nickname;
    private String avatarUrl;
    private Long belongUserId;
    private Integer notesLikeNum;
    private Integer notesCollectNum;
    private Integer notesViewNum;
    private Integer notesType;
    private Boolean isLike;
    private Boolean isCollect;
    private Boolean isFollow;
    private List<ResourcesDTO> notesResources;
    private String province;
    private Date createTime;
    private Date updateTime;
}
