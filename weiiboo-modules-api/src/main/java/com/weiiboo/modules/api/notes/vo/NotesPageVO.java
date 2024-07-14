package com.weiiboo.modules.api.notes.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class NotesPageVO implements Serializable {
    private List<NotesVO>list;
    private Integer total;
    private Integer page;
    private Integer pageSize;
}