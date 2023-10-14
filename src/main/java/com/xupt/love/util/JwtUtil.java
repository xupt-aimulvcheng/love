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

    /**
     * 过期时间: 2小时
     */
    private static final long EXPIRE_TIME = 7200;

    /**
     * 使用 appid 签名
     */
    @Value("${WeChat.appSecret}")
    private String appSecret;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 根据微信用户登陆信息创建 token
     *
     * @param account 微信用户信息
     * @return 返回 jwt token
     */
    public String sign(WeChatUser account) {
        // JWT 随机ID,作为 redis 验证的 key
        String jwtId = UUID.randomUUID().toString();
        // 1. 使用加密算法进行签名得到 token
        Algorithm algorithm = Algorithm.HMAC256(appSecret);
        String token = JWT.create()
                .withClaim("openId", account.getOpenId())
                .withClaim("jwt-id", jwtId)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME * 1000))
                .sign(algorithm);
        // 2. Redis 缓存 JWT, 注: 请和 JWT 过期时间一致
        redisTemplate.opsForValue().set("WX-JWT-SESSION-" + jwtId, token, EXPIRE_TIME, TimeUnit.SECONDS);
        logger.info("为 openId: {} 生成了 JWT", account.getOpenId());
        return token;
    }

    /**
     * token 检验
     *
     * @param token token
     * @return bool
     */
    public boolean verify(String token) {
        try {
            // 1. 根据 token 解密，解密出 jwt-id, 先从 redis 中查找出 redisToken, 匹配是否相同
            String redisToken = redisTemplate.opsForValue().get("WX-JWT-SESSION-" + getClaimsByToken(token).get("jwt-id").asString());
            if (!token.equals(redisToken)) {
                return Boolean.FALSE;
            }
            // 2. 得到算法相同的 JWTVerifier
            Algorithm algorithm = Algorithm.HMAC256(appSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("openId", getClaimsByToken(redisToken).get("openId").asString())
                    .withClaim("jwt-id", getClaimsByToken(redisToken).get("jwt-id").asString())
                    .build();
            // 3. 验证 token
            verifier.verify(token);
            // 4. Redis 缓存 JWT 续期
            redisTemplate.opsForValue().set("WX-JWT-SESSION-" + getClaimsByToken(token).get("jwt-id").asString(),
                    redisToken,
                    EXPIRE_TIME,
                    TimeUnit.SECONDS);
            return Boolean.TRUE;
        } catch (JWTVerificationException e) {
            logger.warn("验证 token: {} 失败", token, e);
            return Boolean.FALSE;
        }
    }

    /**
     * 从 token 解密信息
     *
     * @param token token
     * @return 解密得到的声明集合
     * @throws JWTDecodeException 解密异常
     */
    public Map<String, Claim> getClaimsByToken(String token) throws JWTDecodeException {
        return JWT.decode(token).getClaims();
    }
}
