package com.taiyidu.taiyidu.gongsheng.controller;

import com.taiyidu.taiyidu.gongsheng.result.HeadRequest;
import com.taiyidu.taiyidu.gongsheng.result.Result;
import com.taiyidu.taiyidu.gongsheng.result.GeneralResult;
import com.taiyidu.taiyidu.gongsheng.service.videoScraperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scraper/douyin/tasks")
@Slf4j
public class SingleVideoScraper{
    @Autowired
    private videoScraperService videoScraperService;
    /**
     * 视频响应接口
     */
    @PostMapping
    public Result videoScraper(@RequestBody HeadRequest headRequest){
        log.info("[视频解析接口] 正在解析视频... 链接:{}",headRequest.getUrl());
        GeneralResult generalResult = videoScraperService.videoScraper(headRequest);
        return Result.success(generalResult);
    }
}
