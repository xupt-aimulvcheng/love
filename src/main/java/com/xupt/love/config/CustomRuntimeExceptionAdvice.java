package com.xupt.love.config;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.xupt.love.config.enums.Result;
//import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomRuntimeExceptionAdvice {
    //推荐创建不可变静态类成员变量
    private static final Log log = LogFactory.get();
    @ExceptionHandler(CustomRuntimeException.class)
    public Result handleCustomRuntimeException( CustomRuntimeException e) {
        return Result.fail(e.getMessage());
    }
}
