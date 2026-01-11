package com.course.service;

import com.course.dto.LoginRequest;
import com.course.dto.LoginResponse;
import com.course.dto.RegisterRequest;
import com.course.entity.User;

public interface UserService {
    //注册
    void register(RegisterRequest registerRequest);
    //登录
    LoginResponse login(LoginRequest loginRequest);
    //根据id获取用户
    User getUserById(Long id);
    //根据用户名获取用户
    User getUserByUsername(String username);
}
