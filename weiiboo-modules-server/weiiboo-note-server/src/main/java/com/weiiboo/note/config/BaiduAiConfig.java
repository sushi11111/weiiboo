package com.weiiboo.note.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class BaiduAiConfig {
    @Value("${baiduyun.ai.apiKey}")
    private String apiKey;

    @Value("${baiduyun.ai.secretKey}")
    private String secretKey;
}
