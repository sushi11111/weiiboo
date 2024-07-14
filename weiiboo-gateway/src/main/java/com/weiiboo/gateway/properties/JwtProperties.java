package com.weiiboo.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    // 密钥
    private String secret;
    // token过期时间
    private long expireTime;
    // 剩余多久时间刷新token
    private long refreshTime;
}