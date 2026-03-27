package com.jmarfildev.rockalendar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

import com.jmarfildev.rockalendar.config.interceptors.TrafficLoggingInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TrafficLoggingInterceptor trafficLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trafficLoggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/me/**",
                    "/api/moderation/**",
                    "/api/admin/**"
                );
    }
}
