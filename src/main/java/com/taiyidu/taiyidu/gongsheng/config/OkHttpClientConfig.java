package com.taiyidu.taiyidu.gongsheng.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.http.HttpClient;

@Configuration
public class OkHttpClientConfig {
    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient();
    }
}
