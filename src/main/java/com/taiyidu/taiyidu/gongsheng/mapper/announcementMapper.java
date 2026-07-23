package com.taiyidu.taiyidu.gongsheng.mapper;

import com.taiyidu.taiyidu.gongsheng.pojo.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface announcementMapper {
    Announcement getAnnouncement();
}
