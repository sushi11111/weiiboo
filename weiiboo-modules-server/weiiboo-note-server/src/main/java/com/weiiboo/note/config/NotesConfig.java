package com.weiiboo.note.config;


import com.weiiboo.common.redis.constant.BloomFilterMap;
import com.weiiboo.common.redis.utils.BloomFilterUtils;
import com.weiiboo.note.mapper.NotesMapper;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class NotesConfig {
    @Resource
    private BloomFilterUtils bloomFilterUtils;
    @Resource
    private NotesMapper notesMapper;

    // 初始化布隆过滤器
    @PostConstruct
    public void initBloomFilter() {
        List<String> list = notesMapper.selectList(null).stream().map(notesDO -> String.valueOf(notesDO.getId())).collect(Collectors.toList());
        // 先判断有没有该布隆过滤器，没有则初始化
        long bloomFilterSize = bloomFilterUtils.getBloomFilterSize(BloomFilterMap.NOTES_ID_BLOOM_FILTER);
        if(bloomFilterSize==list.size()){
            return;
        }
        bloomFilterUtils.initBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER, list.isEmpty() ? 10000 : list.size() * 4L, 0.01);
        bloomFilterUtils.addAllBloomFilter(BloomFilterMap.NOTES_ID_BLOOM_FILTER, list);
    }
}
