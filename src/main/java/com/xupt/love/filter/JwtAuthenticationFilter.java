package com.xupt.love.filter;

import com.auth0.jwt.interfaces.Claim;
import com.xupt.love.config.enums.SecurityProperties;
import com.xupt.love.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private SecurityProperties securityProperties;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, SecurityProperties securityProperties) {
        this.jwtUtil = jwtUtil;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 打印请求的URL
        logger.info("进入JwtAuthenticationFilter，正在处理URL: {}", request.getRequestURI());

        String requestURI = request.getRequestURI();
        boolean isPublicPath = securityProperties.getPublicPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
        // 检查URL是否在公开访问列表中
        if (isPublicPath) {
            logger.info("当前URL在允许访问的列表中，不进行JWT验证");
            filterChain.doFilter(request, response);
            return;
        }


        final String authorizationHeader = request.getHeader("Authorization");
        String openId = null;
        String jwt = null;
        // 检查Authorization头部是否存在
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            logger.info("找到Authorization头部: {}", authorizationHeader);
            jwt = authorizationHeader.substring(7); // 提取JWT令牌
            try {
                // 从JWT令牌中解码claims
                Map<String, Claim> claims = jwtUtil.getClaimsByToken(jwt);
                openId = claims.get("openId").asString();
                logger.info("从JWT中获取的openId: {}", openId);
            } catch (Exception e) {
                logger.error("JWT解码时出错", e);
            }
        } else {
            logger.info("未找到Authorization头部或格式不正确");
        }

        boolean verify = jwtUtil.verify(jwt);
        logger.info("JWT验证结果: {}", verify);

        logger.info("当前SecurityContextHolder的认证状态: {}", SecurityContextHolder.getContext().getAuthentication());
        if (!verify) {
            SecurityContextHolder.clearContext();
        }

        // 如果openId有效且JWT验证成功，则为用户进行认证
        if (openId != null && SecurityContextHolder.getContext().getAuthentication() == null  && verify) {
            logger.info("JWT验证成功，openId: {}", openId);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(openId, null, new ArrayList<>());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            logger.info("JWT验证失败或openId为空");
        }

        filterChain.doFilter(request, response);
        logger.info("退出JwtAuthenticationFilter，完成处理URL: {}", request.getRequestURI());
    }
}
