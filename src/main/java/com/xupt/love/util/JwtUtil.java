package com.xupt.love.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.xupt.love.pojo.WeChatUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings("All")
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final long EXPIRE_TIME = 7200;
    public static final String OPEN_ID = "openId";
    public static final String JWT_ID = "jwt-id";
    public static final String WX_JWT_SESSION = "WX-JWT-SESSION-";

    @Value("${WeChat.appSecret}")
    private String appSecret;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 为用户签署JWT令牌
    public String sign(WeChatUser account) {
        String jwtId = UUID.randomUUID().toString();
        Algorithm algorithm = Algorithm.HMAC256(appSecret);
        // 用户为微信登录openId坐为值,为密码登录用户id作为值
        String value;
        if (account.getOpenId() != null) {
            value = "openId:" + account.getOpenId();
        } else {
            value = "userId:" + account.getUserId().toString();
        }
        // 创建JWT令牌
        String token = JWT.create()
                .withClaim(OPEN_ID, value)
                .withClaim(JWT_ID, jwtId)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME * 1000))
                .sign(algorithm);
        // 在Redis中存储JWT令牌
        redisTemplate.opsForValue().set(WX_JWT_SESSION + jwtId, token, EXPIRE_TIME, TimeUnit.SECONDS);
        logger.info("为 openId: {} 生成了 JWT", account.getOpenId());
        return token;
    }


    // 验证JWT令牌
    public boolean verify(String token) {
        try {
            // 从Redis中获取JWT令牌
            String redisToken = redisTemplate.opsForValue().get(WX_JWT_SESSION + getClaimsByToken(token).get(JWT_ID).asString());
            logger.info("从Redis中检索到的token: {}", redisToken);
            if (!token.equals(redisToken)) {
                logger.warn("传入的token与Redis中的token不匹配");
                return Boolean.FALSE;
            }
            Algorithm algorithm = Algorithm.HMAC256(appSecret);
            logger.info("使用的appSecret: {}", appSecret);
            // 使用JWT令牌进行验证
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim(OPEN_ID, getClaimsByToken(redisToken).get(OPEN_ID).asString())
                    .withClaim(JWT_ID, getClaimsByToken(redisToken).get(JWT_ID).asString())
                    .build();
            verifier.verify(token);  // 这里可能抛出异常
            logger.info("JWT验证成功");
            redisTemplate.opsForValue().set(WX_JWT_SESSION + getClaimsByToken(token).get(JWT_ID).asString(),
                    redisToken,
                    EXPIRE_TIME,
                    TimeUnit.SECONDS);
            return Boolean.TRUE;
        } catch (JWTVerificationException e) {
            logger.warn("JWT验证失败，异常原因: {}", e.getMessage());
            return Boolean.FALSE;
        } catch (Exception e) {
            logger.error("处理 token: {} 时发生未知异常", token, e);
            return Boolean.FALSE;
        }
    }

    // 从JWT令牌中解码claims
    public Map<String, Claim> getClaimsByToken(String token) throws JWTDecodeException {
        Map<String, Claim> claims = JWT.decode(token).getClaims();
        logger.info("从token中解析的声明: {}", claims);
        return claims;
    }
}
