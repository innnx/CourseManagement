package com.course.service;

import com.course.BaseTest;
import com.course.common.exception.BusinessException;
import com.course.dto.LoginRequest;
import com.course.dto.LoginResponse;
import com.course.dto.RegisterRequest;
import com.course.entity.User;
import com.course.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("用户测试服务")
public class UserServiceTest extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp(){
        //准备测试数据
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser001");
        registerRequest.setPassword("Test1234");
        registerRequest.setNickname("测试用户");
        registerRequest.setEmail("test001@example.com");
        registerRequest.setPhone("13800138001");
    }

    @AfterEach
    void tearDown(){
        //清理测试数据
        User user = userService.getUserByUsername("testuser001");
        if (user != null){
            userMapper.deleteById(user.getId());
        }
    }

    @Test
    @DisplayName("用户注册-成功")
    void testRegisterSuccess(){
        //执行注册
        userService.register(registerRequest);
        //验证用户是否注册成功
        User user = userService.getUserByUsername("testuser001");
        assertNotNull(user,"用户应该被成功创建");
        assertEquals("testuser001",user.getUsername());
        assertEquals("测试用户", user.getNickname());
        assertEquals("STUDENT", user.getRole());
        assertEquals(1, user.getStatus());
    }

    @Test
    @DisplayName("用户注册-用户名重复")
    void testRegisterDuplicateUsername(){
        //第一次注册
        userService.register(registerRequest);
        //第二次注册相同用户名
        BusinessException exception = assertThrows(
                BusinessException.class,
                ()->userService.register(registerRequest),
                "应该抛出用户名已存在异常"
        );
        assertEquals("用户名已存在",exception.getMessage());
    }

    @Test
    @DisplayName("用户登录-成功")
    void testLoginSuccess(){
        //先注册用户
        userService.register(registerRequest);
        //构造登录请求
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser001");
        loginRequest.setPassword("Test1234");
        //执行登录
        LoginResponse response = userService.login(loginRequest);
        //验证登录结果
        assertNotNull(response,"登录响应不为空");
        assertNotNull(response.getToken(),"token不应为空");
        assertEquals("testuser001",response.getUsername());
        assertEquals("STUDENT",response.getRole());
    }

    @Test
    @DisplayName("用户登录-密码错误")
    void testLoginWrongPassword(){
        //先注册用户
        userService.register(registerRequest);
        //构造错误的登录请求
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser001");
        request.setPassword("12313123123");
        //执行登录，应该抛出异常
        BusinessException exception = assertThrows(
                BusinessException.class,
                ()->userService.login(request),
                "应该抛出密码错误异常"
        );
        assertEquals("用户名或密码错误",exception.getMessage());
    }

    @Test
    @DisplayName("用户登录-用户不存在")
    void testLoginUserNotExists(){
        LoginRequest request = new LoginRequest();
        request.setUsername("qweasdasd");
        request.setPassword("123123");
        BusinessException exception = assertThrows(
                BusinessException.class,
                ()->userService.login(request)
        );
        assertEquals("用户名或密码错误",exception.getMessage());
    }

}
