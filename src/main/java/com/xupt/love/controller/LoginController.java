package com.xupt.love.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.xupt.love.util.WxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@RestController
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private WxUtils wxUtils;  // 注入 WxUtils 实例

    @Value("${WeChat.url}")
    private String WXurl;

    @RequestMapping("/WxCheck")
    public String wxSignatureCheck(
            @RequestParam(value = "signature")String signature,
            @RequestParam(value = "timestamp")String timestamp,
            @RequestParam(value = "nonce") String nonce,
            @RequestParam(value = "echostr")String echostr ) {
        logger.info("signature: {} timestamp: {} nonce: {} echostr: {}", signature, timestamp, nonce, echostr);
        return echostr;
    }

    @GetMapping("/WxLogin")
    public void wxLoginPage(HttpServletResponse httpServletResponse) throws IOException {
        String redirectURL = URLEncoder.encode(WXurl,"UTF-8");
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx65db20ecfd57513f&redirect_uri="+redirectURL+"&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
        //返回图片
        httpServletResponse.setContentType("image/png");
        QrCodeUtil.generate(url,300,300,"jpg",httpServletResponse.getOutputStream());
    }
    @GetMapping("/WxCallBack")
    public String wxCallBack(String code, String state, HttpServletRequest request, HttpServletResponse response) {
        return wxUtils.getUserInfo(code);  // 返回JWT
    }
    @GetMapping("/order/a")
    public String aVoid(){
        logger.info("aVoid method called");
        return "认证成功";
    }

}
