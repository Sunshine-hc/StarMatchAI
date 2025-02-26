package com.hc.starmatchai.config;

import com.hc.starmatchai.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    // 需要拦截的路径
    private static final List<String> INCLUDE_PATHS = Collections.unmodifiableList(Arrays.asList(
            "/user/**",
            "/api/**",
            "/starMatchAI/match/**",
            "/starMatchAI/match-record/**"
            // 添加其他需要拦截的路径
    ));

    // 不需要拦截的路径
    private static final List<String> EXCLUDE_PATHS = Collections.unmodifiableList(Arrays.asList(
            "/user/login",
            "/user/register",
            "/user/send-code",
            "/api/public/**",
            "/public/**"));

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering AuthInterceptor");
        log.info("Include paths: {}", INCLUDE_PATHS);
        log.info("Exclude paths: {}", EXCLUDE_PATHS);

        registry.addInterceptor(authInterceptor)
                .addPathPatterns(INCLUDE_PATHS)
                .excludePathPatterns(EXCLUDE_PATHS);
    }
}