package com.taiyidu.taiyidu.gongsheng.controller;

import com.taiyidu.taiyidu.gongsheng.pojo.vo.AnnouncementVO;
import com.taiyidu.taiyidu.gongsheng.result.Result;
import com.taiyidu.taiyidu.gongsheng.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/announcement")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;
    @RequestMapping
    public Result announcement(){
        log.info("公告接口");
        AnnouncementVO announcementVO = announcementService.getAnnouncement();
        return Result.success(announcementVO);
    }
}
