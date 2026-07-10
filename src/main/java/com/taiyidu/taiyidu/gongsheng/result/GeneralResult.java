package com.taiyidu.taiyidu.gongsheng.result;
import lombok.Data;

import java.util.List;

@Data
public class GeneralResult {
    private String author;
    private String desc;
    private String type;
    private String downloadUrl;
    private List<String> imagesList;
}
