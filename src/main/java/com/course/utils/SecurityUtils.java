package com.course.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
