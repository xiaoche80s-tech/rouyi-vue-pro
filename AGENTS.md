# AGENTS.md

This file provides guidance to Qoder (qoder.com) when working with code in this repository.

## 项目概述

芋道（ruoyi-vue-pro）是一个基于 Spring Boot 3 + JDK 17 的多模块快速开发平台。后端采用 Maven 多模块架构，前端位于 `yudao-ui/yudao-ui-admin-vue3/`（Vue3 + Element Plus）。

## 构建与运行命令

### 后端（Maven）

```bash
# 完整构建（跳过测试）
mvn clean install -DskipTests

# 仅编译不打包
mvn clean compile

# 运行某个模块的测试
mvn test -pl yudao-module-system

# 运行单个测试类
mvn test -pl yudao-module-im -Dtest=ImWebSocketServiceImplTest

# 运行单个测试方法
mvn test -pl yudao-module-im -Dtest=ImWebSocketServiceImplTest#testSendPrivateMessageAsync_exceptionSwallowed

# 启动应用（主入口）
mvn spring-boot:run -pl yudao-server

# 从某个失败模块恢复构建
mvn install -DskipTests -rf :yudao-server
```

### 前端（Vue3）

```bash
cd yudao-ui/yudao-ui-admin-vue3
pnpm install
pnpm dev          # 开发模式
pnpm build:prod   # 生产构建
pnpm lint         # ESLint 检查
```

> 注意：项目根目录 `yudao-ui/yudao-ui-admin-vue3/.npmrc` 已通过 `onlyBuiltDependencies` 放行了 `@parcel/watcher`、`@swc/core`、`core-js`、`core-js-pure`、`es5-ext`、`esbuild` 等原生编译包。如果 `pnpm install` 报 `ERR_PNPM_IGNORED_BUILDS`，需在该文件中添加对应包名。

## 架构设计

### 模块层次

```
yudao-dependencies     — BOM 依赖版本管理（所有第三方版本在此锁定）
yudao-framework/       — 技术组件层（Spring Boot Starter 封装）
  yudao-common           — 通用 POJO、异常、工具类
  yudao-spring-boot-starter-web       — Web 层封装（全局异常、API 返回格式）
  yudao-spring-boot-starter-security  — 权限认证（Spring Security + Token）
  yudao-spring-boot-starter-mybatis   — MyBatis Plus 增强（BaseMapperX、BaseDO）
  yudao-spring-boot-starter-redis     — Redis + Redisson 封装
  yudao-spring-boot-starter-mq        — 消息队列（Redis Stream/Pub-Sub）
  yudao-spring-boot-starter-job       — 定时任务
  yudao-spring-boot-starter-excel     — Excel 导入导出
  yudao-spring-boot-starter-test      — 测试基础设施（BaseMockitoUnitTest 等）
  yudao-spring-boot-starter-biz-tenant         — 多租户
  yudao-spring-boot-starter-biz-data-permission — 数据权限
  yudao-spring-boot-starter-biz-ip             — IP 地区解析
  yudao-spring-boot-starter-protection         — 分布式锁、幂等、限流
  yudao-spring-boot-starter-websocket          — WebSocket 支持
yudao-module-system    — 系统管理（用户、角色、菜单、租户、字典等）
yudao-module-infra     — 基础设施（代码生成、文件存储、API 日志、定时任务等）
yudao-module-im        — 即时通讯（好友、群组、消息、频道）
yudao-module-bpm       — 工作流（Flowable）— 默认注释
yudao-module-report    — 报表 — 默认注释
yudao-server           — 应用入口（空壳容器，通过依赖组合启用模块）
```

### 启用/禁用模块

模块的启用需要**两处同步配置**：
1. 根 `pom.xml` 的 `<modules>` 中取消注释对应 `<module>`
2. `yudao-server/pom.xml` 的 `<dependencies>` 中取消注释对应依赖

如果只在一处启用而另一处遗漏，会导致 Maven 构建失败（依赖找不到）。

### 版本管理

- 项目版本通过根 `pom.xml` 的 `<revision>` 属性统一管理（当前为 `2026.05-SNAPSHOT`）
- `flatten-maven-plugin` 会在 `process-resources` 阶段将 `${revision}` 展开到 `.flattened-pom.xml`
- 所有第三方依赖版本在 `yudao-dependencies/pom.xml` 中集中锁定

### 业务模块内部结构

每个 `yudao-module-xxx` 遵循统一的包结构（以 `cn.iocoder.yudao.module.xxx` 为根包）：

```
controller/admin/    — REST API 控制器（admin 后台接口）
  └── vo/            — 请求/响应 VO 对象（XxxSaveReqVO, XxxxRespVO, XxxPageReqVO）
service/             — 业务逻辑层
dal/                 — 数据访问层
  ├── dataobject/    — DO 对象（继承 BaseDO）
  └── mysql/         — MyBatis Mapper 接口（继承 BaseMapperX）
convert/             — MapStruct 转换器（DO ↔ VO 转换）
enums/               — 枚举常量
api/                 — 模块间 API 接口定义（供其他模块调用）
framework/           — 模块级配置类
```

### 核心基类与约定

- **BaseDO** — 所有 DO 实体的基类，提供 `createTime`、`updateTime`、`creator`、`updater`、`deleted`（逻辑删除）字段，自动填充
- **BaseMapperX\<T\>** — MyBatis Plus BaseMapper 增强，提供分页查询、批量插入等便捷方法
- **CommonResult\<T\>** — 统一 API 返回格式，包含 `code`、`msg`、`data`
- **PageParam / PageResult** — 分页请求与响应
- **ErrorCode** — 错误码体系，每个模块定义自己的错误码常量

### 关键配置

- `lombok.config`（项目根目录）：启用 `chain=true`（链式 setter）、`toString/equals` 调用 super
- 编译配置：`-parameters` 参数已启用（Spring Boot 3.2+ 参数名发现）
- 注解处理器链：spring-boot-configuration-processor → lombok → lombok-mapstruct-binding → mapstruct-processor

## 测试规范

### 测试基类

项目提供三种测试基类（位于 `yudao-spring-boot-starter-test`）：

| 基类 | 用途 | 特点 |
|------|------|------|
| `BaseMockitoUnitTest` | 纯 Mockito 单元测试 | 不启动 Spring 容器，速度最快 |
| `BaseDbUnitTest` | 依赖数据库的测试 | 使用 H2 内存数据库，自动建表，每个测试后自动清理 |
| `BaseRedisUnitTest` | 依赖 Redis 的测试 | 使用内嵌 jedis-mock |

### 测试文件位置

- 测试代码位于各模块的 `src/test/java/` 下，包结构与主代码一致
- `BaseDbUnitTest` 使用的 SQL 清理脚本：`src/test/resources/sql/clean.sql`
- 测试 Profile：`unit-test`（使用 `application-unit-test` 配置文件）

### 运行测试

```bash
# 全量测试
mvn test

# 跳过测试构建
mvn install -DskipTests

# 单模块测试
mvn test -pl yudao-module-system

# 单个测试类
mvn test -pl yudao-module-system -Dtest=UserServiceTest

# 单个测试方法
mvn test -pl yudao-module-system -Dtest=UserServiceTest#testCreateUser
```

## 前端项目结构

前端位于 `yudao-ui/yudao-ui-admin-vue3/`，基于 Vue3 + TypeScript + Element Plus + Vite：

```
src/
  api/        — 后端 API 接口定义（按模块组织）
  views/      — 页面组件（按模块组织）
  components/ — 公共组件
  layout/     — 布局组件
  store/      — Pinia 状态管理
  router/     — 路由配置
  utils/      — 工具函数
```

## 数据库

- 主数据库：MySQL（支持 Oracle、PostgreSQL、SQL Server、达梦 DM、人大金仓等）
- SQL 脚本位于 `sql/mysql/` 目录（`ruoyi-vue-pro.sql` 为主库初始化脚本）
- 定时任务表：`sql/mysql/quartz.sql`
- 多数据库适配脚本分别在 `sql/db2/`、`sql/dm/`、`sql/kingbase/`、`sql/oracle/`、`sql/postgresql/`、`sql/sqlserver/`、`sql/opengauss/`

## 注意事项

- 项目使用 `allow-circular-references: true`（三层架构无法完全避免循环依赖）
- 默认激活的 Spring Profile 是 `local`（`application-local.yaml`）
- Maven 仓库使用华为云 + 阿里云镜像加速下载
- WebSocket 推送采用事务感知模式（`executeAfterTransaction`），事务提交后才异步发送，避免客户端收到消息时数据尚未落盘
