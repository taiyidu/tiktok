package com.taiyidu.taiyidu.gongsheng.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.microsoft.playwright.*;
import com.taiyidu.taiyidu.gongsheng.config.PlaywrightConfig;
import com.taiyidu.taiyidu.gongsheng.context.BaseContext;
import com.taiyidu.taiyidu.gongsheng.exception.BaseException;
import com.taiyidu.taiyidu.gongsheng.mapper.LogDownloadMapper;
import com.taiyidu.taiyidu.gongsheng.mapper.UserMapper;
import com.taiyidu.taiyidu.gongsheng.pojo.entity.LogDownload;
import com.taiyidu.taiyidu.gongsheng.pojo.entity.User;
import com.taiyidu.taiyidu.gongsheng.pojo.vo.HistoryRecordVo;
import com.taiyidu.taiyidu.gongsheng.result.HeadRequest;
import com.taiyidu.taiyidu.gongsheng.pojo.vo.GeneralResultVo;
import com.taiyidu.taiyidu.gongsheng.service.videoScraperService;
import com.taiyidu.taiyidu.gongsheng.utils.InformationProcessing;
import com.taiyidu.taiyidu.gongsheng.utils.SafeFileNameUtils;
import constant.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
@Service
public class videoScraperServiceImpl implements videoScraperService {
    //浏览器的驱动基底程序以及启动程序
    @Autowired
    private PlaywrightConfig playwrightConfig;
    //集成阿里云服务的程序基底
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private LogDownloadMapper logDownloadMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public GeneralResultVo videoScraper(HeadRequest headRequest) {

        User user = userMapper.getById(BaseContext.getCurrentId());
        if(user.getRemain() <= 0){
            throw new BaseException("解析次数不够喽");
        }
        userMapper.updateUser(User.builder().id(user.getId()).remain(user.getRemain() - 1).build());


        // 1. 提取抖音分享短链接 对文件名进行处理
        String shareUrl = SafeFileNameUtils.extractDouyinUrl(headRequest.getUrl());
        if (shareUrl.isEmpty()) {
            log.warn("⚠️ 未从输入中提取到抖音分享链接: {}", headRequest.getUrl());
            return new GeneralResultVo();
        }
        log.info("🕵️‍♂️ 启动抖音资源抓取模式...");
        GeneralResultVo generalResult = new GeneralResultVo();
        BrowserContext context = null;
        Page page = null;
        // 确保只有一个回调处理数据，避免重复处理
        AtomicBoolean processed = new AtomicBoolean(false);
        try{
            Playwright playwright = playwrightConfig.playwright();
            Browser browser = playwrightConfig.browser(playwright);

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            context = browser.newContext(contextOptions);
            page = context.newPage();

            // 2. 核心：注册网络响应拦截器
            page.onResponse(response -> {
                // 已经有回调处理过了，直接跳过
                if (processed.get()) return;

                String url = response.url();
                if (!url.contains("/aweme/v1/web/aweme/detail/")) return;

                System.out.println("\n🎯 成功拦截到该单视频的核心数据接口！");

                // 立即标记为已处理，防止第二个匹配响应也进入下载上传
                processed.set(true);

                try {
                    String jsonText = response.text();
                    JSONObject root = JSON.parseObject(jsonText);
                    JSONObject videoDetail = root.getJSONObject("aweme_detail");

                    if (videoDetail != null) {
                        String desc = videoDetail.getString("desc");
                        String awemeId = videoDetail.getString("aweme_id");
                        String authorName = videoDetail.getJSONObject("author").getString("nickname");

                        JSONObject stats = videoDetail.getJSONObject("statistics");
                        long diggCount = stats.getLongValue("digg_count");
                        long commentCount = stats.getLongValue("comment_count");

                        JSONArray videoUrlList = videoDetail.getJSONObject("video")
                                .getJSONObject("play_addr")
                                .getJSONArray("url_list");
                        String downloadUrl = (videoUrlList != null && !videoUrlList.isEmpty()) ? videoUrlList.getString(0) : "未找到";

                        log.info("==================== 抓取结果 ====================");
                        log.info("【视频作者】: " + authorName);
                        log.info("【视频文案】: " + desc);
                        log.info("【互动数据】: 👍 点赞 " + diggCount + " | 💬 评论 " + commentCount);
                        log.info("【视频直链】: " + downloadUrl);
                        log.info("==================================================");

                        if (!"未找到".equals(downloadUrl)) {
                            log.info("📥 尝试流式下载上传视频...");

                            HttpClient httpClient = HttpClient.newBuilder()
                                    .followRedirects(HttpClient.Redirect.NORMAL)
                                    .build();

                            HttpRequest videoRequest = HttpRequest.newBuilder()
                                    .uri(URI.create(downloadUrl))
                                    .header("Referer", "https://www.douyin.com/")
                                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                    .GET()
                                    .build();

                            HttpResponse<InputStream> videoResponse = httpClient.send(videoRequest, HttpResponse.BodyHandlers.ofInputStream());

                            if (videoResponse.statusCode() == 200) {
                                InputStream videoStream = videoResponse.body();

                                InformationProcessing information = InformationProcessing.getInformation(desc, awemeId);
                                String fileName = information.getFileName();
                                String objectName = information.getObjectName();

                                FileInfo fileInfo = fileStorageService
                                        .of(videoStream)
                                        .setOriginalFilename(fileName)
                                        .setPath(objectName)
                                        .upload();

                                log.info("🎉 [上传成功] OSS 地址: " + fileInfo.getUrl());
                                generalResult.setAuthor(authorName);
                                generalResult.setDesc(desc);
                                generalResult.setType("VIDEO");
                                generalResult.setDownloadUrl(fileInfo.getUrl());
                                logDownloadMapper.insert(LogDownload.builder()
                                        .awemeId(awemeId)
                                        .title(desc)
                                        .mediaType(2)
                                        .originUrl(shareUrl)
                                        .downloadUrls(fileInfo.getUrl())
                                        .parseStatus(1)
                                        .errorMsg(null)
                                        .createTime(LocalDateTime.now())
                                        .build());
                            } else {
                                log.info("❌ 下载失败，抖音 CDN 节点返回状态码: " + videoResponse.statusCode());
                            }
                        }
                    } else {
                        log.info("⚠️ 接口未返回视频详情，可能被风控或需要滑块验证。");
                    }

                } catch (Exception e) {
                    // 页面已关闭导致的错误是预期行为，不打印
                    String errMsg = e.getMessage();
                    if (errMsg != null && errMsg.contains("Target page, context or browser has been closed")) {
                        return;
                    }
                    log.error("❌ 解析或下载失败: ", e);
                }
            });

            // 3. 导航到分享链接
            log.info("🌐 正在解析分享链接...");
            page.navigate(shareUrl);

            // 停顿 5 秒确保数据完全加载以及接口被成功拦截
            page.waitForTimeout(5000);
            log.info("\n👋 任务结束，正在关闭浏览器。");
        } catch (Exception e) {
            log.error("❌ videoScraper 执行异常: ", e);
        } finally {
            if (page != null) {
                try { page.close(); } catch (Exception e) { log.warn("Page 关闭异常: {}", e.getMessage()); }
            }
            if (context != null) {
                try { context.close(); } catch (Exception e) { log.warn("BrowserContext 关闭异常: {}", e.getMessage()); }
            }
        }
        return generalResult;
    }
    @Override
    public List<HistoryRecordVo> showHistory() {
        return logDownloadMapper.showHistory();
    }
}
