package com.weiiboo.common.web.exception.handler;

import com.weiiboo.common.domin.Result;
import com.weiiboo.common.web.exception.BusinessException;
import com.weiiboo.common.web.exception.OnlyWarnException;
import com.weiiboo.common.web.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleAuthException(BusinessException businessException){
        log.error("业务异常:{}",businessException.getExceptionMsgEnum());
        return new Result<>(
                businessException.getExceptionMsgEnum().getCode(),
                businessException.getExceptionMsgEnum().getMsg(),
                null);
    }

    @ExceptionHandler(SystemException.class)
    public Result<?> handleAuthException(SystemException systemException){
        log.error("系统异常状态:{};系统异常:",systemException.getExceptionMsgEnum(),systemException);
        return new Result<>(
                systemException.getExceptionMsgEnum().getCode(),
                systemException.getExceptionMsgEnum().getMsg(),
                null);
    }
    @ExceptionHandler(OnlyWarnException.class)
    public void handleAuthException(OnlyWarnException onlyWarnException){
        log.warn("警告异常:{};异常信息:",onlyWarnException.getExceptionMsgEnum(),onlyWarnException);
    }
}
