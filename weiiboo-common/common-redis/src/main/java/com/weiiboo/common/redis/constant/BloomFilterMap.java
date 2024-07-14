package com.weiiboo.common.redis.constant;

public class BloomFilterMap {
    /**
     * 笔记id布隆过滤器
     */
    public static final String NOTES_ID_BLOOM_FILTER = "notesIdBloomFilter";
    /**
     * 用户id布隆过滤器
     */
    public static final String USER_ID_BLOOM_FILTER = "userIdBloomFilter";
    /**
     * rocketmq幂等布隆过滤器
     */
    public static final String ROCKETMQ_IDEMPOTENT_BLOOM_FILTER = "rocketmqIdempotentBloomFilter";
}