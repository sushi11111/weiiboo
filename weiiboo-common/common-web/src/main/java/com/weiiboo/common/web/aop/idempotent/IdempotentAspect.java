package com.weiiboo.common.web.aop.idempotent;

import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.common.redis.utils.RedisKey;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class IdempotentAspect {
    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private RedisCache redisCache;

    // 切点，使用了@Idempotent注解的方法
    @Pointcut("@annotation(idempotent)")
    public void idempotentPointcut(Idempotent idempotent) {
    }

    @Around(value = "idempotentPointcut(idempotent)",argNames = "point,idempotent")
    public Object around(ProceedingJoinPoint point,Idempotent idempotent) throws Throwable{
        // 获取token
        // String token = httpServletRequest.getHeader("token");

        String redisKey = getRedisKey(point,idempotent,"token");

        // 获取过期时间
        long exprie = idempotent.expireTime();
        // 使用redis的setnx实现幂等
        boolean result = redisCache.setnxAndExpire(redisKey,exprie, TimeUnit.MICROSECONDS);
        if(!result){
            log.error("重复操作");
            throw new BusinessException(ExceptionMsgEnum.REPEAT_OPERATION);
        }else {
            // 是第一次请求
            // 执行目标方法
            return point.proceed();
        }
    }

    private String getRedisKey(ProceedingJoinPoint point, Idempotent idempotent, String token) {
        if(token == null){
            throw new BusinessException(ExceptionMsgEnum.NOT_LOGIN);
        }
        // 获取注解中的唯一标识
        String value = idempotent.value();
        // 获取方法名
        MethodSignature signature = (MethodSignature) point.getSignature();
        String methodName = signature.getMethod().getName();
        // 拼接唯一后缀标识，token + 路径 + 方法名 + 参数
        String suffix = token +"_"+ value+"_"+ methodName+ "_" + Arrays.toString(point.getArgs());
        return RedisKey.build(RedisConstant.REDIS_KEY_IDEMPOTENT, suffix);
    }
}
