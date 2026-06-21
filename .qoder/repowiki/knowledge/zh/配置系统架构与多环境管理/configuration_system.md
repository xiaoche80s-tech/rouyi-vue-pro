## 1. 系统概述
本项目采用 **Spring Boot Profile**（后端）与 **Vite Environment Variables**（前端）相结合的配置管理体系。通过分层配置文件实现不同环境（本地、开发、生产）的隔离，并利用环境变量覆盖机制确保配置的灵活性。

## 2. 后端配置体系 (Spring Boot)
### 2.1 核心文件结构
- **`application.yaml`**: 全局基础配置。包含应用名称、Jackson 序列化规则、MyBatis Plus 全局策略、Flowable 工作流引擎配置、AI 模型密钥（OpenAI, 通义千问等）以及芋道框架特有的业务配置（如租户开关、API 加密密钥）。
- **`application-{profile}.yaml`**: 环境特定配置。通过 `spring.profiles.active` 激活。
  - `local`: 本地开发环境。默认激活，连接远程 PostgreSQL/Redis，关闭验证码以便调试，开启 Mock 安全模式。
  - `dev`: 开发/测试环境。连接本地 MySQL/Redis，开启 Quartz 定时任务。
  - `prod`: 生产环境（通常通过外部挂载或 CI/CD 注入）。

### 2.2 关键配置项
- **数据源**: 使用 `dynamic-datasource` 实现主从分离。`local` 环境使用 PostgreSQL，`dev` 环境使用 MySQL。
- **多租户**: 在 `yudao.tenant.enable` 中控制，支持忽略特定 URL 和表以实现全局访问。
- **安全与加密**: 内置 API 加解密配置（AES/RSA），密钥在 `application.yaml` 中定义，前端需保持一致。
- **消息队列**: 同时预留了 RocketMQ、RabbitMQ、Kafka 的配置占位，通过 `sender-type` 切换 WebSocket 消息发送方式。

## 3. 前端配置体系 (Vite + Vue3)
### 3.1 核心文件结构
- **`.env`**: 全局通用变量。定义应用标题、默认登录账号、API 加密算法及密钥。
- **`.env.dev` / `.env.prod`**: 环境特定变量。定义后端接口地址 (`VITE_BASE_URL`)、上传类型、打包路径及日志过滤规则。
- **`vite.config.ts`**: 构建配置。利用 `loadEnv` 加载对应环境的 `.env` 文件，并配置代理、别名及 SCSS 全局注入。

### 3.2 关键配置项
- **接口代理**: 开发环境下通过 `VITE_API_URL` 定义前缀，配合 Vite 的 `proxy` 选项（目前注释掉，依赖后端 CORS）解决跨域。
- **功能开关**: 通过 `VITE_APP_TENANT_ENABLE` 和 `VITE_APP_CAPTCHA_ENABLE` 动态控制前端 UI 展示。

## 4. 开发者规范
1. **敏感信息管理**: 严禁将生产环境的数据库密码、AI API Key 提交至 Git。建议使用环境变量或外部配置中心（如 Nacos）覆盖 `application.yaml` 中的默认值。
2. **环境切换**: 
   - 后端：修改 `application.yaml` 中的 `spring.profiles.active` 或通过启动参数 `--spring.profiles.active=dev` 指定。
   - 前端：通过 `npm run dev --mode dev` 或 `npm run build:prod` 指定环境。
3. **新增配置**: 
   - 后端：在 `application.yaml` 中添加默认值，并在对应的 Profile 文件中覆盖差异项。
   - 前端：在 `.env` 中定义 `VITE_` 开头的变量，并在 `src/config/axios/config.ts` 等位置引用。
4. **数据库适配**: 注意 `local` 与 `dev` 环境使用的数据库类型不同（PostgreSQL vs MySQL），编写 SQL 时需考虑兼容性或使用 MyBatis Plus 的动态方言支持。