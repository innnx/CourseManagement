package com.course.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {
    @Bean   //将加密方法返回的对象BCryptPasswordEncoder注册到spring 容器
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
