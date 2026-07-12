package com.taiyidu.taiyidu.gongsheng.mapper;

import com.taiyidu.taiyidu.gongsheng.pojo.entity.LogDownload;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface videoScraperMapper {
    void insert(LogDownload logDownload);
}
