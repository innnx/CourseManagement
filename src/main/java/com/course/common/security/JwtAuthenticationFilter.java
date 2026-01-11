package com.course.common.security;

import com.course.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String,Object> redisTemplate;

    private static final String TOKEN_PREFIX = "token:";
    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_TYPE = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //从请求头获取 token
        String token = getTokenFromRequest(request);
        if (token!=null && jwtUtils.validateToken(token)){
            try {
                //解析token 获取用户id
                Long userId = jwtUtils.getUserIdFromToken(token);
                //验证token在redis中是否有效（防止用户退出登录）
                String redisToken = (String) redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
                if (token.equals(redisToken)){
                    //创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    //将角色权限写入单元素集合中
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    //将认证信息存入SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("用户{}认证成功",userId);
                }
            }catch (Exception e){
                log.error("Token解析失败：{}",e.getMessage());
            }
        }
        filterChain.doFilter(request,response);
    }
    //从请求头获取 token的具体方法
    private String getTokenFromRequest(HttpServletRequest request) {
        //通过 HEADER_NAME（即 "Authorization"）从 HTTP 请求头中获取对应的值。
        String bearerToken = request.getHeader(HEADER_NAME);
        //判断字符串是否为 null 或长度为 0或是否全是空格。且以bearer开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_TYPE)){
            //从字符串剥离TOKEN_TYPE返回纯净的JWT令牌
            return bearerToken.substring(TOKEN_TYPE.length());
        }
        return null;
    }
}
