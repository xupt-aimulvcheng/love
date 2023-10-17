package com.xupt.love.service;

import com.xupt.love.dto.UserDTO;

public interface UserService {
    String sendEmailCode(String email);

    boolean validateEmailCode(UserDTO userDTO);
}
