package com.xupt.love.config.enums;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class WeChatConfig {

    @Value("${WeChat.appID}")
    private String appId;

    @Value("${WeChat.appSecret}")
    private String secret;

    @Value("${WeChat.aesKey}")
    private String aesKey;

    @Value("${WeChat.token}")
    private String token;

    public static String APP_ID;
    public static String SECRET;
    public static String AES_KEY;
    public static String TOKEN;

    @PostConstruct
    private void init() {
        APP_ID = appId;
        SECRET = secret;
        AES_KEY = aesKey;
        TOKEN = token;
    }
}
