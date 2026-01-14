package com.course.common.security;

import com.course.entity.User;
import com.course.service.UserService;
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
    private final UserService userService;

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
                    //获取用户信息
                    User user = userService.getUserById(userId);
                    if (user!=null && user.getStatus() ==1){
                        //创建权限列表(添加ROLE_前缀),Spring Security 内部对“角色（Role）”有一种默认约定，即必须以 ROLE_ 开头,如果数据库存的是 ADMIN
                        // 这里会转为 ROLE_ADMIN。当你使用 @PreAuthorize("hasRole('ADMIN')") 时，Spring 会自动去匹配这个带前缀的字符串。
                        String role = "ROLE_" + user.getRole();
                        //创建认证对象,UsernamePasswordAuthenticationToken是Spring Security 用于封装用户身份的核心对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userId,
                                        //在 JWT 流程中，我们已经校验过 Token 了，不需要再保存用户的原始密码。
                                        null,
                                        //SimpleGrantedAuthority 是权限的最小单位。通过这一步，系统知道了这个 ID 为 101 的人拥有 ROLE_ADMIN 权限。
                                        Collections.singletonList(new SimpleGrantedAuthority(role))
                                );
                        //将认证信息存入 SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("用户{}认证成功",userId);
                    }
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
