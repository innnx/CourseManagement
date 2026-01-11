package com.course.common.exception;

import com.course.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e){
        log.error("业务异常:{}",e.getMessage());
        return Result.error(e.getCode(),e.getMessage());
    }
    //参数校验异常
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e){
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("参数校验失败：{}",message);
        return Result.error(400,message);
    }
    //其他异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        log.error("系统异常：",e);
        return Result.error("系统异常，请联系管理员");
    }
}
