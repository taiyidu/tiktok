package com.taiyidu.taiyidu.gongsheng.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinAuthor {
    private String uid;
    private String name;
    private String avatar;
}
