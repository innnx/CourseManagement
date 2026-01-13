package com.course.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/*
SecurityContextHolder：这是 Spring Security 的核心存储机制。默认情况下，它使用 ThreadLocal 来存储信息，
这意味着当前线程（即处理当前请求的线程）可以随时随地访问用户信息，而不需要通过方法参数层层传递。

getContext()：获取当前的上下文对象。

getAuthentication()：获取认证信息对象。如果用户已经登录，这个对象里会包含用户的权限、凭证和身份详情。

getPrincipal()：获取“主体”。在认证成功后，这里通常存储的是用户的唯一标识。在你的项目中，它被存储为 Long 类型的用户 ID。
 */

public class SecurityUtils {
    //获取当前登录用户的id
    public static Long getCurrentUsrId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long){
            return (Long) authentication.getPrincipal();
        }
        return null;
    }
}
