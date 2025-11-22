package com.example.qimo.config;

import com.example.qimo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 启用CSRF保护，提高安全性
            .csrf().and()
            .authorizeRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/"),
                    new AntPathRequestMatcher("/register"),
                    new AntPathRequestMatcher("/login"),
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/images/**")
                ).permitAll()
                // 允许任何人查看书籍列表和详情
                .requestMatchers(
                    new AntPathRequestMatcher("/books"),
                    new AntPathRequestMatcher("/books/**", "GET")
                ).permitAll()
                // 要求评论接口必须认证
                .requestMatchers(new AntPathRequestMatcher("/books/**/comments")).authenticated()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/books")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // 明确指定登出URL
                .permitAll()
                // 设置登出成功后重定向到登录页
                .logoutSuccessUrl("/login")
                // 确保会话被销毁
                .invalidateHttpSession(true)
                // 清除认证信息
                .clearAuthentication(true)
            )
            // 配置会话管理，确保会话在重启后不会保留
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false));

        return http.build();
    }
}