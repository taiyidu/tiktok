package com.taiyidu.taiyidu.gongsheng.service.impl;

import com.taiyidu.taiyidu.gongsheng.mapper.announcementMapper;
import com.taiyidu.taiyidu.gongsheng.pojo.entity.Announcement;
import com.taiyidu.taiyidu.gongsheng.pojo.vo.AnnouncementVO;
import com.taiyidu.taiyidu.gongsheng.service.AnnouncementService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {
    @Autowired
    private announcementMapper announcementMapper;
    @Override
    public AnnouncementVO getAnnouncement() {
        Announcement announcement = announcementMapper.getAnnouncement();
        AnnouncementVO announcementVO = new AnnouncementVO();
        BeanUtils.copyProperties(announcement, announcementVO);
        return announcementVO;
    }
}
