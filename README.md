# 在线课程管理系统

一个基于SpringBoot的在线教育平台后端项目，实现了用户管理、课程管理、订单管理等核心功能。

## 技术栈

- **后端框架**: SpringBoot 3.2.0
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x
- **ORM**: MyBatis-Plus 3.5.5
- **认证**: Spring Security + JWT
- **接口文档**: SpringDoc (Swagger)
- **工具库**: Hutool
- **部署**: Docker + Docker Compose

## 功能特性

### 用户模块
- ✅ 用户注册/登录
- ✅ JWT认证授权
- ✅ 角色权限管理 (学生/教师/管理员)

### 课程模块
- ✅ 课程CRUD操作
- ✅ 课程分类管理
- ✅ 课程搜索和分页
- ✅ Redis缓存优化
- ✅ 教师权限控制

### 订单模块
- ✅ 创建订单
- ✅ 订单支付/取消
- ✅ 订单状态流转
- ✅ 防止重复购买
- ✅ Redis分布式锁
- ✅ 事务管理

### 其他功能
- ✅ 定时任务 (每日订单统计)
- ✅ 全局异常处理
- ✅ 统一响应格式
- ✅ 参数校验
- ✅ 单元测试

## 快速开始

### 方式一: Docker部署 (推荐)
```bash
# 1. 克隆项目
git clone https://github.com/innnx/CourseManagement.git
cd CourseManagement

# 2. 打包项目
mvn clean package -DskipTests

# 3. 启动Docker容器
docker-compose up -d

# 4. 访问Swagger文档
http://localhost:8080/swagger-ui.html
```

### 方式二: 本地运行
```bash
# 1. 创建数据库
# 在MySQL中执行 init.sql

# 2. 修改配置
# 编辑 src/main/resources/application.yml
# 修改数据库连接信息

# 3. 启动Redis
# 确保Redis服务已启动

# 4. 运行项目
mvn spring-boot:run
```

## 项目结构
```
src/main/java/com/course
├── common              # 公共模块
│   ├── config         # 配置类
│   ├── constant       # 常量
│   ├── exception      # 异常处理
│   ├── result         # 统一响应
│   ├── security       # 安全配置
│   └── task           # 定时任务
├── controller         # 控制器
├── service            # 服务层
│   └── impl          # 实现类
├── mapper             # 数据访问层
├── entity             # 实体类
├── dto                # 数据传输对象
└── utils              # 工具类
```

## API文档

启动项目后访问: http://localhost:8080/swagger-ui.html

主要接口:
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/course/page` - 查询课程列表
- `POST /api/course` - 创建课程 (需教师权限)
- `POST /api/order` - 创建订单 (需登录)
- `PUT /api/order/{orderNo}/pay` - 支付订单

## 测试
```bash
# 运行所有单元测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest
```

## 默认账号

- 管理员: `admin` / `Admin123`

## 开发团队

- 开发者: [@innnx](https://github.com/innnx)

## License

MIT License