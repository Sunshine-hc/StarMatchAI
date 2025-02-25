package com.hc.starmatchai.interceptor;

import cn.hutool.core.util.StrUtil;
import com.hc.starmatchai.common.BusinessException;
import com.hc.starmatchai.util.JwtUtil;
import com.hc.starmatchai.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        log.info("AuthInterceptor preHandle for path: {}", path);

        String token = getTokenFromRequest(request);
        log.info("Token extracted: {}", token != null ? "present" : "null");
        if (StrUtil.isBlank(token) || "null".equals(token)) {
            throw new BusinessException(401, "未登录");
        }

        if (StringUtils.hasText(token)) {
            boolean isValid = jwtUtil.validateToken(token);
            log.info("Token validation result: {}", isValid);

            if (isValid) {
                Long userId = jwtUtil.extractUserId(token);
                log.info("Extracted userId from token: {}", userId);
                UserContext.setCurrentUserId(userId);
                return true;
            }
        }

        // 对于需要登录的接口，返回401
        log.warn("Unauthorized access to {}", path);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        log.info("AuthInterceptor afterCompletion for path: {}", request.getRequestURI());
        UserContext.clear();
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.info("Authorization header: {}", bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}