package com.taiyidu.taiyidu.gongsheng.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnnouncementVO {
    private Long id;
    private String imageUrl;
    private String content;
    private LocalDateTime createTime;
}