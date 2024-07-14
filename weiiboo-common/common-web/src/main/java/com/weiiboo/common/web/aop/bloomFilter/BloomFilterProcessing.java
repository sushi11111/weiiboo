package com.weiiboo.common.web.aop.bloomFilter;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BloomFilterProcessing {
    // 指定使用的布隆过滤器名称
    String map() default "";
    // 需要检查的参数名称
    String[] keys() default {};
}
