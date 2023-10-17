package com.xupt.love.controller;

import com.xupt.love.config.CustomRuntimeException;
import com.xupt.love.config.enums.Result;
import com.xupt.love.dto.UserDTO;
import com.xupt.love.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/send-email-code")
    public Result sendEmailCode(@RequestParam String email) {
        String response = userService.sendEmailCode(email);
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        if(!userService.validateEmailCode(userDTO)) {
            return Result.fail("验证码无效");
        }
        return Result.success("注册成功");
    }

    @ExceptionHandler(CustomRuntimeException.class)
    public Result handleCustomRuntimeException(CustomRuntimeException e) {
        return Result.fail(e.getMessage());
    }
}

