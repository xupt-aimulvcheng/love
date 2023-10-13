package com.xupt.love.util;

import com.alibaba.fastjson.JSON;
import com.xupt.love.pojo.TokenInfo;
import com.xupt.love.pojo.WeChatUser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.xupt.love.config.enums.WeChatConfig.APP_ID;
import static com.xupt.love.config.enums.WeChatConfig.SECRET;

@Component
public class WxUtils {

    private static final Logger logger = LoggerFactory.getLogger(WxUtils.class);
    public static WeChatUser getUserInfo(String code) {
        HttpClient httpClient = HttpClients.createDefault();
        logger.info("APP_ID,"+APP_ID+"SECRET,"+SECRET+"code,"+code);
        String tokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+APP_ID+"&secret="+SECRET+"&code="+code+"&grant_type=authorization_code";
        HttpGet httpGet = new HttpGet(tokenUrl);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            logger.info("responseResult:"+response.getEntity());
            logger.info("responseResult:"+response);
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseResult = EntityUtils.toString(response.getEntity(), "UTF-8");

                TokenInfo tokenInfo = JSON.parseObject(responseResult, TokenInfo.class);
                logger.info("tokenInfo"+tokenInfo);
                String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token="+tokenInfo.getAccessToken()+"&openid="+tokenInfo.getOpenid()+"&lang=zh_CN";
                HttpGet userInfoGet = new HttpGet(userInfoUrl);
                HttpResponse userInfoResponse = httpClient.execute(userInfoGet);
                if (userInfoResponse.getStatusLine().getStatusCode() == 200) {
                    String userInfoResult = EntityUtils.toString(userInfoResponse.getEntity(), "UTF-8");
                    logger.info("获取用户信息成功：" + userInfoResult);
                    return JSON.parseObject(userInfoResult, WeChatUser.class);
                } else {
                    logger.error("获取用户信息失败，HTTP状态码：" + userInfoResponse.getStatusLine().getStatusCode());
                }
            } else {
                logger.error("获取accessToken失败，HTTP状态码：" + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            logger.error("获取accessToken失败", e);
        }
        return null;
    }
}
