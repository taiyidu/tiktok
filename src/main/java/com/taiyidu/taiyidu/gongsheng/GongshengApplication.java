package com.taiyidu.taiyidu.gongsheng;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class GongshengApplication {
    public static void main(String[] args) {
        SpringApplication.run(GongshengApplication.class, args);
        log.info("loading resources......");
    }
}
