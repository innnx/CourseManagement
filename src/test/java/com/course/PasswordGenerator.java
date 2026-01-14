package com.course;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//生成哈希密码
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("Admin123:" + encoder.encode("Admin123"));
    }
}
