package com.weiiboo.common.web.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig implements RequestInterceptor {
    @Resource
    private HttpServletRequest request;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = request.getHeader("token");
        requestTemplate.header("token",token);
    }
}