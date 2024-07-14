package com.weiiboo.es.controller;

import com.weiiboo.common.Utils.ResultUtil;
import com.weiiboo.common.domin.Result;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.common.redis.utils.RedisKey;
import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.es.service.ElasticSearchInitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/search/init")
public class ElasticSearchInitController {

    @Value("${spring.elasticsearch.init.key}")
    private String key;

    @Resource
    private ElasticSearchInitService elasticSearchInitService;
    @Resource
    private RedisCache redisCache;

    /**
     * 初始化es
     * @param key 初始化凭证
     * @param type 初始化类型
     * @return 初始化结果
     */
    @GetMapping("/{key}/{type}")
    public Result<?> initElasticSearch(@PathVariable String key, @PathVariable String type) {
        if (!StringUtils.hasText(key) || !this.key.equals(key) || !StringUtils.hasText(type)) {
            throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
        }
        Boolean setnx = redisCache.setnx(RedisKey.build(RedisConstant.REDIS_KEY_ELASTICSEARCH_INIT, key + ":" + type));
        if (!setnx) {
            throw new BusinessException(ExceptionMsgEnum.ELASTICSEARCH_INIT_ALREADY);
        }
        elasticSearchInitService.initElasticSearch(type);
        return ResultUtil.successGet(null);
    }
}