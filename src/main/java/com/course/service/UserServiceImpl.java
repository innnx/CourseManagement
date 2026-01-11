package com.course.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.course.common.exception.BusinessException;
import com.course.dto.LoginRequest;
import com.course.dto.LoginResponse;
import com.course.dto.RegisterRequest;
import com.course.entity.User;
import com.course.mapper.UserMapper;
import com.course.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String,Object> redisTemplate;
    private static final String TOKEN_PREFIX = "token";
    private final PasswordEncoder passwordEncoder;

    //注册
    @Override
    public void register(RegisterRequest registerRequest) {
        //检查用户名是否存在
        User existUser = getUserByUsername(registerRequest.getUsername());
        if (existUser!=null){
            throw new BusinessException("用户名已存在！");
        }
        //创建用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));//加密
        user.setNickname(registerRequest.getNickname());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRole("STUDENT");    //默认学生角色
        user.setStatus(1);

        userMapper.insert(user);
        log.info("用户注册成功：{}"+registerRequest.getUsername());
    }

    //登录
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        //查询用户
        User user = getUserByUsername(loginRequest.getUsername());
        if (user == null){
            throw new BusinessException("用户名或密码错误");
        }
        //验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword())){
            throw new BusinessException("用户名或密码错误");
        }
        //检查用户状态
        if (user.getStatus() == 0){
            throw new BusinessException("账号被禁用");
        }
        //生成token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        //将token放入redis
        redisTemplate.opsForValue().set(
                TOKEN_PREFIX + user.getId(),
                token,
                24,
                TimeUnit.HOURS
        );
        log.info("用户登录成功：{}"+loginRequest.getUsername());
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .password(user.getPassword())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }

    //根据id查询
    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    //根据用户名查询
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,username);
        return userMapper.selectOne(wrapper);
    }
}
