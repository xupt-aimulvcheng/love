package com.xupt.love.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xupt.love.dto.UserDTO;
import com.xupt.love.pojo.WeChatUser;

public interface UserService extends IService<WeChatUser> {
    String sendEmailCode(String email);

    boolean validateEmailCode(UserDTO userDTO);

    String authenticate(String userInfo,String password);

    WeChatUser getOneByOpenidAndAppId(String fromUser, String toUser);
}
