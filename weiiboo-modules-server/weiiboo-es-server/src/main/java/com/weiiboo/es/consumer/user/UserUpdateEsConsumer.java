package com.weiiboo.es.consumer.user;

import com.alibaba.fastjson.JSON;
import com.weiiboo.common.constant.RocketMQConsumerGroupConstant;
import com.weiiboo.common.constant.RocketMQTopicConstant;
import com.weiiboo.es.service.UserSearchService;
import com.weiiboo.modules.api.user.domin.UserEsDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMQTopicConstant.USER_UPDATE_ES_TOPIC,
        consumerGroup = RocketMQConsumerGroupConstant.USER_UPDATE_ES_CONSUMER_GROUP)
public class UserUpdateEsConsumer implements RocketMQListener<String> {
    @Resource
    private UserSearchService userSearchService;

    @Override
    public void onMessage(String s) {
        log.info("{}收到消息:{}", RocketMQTopicConstant.USER_UPDATE_ES_TOPIC,s);
        UserEsDO userEsDO = JSON.parseObject(s, UserEsDO.class);
        log.info("转换后的对象：{}", userEsDO);
        userSearchService.updateUser(userEsDO);
    }
}