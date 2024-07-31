package com.weiiboo.common.web.utils;

import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.common.web.exception.SystemException;
import com.weiiboo.common.web.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Configuration
public class JWTUtil {
    private static JwtProperties jwtProperties;

    @Autowired
    private void setJwtProperties(JwtProperties jwtProperties) {
        JWTUtil.jwtProperties = jwtProperties;
    }

    //生成令牌
    public static String createToken(Map<String,Object> claims){
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512,jwtProperties.getSecret()).compact();
    }

    //解析令牌
    public static Map<String,Object> parseToken(String token){
        return Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token).getBody();
    }

    public static Long getCurrentUserId(String token){
        Object userIdObj = parseToken(token).get("userId");
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else {
            throw new SystemException(ExceptionMsgEnum.SERVER_ERROR);
        }
    }

    public static void isTokenUserIdMatch(HttpServletRequest request, Long userId){
        Map<String,Object>token = JWTUtil.parseToken(request.getHeader("token"));
        Long id = Long.valueOf(String.valueOf(token.get("userId")));
        if(!id.equals(userId)){
            throw new BusinessException(ExceptionMsgEnum.TOKEN_INVALID);
        }
    }
}
