package com.taiyidu.taiyidu.gongsheng.pojo.vo;

import lombok.Data;

@Data
public class HistoryRecordVo {
    private Integer id;
    private String awemeId;
    private String title;
    private Integer mediaType;
    private String originUrl;
    private String createTime;
    private String downloadUrls;
}
