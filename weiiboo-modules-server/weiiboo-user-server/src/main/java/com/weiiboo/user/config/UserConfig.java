package com.weiiboo.user.config;


import com.weiiboo.common.redis.constant.BloomFilterMap;
import com.weiiboo.common.redis.utils.BloomFilterUtils;
import com.weiiboo.user.mapper.UsersMapper;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class UserConfig {
    @Resource
    private UsersMapper usersMapper;
    @Resource
    private BloomFilterUtils bloomFilterUtils;

    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    public void initBloomFilter() {
        // 获取数据库所有用户的id
        List<String> list = usersMapper.selectList(null).stream().map(UserDO -> String.valueOf(UserDO.getId())).collect(Collectors.toList());
        // 先判断有没有该布隆过滤器，没有则初始化
        long bloomFilterSize = bloomFilterUtils.getBloomFilterSize(BloomFilterMap.USER_ID_BLOOM_FILTER);
        if(bloomFilterSize==list.size()){
            return;
        }
        bloomFilterUtils.initBloomFilter(BloomFilterMap.USER_ID_BLOOM_FILTER, list.isEmpty() ? 10000 : list.size() * 4L, 0.01);
        bloomFilterUtils.addAllBloomFilter(BloomFilterMap.USER_ID_BLOOM_FILTER, list);
    }
}