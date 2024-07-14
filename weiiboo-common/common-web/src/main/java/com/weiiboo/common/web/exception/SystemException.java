package com.weiiboo.common.web.exception;



import com.weiiboo.common.myEnum.ExceptionMsgEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemException extends RuntimeException{
    private ExceptionMsgEnum exceptionMsgEnum;

    public SystemException(ExceptionMsgEnum exceptionMsgEnum) {
        this.exceptionMsgEnum=exceptionMsgEnum;
    }
    public SystemException(ExceptionMsgEnum exceptionMsgEnum,Throwable e){
        super(e);
        this.exceptionMsgEnum=exceptionMsgEnum;
    }
}