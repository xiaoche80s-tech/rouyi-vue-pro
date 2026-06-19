# OpsHub 角色化首页仪表盘设计方案

## Context

当前系统首页（`Index.vue`）仅包含一句欢迎语和头像，无法为不同角色的用户提供有价值的业务信息概览。本方案将首页改造为**角色感知的业务仪表盘**，根据登录用户的角色自动展示其关注的核心业务指标、待办事项和快捷操作入口。

系统已有 5 种角色（super_admin / brand_admin / brand_sales / service_executor / dealer）和 6 大业务模块的统计 API（签约、订单、售后、工单、咨询），数据权限机制 `DealerDataPermissionRule` 已就绪，统计查询会自动按角色过滤数据。

---

## 各角色首页信息设计

### super_admin / brand_admin（管理概览型）

| 区域 | 内容 | 数据来源 |
|------|------|---------|
| KPI 卡片行 | 合同总数、签署率、订单总量、总金额、售后完成率、待处理工单数 | signing + order + aftersale + csTask |
| 签约趋势图 | 折线图：按月展示各类型合同签署趋势 | signing.trend |
| 签约类型分布 | 饼图：主合同/政策/补充/终止占比 | signing.statistics |
| 订单状态概览 | 柱状图：已付款/未付款/已开票/未开票 | order.statistics |
| 售后进度 | 环形图：已完成 vs 未完成 | aftersale.statistics |
| 客服工作负荷 | 数字面板：工单待办/咨询待处理/操作请求待处理 | csTask + csConsult + csOpReq |
| 快捷操作 | "导入合同"、"创建工单" | 路由跳转 |

### brand_sales（只读观察型）

与 brand_admin 相同的图表区域，但**移除客户服务面板**（无 csTask/csConsult/csOpReq），无操作按钮。

### service_executor（工作台型）

| 区域 | 内容 | 数据来源 |
|------|------|---------|
| 我的待办 KPI | 待接单工单、待回复咨询、待处理操作请求 | csTask.tab-counts + csConsult + csOpReq |
| 签约/售后概览 | 小型卡片：合同总数/签署率、售后总数/完成率 | signing + aftersale |
| 工单状态分布 | 饼图 | csTask.tab-counts |
| 咨询队列状态 | 条形图 | csConsult.statistics |
| 快捷入口 | "进入工单列表"、"进入咨询工作台"、"进入操作请求" | 路由跳转 |

### dealer（经销商自助型）

| 区域 | 内容 | 数据来源 |
|------|------|---------|
| 我的业务 KPI | 我的合同数、签署中、待付款订单、进行中售后 | signing + order + aftersale |
| 签约/订单/售后状态 | 卡片组：已签署/签署中、未付款/未开票、售后完成率 | 各模块 statistics |
| 操作请求追踪 | 待处理/处理中数量 | csOpReq.statistics |
| 快捷操作 | "申请付款"、"申请开票"、"发起退货"、"发起咨询" | 路由跳转 |

---

## Task 1: 新增操作请求统计 API

操作请求（CsOpReq）缺少统计接口，需补充。

### 新建文件
- `yudao-module-opshub/.../controller/admin/cs/vo/CsOpReqStatisticsRespVO.java`
  - 字段: totalCount, pendingCount, inProgressCount, pendingVerifyCount, completedCount

### 修改文件
- `CsOpReqService.java` — 新增 `CsOpReqStatisticsRespVO getStatistics()` 方法
- `CsOpReqServiceImpl.java` — 实现统计逻辑（复用现有 resolveViewScope + 按 status 分组计数）
- `CsOpReqController.java` — 新增 `GET /opshub/cs-opreq/statistics` 端点

---

## Task 2: 创建 Dashboard 聚合后端

### 新建文件

**DashboardRespVO.java** — `yudao-module-opshub/.../controller/admin/dashboard/vo/DashboardRespVO.java`

```
DashboardRespVO
├── role: String                        // 当前角色 code
├── signing: SigningBlock | null
│   ├── statistics: SigningContractStatisticsRespVO
│   └── trend: List<SigningContractTrendRespVO>
├── order: OrderBlock | null
│   └── statistics: OrderStatisticsRespVO
├── aftersale: AfterSaleBlock | null
│   └── statistics: AfterSaleStatisticsRespVO
├── csTask: CsTaskBlock | null
│   └── tabCounts: Map<String, Long>
├── csConsult: CsConsultBlock | null
│   └── statistics: CsConsultStatisticsRespVO
├── csOpReq: CsOpReqBlock | null
│   └── statistics: CsOpReqStatisticsRespVO
└── quickActions: List<QuickAction>
    └── QuickAction { label, icon, route }
```

**DashboardService.java** + **DashboardServiceImpl.java** — 聚合逻辑：
- 注入: SigningContractService, OrderInfoService, AfterSaleInfoService, CsTaskService, CsSessionService, CsOpReqService
- 角色检测: 通过 PermissionApi 获取用户角色 code
- 优先级: super_admin > brand_admin > brand_sales > service_executor > dealer
- 按角色填充对应 Block（不需要的模块设为 null）
- 生成 quickActions 列表

**DashboardController.java** — `GET /opshub/dashboard`，无特殊权限注解（已登录即可访问，数据由各 Service 内部的数据权限自动过滤）

---

## Task 3: 前端组件开发

### 新建文件

**API 客户端**: `src/api/opshub/dashboard/index.ts`
- 定义 TypeScript 接口和 `getDashboard()` 函数

**组件** (`src/views/Home/components/`):

| 组件 | 功能 |
|------|------|
| `DashboardHeader.vue` | 欢迎横幅 + 角色标签 |
| `StatCard.vue` | 通用统计卡片（数字动画 + 图标 + 颜色主题） |
| `SigningTrendChart.vue` | 签约趋势折线图（ECharts line） |
| `SigningPieChart.vue` | 签约类型分布饼图（ECharts pie） |
| `OrderBarChart.vue` | 订单状态柱状图（ECharts bar） |
| `AftersaleDonutChart.vue` | 售后进度环形图（ECharts pie donut） |
| `CsWorkloadPanel.vue` | 客服工作负荷面板（工单/咨询/操作请求数字） |
| `QuickActionsPanel.vue` | 快捷操作按钮面板 |

---

## Task 4: 重写首页入口

### 修改文件
`src/views/Home/Index.vue` — 完全重写

核心逻辑:
1. 调用 `getDashboard()` 获取角色化数据
2. 根据 `dashboard.role` 和 Block 是否为 null 条件渲染组件
3. 响应式布局（el-row/el-col，适配移动端）

布局结构:
```
<DashboardHeader />
<StatCardRow>  — 4~6 张 KPI 卡片
<el-row> — 签约趋势图 + 签约类型饼图（各占 50%）
<el-row> — 订单柱状图 + 售后环形图（各占 50%）
<CsWorkloadPanel v-if="有客服数据" />
<QuickActionsPanel v-if="有快捷操作" />
```

---

## Task 5: 验证

- 分别以 5 种角色登录，验证首页展示内容是否符合角色权限
- 验证数据权限过滤是否生效（dealer 只能看到授权经销商的数据）
- 验证 brand_sales 不显示客户服务区域
- 验证快捷操作按钮的路由跳转
- 验证响应式布局在不同屏幕宽度下的表现

---

## 注意事项

- **政策看板模块尚未实现后端**，首页暂不包含政策看板区域，后续实现后补充
- **多角色用户**取优先级最高的角色展示
- 所有统计查询复用现有 Service，`DealerDataPermissionRule` 自动注入数据过滤条件
- ECharts 图表使用按需引入，避免全量加载影响性能
- `Index2.vue` 保留不动作为备用
