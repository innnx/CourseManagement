package com.course.common.config;

import com.course.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity          //开启Web 安全防护
@EnableMethodSecurity       //开启方法级别的权限控制,配合@PreAuthorize使用
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    /*
A. 权限请求控制 (URL 匹配)
这是配置“谁能访问什么”的地方。
.authorizeHttpRequests(auth -> ...)：开启请求权限配置。
.requestMatchers("/path")：指定要操作的路径。支持通配符（如 /**）。
.permitAll()：对匹配的路径完全开放，无需登录（常用于登录、注册接口）。
.authenticated()：要求匹配的路径必须登录后才能访问。
.hasRole("ADMIN")：要求用户必须拥有特定角色。

B. 登录与注销管理
.formLogin(conf -> ...)：开启 Spring Security 默认的表单登录页。
.logout(conf -> ...)：配置注销逻辑，如清除 Cookie、注销 Session 等。
.httpBasic()：开启 HTTP Basic 认证（通常在测试或非浏览器客户端中使用）。

C. 安全防御与跨域
.csrf(csrf -> csrf.disable())：禁用 CSRF（跨站请求伪造）防御。在前后端分离（尤其是使用 JWT）的项目中，通常需要禁用它，否则 POST 请求会被拦截。
.cors(conf -> ...)：开启并配置跨域支持，允许前端跨域访问接口。

D. 会话与过滤器控制
.sessionManagement(...)：配置会话策略。如代码中的 STATELESS（无状态），意味着不使用服务器 Session。
.addFilterBefore(filter, class)：在特定过滤器之前插入自定义过滤器。
 */

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
                        //添加公开接口
                        .requestMatchers(
                                "/api/category/list",
                                "/api/course/page",
                                "/api/course/{id}"
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
}
