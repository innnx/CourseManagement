# 使用OpenJDK 17作为基础镜像
FROM eclipse-temurin:17-jre
# 设置工作目录
WORKDIR /app

# 复制jar包到容器
COPY target/course-management-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]