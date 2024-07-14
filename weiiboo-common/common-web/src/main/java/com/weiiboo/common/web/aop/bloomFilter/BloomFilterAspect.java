package com.weiiboo.common.web.aop.bloomFilter;


import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.redis.utils.BloomFilterUtils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class BloomFilterAspect {
    @Resource
    private BloomFilterUtils bloomFilterUtils;

    // 切点
    @Pointcut("@annotation(bloomFilterProcessing)")
    public void bloomFilterPointcut(BloomFilterProcessing bloomFilterProcessing) {
    }

    // 环绕通知
    @Around(value = "bloomFilterPointcut(bloomFilterProcessing)", argNames = "point,bloomFilterProcessing")
    public Object around(ProceedingJoinPoint point,BloomFilterProcessing bloomFilterProcessing) throws Throwable{
        // 获取注解中的布隆过滤器名称
        String map = bloomFilterProcessing.map();
        // 获取参数值
        Object[] args = point.getArgs();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<?> targetClass = point.getTarget().getClass();
        Method method = targetClass.getDeclaredMethod(signature.getName(), signature.getMethod().getParameterTypes());
        ExpressionParser parser = new SpelExpressionParser();
        ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        Arrays.stream(bloomFilterProcessing.keys()).map(parser::parseExpression).forEach(expression -> {
            EvaluationContext context = new MethodBasedEvaluationContext(TypedValue.NULL, method, args, parameterNameDiscoverer);
            // 解析表达式获取参数值
            Object value = expression.getValue(context);
            if (value == null) {
                throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
            }
            Long id = Long.valueOf(value.toString());
            log.info("map:{},id:{}", map, id);
            // 布隆过滤器中不存在该值
            if (!bloomFilterUtils.mightContainBloomFilter(map, String.valueOf(id))) {
                log.error("布隆过滤器中不存在该请求值");
                throw new BusinessException(ExceptionMsgEnum.PARAMETER_ERROR);
            }
        });
        // 执行目标方法
        return point.proceed();
    }
}