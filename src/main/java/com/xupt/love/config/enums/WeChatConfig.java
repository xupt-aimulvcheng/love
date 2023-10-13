package com.xupt.love.config.enums;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class WeChatConfig {

    private static String appId;
    private static String secret;

    public static String APP_ID;
    public static String SECRET;

    @Value("${WeChat.appID}")
    public void setAppId(String appId) {
        WeChatConfig.appId = appId;
    }

    @Value("${WeChat.appSecret}")
    public void setSecret(String secret) {
        WeChatConfig.secret = secret;
    }

    @PostConstruct
    public void init() {
        updateStaticFields(appId, secret);
    }

    private synchronized static void updateStaticFields(String appId, String secret) {
        APP_ID = appId;
        SECRET = secret;
    }
}
