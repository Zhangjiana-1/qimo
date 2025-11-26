package com.example.qimo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.ServletException;
import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 添加HiddenHttpMethodFilter以支持PUT和DELETE方法
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
    
    // 兼容性过滤器，确保在某些环境中也能正确处理PUT/DELETE请求
    @Bean
    public OncePerRequestFilter methodOverrideFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                            javax.servlet.FilterChain filterChain) 
                    throws ServletException, IOException {
                // 检查是否是POST请求并且包含_method参数
                if ("POST".equals(request.getMethod()) && request.getParameter("_method") != null) {
                    String method = request.getParameter("_method").toUpperCase();
                    HttpServletRequest wrappedRequest = new javax.servlet.http.HttpServletRequestWrapper(request) {
                        @Override
                        public String getMethod() {
                            return method;
                        }
                    };
                    filterChain.doFilter(wrappedRequest, response);
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        };
    }
}