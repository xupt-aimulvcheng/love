package com.xupt.love.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.mail.MailUtil;
import com.xupt.love.config.CustomRuntimeException;
import com.xupt.love.dto.UserDTO;
import com.xupt.love.mapper.UserMapper;
import com.xupt.love.pojo.WeChatUser;
import com.xupt.love.service.UserService;
import com.xupt.love.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Service
public class UserServiceImpl implements UserService {

    private static final long DEFAULT_EXPIRE_TIME = 3L; // 邮箱验证码的过期时间，设置为3分钟
    private static final String REDIS_KEY = "EMAIL_CODE:"; // Redis中保存验证码的key前缀
    private static final String REDIS_KEY_COOLDOWN = "EMAIL_COOLDOWN:"; // Redis中保存冷却时间的key前缀，用于防抖
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;  // 假设UserDAO是用于访问用户数据的类

    @Autowired
    private JwtUtil jwtUtil;  // 假设JwtUtil是用于生成JWTs的类


    /**
     * 发送邮箱验证码
     *
     * @param emailAddress 用户邮箱地址
     * @return 提示信息
     */
    public String sendEmailCode(String emailAddress) {
        // 校验邮箱是否为空
        if (CharSequenceUtil.isBlank(emailAddress)) {
            throw new CustomRuntimeException(400, "邮箱不能为空");
        }
        // 校验邮箱格式
        if (!Validator.isEmail(emailAddress)) {
            throw new CustomRuntimeException(400, "邮箱格式错误");
        }

        // 检查Redis中是否有冷却记录，用于防抖
        String cooldownValue = redisTemplate.opsForValue().get(REDIS_KEY_COOLDOWN + emailAddress);
        if (cooldownValue != null) {
            throw new CustomRuntimeException(400, "请等待60秒后再次尝试发送验证码");
        }

        // 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);
        // 使用Hutool的MailUtil发送邮件
        // 使用HTML模板发送邮件
        String mailContent = "<div style='border:1px solid #e9e9e9; padding: 15px; font-family: Arial, sans-serif;'>"
                + "<h2 style='color: #333;'>欢迎使用我们的服务</h2>"
                + "<p style='color: #666;'>您的验证码为：</p>"
                + "<h3 style='background-color: #f2f2f2; padding: 10px; display: inline-block;'>" + code + "</h3>"
                + "<p style='color: #999; font-size: 0.9em;'>此验证码3分钟内有效。</p>"
                + "</div>";
        MailUtil.send(emailAddress, "您的验证码", mailContent, true); // 注意：这里的最后一个参数设置为true，表示内容是HTML
        // 将验证码保存到Redis，并设置过期时间
        redisTemplate.opsForValue().set(REDIS_KEY + emailAddress, code, DEFAULT_EXPIRE_TIME, TimeUnit.MINUTES);
        // 设置60秒的冷却时间，用于防抖
        redisTemplate.opsForValue().set(REDIS_KEY_COOLDOWN + emailAddress, "1", 60, TimeUnit.SECONDS);

        return "验证码已发送，请检查您的邮箱";
    }

    /**
     * 校验邮箱验证码
     *
     * @param userDTO 用户数据传输对象，包含邮箱和验证码
     * @return 验证码是否正确
     */
    public boolean validateEmailCode(@org.jetbrains.annotations.NotNull UserDTO userDTO) {
        // 校验邮箱是否为空
        if (CharSequenceUtil.isBlank(userDTO.getEmail())) {
            throw new CustomRuntimeException(400, "邮箱不能为空");
        }
        // 校验邮箱格式
        if (!Validator.isEmail(userDTO.getEmail())) {
            throw new CustomRuntimeException(400, "邮箱格式错误");
        }
        // 校验验证码是否为空
        if (CharSequenceUtil.isBlank(userDTO.getEmailCode())) {
            throw new CustomRuntimeException(400, "验证码不能为空");
        }

        // 从Redis中获取存储的验证码
        String storedCode = redisTemplate.opsForValue().get(REDIS_KEY + userDTO.getEmail());
        // 比对验证码是否匹配
        if (storedCode != null && storedCode.equals(userDTO.getEmailCode())) {
            logger.info("userDTO:{}",userDTO);
            WeChatUser weChatUser = new WeChatUser();
            CopyOptions copyOptions = CopyOptions.create().setIgnoreNullValue(true); // 设置忽略null值
            BeanUtil.copyProperties(userDTO, weChatUser, copyOptions);
            userMapper.insertUser(weChatUser);
            // 如果验证码验证成功，从Redis中删除验证码
            redisTemplate.delete(REDIS_KEY + userDTO.getEmail());
            return true;
        }
        return false;
    }


    /**
     * 校验用户名和密码
     * @param username 用户名
     * @param password 密码
     * @return 生成的jwt
     */
    @Override
    public String authenticate(String username, String password) {
        // 1. 使用用户名查询用户
        WeChatUser user = userMapper.getByUsername(username);
        if (user == null) {
            return null;  // 用户未找到
        }

        // 2. 验证密码
        // 假设存储的密码是使用SHA-256哈希的
        if (!SecureUtil.sha256(password).equals(user.getPassword())) {
            return null;  // 密码错误
        }

        // 3. 如果验证成功，生成JWT并返回；否则返回null
        return jwtUtil.sign(user);
    }

}
