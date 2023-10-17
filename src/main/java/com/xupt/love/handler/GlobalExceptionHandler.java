package com.xupt.love.handler;

import com.xupt.love.config.CustomRuntimeException;
import com.xupt.love.config.enums.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = CustomRuntimeException.class)
    @ResponseBody
    public Result handleCustomRuntimeException(CustomRuntimeException e) {
        return new Result(400, e.getMessage());
    }

    // 其他异常处理...
}
