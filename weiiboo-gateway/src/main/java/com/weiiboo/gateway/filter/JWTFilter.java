package com.weiiboo.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.redis.constant.RedisConstant;
import com.weiiboo.common.redis.utils.RedisCache;
import com.weiiboo.common.redis.utils.RedisKey;
import com.weiiboo.gateway.properties.JwtProperties;
import com.weiiboo.gateway.properties.ReleasePath;
import com.weiiboo.gateway.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yyh
 * @date 2023-11-30
 */
@Component
@Slf4j
public class JWTFilter implements GlobalFilter{
    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ReleasePath releasePath;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        // 判断是否是放行的路径
        Boolean isRelease=checkPath(path);
        if(isRelease){
            return chain.filter(exchange);
        }
        // 获取token
        String token = exchange.getRequest().getHeaders().getFirst("token");
        ServerHttpResponse response = exchange.getResponse();
        if (!StringUtils.hasText(token)) {
            log.warn("token为空");
            return tokenFailure(response, ExceptionMsgEnum.NOT_LOGIN);
        }
        try {
            Map<String, Object> map = JWTUtil.parseToken(token);
            if (map == null ||!StringUtils.hasText(String.valueOf(map.get("userId")))) {
                log.error("token为null或没有获取到token");
                return tokenFailure(response,ExceptionMsgEnum.TOKEN_INVALID);
            }
            // 判断token是否过期
            Long expire = (Long) redisCache.get(
                    RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_EXPIRE,
                            String.valueOf(map.get("userId"))));
            if (expire == null || expire < System.currentTimeMillis()) {
                log.warn("token过期");
                return tokenFailure(response,ExceptionMsgEnum.TOKEN_EXPIRED);
            }
            // 将其他token踢下线
            String currentToken = (String)redisCache.hget(RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_INFO, String.valueOf(map.get("userId"))),
                    "token");
            if(!token.equals(currentToken)){
                log.warn("您的账号在其他地方登录或者登录信息已过期");
                return tokenFailure(response,ExceptionMsgEnum.ACCOUNT_OTHER_LOGIN);
            }
            // 刷新token的过期时间
            if(expire - System.currentTimeMillis() < jwtProperties.getRefreshTime()){
                redisCache.set(
                        RedisKey.build(RedisConstant.REDIS_KEY_USER_LOGIN_EXPIRE, String.valueOf(map.get("userId"))),
                        System.currentTimeMillis() + jwtProperties.getExpireTime());
            }
        } catch (Exception e) {
            log.error("token不合法", e);
            return tokenFailure(response,ExceptionMsgEnum.TOKEN_INVALID);
        }
        //TODO:这里可以做权限校验
        return chain.filter(exchange);
    }

    private Boolean checkPath(String path) {
        List<String> paths= releasePath.getPath();
        if(paths==null|| paths.isEmpty()){
            return false;
        }
        return paths.contains(path);
    }

    private Mono<Void> tokenFailure(ServerHttpResponse response, ExceptionMsgEnum exceptionMsgEnum) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        map.put("code", exceptionMsgEnum.getCode());
        map.put("msg", exceptionMsgEnum.getMsg());
        try {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(objectMapper.writeValueAsBytes(map))));
        } catch (JsonProcessingException e) {
            log.error("写入响应失败", e);
            return response.setComplete();
        }
    }
}
