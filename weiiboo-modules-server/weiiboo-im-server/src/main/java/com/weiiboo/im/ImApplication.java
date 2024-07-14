package com.weiiboo.im;

import com.weiiboo.common.web.config.FeignConfig;
import com.weiiboo.im.server.IMServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import javax.annotation.Resource;

@SpringBootApplication(exclude = {FeignConfig.class})
@EnableFeignClients
public class ImApplication implements CommandLineRunner {
    @Resource
    private IMServer imServer;

    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        imServer.start();
    }
}