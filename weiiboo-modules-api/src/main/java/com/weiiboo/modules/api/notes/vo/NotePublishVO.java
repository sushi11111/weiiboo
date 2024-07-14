package com.weiiboo.modules.api.notes.vo;

import lombok.Data;

@Data
public class NotePublishVO {
    private Long notesId;
    private String title;
    private String content;
    private String realContent;
    private Long belongUserid;
    private Integer belongCategory;
    private String coverPicture;
    private Integer type;
    private String notesResources;
    private Double longitude;
    private Double latitude;
    private String address;
    private Integer authority;

    private static final long serialVersionUID = 1L;
}
