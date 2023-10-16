package com.xupt.love.config;

import com.google.gson.JsonObject;
import com.xupt.love.config.enums.SecurityProperties;
import com.xupt.love.filter.JwtAuthenticationFilter;
import com.xupt.love.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig() {
        logger.info("SecurityConfig加载完毕");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        securityProperties.getPublicPaths().forEach(s -> {
            logger.info("publishPaths:{}",s);
        });
        securityProperties.getAuthenticatedPaths().forEach(s -> {
            logger.info("authenticatedPaths:{}",s);
        });
        http
                .csrf().disable()
                .authorizeRequests()
//                // 配置公开访问的URL和需要认证的URL
                 .antMatchers(securityProperties.getPublicPaths().toArray(new String[0])).permitAll()
                .antMatchers(securityProperties.getAuthenticatedPaths().toArray(new String[0])).authenticated()
                .anyRequest().denyAll()
                .and()
                .exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
                        response.sendRedirect("/WxLogin");  // 重定向到微信登录页面
                    }
                })
//                .exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
//                    logger.info("用户未认证，正在调用AuthenticationEntryPoint.commence()");
//                    response.setContentType("application/json;charset=UTF-8");
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    JsonObject errorObj = new JsonObject();
//                    errorObj.addProperty("error", "用户未认证");
//                    errorObj.addProperty("message", "请登录后再访问该资源");
//                    PrintWriter out = response.getWriter();
//                    out.print(errorObj.toString());
//                    out.flush();
//                    out.close();
//                })
                .and()
//                // 添加JWT验证过滤器
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil,securityProperties), UsernamePasswordAuthenticationFilter.class)
        ;
    }
}
