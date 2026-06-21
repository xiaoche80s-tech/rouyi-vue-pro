该项目采用前后端分离的依赖管理策略，后端基于 Maven 多模块架构，前端基于 pnpm 工作区。

### 1. 后端依赖管理 (Java/Maven)
- **核心机制**：采用 **Maven BOM (Bill of Materials)** 模式进行统一版本控制。根目录下的 `yudao-dependencies` 模块作为中央依赖清单，定义了所有第三方库（如 Spring Boot, MyBatis-Plus, Flowable 等）的版本号。
- **版本协调**：使用 `${revision}` 属性配合 `flatten-maven-plugin` 插件，实现多模块版本的统一管理（当前版本为 `2026.05-SNAPSHOT`），避免了在多模块项目中手动同步版本的繁琐。
- **仓库配置**：在根 `pom.xml` 中配置了华为云和阿里云 Maven 镜像源，以加速国内环境下的依赖下载。
- **关键依赖**：
  - 基础框架：Spring Boot 3.5.x
  - 持久层：MyBatis-Plus 3.5.x, Dynamic-Datasource
  - 工作流：Flowable 7.2.0
  - 工具类：Hutool, Lombok, MapStruct

### 2. 前端依赖管理 (Vue3/pnpm)
- **包管理器**：强制使用 **pnpm**（要求版本 >= 8.6.0），通过 `pnpm-lock.yaml` 锁定依赖树，确保构建的一致性。
- **依赖结构**：位于 `yudao-ui/yudao-ui-admin-vue3` 目录下，采用标准的 Vue3 + Vite + TypeScript 技术栈。
- **关键依赖**：
  - 核心框架：Vue 3.5.x, Pinia, Vue Router 5.x
  - UI 组件：Element Plus 2.13.x
  - 构建工具：Vite 8.x, UnoCSS
  - 业务组件：BPMN.js (工作流设计), LiveKit (实时音视频)

### 3. 开发规范与约束
- **后端**：所有子模块应通过 `yudao-dependencies` 引入依赖，禁止在子模块中硬编码版本号，以防止依赖冲突。
- **前端**：提交代码前需确保 `pnpm-lock.yaml` 已更新，并通过 `pnpm install` 保持本地环境与锁文件一致。
- **环境要求**：后端需 JDK 17+，前端需 Node.js 20.19+。