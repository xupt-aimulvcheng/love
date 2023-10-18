package com.xupt.love.controller;

import com.xupt.love.config.CustomRuntimeException;
import com.xupt.love.config.enums.Result;
import com.xupt.love.dto.UserDTO;
import com.xupt.love.service.UserService;
import org.jetbrains.annotations.NotNull;
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
            throw new CustomRuntimeException(400,"验证码无效");
        }
        return Result.success("注册成功");
    }
    @PostMapping("/login")
    public Result login(@RequestBody @NotNull UserDTO loginRequest) {
        String jwt = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        if (jwt != null) {
            return Result.success(jwt);
        }
        else throw new CustomRuntimeException(400, "用户名或密码错误");
    }
}

