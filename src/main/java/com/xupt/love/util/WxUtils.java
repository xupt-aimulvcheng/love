package com.xupt.love.util;

import com.alibaba.fastjson.JSON;
import com.xupt.love.mapper.UserMapper;
import com.xupt.love.pojo.TokenInfo;
import com.xupt.love.pojo.WeChatUser;
import com.xupt.love.service.UserService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.xupt.love.config.enums.WeChatConfig.APP_ID;
import static com.xupt.love.config.enums.WeChatConfig.SECRET;

@Component
public class WxUtils {

    private static final Logger logger = LoggerFactory.getLogger(WxUtils.class);

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;

    private final HttpClient httpClient = HttpClients.createDefault();

    public String getUserInfo(String code) {
        String tokenUrl = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", APP_ID, SECRET, code);
        HttpGet httpGet = new HttpGet(tokenUrl);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseResult = EntityUtils.toString(response.getEntity(), "UTF-8");
                TokenInfo tokenInfo = JSON.parseObject(responseResult, TokenInfo.class);

                String userInfoUrl = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN", tokenInfo.getAccessToken(), tokenInfo.getOpenid());
                HttpGet userInfoGet = new HttpGet(userInfoUrl);
                HttpResponse userInfoResponse = httpClient.execute(userInfoGet);
                if (userInfoResponse.getStatusLine().getStatusCode() == 200) {
                    String userInfoResult = EntityUtils.toString(userInfoResponse.getEntity(), "UTF-8");
                    WeChatUser weChatUser = JSON.parseObject(userInfoResult, WeChatUser.class);
                    // todo 数据库没有该用户时自动注册
                    // 根据openId查询用户是否存在
                    if(userMapper.getByOpenId(weChatUser.getOpenId()) == null){
                        userMapper.insertUser(weChatUser);
                    }

                    // 在此处生成 JWT 令牌，并将其返回
                    String jwt = jwtUtil.sign(weChatUser);
                    logger.info("获取用户信息成功：{}", userInfoResult);
                    return jwt;
                } else {
                    logger.error("获取用户信息失败，HTTP状态码：{}", userInfoResponse.getStatusLine().getStatusCode());
                }
            } else {
                logger.error("获取accessToken失败，HTTP状态码：{}", response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            logger.error("获取accessToken失败", e);
        }
        return null;
    }
}
