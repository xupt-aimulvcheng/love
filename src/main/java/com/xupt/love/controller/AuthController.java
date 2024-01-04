package com.xupt.love.controller;

import com.xupt.love.config.CustomRuntimeException;
import com.xupt.love.config.enums.Result;
import com.xupt.love.dto.UserDTO;
import com.xupt.love.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @return 发送信息
     */
    @PostMapping("/send-email-code")
    public Result sendEmailCode(@RequestParam String email) {
        String response = userService.sendEmailCode(email);
        return Result.success(response);
    }

    /**
     * 用户邮箱注册
     * @param userDTO 用户信息
     * @return 注册信息
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        if(!userService.validateEmailCode(userDTO)) {
            throw new CustomRuntimeException(400,"验证码无效");
        }
        return Result.success("注册成功");
    }

    /**
     *
     * @param password 密码
     * @param userInfo 用户名或者邮箱
     * @return 登录信息（jwt唯一标识，请放在请求头的唯一身份认证里）
     */
    @PostMapping("/login")
    public Result login(@RequestParam @NotNull String password,@RequestParam String userInfo) {
        String jwt = userService.authenticate(userInfo,password);
        if (jwt != null) {
            return Result.success(jwt);
        }
        else throw new CustomRuntimeException(400, "用户名或密码错误");
    }
}

