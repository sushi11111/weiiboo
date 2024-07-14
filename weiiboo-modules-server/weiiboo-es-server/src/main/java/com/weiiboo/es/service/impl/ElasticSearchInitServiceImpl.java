package com.weiiboo.es.service.impl;

import com.weiiboo.common.constant.BaseConstant;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.web.exception.SystemException;
import com.weiiboo.es.service.ElasticSearchInitService;
import com.weiiboo.modules.api.notes.domin.NotesEsDO;
import com.weiiboo.modules.api.user.domin.UserEsDO;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ElasticSearchInitServiceImpl implements ElasticSearchInitService {
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void initElasticSearch(String type) {
        if(BaseConstant.ES_INIT_USER_CODE.equals(type)){
            try {
                if (!elasticsearchRestTemplate.indexOps(UserEsDO.class).exists()) {
                    // 创建索引
                    elasticsearchRestTemplate.indexOps(UserEsDO.class).create();
                    // 创建映射
                    elasticsearchRestTemplate.indexOps(UserEsDO.class).putMapping();
                }
            } catch (Exception e) {
                throw new SystemException(ExceptionMsgEnum.ELASTICSEARCH_INIT_ERROR, e);
            }
        }else{
            // 判断索引是否存在
            try {
                if (!elasticsearchRestTemplate.indexOps(NotesEsDO.class).exists()) {
                    // 创建索引
                    elasticsearchRestTemplate.indexOps(NotesEsDO.class).create();
                    // 创建映射
                    elasticsearchRestTemplate.indexOps(NotesEsDO.class).putMapping();
                }
            } catch (Exception e) {
                throw new SystemException(ExceptionMsgEnum.ELASTICSEARCH_INIT_ERROR, e);
            }
        }
    }
}
