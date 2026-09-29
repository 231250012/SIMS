# 学生信息管理系统 (Student Information Management System)

基于Spring Boot 3.2.x的现代化学生信息管理系统，支持多角色用户管理、权限控制、缓存优化和消息队列处理。

## 技术栈

- **后端框架**: Spring Boot 3.2.x
- **安全框架**: Spring Security + JWT
- **数据持久化**: Spring Data JPA + MyBatis
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.x（会话、API 计数、Spring Cache）
- **消息队列**: Apache Kafka 3.6.x
- **数据库迁移**: Flyway
- **测试框架**: JUnit 5 + jqwik (Property-based Testing) + Testcontainers

## 本地依赖服务

开发前可先启动 Redis：

```bash
docker compose up -d redis
```

默认连接 `localhost:6379`（见 `application-dev.yml`）。

## 项目结构

```
src/
├── main/
│   ├── java/com/sims/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # REST控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── entity/         # JPA实体类
│   │   ├── exception/      # 异常类
│   │   ├── repository/     # 数据访问层
│   │   ├── security/       # 安全配置
│   │   ├── service/        # 业务逻辑层
│   │   └── util/           # 工具类
│   └── resources/
│       ├── db/migration/   # Flyway迁移脚本
│       ├── application.yml # 主配置文件
│       ├── application-dev.yml  # 开发环境配置
│       ├── application-local.yml # 本地联调配置
│       └── application-prod.yml # 生产环境配置
└── test/
    ├── java/com/sims/      # 测试类
    └── resources/          # 测试资源
```

## 环境配置

### 开发环境 (dev)

- 数据库: MySQL (localhost:3306/sims_dev)
- Redis: localhost:6379
- Kafka: localhost:9092
- 日志级别: DEBUG

### 生产环境 (prod)

- 数据库: 通过环境变量配置
- Redis: 通过环境变量配置
- Kafka: 通过环境变量配置
- 日志级别: INFO

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.x
- Apache Kafka 3.6.x

### 运行应用

1. 克隆项目
2. 配置数据库连接 (application-dev.yml)
3. 启动依赖服务 (MySQL, Redis, Kafka)
4. 运行应用:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=StudentManagementSystemApplicationTests
```

## 核心功能

- 用户认证与授权 (JWT)
- 学生信息管理 (CRUD)
- 班级管理与权限控制
- 成绩管理与排行榜
- API调用统计与监控
- 异步事件处理 (Kafka)
- 缓存优化 (Redis)
- 文件上传管理

## 开发指南

### 代码规范

- 遵循Java编码规范
- 使用Spring Boot最佳实践
- 编写单元测试和属性测试
- 使用Flyway管理数据库变更

### 测试策略

- 单元测试: 验证具体功能和边界条件
- 属性测试: 使用jqwik验证通用属性
- 集成测试: 使用Testcontainers测试完整流程

## 部署

支持Docker容器化部署和Kubernetes集群部署，详见后续任务实现。