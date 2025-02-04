package com.weiiboo.common.web.aop.idempotent;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    /**
     * 唯一标识
     * @return String
     */
    String value() default "";
    /**
     * 过期时间
     * @return long 单位毫秒
     */
    long expireTime() default 1000;
}
