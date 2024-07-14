package com.weiiboo.gateway.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author hhh
 * @date 2024-3-17
 * @desc 不需要token的路径
 */
@Data
@Component
@ConfigurationProperties(prefix = "release.auth")
public class ReleasePath {
    private List<String> path;
}