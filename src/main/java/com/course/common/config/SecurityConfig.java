package com.course.common.config;

import com.course.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity          //开启Web 安全防护
@EnableMethodSecurity       //开启方法级别的权限控制,配合@PreAuthorize使用
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                //禁用CSRF（前后端分离项目不需要）
                .csrf(AbstractHttpConfigurer::disable)
                //配置请求授权
                .authorizeHttpRequests(auth-> auth
                        //授权：请求匹配登录/注册路径时，允许所有操作
                        .requestMatchers("/api/user/register","/api/user/login").permitAll()
                        //放行Swagger接口文档
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        //其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                //禁用session（使用JWT不需要session）
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        )
                //添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean   //将加密方法返回的对象BCryptPasswordEncoder注册到spring 容器
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
