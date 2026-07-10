package com.taiyidu.taiyidu.gongsheng.pojo.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogDownload {
    
    private Long id;                  // 自增主键
    private String awemeId;           // 作品唯一ID
    private String title;             // 作品文案描述
    private Integer mediaType;        // 作品类型: 1-图集, 2-视频
    private String originUrl;         // 用户输入的原始分享链接
    
    /**
     * 核心返回字段：如果是视频就是一条，如果是图集，建议在存入前用 String.join(",", urlList) 拼成逗号隔开的字符串
     */
    private String downloadUrls;      
    
    private Integer parseStatus;      // 解析状态: 1-成功, 0-失败
    private String errorMsg;          // 失败时的错误堆栈简要记录
    private LocalDateTime createTime; // 记录时间
}