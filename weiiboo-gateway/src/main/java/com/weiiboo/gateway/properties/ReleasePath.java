package com.weiiboo.gateway.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "release.auth")
public class ReleasePath {
    private List<String> path;
}