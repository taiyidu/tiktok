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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class videoScraperServiceImpl implements videoScraperService {

    @Autowired
    private LogDownloadMapper logDownloadMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OkHttpClient okHttpClient;


    @Value("${ILoveApi.apikey}")
    private String apikey;
    @Value("${ILoveApi.apiBaseUrl}")
    private String apiBaseUrl;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public GeneralResultVo videoScraper(HeadRequest headRequest) {

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
        // 2. 配置你的“我爱API”接口信息 (这里以常见的接口参数格式为例，请替换为你实际使用的域名和Key)
        String apiBaseUrl = this.apiBaseUrl; // 示例接口
        String apiKey = this.apikey; // 替换为你的平台 Token/Key

        // 3. 构建请求 URL（根据骁脱云文档，将参数拼接到 URL 后，或者用 POST 传参）
        // 这里以最常见的 GET 请求传参为例：
        String requestUrl = apiBaseUrl + "?apikey=" + apikey + "&url=" + java.net.URLEncoder.encode(targetUrl, java.nio.charset.StandardCharsets.UTF_8);
        Request request = new Request.Builder()
                .url(requestUrl)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build();
        GeneralResultVo generalResultVo = new GeneralResultVo();
        try{
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String jsonResult = response.body().string();
                log.info("apikey:  {}",apiKey);
                log.info("接口返回的原始数据:{}" + jsonResult);
                //获取作品类型判断
                DouyinParseResp douyinParseResp = JSON.parseObject(jsonResult, new TypeReference<>() {});
//                if(douyinParseResp.getData().isVideo()){
//                    generalResultVo.setDesc(douyinParseResp.getData().getTitle());
//                    generalResultVo.setAuthor(douyinParseResp.getData().getAuthor());
//                    generalResultVo.setType("VIDEO");
//                    generalResultVo.setDownloadUrl(douyinParseResp.getData().getUrl());
//                    generalResultVo.setImagesList(null);
//                }else{
//                    generalResultVo.setDesc(douyinParseResp.getData().getTitle());
//                    generalResultVo.setAuthor(douyinParseResp.getData().getAuthor());
//                    generalResultVo.setType("IMAGES");
//                    generalResultVo.setImagesList(new java.util.ArrayList<>());
//                    douyinParseResp.getData().getImageList().forEach(image -> {
//                        generalResultVo.getImagesList().add(image);
//                    });
//                }
//                LogDownload logDownload = LogDownload.builder()
//                        .awemeId(douyinParseResp.getData().getUid())
//                        .title(douyinParseResp.getData().getTitle())
//                        .mediaType(douyinParseResp.getData().isVideo() ? 1 : 2)
//                        .originUrl(targetUrl)
//                        .downloadUrls(douyinParseResp.getData().isVideo() ? douyinParseResp.getData().getUrl() : String.join(",", douyinParseResp.getData().getImageList()))
//                        .parseStatus(1)
//                        .errorMsg(null)
//                        .createTime(LocalDateTime.now())
//                        .userId(BaseContext.getCurrentId())
//                        .build();
//                logDownloadMapper.insert(logDownload);
                DouyinData douyinData = douyinParseResp.getData();
                if(douyinData.isVideo()){
                    //视频
                    generalResultVo.setDesc(douyinData.getTitle());
                    generalResultVo.setAuthor(douyinData.getAuthor());
                    generalResultVo.setType("VIDEO");
                    generalResultVo.setDownloadUrl(douyinData.getUrl());
                    generalResultVo.setImagesList(null);
                }else{
                    generalResultVo.setDesc(douyinData.getTitle());
                    generalResultVo.setAuthor(douyinData.getAuthor());
                    generalResultVo.setType("IMAGES");
                    generalResultVo.setImagesList(new java.util.ArrayList<>());
                    douyinData.getImageList().forEach(image -> {
                        generalResultVo.getImagesList().add(image);
                    });
                }
                LogDownload logDownload = LogDownload.builder()
                        .awemeId(String.valueOf(douyinData.getUid()))
                        .title(douyinData.getTitle())
                        .mediaType(douyinData.isVideo() ? 1 : 2)
                        .originUrl(targetUrl)
                        .downloadUrls(douyinData.isVideo() ? douyinData.getUrl() : String.join(",", douyinData.getImageList()))
                        .parseStatus(1)
                        .errorMsg(null)
                        .createTime(LocalDateTime.now())
                        .userId(BaseContext.getCurrentId())
                        .build();
                logDownloadMapper.insert(logDownload);
            } else {
                log.info("服务器响应失败，状态码: " + response.code());
            }
        } catch (IOException e) {
            log.error("网络请求发生异常: " + e.getMessage());
            e.printStackTrace();
            throw new BaseException("网络请求发生异常: " + e.getMessage());
        }
        return generalResultVo;
    }

    @Override
    public List<HistoryRecordVo> showHistory() {
        return logDownloadMapper.showHistory(BaseContext.getCurrentId());
    }
}
