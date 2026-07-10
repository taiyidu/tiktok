package com.taiyidu.taiyidu.gongsheng.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class PlaywrightConfig {
    // 是否无头模式的开关
    @Value("${playwright.browser.headless}")
    private boolean openSwitch;

    // 1. 全局唯一个 Playwright 驱动实例 Bean
    @Bean(destroyMethod = "close") // 项目关闭时自动释放资源
    public Playwright playwright() {
        log.info("[Spring 容器初始化] 创建全局唯一的 Playwright 驱动实例...");
        return Playwright.create();
    }

    // 2. 全局唯一个 浏览器进程 Bean（由上面单例的 playwright 启动）
    @Bean(destroyMethod = "close")
    public Browser browser(Playwright playwright) {
        log.info("[Spring 容器初始化] 正在创建全局唯一的 Chromium 浏览器进程...");
        return playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(openSwitch) // 服务器运行必须无头
        );
    }
}
