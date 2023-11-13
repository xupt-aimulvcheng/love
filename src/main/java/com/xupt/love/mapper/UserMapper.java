package com.xupt.love.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xupt.love.pojo.WeChatUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<WeChatUser> {
    void insertUser(WeChatUser weChatUser);

}
