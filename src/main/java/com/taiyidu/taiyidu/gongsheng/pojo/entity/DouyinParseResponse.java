package com.taiyidu.taiyidu.gongsheng.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinParseResponse {
    private Integer code;
    private String msg;
    private DouyinParseData data;
}
