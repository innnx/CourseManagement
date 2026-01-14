package com.course.common.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("课程管理系统 API")
                        .version("1.0")
                        .description("基于 Spring Boot 3 + Spring Security 的课程管理系统接口文档"))
                // 1. 定义安全方案（JWT 认证）
                .components(new Components()
                        .addSecuritySchemes("JWT_AUTH", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                // 2. 全局应用安全方案
                .addSecurityItem(new SecurityRequirement().addList("JWT_AUTH"));
    }
}
