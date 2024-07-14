package com.weiiboo.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {

        if (metaObject.hasGetter("createTime")) {
            metaObject.setValue("createTime", new Date());
        }
        if (metaObject.hasGetter("updateTime")) {
            metaObject.setValue("updateTime", new Date());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if(metaObject.hasGetter("updateTime")) {
            metaObject.setValue("updateTime", new Date());
        }
    }
}
