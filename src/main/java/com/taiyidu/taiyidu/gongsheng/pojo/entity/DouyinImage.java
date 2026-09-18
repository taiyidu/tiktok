package com.taiyidu.taiyidu.gongsheng.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinImage {
    private String url;
    private String live_photo_url;
}
