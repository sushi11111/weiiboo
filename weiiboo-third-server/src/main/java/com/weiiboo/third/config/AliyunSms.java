package com.weiiboo.third.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.weiiboo.common.web.exception.SystemException;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AliyunSms {
    @Value("${aliyun.sms.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.sms.accessKeySecret}")
    private String accessKeySecret;
    private static final String ENDPOINT = "dysmsapi.aliyuncs.com";

    @Bean
    public Client smsClient() {
        Config config = new Config().setAccessKeyId(accessKeyId).setAccessKeySecret(accessKeySecret);
        config.endpoint = ENDPOINT;
        try {
            return new Client(config);
        } catch (Exception e) {
            throw new SystemException(ExceptionMsgEnum.ALIYUN_SMS_INIT_ERROR, e);
        }
    }
}