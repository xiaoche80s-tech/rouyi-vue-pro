# Step 7 — 电商客服系统实施 PRD

> **版本**: V1.0 | **日期**: 2026-06-16  
> **文档性质**: 分阶段实施 PRD — Step 7（电商客服系统 / 在线咨询聊天）  
> **前置文档**: `docs/PRD-Step5-售后模块.md`（Step 5）、`docs/PRD-Step6-客户服务模块.md`（Step 6，如有）、`docs/PRD-Step4-订单模块.md`（Step 4）、`docs/PRD-Step3-签约进度模块.md`（Step 3）、`docs/PRD-Step2-基础数据模块.md`（Step 2）、`docs/PRD-Step1-角色菜单经销商产品线.md`（Step 1）  
> **输入来源**: PRD V1.0 模块六（客户服务）+ PRD V2.0 在线客服聊天系统（第十章）+ PRD V2.0 咨询队列（9.2 节）+ 跨模块联动（第十一章）

---

## 一、Step 7 目标

在 Step 1-6 基础设施层之上，实现**电商客服系统**（在线咨询聊天），支持经销商与执行员之间的实时会话通讯，并**持久化保存每次会话的所有聊天记录**：

| 目标 | 说明 |
|------|------|
| 会话数据模型 | `ops_cs_session` 会话主表 + `ops_cs_message` 消息记录子表 |
| 6 种咨询类型 | signing（签约）/ policy（政策）/ aftersale（售后）/ order（订单）/ basedata（基础数据）/ other（其他） |
| 会话生命周期 | 创建 → 待处理 → 处理中 → 已完成/已关闭，支持去重（相同类型+上下文不重复创建） |
| 消息持久化 | 每条消息（文本/附件/系统消息）均持久化到 `ops_cs_message`，支持历史消息加载 |
| 实时通讯 | 复用 `CsWebSocketService` + `WebSocketSenderApi`，在线实时推送 + 离线站内信双通道 |
| 上下文关联 | 从各模块发起咨询时自动携带业务上下文（合同号/订单号/售后单号等） |
| 附件功能 | 复用 Step 6 `ops_cs_attachment` 通用附件表，支持文件/图片/音视频/链接附件 |
| 咨询队列 | 客户服务模块新增"咨询队列"Tab，执行员可查看所有待处理/处理中咨询 |
| 跨模块入口 | 签约/售后/订单/基础数据模块的"💬 咨询"按钮统一接入客服聊天窗口 |
| 数据权限 | 注册 `ops_cs_session` 到 `DealerDataPermissionRule`（dealer_code + product_line_code） |

**本阶段不包含**：AI 智能客服自动回复（当前仅人工客服 + 模拟 AI 回复骨架）、语音/视频通话、聊天消息已读回执、消息撤回、聊天记录导出（均划归后续阶段）。

---

## 二、数据模型

### 2.1 会话主表 — ops_cs_session

继承 `TenantBaseDO`，PostgreSQL 语法。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_cs_session_seq`） |
| `session_no` | varchar(30) | Y | 会话编号（唯一），如 `CS-20260616-001` |
| `consult_type` | varchar(20) | Y | 咨询类型：`signing` / `policy` / `aftersale` / `order` / `basedata` / `other` |
| `status` | int | Y | 状态：`0`=待处理 / `1`=处理中 / `2`=已完成 / `3`=已关闭 |
| `context` | varchar(500) | N | 咨询上下文（如"合同 MC-2026-001"、"售后单 SO20260615-001"） |
| `context_id` | int8 | N | 上下文关联业务 ID（合同 ID / 订单 ID / 售后单 ID 等） |
| `context_code` | varchar(50) | N | 上下文关联业务编号（冗余存储，展示用） |
| `source_module` | varchar(20) | Y | 来源模块：`signing` / `order` / `aftersale` / `basedata` / `policy` / `manual` |
| `dealer_code` | varchar(50) | Y | 经销商编码（数据权限用） |
| `dealer_name` | varchar(100) | Y | 经销商名称（冗余存储） |
| `product_line_code` | varchar(50) | N | 产品线编码（数据权限用） |
| `product_line_name` | varchar(100) | N | 产品线名称（冗余存储） |
| `initiator_id` | int8 | Y | 发起人用户 ID |
| `initiator_name` | varchar(50) | Y | 发起人姓名（冗余存储） |
| `assignee_id` | int8 | N | 当前处理人（执行员）用户 ID |
| `assignee_name` | varchar(50) | N | 当前处理人姓名（冗余存储） |
| `last_message` | varchar(500) | N | 最后一条消息摘要（列表展示用） |
| `last_message_time` | timestamp | N | 最后一条消息时间 |
| `message_count` | int | Y | 消息总数（默认 0） |
| `accept_time` | timestamp | N | 接单时间（执行员首次回复时间） |
| `complete_time` | timestamp | N | 完成时间 |
| `close_time` | timestamp | N | 关闭时间（经销商主动关闭） |
| `solution_summary` | varchar(1000) | N | 解决方案摘要（完成处理时填写） |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

**索引**：

| 索引名 | 类型 | 列 |
|--------|------|----|
| `uk_ops_cs_session_no` | UNIQUE (partial) | session_no WHERE deleted = 0 |
| `idx_ops_cs_session_type` | INDEX | consult_type |
| `idx_ops_cs_session_status` | INDEX | status |
| `idx_ops_cs_session_dealer_code` | INDEX | dealer_code |
| `idx_ops_cs_session_pl_code` | INDEX | product_line_code |
| `idx_ops_cs_session_initiator` | INDEX | initiator_id |
| `idx_ops_cs_session_assignee` | INDEX | assignee_id |
| `idx_ops_cs_session_context` | INDEX | source_module, context_code |
| `idx_ops_cs_session_last_msg_time` | INDEX | last_message_time |

### 2.2 消息记录子表 — ops_cs_message

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_cs_message_seq`） |
| `session_id` | int8 | Y | 关联会话 ID（`ops_cs_session.id`） |
| `session_no` | varchar(30) | Y | 关联会话编号（业务字段冗余） |
| `sender_id` | int8 | Y | 发送人用户 ID |
| `sender_name` | varchar(50) | Y | 发送人姓名（冗余存储） |
| `sender_role` | varchar(20) | Y | 发送人角色标识：`dealer` / `executor` / `admin` / `system` |
| `message_type` | varchar(20) | Y | 消息类型：`text` / `attachment` / `system` / `link` |
| `content` | text | N | 文本内容（text 类型使用） |
| `attachment_ids` | varchar(500) | N | 附件 ID 列表（逗号分隔，attachment/link 类型使用） |
| `link_url` | varchar(500) | N | 链接地址（link 类型使用） |
| `link_title` | varchar(200) | N | 链接标题（link 类型使用） |
| `is_read` | boolean | Y | 是否已读（默认 false） |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

**索引**：

| 索引名 | 类型 | 列 |
|--------|------|----|
| `idx_ops_cs_message_session` | INDEX | session_id |
| `idx_ops_cs_message_session_no` | INDEX | session_no |
| `idx_ops_cs_message_sender` | INDEX | sender_id |
| `idx_ops_cs_message_create_time` | INDEX | create_time |

### 2.3 会话编号规则

会话编号格式为 `CS-{YYYYMMDD}-{seq}`（如 `CS-20260616-001`）。编码在 Service 层通过数据库序列 + 当日计数自动生成。

### 2.4 status 状态流转

```
                    ┌───────────┐
                    │  pending   │  ← 经销商/系统创建会话
                    │  待处理(0)  │
                    └─────┬─────┘
                          │ 执行员首次回复（自动接单）
                          ▼
                    ┌───────────┐
                    │ processing │  ← 执行员处理中
                    │  处理中(1)  │
                    └─────┬─────┘
                ┌─────────┼──────────┐
                │                    │
                ▼                    ▼
        ┌──────────────┐    ┌──────────────┐
        │  completed   │    │   closed     │
        │ 已完成(2)     │    │  已关闭(3)   │
        │ (执行员完成)  │    │ (经销商关闭) │
        └──────────────┘    └──────────────┘
```

| 触发条件 | 目标状态 | 说明 |
|----------|---------|------|
| 经销商创建咨询 | pending(0) | 初始状态 |
| 执行员首次回复 | processing(1) | 自动接单，填充 assignee_id 和 accept_time |
| 执行员点击"完成处理" | completed(2) | 需填写解决方案摘要 + 上传解决方案附件 |
| 经销商点击"关闭对话" | closed(3) | 经销商主动关闭，归档不可恢复 |

### 2.5 咨询类型与来源模块映射

| consult_type | 对应 source_module | 上下文内容 | 对应菜单 |
|---|---|---|---|
| `signing` | signing | 合同编号 | 签约进度 |
| `policy` | policy | 政策编码 | 政策看板 |
| `aftersale` | aftersale | 售后单号 | 售后模块 |
| `order` | order | 订单号 | 订单模块 |
| `basedata` | basedata | 文件编号 | 基础数据 |
| `other` | manual | 无/自定义 | 浮动按钮 / 客户服务 |

### 2.6 消息类型定义

| message_type | 说明 | 使用字段 |
|---|---|---|
| `text` | 纯文本消息 | content |
| `attachment` | 文件附件消息 | attachment_ids → `ops_cs_attachment` |
| `system` | 系统消息（如转人工、状态变更） | content |
| `link` | 链接分享 | link_url, link_title |

### 2.7 去重规则

相同 `consult_type` + `context_code` + `status IN (0, 1)` 的活跃咨询不允许重复创建。若已存在，直接返回已有会话 ID 并打开聊天窗口。

### 2.8 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 自建客服会话层 | 不复用 IM 模块 | opshub 客服需关联业务上下文（合同/订单/售后），IM 模块面向通用社交场景，耦合代价高 |
| session + message 双表 | 不使用 JSON 字段存消息 | 消息需独立查询、分页、按时间排序；需支持全文搜索；JSON 字段无法满足 |
| 消息全量持久化 | 每条消息写 DB | 需求明确要求"保存每次会话所有聊天记录"，支持历史消息回溯和审计 |
| sender_role 独立存储 | 不使用 sender_id 反查角色 | 消息展示高频使用，避免每次 JOIN 查询用户表 |
| last_message / message_count | 冗余在主表 | 列表展示高频使用，避免每次子查询聚合 |
| 复用 ops_cs_attachment | 不新建消息附件表 | Step 6 已建通用附件表，module='message' 即可区分 |
| WebSocket 双通道 | 复用 CsWebSocketService | 已有事务感知推送基础设施，追加聊天消息推送方法即可 |

---

## 三、后端实现

### 3.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `enums/CsConsultTypeEnum.java` | 咨询类型枚举 |
| 2 | `enums/CsSessionStatusEnum.java` | 会话状态枚举 |
| 3 | `enums/CsMessageTypeEnum.java` | 消息类型枚举 |
| 4 | `enums/CsSenderRoleEnum.java` | 发送人角色枚举 |
| 5 | `dal/dataobject/cs/CsSessionDO.java` | 会话主表 DO |
| 6 | `dal/dataobject/cs/CsMessageDO.java` | 消息记录 DO |
| 7 | `dal/mysql/cs/CsSessionMapper.java` | 会话 Mapper |
| 8 | `dal/mysql/cs/CsMessageMapper.java` | 消息 Mapper |
| 9 | `controller/admin/cs/vo/CsSessionCreateReqVO.java` | 创建会话请求 VO |
| 10 | `controller/admin/cs/vo/CsSessionPageReqVO.java` | 会话分页请求 VO |
| 11 | `controller/admin/cs/vo/CsSessionRespVO.java` | 会话响应 VO |
| 12 | `controller/admin/cs/vo/CsSessionDetailRespVO.java` | 会话详情响应 VO（含消息列表） |
| 13 | `controller/admin/cs/vo/CsMessagePageReqVO.java` | 消息分页请求 VO |
| 14 | `controller/admin/cs/vo/CsMessageRespVO.java` | 消息响应 VO |
| 15 | `controller/admin/cs/vo/CsMessageSendReqVO.java` | 发送消息请求 VO |
| 16 | `controller/admin/cs/vo/CsSessionCompleteReqVO.java` | 完成处理请求 VO |
| 17 | `controller/admin/cs/vo/CsConsultStatisticsRespVO.java` | 咨询统计响应 VO |
| 18 | `service/cs/CsSessionService.java` | 会话 Service 接口 |
| 19 | `service/cs/impl/CsSessionServiceImpl.java` | 会话 Service 实现 |
| 20 | `service/cs/CsMessageService.java` | 消息 Service 接口 |
| 21 | `service/cs/impl/CsMessageServiceImpl.java` | 消息 Service 实现 |
| 22 | `controller/admin/cs/CsSessionController.java` | 会话 REST Controller |
| 23 | `controller/admin/cs/CsMessageController.java` | 消息 REST Controller |
| 24 | `service/cs/websocket/dto/CsChatMessage.java` | 聊天消息 WebSocket 推送 DTO |

> 所有文件在 `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/` 下

### 3.2 修改文件清单

| # | 文件路径 | 修改内容 |
|---|---------|---------|
| 1 | `enums/ErrorCodeConstants.java` | 追加客服会话模块错误码（1-050-009-xxx 段） |
| 2 | `framework/datapermission/config/OpshubDataPermissionConfiguration.java` | 注册 `ops_cs_session` 的 dealer_code + product_line_code |
| 3 | `service/cs/websocket/CsWebSocketService.java` | 追加聊天消息推送方法 |
| 4 | `service/cs/websocket/impl/CsWebSocketServiceImpl.java` | 实现聊天消息推送 |

### 3.3 错误码定义

```java
// ========== 客服会话模块 1-050-009-xxx ==========
ErrorCode CS_SESSION_NOT_EXISTS           = new ErrorCode(1_050_009_000, "咨询会话不存在");
ErrorCode CS_SESSION_DUPLICATE            = new ErrorCode(1_050_009_001, "该上下文已有活跃的咨询会话");
ErrorCode CS_SESSION_ALREADY_COMPLETED    = new ErrorCode(1_050_009_002, "咨询会话已完成，不可继续发送消息");
ErrorCode CS_SESSION_ALREADY_CLOSED       = new ErrorCode(1_050_009_003, "咨询会话已关闭");
ErrorCode CS_SESSION_NOT_ASSIGNABLE       = new ErrorCode(1_050_009_004, "该会话状态不允许此操作");
ErrorCode CS_MESSAGE_SEND_FORBIDDEN       = new ErrorCode(1_050_009_005, "无权发送消息（非会话参与方）");
ErrorCode CS_SESSION_COMPLETE_FORBIDDEN   = new ErrorCode(1_050_009_006, "仅执行员可完成处理");
ErrorCode CS_SESSION_CLOSE_FORBIDDEN      = new ErrorCode(1_050_009_007, "仅经销商可关闭对话");
```

### 3.4 REST API 接口

#### 会话管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/opshub/cs-session/create` | `dealer:cs-consult:query` | 创建/获取咨询会话（去重） |
| GET | `/opshub/cs-session/page` | `dealer:cs-consult:query` | 分页查询咨询列表 |
| GET | `/opshub/cs-session/get?id=` | `dealer:cs-consult:query` | 获取会话详情 |
| GET | `/opshub/cs-session/statistics` | `dealer:cs-consult:query` | 咨询统计数据（各状态数量） |
| PUT | `/opshub/cs-session/complete` | `dealer:cs-consult:complete` | 完成处理（执行员） |
| PUT | `/opshub/cs-session/close` | `dealer:cs-consult:close` | 关闭对话（经销商） |

#### 消息管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/opshub/cs-message/send` | `dealer:cs-consult:reply` | 发送消息 |
| GET | `/opshub/cs-message/page` | `dealer:cs-consult:query` | 分页查询历史消息（按时间正序） |
| GET | `/opshub/cs-message/latest` | `dealer:cs-consult:query` | 获取最新消息（长轮询备选） |
| PUT | `/opshub/cs-message/mark-read` | `dealer:cs-consult:query` | 标记消息已读 |

### 3.5 数据权限集成

在 `OpshubDataPermissionConfiguration` 中追加：

```java
// Step 7：注册客服会话表
rule.addDealerColumn("ops_cs_session");
rule.addProductLineColumn("ops_cs_session");
```

### 3.6 创建会话请求 VO

```java
@Data
public class CsSessionCreateReqVO {
    @NotNull
    private String consultType;       // signing/policy/aftersale/order/basedata/other
    @NotNull
    private String sourceModule;      // signing/order/aftersale/basedata/policy/manual
    private String context;           // 咨询上下文描述
    private Long contextId;           // 关联业务 ID
    private String contextCode;       // 关联业务编号
    private String productLineCode;   // 产品线编码
    private String productLineName;   // 产品线名称
    private String dealerCode;        // 经销商编码
    private String dealerName;        // 经销商名称
}
```

### 3.7 发送消息请求 VO

```java
@Data
public class CsMessageSendReqVO {
    @NotNull
    private Long sessionId;           // 会话 ID
    @NotNull
    private String messageType;       // text/attachment/link
    private String content;           // 文本内容
    private List<Long> attachmentIds; // 附件 ID 列表
    private String linkUrl;           // 链接地址
    private String linkTitle;         // 链接标题
}
```

### 3.8 会话分页请求 VO

```java
@Data @EqualsAndHashCode(callSuper = true)
public class CsSessionPageReqVO extends PageParam {
    private String consultType;       // 咨询类型筛选
    private String status;            // 状态筛选（逗号分隔多选）
    private String dealerCode;        // 经销商筛选
    private String productLineCode;   // 产品线筛选
    private String sourceModule;      // 来源模块筛选
    private String keyword;           // 模糊搜索（会话编号/上下文/经销商名称）
}
```

### 3.9 Service 核心逻辑

**创建/获取会话**：
1. 检查去重：相同 `consult_type` + `context_code` + `status IN (0, 1)` 是否已存在
2. 若存在 → 直接返回已有会话
3. 若不存在 → 生成 session_no（`CS-{YYYYMMDD}-{seq}`），创建会话记录
4. 创建系统消息（message_type=system）："咨询会话已创建"
5. WebSocket 通知：推送给所有执行员/管理员"新咨询待处理"

**发送消息**：
1. 校验会话存在且 status IN (0, 1)
2. 校验发送人权限（会话发起方或处理方）
3. 创建消息记录到 `ops_cs_message`
4. 更新会话主表：last_message、last_message_time、message_count
5. 若 status == pending(0) 且发送人为执行员 → 自动接单（status → processing，填充 assignee_id、accept_time）
6. WebSocket 推送消息给对方（事务感知）
7. 站内信通知（离线兜底）

**完成处理（执行员）**：
1. 校验 status == processing(1)
2. 更新 status → completed(2)，填充 complete_time、solution_summary
3. 创建系统消息："咨询已完成处理"
4. WebSocket 通知发起人
5. 站内信通知发起人

**关闭对话（经销商）**：
1. 校验 status IN (0, 1) 且操作人为发起人
2. 更新 status → closed(3)，填充 close_time
3. 创建系统消息："经销商已关闭对话"
4. WebSocket 通知处理人

### 3.10 CsWebSocketService 扩展

```java
// 新增方法
void sendChatMessageAsync(Long userId, CsChatMessage chatMessage);      // 推送聊天消息给指定用户
void sendSessionEventAsync(Long userId, CsTaskNotification event);     // 推送会话事件（新咨询/完成/关闭）
void broadcastNewConsult(CsTaskNotification notification);              // 广播新咨询通知给所有执行员
```

### 3.11 咨询统计数据 VO

```java
@Data
public class CsConsultStatisticsRespVO {
    private Integer totalCount;        // 全部咨询数
    private Integer pendingCount;      // 待处理
    private Integer processingCount;   // 处理中
    private Integer completedCount;    // 已完成
    private Integer closedCount;       // 已关闭
}
```

### 3.12 依赖

- `DealerInfoService`：校验经销商存在性
- `CsAttachmentService`（Step 6）：附件上传/查询
- `CsWebSocketService`（Step 6 已有）：实时推送
- `NotifyMessageSendApi`（infra）：站内信通知
- `WebSocketSenderApi`（infra）：WebSocket 推送

---

## 四、前端实现

### 4.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `src/api/opshub/csSession/index.ts` | 会话 API 接口定义 |
| 2 | `src/api/opshub/csMessage/index.ts` | 消息 API 接口定义 |
| 3 | `src/views/opshub/customerservice/consult/ConsultQueueTab.vue` | 咨询队列 Tab |
| 4 | `src/views/opshub/customerservice/consult/ConsultFilterBar.vue` | 咨询筛选栏 |
| 5 | `src/views/opshub/customerservice/consult/ConsultTable.vue` | 咨询列表表格 |
| 6 | `src/views/opshub/customerservice/consult/ConsultArchiveModal.vue` | 归档咨询查看弹窗 |
| 7 | `src/components/CsChatWindow/index.vue` | 通用聊天窗口组件（全局复用） |
| 8 | `src/components/CsChatWindow/ChatMessageList.vue` | 消息列表（含历史消息加载） |
| 9 | `src/components/CsChatWindow/ChatMessageItem.vue` | 单条消息渲染（文本/附件/系统/链接） |
| 10 | `src/components/CsChatWindow/ChatInputBar.vue` | 输入栏（文本+附件+链接） |
| 11 | `src/components/CsChatWindow/ChatAttachmentList.vue` | 附件列表组件 |
| 12 | `src/components/CsChatWindow/ChatFloatingButton.vue` | 右下角浮动💬按钮 |
| 13 | `src/hooks/useCsWebSocket.ts` | WebSocket 连接 Hook（接收聊天消息+会话事件） |

### 4.2 聊天窗口组件设计

```
┌──────────────────────────────────────────┐
│ 📝 签约咨询 — MC-2026-001          [×]   │
│ 经销商: 华康医疗器械 | ID: 200           │
├──────────────────────────────────────────┤
│ ─────── 2026-06-16 ───────              │
│                                          │
│ [系统] 咨询会话已创建                      │
│                                          │
│ [经销商] 合同 MC-2026-001 的签署流程    │
│          想了解下当前进度？               │
│                                  09:30   │
│                                          │
│ [执行员] 您好，该合同目前处于签署中      │
│          状态，预计本周内完成。           │
│                                  09:35   │
│                                          │
│ 📎 合同签署流程说明.pdf (1.2MB)          │
│                                  09:36   │
│                                          │
├──────────────────────────────────────────┤
│ [📎+] [🔗] [输入消息...]      [发送]    │
│                                          │
│ 待发送: [合同附件.pdf ×]                 │
├──────────────────────────────────────────┤
│ [👤 转人工客服] [📋 下发任务] [✅ 完成处理]│
└──────────────────────────────────────────┘
```

### 4.3 咨询队列 Tab（客户服务模块内）

```
┌──────────────────────────────────────────────────────────────┐
│ [全部咨询] [我创建的] [我处理的]                                │
├──────────────────────────────────────────────────────────────┤
│ [状态▼(待处理/处理中/已完成)] [类型▼(6种)] [重置]                │
├──────────────────────────────────────────────────────────────┤
│ □ | 咨询编号     | 类型   | 经销商    | 上下文          | 状态  │
│   | 创建时间  | 完成时间 | 用时  | 操作                          │
│   | [回应] [查看归档] [关闭(经销商)] [完成处理(执行员)]           │
├──────────────────────────────────────────────────────────────┤
│ 共 N 条咨询              第 1/N 页  每页 20 条                  │
└──────────────────────────────────────────────────────────────┘
```

### 4.4 关键交互

| 功能 | 实现 | 权限控制 |
|------|------|---------|
| 浮动💬按钮 | 右下角固定，点击打开聊天窗口（other 类型，无上下文） | 所有角色可见 |
| 各模块咨询按钮 | 点击自动创建咨询会话，携带 consult_type + context_code | `dealer:*:consult` |
| 聊天窗口打开 | 调用 `/opshub/cs-session/create`（去重），加载历史消息 | — |
| 发送文本消息 | 调用 `/opshub/cs-message/send`，WebSocket 实时推送 | `dealer:cs-consult:reply` |
| 附件上传 | 复用 Step 6 `ops_cs_attachment`，module='message' | — |
| 链接分享 | 输入 URL → 回车 → 创建 link 类型消息 | — |
| 历史消息加载 | 打开会话时自动加载全部历史消息（按时间正序） | — |
| 自动滚动 | 新消息自动滚动到底部 | — |
| 自动接单 | 执行员首次回复时自动 pending → processing | — |
| 完成处理 | 执行员点击，弹出解决方案表单，上传附件 | `dealer:cs-consult:complete` |
| 关闭对话 | 经销商点击，二次确认 | `dealer:cs-consult:close` |
| 查看归档 | 已完成/已关闭咨询查看完整对话记录 + 解决方案附件 | `dealer:cs-consult:query` |
| WebSocket 实时接收 | `useCsWebSocket` Hook 监听消息推送，自动追加到消息列表 | — |
| 导航角标 | 执行员视角下各模块导航显示未处理咨询数量 | — |
| 行高亮 | 执行员进入有待处理咨询的模块时，对应表格行高亮 | — |

### 4.5 WebSocket 消息类型

| type | 说明 | 推送对象 |
|------|------|---------|
| `cs-chat-message` | 新聊天消息 | 会话对方用户 |
| `cs-session-created` | 新咨询创建 | 所有执行员/管理员 |
| `cs-session-accepted` | 咨询已被接单 | 发起人 |
| `cs-session-completed` | 咨询已完成 | 发起人 |
| `cs-session-closed` | 咨询已关闭 | 处理人 |

### 4.6 菜单更新

客户服务模块内新增"咨询队列"子 Tab，不新增菜单项（复用 Step 6 已预留的 6086-6089 咨询按钮权限）。

### 4.7 跨模块咨询入口集成

以下模块需修改，将"💬 咨询"按钮的点击事件改为调用 `CsSessionService.create` 并打开聊天窗口：

| 模块 | 按钮位置 | consult_type | context 来源 |
|------|---------|---|---|
| 签约进度 | 合同列表行操作 | signing | contract_code |
| 售后模块 | 售后单列表行操作 | aftersale | aftersale_code |
| 订单模块 | 订单列表行操作 | order | order_code |
| 基础数据 | 文件列表行操作 | basedata | file_no |
| 政策看板 | 政策详情弹窗 | policy | policy_code |
| 客户服务 | 6 大分类卡片 | 对应类型 | 无上下文 |
| 全局 | 浮动💬按钮 | other | 无上下文 |

---

## 五、角色权限矩阵

### 5.1 按钮级权限

| 权限标识 | 说明 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|------|:---------:|:---------:|:----------:|:-----:|
| `dealer:cs-consult:query` | 查看咨询列表 + 详情 | ✅ | ✅ | ✅ | ✅ |
| `dealer:cs-consult:reply` | 发送消息（回应咨询） | ✅ | — | ✅ | ✅ |
| `dealer:cs-consult:close` | 关闭对话 | — | — | — | ✅ |
| `dealer:cs-consult:complete` | 完成处理 | ✅ | — | ✅ | — |

### 5.2 UI 联动规则

| UI 元素 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|-----------|-----------|-------------|-------|
| 咨询队列 Tab | 可见（全部咨询） | 可见（只读） | 可见（全部+我处理的） | 可见（我创建的） |
| "完成处理"按钮 | 显示 | 隐藏 | 显示 | 隐藏 |
| "关闭对话"按钮 | 隐藏 | 隐藏 | 隐藏 | 显示 |
| "回应"操作 | 显示 | 隐藏 | 显示 | 显示 |
| 聊天输入栏 | 可输入 | 只读 | 可输入 | 可输入 |
| 导航角标（未处理数） | 不显示 | 不显示 | 显示 | 不显示 |
| 行高亮（有待处理咨询） | 不高亮 | 不高亮 | 高亮 | 不高亮 |
| 浮动💬按钮 | 显示 | 显示 | 显示 | 显示 |

### 5.3 数据可见性规则

| 角色 | 咨询列表可见范围 |
|------|----------------|
| 经销商 | 仅自己发起的咨询（initiator_id = 当前用户） |
| 执行员 | 待处理咨询（status=0）+ 当前处理人为自己的咨询（assignee_id = 当前用户） |
| 管理员/销售员 | 全部咨询 |

该规则在 Service 层实现，与 `DealerDataPermissionRule` 进行 AND 叠加过滤。

---

## 六、SQL 脚本

### 6.1 DDL

PostgreSQL 语法：2 张 CREATE TABLE + UNIQUE INDEX + 多个 INDEX + SEQUENCE + COMMENT。

表清单：
1. `ops_cs_session` — 会话主表
2. `ops_cs_message` — 消息记录子表

### 6.2 DML

- 更新客户服务菜单 id=6008 的 component（如有变化）
- 确保 6086-6089 咨询按钮权限已正确分配给各角色：
  - 6086（查看咨询）→ 4 角色均有
  - 6087（回复咨询）→ brand_admin(157) / service_executor(159) / dealer(160)
  - 6088（关闭咨询）→ dealer(160) only
  - 6089（完成处理）→ brand_admin(157) / service_executor(159)
- 测试数据约 10-15 条会话 + 每条会话 3-10 条消息，覆盖：
  - 6 种咨询类型
  - 4 种状态混合（pending/processing/completed/closed）
  - 多个经销商 × 多个产品线
  - 不同消息类型（text/attachment/system/link）
  - 多种上下文关联（合同号/订单号/售后单号/文件编号/无上下文）
  - 完整的会话生命周期消息（创建→回复→完成/关闭的系统消息链）

### 6.3 归档路径

`db/branches/feature_step7-电商客服系统/`
- `feature_step7-电商客服系统_ddl.sql`
- `feature_step7-电商客服系统_dml.sql`

---

## 七、与现有模块的交互

| 关联模块 | 关系 | 说明 |
|---------|------|------|
| Step 1 经销商/产品线 | `dealer_code` / `product_line_code` | 数据权限过滤依赖扩展表 |
| Step 3 签约进度 | `context_code` → contract_code | 签约咨询上下文关联，咨询按钮入口 |
| Step 4 订单模块 | `context_code` → order_code | 订单咨询上下文关联，咨询按钮入口 |
| Step 5 售后模块 | `context_code` → aftersale_code | 售后咨询上下文关联，咨询按钮入口 |
| Step 2 基础数据 | `context_code` → file_no | 基础数据咨询上下文关联，咨询按钮入口 |
| Step 6 客户服务 | `ops_cs_attachment` | 复用通用附件表（module='message'） |
| Step 6 客户服务 | `ops_cs_task` | 聊天中"下发任务"创建工单 |
| Step 6 CsWebSocketService | WebSocket 推送 | 扩展聊天消息推送方法 |
| infra WebSocketSenderApi | 底层推送 | 已有 WebSocket 基础设施 |
| infra NotifyMessageSendApi | 站内信 | 离线通知兜底 |

---

## 八、验证方式

| 验证项 | 验证方法 |
|--------|---------|
| 表创建 | DDL 执行后 `SELECT * FROM ops_cs_session` 和 `ops_cs_message` 确认 2 张表存在 |
| 创建会话 | 从签约模块点击"💬 咨询"，验证自动创建会话并携带上下文 |
| 去重逻辑 | 同一合同重复点击咨询，验证返回已有会话而非创建新会话 |
| 发送消息 | 经销商发送文本消息，验证 DB 中 `ops_cs_message` 持久化记录 |
| 历史消息 | 重新打开会话，验证全部历史消息按时间正序加载 |
| 自动接单 | 执行员首次回复，验证 status 从 pending → processing，assignee_id 自动填充 |
| 完成处理 | 执行员点击完成，验证 status → completed，解决方案摘要保存 |
| 关闭对话 | 经销商点击关闭，验证 status → closed，不可再发消息 |
| WebSocket 推送 | 经销商发消息，执行员端实时收到（需两端同时在线测试） |
| 离线通知 | 执行员不在线时，验证站内信通知已发送 |
| 附件消息 | 上传文件后发送，验证 `ops_cs_attachment` 记录 + 消息中附件可下载 |
| 链接消息 | 输入 URL 发送，验证链接可点击跳转 |
| 系统消息 | 创建/完成/关闭时验证系统消息自动生成 |
| 数据权限 | dealer 仅看到自己的咨询；执行员看到待处理+自己的；管理员看到全部 |
| 咨询队列 | 客户服务模块咨询队列 Tab 列表正确展示，筛选功能正常 |
| 咨询统计 | `/opshub/cs-session/statistics` 验证各状态数量正确 |
| 跨模块入口 | 签约/售后/订单/基础数据/浮动按钮各入口均能正确创建对应类型咨询 |
| 消息全量持久化 | 发送 10+ 条消息后重新打开会话，验证所有消息均可加载（无遗漏） |
| 编译验证 | `mvn clean compile -pl yudao-module-opshub` 通过 |
| 前端验证 | `pnpm dev` 聊天窗口正常渲染（消息列表+输入栏+附件+历史加载） |

---

## 九、后续阶段预留

| 功能 | 说明 | 预留阶段 |
|------|------|--------|
| AI 智能客服 | 接入 AI 模型自动回复，NLP 意图识别，知识库问答 | 后续 Step |
| 已读回执 | 消息已读状态追踪，显示"对方已读" | 后续 Step |
| 消息撤回 | 2 分钟内撤回已发送消息 | 后续 Step |
| 聊天记录导出 | 导出咨询对话为 PDF/Excel | 后续 Step |
| 语音/视频通话 | WebRTC 实时音视频通话 | 后续 Step |
| 消息搜索 | 全文搜索历史消息内容 | 后续 Step |
| 快捷回复 | 执行员预设常用回复模板 | 后续 Step |
| 满意度评价 | 咨询结束后经销商评价服务质量 | 后续 Step |
| 智能分配 | 根据产品线/经销商自动分配执行员 | 后续 Step |
| 聊天消息分页加载 | 大量消息时按页加载（当前全量加载） | 后续 Step |

---

## 附录：与总体 PRD 的关系

```
┌────────────────────────────────────────────────────────────────────┐
│               经销商管理客服SaaS 分阶段实施                           │
├──────┬──────┬──────┬──────┬──────┬──────┬─────────────────────────┤
│Step1 │Step2 │Step3 │Step4 │Step5 │Step6 │        Step 7 ★ 当前    │
│(done)│(done)│(done)│(done)│(done)│(done)│                         │
├──────┼──────┼──────┼──────┼──────┼──────┼─────────────────────────┤
│角色  │基础  │签约  │订单  │售后  │客服  │ 电商客服系统              │
│菜单  │数据  │进度  │模块  │模块  │工单  │ · 会话+消息持久化         │
│权限  │文件  │合同  │统计  │统计  │操作  │ · 6种咨询类型             │
│经销商│AI解读│趋势  │6Tab  │进度  │请求  │ · WebSocket实时推送       │
│产品线│      │图表  │详情  │追踪  │附件  │ · 跨模块咨询入口          │
│授权  │      │签署  │付款  │9类型 │      │ · 聊天窗口组件            │
│      │      │      │开票  │      │      │ · 咨询队列Tab             │
├──────┴──────┴──────┴──────┴──────┴──────┴─────────────────────────┤
│  后续 Step：政策看板 / AI 智能客服 / 操作请求 BPM / 语音视频通话      │
└────────────────────────────────────────────────────────────────────┘
```
