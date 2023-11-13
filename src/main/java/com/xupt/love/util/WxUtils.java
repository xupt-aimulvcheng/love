package com.xupt.love.util;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

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
        // 获取微信用户信息
        try {
            TokenInfo tokenInfo = requestTokenInfo(code);
            if (tokenInfo != null) {
                WeChatUser weChatUser = requestWeChatUserInfo(tokenInfo);
                if (weChatUser != null) {
                    // 确保用户已注册
                    ensureUserRegistered(weChatUser);
                    // 生成并返回JWT令牌
                    return jwtUtil.sign(weChatUser);
                }
            }
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
        }
        return null;
    }

    // 请求并获取Token信息
    private TokenInfo requestTokenInfo(String code) throws IOException {
        // 构建请求URL
        String tokenUrl = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code", APP_ID, SECRET, code);
        HttpResponse response = httpClient.execute(new HttpGet(tokenUrl));
        if (response.getStatusLine().getStatusCode() == 200) {
            String responseResult = EntityUtils.toString(response.getEntity(), "UTF-8");
            return JSON.parseObject(responseResult, TokenInfo.class);
        } else {
            logger.error("获取accessToken失败，HTTP状态码：{}", response.getStatusLine().getStatusCode());
            return null;
        }
    }

    // 请求并获取微信用户信息
    private WeChatUser requestWeChatUserInfo(TokenInfo tokenInfo) throws IOException {
        // 构建请求URL
        String userInfoUrl = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN", tokenInfo.getAccessToken(), tokenInfo.getOpenid());
        HttpResponse userInfoResponse = httpClient.execute(new HttpGet(userInfoUrl));
        if (userInfoResponse.getStatusLine().getStatusCode() == 200) {
            String userInfoResult = EntityUtils.toString(userInfoResponse.getEntity(), "UTF-8");
            return JSON.parseObject(userInfoResult, WeChatUser.class);
        } else {
            logger.error("获取用户信息失败，HTTP状态码：{}", userInfoResponse.getStatusLine().getStatusCode());
            return null;
        }
    }

    // 确保用户已注册
    private void ensureUserRegistered(WeChatUser weChatUser) {
        LambdaQueryWrapper<WeChatUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WeChatUser::getOpenId, weChatUser.getOpenId());
        if (userMapper.selectOne(lambdaQueryWrapper) == null) {
            registerUserWithUniqueUsername(weChatUser, 0);
        }
    }

    // 使用唯一用户名注册用户，重试最多5次
    private void registerUserWithUniqueUsername(WeChatUser weChatUser, int attempt) {
        if (attempt >= 5) {
            logger.error("尝试注册用户超过5次，请稍后重试");
            return;
        }
        String randomUsername = generateRandomUsername();
        weChatUser.setUsername(randomUsername);
        try {
            userMapper.insertUser(weChatUser);
        } catch (DataIntegrityViolationException e) {
            logger.error("用户名重复或违反数据完整性", e);
            registerUserWithUniqueUsername(weChatUser, attempt + 1);
        }
    }

    // 生成随机用户名
    private String generateRandomUsername() {
        // 生成至少6位随机英文字母的用户名
        String randomLetters = RandomUtil.randomString("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", 6 + RandomUtil.randomInt(0, 5));
        return "xiaopi-" + randomLetters;
    }
}
