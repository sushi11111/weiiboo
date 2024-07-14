package com.weiiboo.common.web.exception;

import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OnlyWarnException extends RuntimeException{
    private ExceptionMsgEnum exceptionMsgEnum;
    public OnlyWarnException(ExceptionMsgEnum exceptionMsgEnum) {
        this.exceptionMsgEnum=exceptionMsgEnum;
    }
    public OnlyWarnException(ExceptionMsgEnum exceptionMsgEnum, Throwable e){
        super(e);
        this.exceptionMsgEnum=exceptionMsgEnum;
    }
}