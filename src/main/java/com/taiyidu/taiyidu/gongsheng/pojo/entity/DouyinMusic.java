package com.taiyidu.taiyidu.gongsheng.pojo.entity;

import lombok.Data;

@Data
public class DouyinMusic {
    private String title;
    private String author;
    private String avatar;
    // 视频：videoId字符串 / 图文：完整mp3链接
    private String url;
}