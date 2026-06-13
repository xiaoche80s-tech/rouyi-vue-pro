# 芋道快速开发平台 — 启动指南

## 一、环境依赖

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 后端编译运行 |
| Maven | 3.8+ | 后端构建工具 |
| Node.js | 18+ | 前端编译运行 |
| pnpm | 9+ | 前端包管理 |
| PostgreSQL | 13+ | 数据库 |
| Redis | 6+ | 缓存 |
| MinIO | 可选 | 文件存储（S3 兼容） |

---

## 二、后端启动

### 2.1 初始化数据库

```bash
# 创建数据库
psql "postgresql://chedejun:abcd1234@1p.inas.club:15432" \
  -c "CREATE DATABASE \"gk_service_hub\" ENCODING 'UTF8';"

# 导入主库脚本（包含所有业务表 + 初始数据）
psql "postgresql://chedejun:abcd1234@1p.inas.club:15432/gk_service_hub" \
  -f sql/postgresql/ruoyi-vue-pro.sql

# 导入定时任务表（如需要）
psql "postgresql://chedejun:abcd1234@1p.inas.club:15432/gk_service_hub" \
  -f sql/postgresql/quartz.sql
```

### 2.2 配置文件

配置文件位于 `yudao-server/src/main/resources/application-local.yaml`，已预配置好：

| 中间件 | 地址 | 数据库/库 | 用户名 | 密码 |
|--------|------|-----------|--------|------|
| PostgreSQL | `1p.inas.club:15432` | `gk_service_hub` | `chedejun` | `abcd1234` |
| Redis | `1p.inas.club:6379` | db 0 | — | `abcd1234` |

### 2.3 编译 & 启动

```bash
# 完整构建（跳过测试，首次或依赖变更时执行）
mvn clean install -DskipTests

# 启动后端（端口 48080）
mvn spring-boot:run -pl yudao-server

# 或者在 IDE 中直接运行 yudao-server 模块下的 Application 主类
```

启动成功后访问：
- 后端 API：`http://localhost:48080`
- Swagger 文档：`http://localhost:48080/swagger-ui`

---

## 三、前端启动

### 3.1 安装依赖

```bash
cd yudao-ui/yudao-ui-admin-vue3

# 安装依赖（.npmrc 已配置构建脚本放行）
pnpm install
```

### 3.2 启动开发服务器

```bash
# 本地模式：前端请求 → http://localhost:48080（需先启动后端）
pnpm dev
```

前端默认使用 `.env.local` 配置，API 指向 `http://localhost:48080`。

如需指向远程后端，可使用：

```bash
# 开发环境模式：前端请求 → http://api-dashboard.yudao.iocoder.cn
pnpm dev-server
```

### 3.3 环境配置文件说明

| 文件 | 用途 | 后端地址 |
|------|------|---------|
| `.env.local` | 本地全栈开发（前端+后端都在本地） | `http://localhost:48080` |
| `.env.dev` | 仅启动前端，对接远程开发环境 | `http://api-dashboard.yudao.iocoder.cn` |
| `.env.prod` | 生产构建 | 按部署配置 |

---

## 四、MinIO 文件存储配置（可选）

MinIO 不在 YAML 中配置，启动后端后通过**管理后台**配置：

**管理后台 → 基础设施 → 文件管理 → 文件配置 → 新增**

| 字段 | 值 |
|------|------|
| 配置名 | 自定义（如 `MinIO`） |
| 存储器 | S3 |
| endpoint | `http://1p.inas.club:9000` |
| bucket | `yudao` |
| accessKey | `chedejun` |
| accessSecret | `abcd1234` |
| domain | `http://1p.inas.club:9000/yudao` |
| enablePathStyleAccess | `true`（MinIO 必须开启） |
| enablePublicAccess | `true` |
| region | `us-east-1` |

配置完成后点击 **「设为主配置」** 即可。

也可以直接通过 SQL 插入配置：

```sql
INSERT INTO infra_file_config (
    name, storage, master, config, creator, create_time, updater, update_time, deleted, tenant_id
) VALUES (
    'MinIO', 34, true,
    '{"@class":"cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig","endpoint":"http://1p.inas.club:9000","bucket":"yudao","accessKey":"chedejun","accessSecret":"abcd1234","domain":"http://1p.inas.club:9000/yudao","enablePathStyleAccess":true,"enablePublicAccess":true,"region":"us-east-1"}',
    'admin', NOW(), 'admin', NOW(), false, 1
);
```

---

## 五、登录管理后台

前端启动后访问 `http://localhost:80`（Vite 默认端口），使用默认账号登录：

| 用户名 | 密码 |
|--------|------|
| `admin` | `admin123` |

---

## 六、常见问题

### Q: `mvn install` 报找不到 `yudao-module-xxx`

根 `pom.xml` 的 `<modules>` 和 `yudao-server/pom.xml` 的 `<dependencies>` 必须同步启用。检查两处是否都取消了对应模块的注释。

### Q: 前端 `pnpm install` 报 `ERR_PNPM_IGNORED_BUILDS`

`yudao-ui/yudao-ui-admin-vue3/.npmrc` 已配置 `onlyBuiltDependencies` 放行了必要的原生编译包。如仍有报错，在 `.npmrc` 中追加对应包名。

### Q: 后端启动报 Redis/数据库连接失败

检查 `application-local.yaml` 中的连接地址、用户名、密码是否正确，以及远程服务器的防火墙是否开放了对应端口（PostgreSQL: 15432, Redis: 6379）。
