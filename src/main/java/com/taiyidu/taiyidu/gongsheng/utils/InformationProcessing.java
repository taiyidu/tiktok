package com.taiyidu.taiyidu.gongsheng.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InformationProcessing{
    private String fileName;
    private String objectName;
    public static InformationProcessing getInformation(String desc ,String awemeId){
        // 清理文案中的非法特殊字符，防止作为文件名时系统报错
        String safeTitle = desc.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "").trim();
        if (safeTitle.isEmpty()) {
            safeTitle = awemeId;
        }
        // 限制文件名长度，防止过长报错
        if (safeTitle.length() > 50) {
            safeTitle = safeTitle.substring(0, 50);
        }

        String fileName = safeTitle + ".mp4";
        String objectName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/";
        return new InformationProcessing(fileName, objectName);
    }
}
