package com.xupt.love.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.mail.MailUtil;
import com.xupt.love.config.CustomRuntimeException;
import com.xupt.love.dto.UserDTO;
import com.xupt.love.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Service
public class UserServiceImpl implements UserService {

    private static final long DEFAULT_EXPIRE_TIME = 10L;
    private static final String REDIS_KEY = "EMAIL_CODE:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String sendEmailCode(String emailAddress) {
        if (CharSequenceUtil.isBlank(emailAddress)) {
            throw new CustomRuntimeException("邮箱不能为空");
        }
        if (!Validator.isEmail(emailAddress)) {
            throw new CustomRuntimeException("邮箱格式错误");
        }
        String code = RandomUtil.randomNumbers(6);
        MailUtil.send(emailAddress, "验证码", "您的验证码为：" + code, false);
        redisTemplate.opsForValue().set(REDIS_KEY + emailAddress, code, DEFAULT_EXPIRE_TIME, TimeUnit.MINUTES);
        return "验证码已发送，请检查您的邮箱";
    }

    public boolean validateEmailCode(UserDTO userDTO) {
        if (CharSequenceUtil.isBlank(userDTO.getEmail())) {
            throw new CustomRuntimeException("邮箱不能为空");
        }
        if (!Validator.isEmail(userDTO.getEmail())) {
            throw new CustomRuntimeException("邮箱格式错误");
        }
        if (CharSequenceUtil.isBlank(userDTO.getEmailCode())) {
            throw new CustomRuntimeException("验证码不能为空");
        }
        String storedCode = redisTemplate.opsForValue().get(REDIS_KEY + userDTO.getEmail());
        if (storedCode != null && storedCode.equals(userDTO.getEmailCode())) {
            redisTemplate.delete(REDIS_KEY + userDTO.getEmail());

            return true;
        }
        return false;
    }
}