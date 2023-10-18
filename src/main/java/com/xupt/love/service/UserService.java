package com.xupt.love.service;

import com.xupt.love.dto.UserDTO;
import com.xupt.love.pojo.WeChatUser;

public interface UserService {
    String sendEmailCode(String email);

    boolean validateEmailCode(UserDTO userDTO);

    String authenticate(String username, String password);
}
