package com.weiiboo.common.redis.utils;

/** 构建 redis key
 * @author hhh
 * @date 2024-3-17
 */
public class RedisKey {
    public static String build(String prefix,String key){
        return prefix+key;
    }
}
