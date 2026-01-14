package com.course.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        //实例化redis 工具类
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        //与工厂建立链接
        template.setConnectionFactory(factory);
        //String 序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 1. 定义定制化的 ObjectMapper
        ObjectMapper om = new ObjectMapper();
        // 关键：注册 JavaTimeModule 以支持 LocalDateTime 等类型
        om.registerModule(new JavaTimeModule());
        // 关键：禁用将日期序列化为时间戳，确保以可读字符串格式存储
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 设置可见性，确保可以序列化所有类型的属性
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 序列化时带上类信息，否则 Redis 反序列化回 Object 时会变成 Map
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // 2. 使用带定制 ObjectMapper 的序列化器
        Jackson2JsonRedisSerializer<Object> jsonRedisSerializerOb = new Jackson2JsonRedisSerializer<>(om, Object.class);

        //key采用String 的序列化方式
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        //value采用json 的序列化方式
        template.setValueSerializer(jsonRedisSerializerOb);
        template.setHashValueSerializer(jsonRedisSerializerOb);

        template.afterPropertiesSet();
        return template;
    }
}
