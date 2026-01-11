package com.course.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    //生成 token
    public String generateToken(Long userId,String username){
        //使用Keys工具类加密初始化的secret，生成一个签名密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()   //生产token方法，开启入口
                .subject(userId.toString())     //设置主题，通常存用户id
                .claim("username",username)  //设置荷载，自定义
                .issuedAt(new Date())           //设置创建时间
                .expiration(new Date(System.currentTimeMillis()+expiration))    //设置有效期
                .signWith(key)      //设置签名密钥和算法
                .compact();         //压缩合成该token/结束标语
    }

    //解析 token
    public Claims parseToken(String token){
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()        //用于验证和读取 Token。它先构建一个解析器，再进行解析。
                .verifyWith(key)    //设置验真用的密钥。必须与生成时的密钥完全一致。
                .build()            //将配置好的参数实例化为一个真正的 JwtParser
                .parseSignedClaims(token)   //核心方法。执行解析动作。它会自动检查签名、检查是否过期、检查格式。
                .getPayload();      //只有通过了上述所有地狱级检查，代码才会运行到这一行。它会把中间段（Payload）的内容从 JSON 转回 Claims 对象
    }

    //从token 获取用户id
    public Long getUserIdFromToken(String token){
        Claims claims = parseToken(token);
        //解析token后 转回的Claims对象 通过getSubject获取用户id
        return Long.parseLong(claims.getSubject());
    }

    //验证token 是否有效
    public boolean validateToken(String token){
        try{
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
