package com.weiiboo.common.domin;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageParam implements Serializable {
    private Integer page;
    private Integer pageSize;
    private Double longitude;
    private Double latitude;
    private String keyword;
}
