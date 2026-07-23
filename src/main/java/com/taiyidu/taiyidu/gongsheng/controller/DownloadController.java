package com.taiyidu.taiyidu.gongsheng.controller;

import jakarta.servlet.http.HttpServletResponse; // 如果是 Spring Boot 2.x 请改成 javax.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1/download")
@Slf4j
public class DownloadController {

    /**
     * 1. 视频下载接口 (返回值必须为 void!)
     */
    @GetMapping("/video")
    public void downloadVideo(@RequestParam("url") String encodedUrl, HttpServletResponse response) {
        log.info("视频下载接口");

        try {
            String targetUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.name());
            HttpURLConnection conn = createConnection(targetUrl);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                sendErrorJson(response);
                return;
            }

            // 设置响应头
            response.setContentType("video/mp4");
            response.setHeader("Content-Disposition", "attachment; filename=\"video.mp4\"");
            
            int contentLength = conn.getContentLength();
            if (contentLength > 0) {
                response.setContentLength(contentLength);
            }

            // 直接流式输出
            try (InputStream inputStream = conn.getInputStream();
                 OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            } finally {
                conn.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorJson(response);
        }
    }

    /**
     * 2. 图片批量下载接口 (解决 403 与 Committed 报错)
     */
    @GetMapping("/images")
    public void downloadImages(@RequestParam("urls") String encodedUrls, HttpServletResponse response) {
        log.info("图片批量下载接口");
        try {
            String[] urlArray = encodedUrls.split(",");
            if (urlArray.length == 0) {
                sendErrorJson(response);
                return;
            }

            List<String> validUrls = new ArrayList<>();
            for (String raw : urlArray) {
                String trimmed = raw.trim();
                if (!trimmed.isEmpty()) {
                    validUrls.add(decodeUrlIfNeeded(trimmed));
                }
            }

            if (validUrls.isEmpty()) {
                sendErrorJson(response);
                return;
            }

            // 先预检第一个图片是否能够正常连接（防止全都 403 导致中途返回 JSON 报错）
            HttpURLConnection firstConn = createConnection(validUrls.get(0));
            int firstCode = firstConn.getResponseCode();
            firstConn.disconnect();

            // 如果连第一张都拿不到，直接返回 500 JSON，此时 Response 尚未被提交（Committed）
            if (firstCode != HttpURLConnection.HTTP_OK) {
                System.err.println("预检图片失败，状态码: " + firstCode);
                sendErrorJson(response);
                return;
            }

            // 确认可以开始传输后，再设置 Response Header
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"images.zip\"");

            int successCount = 0;
            try (OutputStream outputStream = response.getOutputStream();
                 ZipOutputStream zos = new ZipOutputStream(outputStream)) {

                for (int i = 0; i < validUrls.size(); i++) {
                    String targetUrl = validUrls.get(i);
                    HttpURLConnection conn = null;
                    try {
                        conn = createConnection(targetUrl);
                        int responseCode = conn.getResponseCode();

                        // 重定向处理
                        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                            String redirectUrl = conn.getHeaderField("Location");
                            conn.disconnect();
                            conn = createConnection(redirectUrl);
                            responseCode = conn.getResponseCode();
                        }

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            String extension = getFileExtension(targetUrl);
                            ZipEntry entry = new ZipEntry("image_" + (i + 1) + extension);
                            zos.putNextEntry(entry);

                            try (InputStream is = conn.getInputStream()) {
                                byte[] buffer = new byte[8192];
                                int len;
                                while ((len = is.read(buffer)) > 0) {
                                    zos.write(buffer, 0, len);
                                }
                            }
                            zos.closeEntry();
                            successCount++;
                        } else {
                            System.err.println("图片下载失败，HTTP状态码: " + responseCode + "，URL: " + targetUrl);
                        }
                    } catch (Exception e) {
                        System.err.println("单图抓取异常，URL: " + targetUrl);
                        e.printStackTrace();
                    } finally {
                        if (conn != null) conn.disconnect();
                    }
                }

                // 写入 ZIP 中央目录，保证 ZIP 格式完整
                zos.finish();
                zos.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorJson(response);
        }
    }
    /**
     * 防止二次解码的防错处理
     */
    /**
     * 优化后的 URL 参数解析（解决签名中的 + 号变空格引发 403 的问题）
     */
    private String decodeUrlIfNeeded(String rawUrl) {
        if (rawUrl == null) return "";
        String url = rawUrl.trim();

        // 如果前端进行了 encodeURIComponent，通常 %3A 或 %2F 会被替换
        // 注意：切勿直接用 URLDecoder.decode()，否则 query 中的 '+' 会变成空格 ' '，破坏抖音签名
        if (url.contains("%3A") || url.contains("%2F") || url.contains("%3a") || url.contains("%2f")) {
            try {
                // 将 %2B 还原为 +，防止 decode 时丢失，或者直接替换常见 encode 字符
                url = url.replace("%2B", "%2B") // 保护 +
                        .replace("+", "%2B");  // 将空格或原加号规范化
                url = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
            } catch (Exception ignored) {}
        }
        return url;
    }

    /**
     * 创建网络连接（深度伪造 Header 突破抖音 403 限制）
     */
    private HttpURLConnection createConnection(String targetUrl) throws Exception {
        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000);

        // 1. 设置动态 Host Header
        conn.setRequestProperty("Host", url.getHost());

        // 2. 伪装 Chrome 120 请求头
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        conn.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");

        // 3. 补充 Sec-Fetch 防盗链安全头
        conn.setRequestProperty("Sec-Fetch-Dest", "image");
        conn.setRequestProperty("Sec-Fetch-Mode", "no-cors");
        conn.setRequestProperty("Sec-Fetch-Site", "cross-site");

        // 4. 抖音图片专用 Referer (尝试带 passthrough 路径)
        conn.setRequestProperty("Referer", "https://www.douyin.com/");

        return conn;
    }

    /**
     * 失败时向客户端写入 JSON
     */
    private void sendErrorJson(HttpServletResponse response) {
        try {
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 500, \"message\": \"下载失败\"}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 根据 URL 解析扩展名
     */
    private String getFileExtension(String urlString) {
        try {
            String path = new URL(urlString).getPath();
            int lastDot = path.lastIndexOf('.');
            if (lastDot != -1 && lastDot < path.length() - 1) {
                String ext = path.substring(lastDot).toLowerCase();
                if (ext.matches("^\\.(jpg|jpeg|png|gif|webp|bmp)$")) {
                    return ext;
                }
            }
        } catch (Exception ignored) {}
        return ".jpeg";
    }
}