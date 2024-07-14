package com.weiiboo.im.config;

import com.weiiboo.common.redis.constant.BloomFilterMap;
import com.weiiboo.common.redis.utils.BloomFilterUtils;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ImConfig {
    private final BloomFilterUtils bloomFilterUtils;

    public ImConfig(BloomFilterUtils bloomFilterUtils) {
        this.bloomFilterUtils = bloomFilterUtils;
    }

    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    public void initBloomFilter() {
        // 先判断有没有该布隆过滤器，没有则初始化
        long expectedInsertionsBloomFilter = bloomFilterUtils.getExpectedInsertionsBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER);
        if (expectedInsertionsBloomFilter > 0) {
            return;
        }
        bloomFilterUtils.initBloomFilter(BloomFilterMap.ROCKETMQ_IDEMPOTENT_BLOOM_FILTER, 100000, 0.01);
    }
}