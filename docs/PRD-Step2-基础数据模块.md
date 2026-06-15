# Step 2 — 基础数据模块实施 PRD

> **版本**: V1.0 | **日期**: 2026-06-14  
> **文档性质**: 分阶段实施 PRD — Step 2（基础数据模块）  
> **前置文档**: `docs/PRD-Step1-角色菜单经销商产品线.md`（Step 1）  
> **输入来源**: PRD V1.0 + PRD V2.0 基础数据模块部分

---

## 一、Step 2 目标

在 Step 1 基础设施层之上，实现**基础数据模块**的文件管理功能：

| 目标 | 说明 |
|------|------|
| 文件查询 | 按 4 大分类（资质/授权/合同/产品）分页查询文件列表 |
| 分类体系 | 14 种文件子类型的完整分类编码体系 |
| 文件下载 | 基于 infra FileService 的预签名 URL 下载 |
| AI 解读骨架 | AI 解读弹窗 UI 占位，不对接真实 AI 服务 |
| 数据权限 | 将 `ops_basedata_file` 表注册到 `DealerDataPermissionRule` |

**本阶段不包含**：文件上传、盖章申请联动（Step 3 客户服务）、真实 AI 解读对接。

---

## 二、数据模型

### 2.1 表结构 — ops_basedata_file

继承 `TenantBaseDO`，PostgreSQL 语法（与 Step 1 一致）。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_basedata_file_seq`） |
| `dealer_id` | int8 | Y | 经销商 ID（关联 `ops_dealer_info.id`） |
| `product_line_id` | int8 | N | 产品线 ID（关联 `ops_dealer_product_line.id`，可为空） |
| `category` | varchar(30) | Y | 文件分类：`qualification` / `authorization` / `contract` / `product` |
| `file_name` | varchar(200) | Y | 文件名称 |
| `file_type` | varchar(50) | Y | 文件子类型编码：BL/JYXK/BA/QMS/SQ/MC/POL/SA/TA/ZCZ/HGZ/SMS/JS/JCBG |
| `file_no` | varchar(30) | N | 文件编号（如 `BL-2024-001`） |
| `file_url` | varchar(500) | N | 文件地址（来自 FileService） |
| `file_size` | int8 | N | 文件大小（字节） |
| `expire_date` | date | N | 有效期至 |
| `status` | int2 | Y | 状态（0=正常, 1=停用） |
| `description` | varchar(500) | N | 文件描述 |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

**索引**：`dealer_id`、`product_line_id`、`category`

**有效期状态**（前端计算，不存储）：
- `valid`：`expire_date > CURRENT_DATE + 30天`
- `expiring_soon`：`expire_date <= CURRENT_DATE + 30天 AND >= CURRENT_DATE`
- `expired`：`expire_date < CURRENT_DATE`

### 2.2 文件分类体系

| 分类 (category) | 子类型 (file_type) | 编号前缀 | 说明 |
|:---:|:---:|:---:|------|
| qualification | BL | BL- | 营业执照 |
| qualification | JYXK | JYXK- | 经营许可证 |
| qualification | BA | BA- | 备案凭证 |
| qualification | QMS | QMS- | 质量体系认证 |
| authorization | SQ | SQ- | 品牌授权书 |
| contract | MC | MC- | 主合同 |
| contract | POL | POL- | 政策合同 |
| contract | SA | SA- | 补充协议 |
| contract | TA | TA- | 终止协议 |
| product | ZCZ | ZCZ- | 注册证 |
| product | HGZ | HGZ- | 合格证 |
| product | SMS | SMS- | 说明书 |
| product | JS | JS- | 技术文件 |
| product | JCBG | JCBG- | 检测报告 |

---

## 三、后端实现

### 3.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `enums/BasedataCategoryEnum.java` | 文件分类枚举 |
| 2 | `enums/BasedataFileTypeEnum.java` | 文件子类型枚举 |
| 3 | `enums/ErrorCodeConstants.java` | **修改** — 追加 4 个错误码 (1-050-004-xxx) |
| 4 | `dal/dataobject/basedata/BasedataFileDO.java` | DO 对象 |
| 5 | `dal/mysql/basedata/BasedataFileMapper.java` | Mapper 接口 |
| 6 | `controller/admin/basedata/vo/BasedataFilePageReqVO.java` | 分页请求 VO |
| 7 | `controller/admin/basedata/vo/BasedataFileRespVO.java` | 响应 VO |
| 8 | `controller/admin/basedata/vo/BasedataFileSaveReqVO.java` | 创建/修改 VO（预留） |
| 9 | `service/basedata/BasedataFileService.java` | Service 接口 |
| 10 | `service/basedata/impl/BasedataFileServiceImpl.java` | Service 实现 |
| 11 | `controller/admin/basedata/BasedataFileController.java` | REST Controller |

> 所有文件在 `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/` 下

### 3.2 修改文件清单

| # | 文件路径 | 修改内容 |
|---|---------|---------|
| 1 | `framework/datapermission/config/OpshubDataPermissionConfiguration.java` | 注册 `ops_basedata_file` 表的 dealer_id + product_line_id 列 |

### 3.3 REST API 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/opshub/basedata/page` | `dealer:basedata:query` | 分页查询（支持分类/文件名/经销商/有效期状态筛选） |
| GET | `/opshub/basedata/get?id=` | `dealer:basedata:query` | 获取详情 |
| GET | `/opshub/basedata/download-url?id=` | `dealer:basedata:download` | 获取预签名下载 URL |
| GET | `/opshub/basedata/category-count` | `dealer:basedata:query` | 获取各分类文件数量 |

### 3.4 数据权限集成

在 `OpshubDataPermissionConfiguration` 中为 `DealerDataPermissionRule` 注册：

```java
rule.addDealerColumn("ops_basedata_file");      // dealer_id 列
rule.addProductLineColumn("ops_basedata_file");  // product_line_id 列（可空）
```

**注意**：`product_line_id` 为 NULL 的行，在品牌角色按产品线过滤时会被排除。如需品牌角色也能看到无产品线绑定的文件，需在 `buildProductLineExpression` 中添加 `OR product_line_id IS NULL`。

### 3.5 依赖

- `FileApi`（`yudao-module-infra`）：生成预签名下载 URL，`pom.xml` 已包含该依赖
- `DealerInfoService` / `DealerProductLineService`：填充经销商/产品线名称

---

## 四、前端实现

### 4.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `src/api/opshub/basedata/index.ts` | API 接口定义 |
| 2 | `src/views/opshub/basedata/index.vue` | 主页面（分类 Tab + 筛选 + 表格） |
| 3 | `src/views/opshub/basedata/components/FileDetailModal.vue` | 文件明细弹窗 |
| 4 | `src/views/opshub/basedata/components/AiInterpretModal.vue` | AI 解读弹窗骨架 |

### 4.2 页面布局

```
┌──────────────────────────────────────────────┐
│ 分类 Tab：全部 | 资质文件 | 授权文件 | 合同文件 | 产品文件 │
├──────────────────────────────────────────────┤
│ 筛选栏：[文件名] [经销商▼] [有效期状态▼] [搜索] [重置]  │
├──────────────────────────────────────────────┤
│ [批量下载] [批量申请盖章]                           │
├──────────────────────────────────────────────┤
│ □ | 文件名称 | 编号 | 类型 | 经销商 | 产品线 |     │
│   | 有效期至 | 有效期状态 | 操作                    │
│   |                                          │
│   | 操作：[明细] [AI解读] [下载] [申请盖章]        │
├──────────────────────────────────────────────┤
│ 分页                                          │
└──────────────────────────────────────────────┘
```

### 4.3 关键交互

| 功能 | 实现 | 权限控制 |
|------|------|---------|
| 分类 Tab | `el-tabs` 切换 `queryParams.category`，触发重新查询 | `dealer:basedata:query` |
| 经销商筛选器 | `el-select` 多选，仅管理员可见 | 前端判断角色 |
| 有效期状态 | 彩色 Tag：valid=绿 / expiring_soon=橙 / expired=红 | — |
| 文件明细弹窗 | `el-dialog` 展示完整信息 + 底部操作按钮 | — |
| AI 解读弹窗 | UI 骨架 + "功能开发中"占位提示 | `dealer:basedata:ai` |
| 下载 | 调用 download-url API → `window.open(url)` | `dealer:basedata:download` |
| 批量下载 | 遍历选中行逐个下载 | `dealer:basedata:download` |
| 申请盖章 | 占位提示"将在 Step 3 客户服务模块中实现" | `dealer:basedata:stamp` |

### 4.4 菜单更新

Step 1 中菜单 id=6007 的 `component` 为空，需更新为 `opshub/basedata/index`。

---

## 五、SQL 脚本

### 5.1 DDL

PostgreSQL 语法，包含：CREATE TABLE、ALTER TABLE (PK)、CREATE INDEX (3 个)、CREATE SEQUENCE、COMMENT。

### 5.2 DML

- 更新菜单 component 路径
- 预置测试数据（约 10 条，覆盖 4 大分类 × 2 个经销商）

### 5.3 归档路径

按项目规范归档到：`db/branches/feature_step2-基础数据/`
- `feature_step2-基础数据_ddl.sql`
- `feature_step2-基础数据_dml.sql`

---

## 六、验证方式

| 验证项 | 验证方法 |
|--------|---------|
| 表创建 | 执行 DDL 后 `SELECT * FROM ops_basedata_file` 确认表存在 |
| 菜单更新 | 前端登录后确认"基础数据"菜单正确渲染并指向 `opshub/basedata/index` |
| 分页查询 | 4 种角色分别调用 `/opshub/basedata/page`，验证返回数据和分页 |
| 数据权限 | dealer 角色仅看到授权经销商的文件；执行员仅看到授权产品线的文件 |
| 分类筛选 | 切换分类 Tab 后查询结果按分类过滤 |
| 有效期状态 | 确认 valid/expiring_soon/expired 计算正确 |
| 文件下载 | 调用 download-url 接口获取 URL 并可打开 |
| AI 解读 | 点击按钮弹出骨架弹窗，显示"功能开发中" |
| 编译验证 | `mvn clean compile -pl yudao-module-opshub` 编译通过 |
| 前端验证 | `pnpm dev` 启动后页面正常渲染 |

---

## 七、后续阶段预留

| 功能 | 说明 | 预留阶段 |
|------|------|---------|
| 文件上传 | `BasedataFileSaveReqVO` 已定义，Controller 中 create/update 接口待实现 | Step 2+ |
| AI 解读对接 | `AiInterpretModal.vue` 骨架已建，后续对接 OCR+NLP | 后续 |
| 盖章申请 | 联动客户服务模块的操作请求（stamp 类型） | Step 3 |
| 批量盖章 | 依赖盖章申请 + 操作请求工作流 | Step 3 |
| 文件有效期预警通知 | 定时任务扫描即将到期文件 | 后续 |
