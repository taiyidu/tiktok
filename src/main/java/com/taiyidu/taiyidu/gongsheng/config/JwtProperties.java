package com.taiyidu.taiyidu.gongsheng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gongsheng.jwt")
@Data
public class JwtProperties {
    private String adminSecretKey;
    private Long adminTtl;
    private String adminTokenName;
}
