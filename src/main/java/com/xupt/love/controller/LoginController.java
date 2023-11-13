package com.xupt.love.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.xupt.love.config.enums.Result;
import com.xupt.love.util.WxUtils;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.service.WxOAuth2Service;
import me.chanjar.weixin.mp.api.WxMpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/WeChat")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private WxUtils wxUtils;  // 注入 WxUtils 实例

    @Value("${WeChat.url}")
    private String WXurl;

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String key;

    @RequestMapping("/WxCheck")
    public String wxSignatureCheck(
            @RequestParam(value = "signature")String signature,
            @RequestParam(value = "timestamp")String timestamp,
            @RequestParam(value = "nonce") String nonce,
            @RequestParam(value = "echostr")String echostr ) {
        return echostr;
    }

    @GetMapping("/WxLogin")
    public ResponseEntity<?> wxLoginPage(HttpServletResponse httpServletResponse) throws IOException {
        WxOAuth2Service oAuth2Service = wxMpService.getOAuth2Service();
        String url = oAuth2Service.buildAuthorizationUrl(WXurl, WxConsts.OAuth2Scope.SNSAPI_USERINFO, null);
        generateKey();
        Map<String, Object> response = new HashMap<>();
        response.put("qrCode", url);
        response.put("tempId", key);

        return ResponseEntity.ok(response);
    }
    @RequestMapping("/WxCallBack")
    public String wxCallBack(String code, String state, HttpServletRequest request, HttpServletResponse response) throws IOException, WxErrorException {
//        WxOAuth2Service oAuth2Service = wxMpService.getOAuth2Service();
//        // 利用code获取accessToken
//        WxOAuth2AccessToken accessToken = oAuth2Service.getAccessToken(code);
//        // 利用accessToken获取用户信息
//        return oAuth2Service.getUserInfo(accessToken, null);
        String jwtStr = wxUtils.getUserInfo(code);// 返回JWT
        if (!StrUtil.isEmpty(jwtStr)) {
            redisTemplate.opsForValue().set("userLoginStatus:" + getKey(), jwtStr, 3, TimeUnit.MINUTES); // 状态保存5s
            return "恭喜您，登录完成";
        }
        else {
            return "抱歉，请关注公众号后重新扫码登录";
        }
    }
    @GetMapping("/checkIsLogin")
    public Result checkIsLogin(@RequestParam String tempId) {
        Map<String, Object> responseMap = new HashMap<>();
        String jwtStr = redisTemplate.opsForValue().get("userLoginStatus:" + tempId);
        responseMap.put("isLogin", !StrUtil.isEmpty(jwtStr));
        responseMap.put("token", jwtStr);
        return Result.success("查询成功", responseMap);
    }
    private synchronized void generateKey() {
        key = UUID.randomUUID().toString();
    }

    private synchronized String getKey() {
        return key;
    }
}
