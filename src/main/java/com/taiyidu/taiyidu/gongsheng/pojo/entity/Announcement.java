package com.taiyidu.taiyidu.gongsheng.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Announcement {
    /**
     * 公告ID
     */
    private Long id;

    /**
     * 公告图片URL(阿里云OSS)
     */
    private String imageUrl;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 是否启用: 0-禁用, 1-启用
     */
    private Integer isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}