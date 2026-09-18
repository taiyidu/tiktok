package com.taiyidu.taiyidu.gongsheng.service.impl;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.taiyidu.taiyidu.gongsheng.context.BaseContext;
import com.taiyidu.taiyidu.gongsheng.exception.BaseException;
import com.taiyidu.taiyidu.gongsheng.mapper.LogDownloadMapper;
import com.taiyidu.taiyidu.gongsheng.mapper.UserMapper;
import com.taiyidu.taiyidu.gongsheng.pojo.entity.*;
import com.taiyidu.taiyidu.gongsheng.pojo.vo.HistoryRecordVo;
import com.taiyidu.taiyidu.gongsheng.result.HeadRequest;
import com.taiyidu.taiyidu.gongsheng.pojo.vo.GeneralResultVo;
import com.taiyidu.taiyidu.gongsheng.service.videoScraperService;
import com.taiyidu.taiyidu.gongsheng.utils.SafeFileNameUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class videoScraperServiceImpl implements videoScraperService {

    @Autowired
    private LogDownloadMapper logDownloadMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${ILoveApi.appid}")
    private String appId;
    @Value("${ILoveApi.apikey}")
    private String apikey;
    @Value("${ILoveApi.apiBaseUrl}")
    private String apiBaseUrl;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GeneralResultVo videoScraper(HeadRequest headRequest) {
        //现在这里的次数的检测是只要点击就减少次数？ 但是我好像配置了transaction好像只要检测error就会回滚，所以说这里的问题并不大
        User user = userMapper.getById(BaseContext.getCurrentId());
        if(user.getRemain() <= 0){
            throw new BaseException("解析次数不够喽");
        }
        userMapper.updateUser(User.builder().id(user.getId()).remain(user.getRemain() - 1).build());


        // 模拟用户从抖音复制的分享文案
        String shareText = headRequest.getUrl();
        // 1. 提取出干净的 URL
        String targetUrl = SafeFileNameUtils.extractDouyinUrl(shareText);
        if (targetUrl == null) {
            System.out.println("未在文案中找到有效的链接！");
            throw new BaseException("未在文案中找到有效的链接！");
        }
        log.info("提取的抖音短链接为：{}", targetUrl);

        String appId = this.appId;
        String apiBaseUrl = this.apiBaseUrl; // 示例接口
        String apiKey = this.apikey; // 替换为你的平台 Token/Key

        // 构造json参数map
        Map<String, Object> reqBodyMap = new HashMap<>();
        reqBodyMap.put("appId", this.appId);
        reqBodyMap.put("appKey", this.apikey);
        reqBodyMap.put("url", targetUrl);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(reqBodyMap);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, jsonBody);

        Request request = new Request.Builder()
                .url(apiBaseUrl)
                .post(body)
                .build();
        GeneralResultVo generalResultVo = new GeneralResultVo();
        try{
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String jsonResult = response.body().string();
                log.info("接口返回的原始数据:{}" ,jsonResult);
//                获取作品类型判断
                DouyinParseResponse douyinParseResponse = JSON.parseObject(jsonResult, new TypeReference<>() {});
                DouyinParseData douyinParseData = douyinParseResponse.getData();
                if(douyinParseData.isVideo()){
                    //视频
                    generalResultVo.setType("VIDEO");
                    generalResultVo.setDownloadUrl(douyinParseData.getVideo_url());
                    generalResultVo.setImagesList(null);
                }else{
                    generalResultVo.setType("IMAGES");
                    generalResultVo.setImagesList(new java.util.ArrayList<>());
                    douyinParseData.getImages().forEach(image -> {
                        generalResultVo.getImagesList().add(image.getUrl());
                    });
                }
                generalResultVo.setDesc(douyinParseData.getTitle());
                generalResultVo.setAuthor(douyinParseData.getAuthor().getName());


                LogDownload logDownload = LogDownload.builder()
                        .awemeId(String.valueOf(douyinParseData.getAuthor().getUid()))
                        .title(douyinParseData.getTitle())
                        .mediaType(douyinParseData.isVideo() ? 1 : 2)
                        .originUrl(targetUrl)
                        .downloadUrls(douyinParseData.isVideo() ? douyinParseData.getVideo_url() : douyinParseData.getImages().stream().map(DouyinImage::getUrl).collect(Collectors.joining(",")))
                        .parseStatus(1)
                        .errorMsg(null)
                        .createTime(LocalDateTime.now())
                        .userId(BaseContext.getCurrentId())
                        .build();
                logDownloadMapper.insert(logDownload);
            } else {
                log.error("服务器响应失败，状态码: {}", response.code());
            }
        } catch (IOException e) {
            log.error("网络请求发生异常: {}", e.getMessage());
//            e.printStackTrace();
            throw new BaseException("网络请求发生异常: " + e.getMessage());
        }
        return generalResultVo;
    }

    @Override
    public List<HistoryRecordVo> showHistory() {
        return logDownloadMapper.showHistory(BaseContext.getCurrentId());
    }
}
