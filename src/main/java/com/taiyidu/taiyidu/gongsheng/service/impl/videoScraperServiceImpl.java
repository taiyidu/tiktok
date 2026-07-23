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
//                log.info("接口返回的原始数据:{}" + jsonResult);
                //获取作品类型判断
                DouyinParseResp douyinParseResp = JSON.parseObject(jsonResult, new TypeReference<>() {});
                if(douyinParseResp.getData().isVideo()){
                    generalResultVo.setDesc(douyinParseResp.getData().getTitle());
                    generalResultVo.setAuthor(douyinParseResp.getData().getAuthor());
                    generalResultVo.setType("VIDEO");
                    generalResultVo.setDownloadUrl(douyinParseResp.getData().getUrl());
                    generalResultVo.setImagesList(null);
                }else{
                    generalResultVo.setDesc(douyinParseResp.getData().getTitle());
                    generalResultVo.setAuthor(douyinParseResp.getData().getAuthor());
                    generalResultVo.setType("IMAGES");
                    generalResultVo.setImagesList(new java.util.ArrayList<>());
                    douyinParseResp.getData().getImageList().forEach(image -> {
                        generalResultVo.getImagesList().add(image);
                    });
                }
                LogDownload logDownload = LogDownload.builder()
                        .awemeId(douyinParseResp.getData().getUid())
                        .title(douyinParseResp.getData().getTitle())
                        .mediaType(douyinParseResp.getData().isVideo() ? 1 : 2)
                        .originUrl(targetUrl)
                        .downloadUrls(douyinParseResp.getData().isVideo() ? douyinParseResp.getData().getUrl() : String.join(",", douyinParseResp.getData().getImageList()))
                        .parseStatus(1)
                        .errorMsg(null)
                        .createTime(LocalDateTime.now())
                        .userId(BaseContext.getCurrentId())
                        .build();
                logDownloadMapper.insert(logDownload);
            } else {
                System.out.println("服务器响应失败，状态码: " + response.code());
            }
        } catch (IOException e) {
            log.error("网络请求发生异常: " + e.getMessage());
            new BaseException("网络请求发生异常: " + e.getMessage());
            e.printStackTrace();
        }
//        try {
//            // 对参数进行 URL 编码防乱码
//            String encodedTargetUrl = URLEncoder.encode(targetUrl, StandardCharsets.UTF_8);
//            String fullApiUrl = String.format("%s?key=%s&url=%s", apiBaseUrl, apiKey, encodedTargetUrl);
//
//            // 3. 使用 Java 11 的 HttpClient 发送请求
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(fullApiUrl))
//                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
//                    .GET()
//                    .build();
//
//            System.out.println("正在请求解析接口...");
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                String jsonResult = response.body();
//                log.info("接口返回的 JSON 数据: {}", jsonResult);
//                String workType = JSON.parseObject(jsonResult).getJSONObject("data").getString("work_type");
//                log.info("作品类型：" + workType);
//
//                if(workType.equals("video")){
//                    ApiResponse<VideoWorkData> apiResponse = JSON.parseObject(jsonResult, new TypeReference<>() {});
//                    generalResultVo.setAuthor(apiResponse.getData().getWorkAuthor());
//                    generalResultVo.setDesc(apiResponse.getData().getWorkTitle());
//                    generalResultVo.setType("VIDEO");
//                    generalResultVo.setDownloadUrl(apiResponse.getData().getWorkUrl());
//                    logDownloadMapper.insert(LogDownload.builder()
//                            .awemeId(apiResponse.getData().getWorkUid())
//                            .title(apiResponse.getData().getWorkTitle())
//                            .mediaType(2)
//                            .originUrl(targetUrl)
//                            .downloadUrls(apiResponse.getData().getWorkUrl())
//                            .parseStatus(1)
//                            .errorMsg(apiResponse.getMsg())
//                            .createTime(LocalDateTime.now())
//                            .build());
//                } else {
//                    ApiResponse<ImageWorkData> apiResponse = JSON.parseObject(jsonResult, new TypeReference<>() {});
//                    generalResultVo.setAuthor(apiResponse.getData().getWorkAuthor());
//                    generalResultVo.setDesc(apiResponse.getData().getWorkTitle());
//                    generalResultVo.setType("IMAGE");
//                    generalResultVo.setImagesList(new java.util.ArrayList<>());
//                    apiResponse.getData().getWorkUrl().forEach(workUrl -> {
//                        generalResultVo.getImagesList().add(workUrl.getUrl());
//                    });
//
//                    StringBuilder sb = new StringBuilder();
//                    apiResponse.getData().getWorkUrl().forEach(workUrl -> {
//                        sb.append(workUrl.getUrl()).append(",");
//                    });
//                    logDownloadMapper.insert(LogDownload.builder()
//                            .awemeId(apiResponse.getData().getWorkUid())
//                            .title(apiResponse.getData().getWorkTitle())
//                            .mediaType(2)
//                            .originUrl(targetUrl)
//                            .downloadUrls(sb.toString())
//                            .parseStatus(1)
//                            .errorMsg(apiResponse.getMsg())
//                            .createTime(LocalDateTime.now())
//                            .build());
//                }
//
//            } else {
//                log.info("请求失败，HTTP 状态码: " + response.statusCode());
//            }
//        } catch (IOException | InterruptedException e) {
//            log.error("发送请求时发生异常: " + e.getMessage());
//            Thread.currentThread().interrupt();
//        }
        return generalResultVo;
    }

    @Override
    public List<HistoryRecordVo> showHistory() {
        return logDownloadMapper.showHistory(BaseContext.getCurrentId());
    }
}
