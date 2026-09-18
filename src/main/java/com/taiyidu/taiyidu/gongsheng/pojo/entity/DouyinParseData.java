package com.taiyidu.taiyidu.gongsheng.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinParseData {
    private String video_url;
    private String cover_url;
    private String title;
    private String music_url;
    private List<DouyinImage> images;
    private DouyinAuthor author;
    // 判断是否为视频类型
    public boolean isVideo() {
        return video_url != null && !video_url.isBlank();
    }

    // 判断是否为图集类型
    public boolean isImageSet() {
        return images != null && !images.isEmpty();
    }
}
