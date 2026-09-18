package com.taiyidu.taiyidu.gongsheng.controller;

import com.taiyidu.taiyidu.gongsheng.result.HeadRequest;
import com.taiyidu.taiyidu.gongsheng.result.Result;
import com.taiyidu.taiyidu.gongsheng.pojo.vo.GeneralResultVo;
import com.taiyidu.taiyidu.gongsheng.service.videoScraperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scraper/douyin")
@Slf4j
public class DouyinController {
    @Autowired
    private videoScraperService videoScraperService;
    /**
     * 视频响应接口
     */
    @PostMapping("/tasks")
    public Result videoScraper(@RequestBody HeadRequest headRequest){
        log.info("[视频解析接口] 正在解析视频... 链接:{}",headRequest.getUrl());
        GeneralResultVo generalResult = videoScraperService.videoScraper(headRequest);
        return Result.success(generalResult);
    }
    @GetMapping("/history")
    public Result history(){
        log.info("[历史记录接口] 正在获取历史记录...");
        return Result.success(videoScraperService.showHistory());
    }
}
