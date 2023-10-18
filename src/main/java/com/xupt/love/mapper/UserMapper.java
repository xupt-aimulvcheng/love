package com.xupt.love.mapper;

import com.xupt.love.pojo.WeChatUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    WeChatUser getByOpenId(String openId);

    void insertUser(WeChatUser weChatUser);

    WeChatUser getByUsername(String username);
}
