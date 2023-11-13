package com.xupt.love.config;
import com.xupt.love.config.enums.SecurityProperties;
import com.xupt.love.filter.JwtAuthenticationFilter;
import com.xupt.love.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JwtUtil jwtUtil;


    // 配置跨域访问
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
//                "*"
                "http://localhost:80" // 允许本地开发环境
//                ,"https://yourapp.com" // 允许的生产环境域名
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors() // 添加跨域访问支持
                .and()
                .csrf().disable() // 禁用CSRF
                .authorizeRequests()
                .antMatchers(securityProperties.getPublicPaths().toArray(new String[0])).permitAll() // 公开的URL
                .antMatchers(securityProperties.getAuthenticatedPaths().toArray(new String[0])).authenticated() // 需要身份验证的URL
                .anyRequest().denyAll() // 其他所有请求都拒绝
                .and()
                .exceptionHandling().authenticationEntryPoint((request, response, authException) -> { // 用户未认证时的处理
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    PrintWriter out = response.getWriter();
                    out.print("{\"error\":\"用户未认证\",\"message\":\"请登录后再访问该资源\"}");
                    out.flush();
                    out.close();
                })
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil,securityProperties), UsernamePasswordAuthenticationFilter.class); // 添加JWT验证过滤器
    }

}
