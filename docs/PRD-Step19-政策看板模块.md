# PRD-Step19-政策看板模块

## 概述

构建独立的政策看板模块，以多维度 KPI 仪表盘形式展示经销商政策执行情况。管理员可通过后端接口维护政策及指标数据，前端以 8 大指标面板 + 季度柱状图 + 目标分布 + 政策详情的三级钻取交互呈现。

## 变更背景

1. 当前政策数据嵌套在 `ops_signing_contract` 表的 `indicators` JSON 字段中，没有独立实体，无法独立维护
2. 前端没有独立的政策看板页面（菜单 ID 6004 已注册但无对应 Vue 文件）
3. PRD V2.0 要求政策看板支持筛选、8 大 KPI 面板、季度柱状图、三级钻取等完整交互

## 变更内容

### 1. 数据库表设计（P0）

新建两张表：`ops_dealer_policy`（政策主表）和 `ops_dealer_policy_indicator`（政策指标表）。

**ops_dealer_policy**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| dealer_id | BIGINT NOT NULL | 经销商 ID（关联 ops_dealer_info.id） |
| dealer_code | VARCHAR(50) | 经销商编码（数据权限用） |
| product_line_code | VARCHAR(50) | 产品线编码（数据权限用） |
| product_line_name | VARCHAR(100) | 产品线名称（冗余，便于展示） |
| policy_code | VARCHAR(30) NOT NULL | 政策编码（唯一，如 POL-2026-001） |
| policy_name | VARCHAR(200) NOT NULL | 政策名称 |
| policy_type | VARCHAR(20) NOT NULL | 政策类型：rebate/promotion/other |
| achievement_type | VARCHAR(20) NOT NULL | 达成类型：quarter（季度政策）/ month（月度政策） |
| policy_status | VARCHAR(20) NOT NULL DEFAULT 'executing' | 状态：executing/pending/completed |
| contract_code | VARCHAR(30) | 来源合同编码（关联 ops_signing_contract.contract_code） |
| contract_name | VARCHAR(200) | 来源合同名称 |
| policy_desc | TEXT | 政策描述 |
| source_contract_id | BIGINT | 来源政策合同 ID（关联 ops_signing_contract.id，可空） |
| policy_year | INT NOT NULL DEFAULT YEAR(CURRENT_DATE) | 政策年度（如 2026） |
| remark | VARCHAR(500) | 备注 |

**ops_dealer_policy_indicator**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| policy_id | BIGINT NOT NULL | 关联政策 ID |
| policy_code | VARCHAR(30) NOT NULL | 政策编码（冗余字段，关联 ops_dealer_policy.policy_code，便于查询） |
| indicator_name | VARCHAR(100) NOT NULL | 指标名称（动态值，非固定枚举） |
| target_year | INT NOT NULL | 年度 |
| target_month | INT NOT NULL | 月份（季度政策为 3/6/9/12，月度政策为 1-12） |
| target_value | DECIMAL(12,2) NOT NULL | 目标值 |
| achieved_value | DECIMAL(12,2) DEFAULT 0 | 达成值 |
| unit | VARCHAR(20) NOT NULL | 单位（件/万元/家/台） |

**索引设计**：
- `ops_dealer_policy`: UNIQUE(policy_code), INDEX(dealer_id), INDEX(policy_type), INDEX(achievement_type), INDEX(policy_status), INDEX(contract_code)
- `ops_dealer_policy_indicator`: INDEX(policy_id), INDEX(policy_code), INDEX(indicator_name), INDEX(target_month), INDEX(target_year)
- `ops_dealer_policy_achievement`: INDEX(indicator_id), INDEX(achieve_level), INDEX(target_year)

### 2. 政策 CRUD 接口（P0）

**新建文件**：
- `yudao-module-opshub/.../controller/admin/policy/DealerPolicyController.java`
- `yudao-module-opshub/.../controller/admin/policy/vo/` (VO 包)
- `yudao-module-opshub/.../service/policy/DealerPolicyService.java`
- `yudao-module-opshub/.../service/policy/DealerPolicyServiceImpl.java`
- `yudao-module-opshub/.../dal/dataobject/policy/DealerPolicyDO.java`
- `yudao-module-opshub/.../dal/dataobject/policy/DealerPolicyIndicatorDO.java`
- `yudao-module-opshub/.../dal/mysql/policy/DealerPolicyMapper.java`
- `yudao-module-opshub/.../dal/mysql/policy/DealerPolicyIndicatorMapper.java`

**设计逻辑**：
- 政策与指标是一对多关系，创建/更新政策时同步维护指标列表
- 政策编码自动生成，格式 `POL-{年份}-{序号}`
- 数据权限：通过 `DealerDataPermissionRule` 按 dealerCode + productLineCode 过滤

### 3. 政策看板查询接口（P0）

**新建文件**：
- `yudao-module-opshub/.../controller/admin/policy/vo/PolicyKpiPanelRespVO.java`
- `yudao-module-opshub/.../controller/admin/policy/vo/PolicyTargetGroupRespVO.java`
- `yudao-module-opshub/.../controller/admin/policy/vo/PolicyDetailCardRespVO.java`
- `yudao-module-opshub/.../controller/admin/policy/vo/PolicyIndicatorChartRespVO.java`

**设计逻辑**：
- KPI 面板接口：按 indicator_name 分组，返回每个指标在各季度（Q1-Q4）或各月度的汇总达成值/目标值/达成率。季度政策按 3/6/9/12 月汇总，月度政策按月展示
- 柱状图接口：按指标 + 时间维度返回达成值/目标值/达成率序列，时间粒度由政策 achievement_type 决定。X 轴展示规则：当筛选出的政策全部为「季度政策」时展示 Q1-Q4；全部为「月度政策」时展示 1-12 月；混合时统一按月份展示，Q1→3月、Q2→6月、Q3→9月、Q4→12月
- 目标分布接口：按指标分组，按 target_value 聚合，返回各目标值下的政策数量
- 政策详情接口：返回单条政策完整信息含指标列表
- 年度过滤：所有看板查询接口增加 `targetYear` 参数，默认当前年度，用于限定指标/达成明细的年份范围

### 4. 达成明细下钻接口（P1）

**设计逻辑**：
- 达成值下钻需要额外的明细表 `ops_dealer_policy_achievement`，记录按省份/医院/产品级别的达成明细
- 一期先建表和基本 CRUD，数据手动录入
- 前端展示：点击达成值 → 按省汇总列表 → 点击省份 → 医院列表 → 点击医院 → 产品列表

**ops_dealer_policy_achievement**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| indicator_id | BIGINT NOT NULL | 关联指标 ID |
| indicator_name | VARCHAR(100) NOT NULL | 指标名称（冗余字段，便于查询） |
| target_year | INT NOT NULL | 年度 |
| province | VARCHAR(50) | 省份 |
| province_code | VARCHAR(20) | 省份编码 |
| hospital | VARCHAR(200) | 医院名称（可空，省级汇总时为空） |
| hospital_code | VARCHAR(50) | 医院编码（可空，省级汇总时为空） |
| product_name | VARCHAR(200) | 产品名称（可空，医院级汇总时为空） |
| achieved_value | DECIMAL(12,2) | 达成值 |
| achieve_level | VARCHAR(10) NOT NULL | 层级：province/hospital/product |

### 5. Excel 导入（P0）

在已有的 `OpsExcelImportController` / `OpsExcelImportService` 中新增 3 种导入类型，复用现有导入框架。

**新增导入类型**：

| type 值 | 导入目标 | Excel VO 类 |
|---------|---------|-------------|
| `policy` | ops_dealer_policy | `PolicyImportExcelVO` |
| `policy-indicator` | ops_dealer_policy_indicator | `PolicyIndicatorImportExcelVO` |
| `policy-achievement` | ops_dealer_policy_achievement | `PolicyAchievementImportExcelVO` |

**PolicyImportExcelVO 字段**：

| Excel 列头 | 字段 | 说明 |
|-----------|------|------|
| 经销商编码 | dealerCode | 必填，关联已有经销商 |
| 产品线编码 | productLineCode | 必填 |
| 产品线名称 | productLineName | 必填 |
| 政策编码 | policyCode | 必填，唯一标识，存在则更新 |
| 政策名称 | policyName | 必填 |
| 政策类型 | policyType | 必填，下拉：返利/促销/其他 |
| 达成类型 | achievementType | 必填，下拉：季度政策/月度政策 |
| 政策状态 | policyStatus | 必填，下拉：执行中/待执行/已完成 |
| 来源合同编码 | contractCode | 可空 |
| 来源合同名称 | contractName | 可空 |
| 政策描述 | policyDesc | 可空 |

**PolicyIndicatorImportExcelVO 字段**：

| Excel 列头 | 字段 | 说明 |
|-----------|------|------|
| 政策编码 | policyCode | 必填，关联已有政策 |
| 指标名称 | indicatorName | 必填，动态值 |
| 年度 | targetYear | 必填，如 2026 |
| 月份 | targetMonth | 必填，季度政策:3/6/9/12；月度政策:1-12 |
| 目标值 | targetValue | 必填 |
| 达成值 | achievedValue | 可空，默认 0 |
| 单位 | unit | 必填，下拉：件/万元/家/台 |

**PolicyAchievementImportExcelVO 字段**：

| Excel 列头 | 字段 | 说明 |
|-----------|------|------|
| 指标政策编码 | policyCode | 必填，用于查找对应指标 |
| 指标名称 | indicatorName | 必填，配合 policyCode 定位指标 |
| 年度 | targetYear | 必填，配合定位指标 |
| 月份 | targetMonth | 必填，配合定位指标 |
| 层级 | achieveLevel | 必填，下拉：province/hospital/product |
| 省份 | province | province/hospital/product 层级必填 |
| 省份编码 | provinceCode | province/hospital/product 层级必填 |
| 医院 | hospital | hospital/product 层级必填 |
| 医院编码 | hospitalCode | hospital/product 层级必填 |
| 产品名称 | productName | product 层级必填 |
| 达成值 | achievedValue | 必填 |

**设计逻辑**：
- 复用已有 `OpsExcelImportService` 框架，新增 3 个 import 方法
- 政策导入按 `policyCode` 幂等 upsert（存在则更新，不存在则新增）
- 指标导入按 `policyCode + indicatorName + targetYear + targetMonth` 幂等 upsert
- 达成明细导入按 `policyCode + indicatorName + targetYear + targetMonth + achieveLevel + provinceCode + hospitalCode + productName` 幂等 upsert
- 政策导入时，若填写了 `contractCode`，自动查找对应 `ops_signing_contract` 记录并回填 `source_contract_id`（未找到则记录失败行）
- 前端 Excel 导入页新增 3 张导入卡片，归属「政策看板」分组

### 6. 枚举类（P0）

**新建文件**：
- `PolicyTypeEnum.java`：REBATE("rebate","返利"), PROMOTION("promotion","促销"), OTHER("other","其他")
- `PolicyStatusEnum.java`：EXECUTING("executing","执行中"), PENDING("pending","待执行"), COMPLETED("completed","已完成")
- `PolicyAchievementTypeEnum.java`：QUARTER("quarter","季度政策"), MONTH("month","月度政策")
- `IndicatorNameEnum.java`：**不建枚举**，指标名称为动态值，通过 `SELECT DISTINCT indicator_name FROM ops_dealer_policy_indicator` 获取

### 7. 错误码（P0）

在 `ErrorCodeConstants.java` 新增 `1-050-010-xxx` 段：
- `1-050-010-000`：政策不存在
- `1-050-010-001`：政策编码已存在
- `1-050-010-002`：指标数据不合法

### 8. 前端 — 政策看板页面（P0）

**新建文件**：
- `src/views/opshub/policy/index.vue` — 主页面
- `src/views/opshub/policy/components/PolicyFilterBar.vue` — 筛选器
- `src/views/opshub/policy/components/KpiPanel.vue` — 单个 KPI 指标面板（含季度仪表盘）
- `src/views/opshub/policy/components/KpiPanelGrid.vue` — 8 大面板网格布局
- `src/views/opshub/policy/components/IndicatorChart.vue` — 垂直柱状图（ECharts）
- `src/views/opshub/policy/components/TargetDistribution.vue` — 目标值分布标签组
- `src/views/opshub/policy/components/PolicyDetailModal.vue` — 政策详情弹窗
- `src/views/opshub/policy/components/AchievementDrilldown.vue` — 达成明细下钻弹窗
- `src/api/opshub/policy/index.ts` — API 定义

**设计逻辑**：
- 页面顶部为筛选器（政策类型/达成类型（按季度/按月度）/指标类型/月度/产品线/经销商/政策编码搜索）
- 「达成类型」筛选器为多选下拉，选项为「季度政策」「月度政策」，用于筛选 achievement_type
- 主体按指标类型分区展示，每个分区含：标题+关联政策数链接、ECharts 柱状图、目标分布标签组
- 柱状图颜色编码：≥100% 绿色、≥60% 黄色、<60% 红色
- 三级钻取：点击柱子 → 目标分布标签 → 政策详情卡片弹窗
- 达成值下钻：点击政策详情中的达成值 → 省/医院/产品三级列表
- 每条政策卡片可发起"政策咨询"

## 页面元素规格

### 政策看板页（policy/index.vue）

#### Form 表单（输入项）

无输入表单，纯展示+筛选。

| 筛选项 | 元素类型 | 数据类型 | 取值范围 | 必填 | 说明 |
|--------|----------|----------|----------|------|------|
| 年度 | select | Integer | 当前年前后各 3 年 | 是 | 默认当前年，不可清空 |
| 政策类型 | checkbox(多选下拉) | String[] | rebate/promotion/other | 否 | 默认全选 |
| 达成类型 | checkbox(多选下拉) | String[] | quarter/month | 否 | 季度政策/月度政策，默认全选 |
| 指标类型 | checkbox(多选下拉) | String[] | 动态 API 获取（DISTINCT indicator_name） | 否 | 默认全选，按当前年度刷新 |
| 月度 | checkbox(多选下拉) | Integer[] | 1-12 | 否 | 默认全选，与达成类型联动 |
| 产品线 | checkbox(多选下拉) | String[] | 骨科/心内科/外科/神经外科 | 否 | 默认全选 |
| 经销商 | checkbox(多选下拉) | Long[] | 经销商列表 API | 否 | 仅管理员/执行员可见 |
| 政策编码 | text | String | 模糊搜索 | 否 | 如 POL-2026-001 |

#### 接口输出项（展示项）

**KPI 面板区**：

| 字段名 | 数据类型 | 说明 |
|--------|----------|------|
| indicatorName | String | 指标名称（如"骨科关节销量"） |
| policyCount | Integer | 关联政策数量 |
| timeData | List | 时间维度数据列表（季度政策为 Q1-Q4，月度政策为各月） |
| timeData[].label | String | 时间标签（Q1/Q2/Q3/Q4 或 1月/2月/...） |
| timeData[].achieved | BigDecimal | 达成值 |
| timeData[].target | BigDecimal | 目标值 |
| timeData[].rate | BigDecimal | 达成率% |
| unit | String | 单位 |

**柱状图区**：

| 字段名 | 数据类型 | 说明 |
|--------|----------|------|
| timeLabels | List\<String\> | 时间标签（仅季度政策为 Q1/Q2/...；仅月度政策为 1月/2月/...；混合时统一为 1月/2月/...，季度数据按 Q1→3月、Q2→6月、Q3→9月、Q4→12月聚合） |
| bars | List | 每根柱子数据 |
| bars[].achieved | BigDecimal | 达成值 |
| bars[].target | BigDecimal | 目标值 |
| bars[].rate | BigDecimal | 达成率% |
| bars[].color | String | 颜色标识（green/yellow/red） |

**目标分布区**：

| 字段名 | 数据类型 | 说明 |
|--------|----------|------|
| targetValue | BigDecimal | 目标值 |
| policyCount | Integer | 该目标下的政策数量 |

**政策详情弹窗**：

| 字段名 | 数据类型 | 说明 |
|--------|----------|------|
| policyCode | String | 政策编码 |
| policyName | String | 政策名称 |
| policyType | String | 返利/促销/其他 |
| dealerName | String | 经销商名称 |
| productLineName | String | 产品线名称 |
| status | String | 执行中/待执行/已完成 |
| description | String | 政策描述 |
| contractCode | String | 来源合同编码 |
| contractName | String | 来源合同名称 |
| indicators | List | 指标列表（名称/月份/目标/达成/达成率/单位） |
| achievementType | String | 达成类型（quarter/month） |

## API 接口定义

### 接口 1：创建政策

| 项 | 值 |
|------|------|
| 方法 | `POST` |
| 路径 | `/opshub/dealer-policy/create` |
| 权限 | `dealer:policy:create` |
| 说明 | 创建政策及其指标 |

**请求参数**（Body JSON）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| policyName | String | 是 | 政策名称，max=200 |
| policyType | String | 是 | rebate/promotion/other |
| achievementType | String | 是 | quarter/month（达成类型） |
| dealerId | Long | 是 | 经销商 ID |
| productLineCode | String | 是 | 产品线编码 |
| status | String | 是 | executing/pending/completed |
| description | String | 否 | 政策描述，max=2000 |
| contractCode | String | 否 | 来源合同编码 |
| contractName | String | 否 | 来源合同名称 |
| indicators | List | 是 | 指标列表 |
| indicators[].indicatorName | String | 是 | 指标名称（动态值） |
| indicators[].month | Integer | 是 | 月份（季度政策:3/6/9/12；月度政策:1-12） |
| indicators[].targetValue | BigDecimal | 是 | 目标值 |
| indicators[].achievedValue | BigDecimal | 否 | 达成值，默认 0 |
| indicators[].unit | String | 是 | 单位 |

**响应**（`CommonResult<Long>`）：返回新建政策 ID

### 接口 2：更新政策

| 项 | 值 |
|------|------|
| 方法 | `PUT` |
| 路径 | `/opshub/dealer-policy/update` |
| 权限 | `dealer:policy:update` |

**请求参数**：同创建，额外增加 `id`(Long, 必填)

### 接口 3：删除政策

| 项 | 值 |
|------|------|
| 方法 | `DELETE` |
| 路径 | `/opshub/dealer-policy/delete` |
| 权限 | `dealer:policy:delete` |

**请求参数**（Query）：`id`(Long, 必填)

### 接口 4：政策分页列表（管理用）

| 项 | 值 |
|------|------|
| 方法 | `GET` |
| 路径 | `/opshub/dealer-policy/page` |
| 权限 | `dealer:policy:query` |

**请求参数**（Query）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页条数 |
| policyType | String | 否 | 政策类型筛选 |
| status | String | 否 | 状态筛选 |
| dealerId | Long | 否 | 经销商筛选 |
| keyword | String | 否 | 编码/名称模糊搜索 |

**响应**（`CommonResult<PageResult<DealerPolicyRespVO>>`）

### 接口 5：政策详情

| 项 | 值 |
|------|------|
| 方法 | `GET` |
| 路径 | `/opshub/dealer-policy/get` |
| 权限 | `dealer:policy:query` |

**请求参数**（Query）：`id`(Long, 必填)

**响应**（`CommonResult<DealerPolicyDetailRespVO>`）：含政策基本信息 + 指标列表

### 接口 6：KPI 面板数据

| 项 | 值 |
|------|------|
| 方法 | `GET` |
| 路径 | `/opshub/dealer-policy/kpi-panels` |
| 权限 | `dealer:policy:query` |
| 说明 | 返回 8 大指标面板数据，含季度达成汇总 |

**请求参数**（Query）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetYear | Integer | 是 | 年度，如 2026 |
| policyTypes | String | 否 | 逗号分隔，如 rebate,promotion |
| indicatorNames | String | 否 | 逗号分隔的指标名称 |
| achievementTypes | String | 否 | 逗号分隔，如 quarter,month（达成类型筛选） |
| months | String | 否 | 逗号分隔，如 1,2,3 |
| productLineCodes | String | 否 | 逗号分隔 |
| dealerIds | String | 否 | 逗号分隔 |
| policyCode | String | 否 | 政策编码模糊搜索 |

**响应**（`CommonResult<List<PolicyKpiPanelRespVO>>`）：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| indicatorName | String | 指标名称 |
| unit | String | 单位 |
| policyCount | Integer | 关联政策数 |
| periods | List | 时间维度数据列表 |
| periods[].label | String | 时间标签（Q1/Q2/...或1月/2月/...） |
| periods[].achieved | BigDecimal | 汇总达成值 |
| periods[].target | BigDecimal | 汇总目标值 |
| periods[].rate | BigDecimal | 达成率% |

### 接口 7：指标柱状图数据

| 项 | 值 |
|------|------|
| 方法 | `GET` |
| 路径 | `/opshub/dealer-policy/indicator-chart` |
| 权限 | `dealer:policy:query` |
| 说明 | 返回单个指标的时间维度柱状图数据 |

**请求参数**（Query）：同接口 6，额外增加 `indicatorName`(String, 必填)

**响应**（`CommonResult<PolicyIndicatorChartRespVO>`）：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| indicatorName | String | 指标名称 |
| timeLabels | List\<String\> | 时间标签 |
| bars | List | 柱子数据 |
| bars[].achieved | BigDecimal | 达成值 |
| bars[].target | BigDecimal | 目标值 |
| bars[].rate | BigDecimal | 达成率% |

### 接口 8：目标分布数据

| 项 | 值 |
|------|------|
| 方法 | `GET` |
| 路径 | `/opshub/dealer-policy/target-distribution` |
| 权限 | `dealer:policy:query` |
| 说明 | 按指标分组的目标值分布 |

**请求参数**（Query）：同接口 7

**响应**（`CommonResult<List<PolicyTargetGroupRespVO>>`）：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| targetValue | BigDecimal | 目标值 |
| policyCount | Integer | 该目标下政策数 |
| policies | List | 政策简要列表（点击穿透用） |

### 接口 9：达成明细下钻

| 项 | 值 |
|------|------|
| 方法 | `GET` |
| 路径 | `/opshub/dealer-policy/achievement-drilldown` |
| 权限 | `dealer:policy:query` |

**请求参数**（Query）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetYear | Integer | 是 | 年度 |
| indicatorId | Long | 是 | 指标 ID |
| level | String | 是 | province/hospital/product |
| province | String | 否 | 省份（hospital/product 级别必填） |
| hospital | String | 否 | 医院（product 级别必填） |

**响应**（`CommonResult<List<PolicyAchievementRespVO>>`）：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| name | String | 省份/医院/产品名称 |
| achievedValue | BigDecimal | 达成值 |

### 接口 10：达成明细 CRUD

| 项 | 值 |
|------|------|
| 方法 | `POST`/`PUT`/`DELETE`/`GET` |
| 路径 | `/opshub/dealer-policy-achievement/create|update|delete|page` |
| 权限 | `dealer:policy:create`/`update`/`delete`/`query` |
| 说明 | 达成明细的增删改查 |

### 接口 11：获取指标名称列表

| 项 | 值 |
|------|------|
| 方法 | `GET` |
| 路径 | `/opshub/dealer-policy/indicator-names` |
| 权限 | `dealer:policy:query` |
| 说明 | 返回所有已录入的指标名称（去重），用于筛选器和表单下拉 |

**请求参数**（Query）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetYear | Integer | 是 | 年度 |

**响应**（`CommonResult<List<String>>`）：返回去重后的指标名称列表

## DDL 变更

新建文件：`db/branches/feature_step19-政策看板/feature_step19-政策看板_ddl.sql`

```sql
-- 政策主表
CREATE TABLE ops_dealer_policy (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    dealer_id     BIGINT        NOT NULL COMMENT '经销商ID',
    dealer_code   VARCHAR(50)   DEFAULT NULL COMMENT '经销商编码（数据权限用）',
    product_line_code VARCHAR(50) DEFAULT NULL COMMENT '产品线编码（数据权限用）',
    product_line_name VARCHAR(100) DEFAULT NULL COMMENT '产品线名称（冗余，便于展示）',
    policy_code   VARCHAR(30)   NOT NULL COMMENT '政策编码',
    policy_name   VARCHAR(200)  NOT NULL COMMENT '政策名称',
    policy_type   VARCHAR(20)   NOT NULL COMMENT '政策类型(rebate/promotion/other)',
    achievement_type VARCHAR(20)  NOT NULL COMMENT '达成类型(quarter=季度/month=月度)',
    policy_status VARCHAR(20)   NOT NULL DEFAULT 'executing' COMMENT '状态(executing/pending/completed)',
    policy_desc   TEXT          DEFAULT NULL COMMENT '政策描述',
    contract_code VARCHAR(30)   DEFAULT NULL COMMENT '来源合同编码',
    contract_name VARCHAR(200)  DEFAULT NULL COMMENT '来源合同名称',
    source_contract_id BIGINT   DEFAULT NULL COMMENT '来源政策合同ID',
    remark        VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    creator       VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater       VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       BIT(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id     BIGINT        NOT NULL DEFAULT 0 COMMENT '租户编号',
    UNIQUE KEY uk_policy_code (policy_code, tenant_id),
    KEY idx_dealer_id (dealer_id),
    KEY idx_policy_type (policy_type),
    KEY idx_achievement_type (achievement_type),
    KEY idx_policy_status (policy_status),
    KEY idx_contract_code (contract_code)
) ENGINE=InnoDB COMMENT='经销商政策表';

-- 政策指标表
CREATE TABLE ops_dealer_policy_indicator (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id       BIGINT        NOT NULL COMMENT '关联政策ID',
    policy_code     VARCHAR(30)   NOT NULL COMMENT '政策编码（冗余，便于查询）',
    indicator_name  VARCHAR(100)  NOT NULL COMMENT '指标名称',
    target_year     INT           NOT NULL COMMENT '年度',
    target_month    INT           NOT NULL COMMENT '月份(季度政策:3/6/9/12,月度政策:1-12)',
    target_value    DECIMAL(12,2) NOT NULL COMMENT '目标值',
    achieved_value  DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '达成值',
    unit            VARCHAR(20)   NOT NULL COMMENT '单位',
    creator         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id       BIGINT        NOT NULL DEFAULT 0 COMMENT '租户编号',
    KEY idx_policy_id (policy_id),
    KEY idx_policy_code (policy_code),
    KEY idx_indicator_name (indicator_name),
    KEY idx_target_month (target_month),
    KEY idx_target_year (target_year)
) ENGINE=InnoDB COMMENT='政策指标表';

-- 政策达成明细表
CREATE TABLE ops_dealer_policy_achievement (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    indicator_id    BIGINT        NOT NULL COMMENT '关联指标ID',
    indicator_name  VARCHAR(100)  NOT NULL COMMENT '指标名称（冗余，便于查询）',
    province        VARCHAR(50)   DEFAULT NULL COMMENT '省份',
    province_code   VARCHAR(20)   DEFAULT NULL COMMENT '省份编码',
    hospital        VARCHAR(200)  DEFAULT NULL COMMENT '医院名称',
    hospital_code   VARCHAR(50)   DEFAULT NULL COMMENT '医院编码',
    product_name    VARCHAR(200)  DEFAULT NULL COMMENT '产品名称',
    target_year     INT           NOT NULL COMMENT '年度',
    achieved_value  DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '达成值',
    achieve_level   VARCHAR(10)   NOT NULL COMMENT '层级(province/hospital/product)',
    creator         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id       BIGINT        NOT NULL DEFAULT 0 COMMENT '租户编号',
    KEY idx_indicator_id (indicator_id),
    KEY idx_achieve_level (achieve_level),
    KEY idx_target_year (target_year)
) ENGINE=InnoDB COMMENT='政策达成明细表';
```

## DML 变更

新建文件：`db/branches/feature_step19-政策看板/feature_step19-政策看板_dml.sql`

- 新增菜单按钮权限：`dealer:policy:create`、`dealer:policy:update`、`dealer:policy:delete`
- 已有权限（Step1）：`dealer:policy:query`(6040)、`dealer:policy:consult`(6041)

## 涉及文件清单

### 后端（yudao-module-opshub）

| 文件 | 操作 |
|------|------|
| `dal/dataobject/policy/DealerPolicyDO.java` | 新增 |
| `dal/dataobject/policy/DealerPolicyIndicatorDO.java` | 新增 |
| `dal/dataobject/policy/DealerPolicyAchievementDO.java` | 新增 |
| `dal/mysql/policy/DealerPolicyMapper.java` | 新增 |
| `dal/mysql/policy/DealerPolicyIndicatorMapper.java` | 新增 |
| `dal/mysql/policy/DealerPolicyAchievementMapper.java` | 新增 |
| `service/policy/DealerPolicyService.java` | 新增 |
| `service/policy/DealerPolicyServiceImpl.java` | 新增 |
| `controller/admin/policy/DealerPolicyController.java` | 新增 |
| `controller/admin/policy/DealerPolicyAchievementController.java` | 新增 |
| `controller/admin/policy/vo/*.java`（约 12 个 VO） | 新增 |
| `enums/PolicyTypeEnum.java` | 新增 |
| `enums/PolicyStatusEnum.java` | 新增 |
| `enums/PolicyAchievementTypeEnum.java` | 新增 |
| `enums/IndicatorNameEnum.java` | **不建**（动态值，通过接口获取） |
| `controller/admin/excel/vo/PolicyImportExcelVO.java` | 新增 |
| `controller/admin/excel/vo/PolicyIndicatorImportExcelVO.java` | 新增 |
| `controller/admin/excel/vo/PolicyAchievementImportExcelVO.java` | 新增 |
| `controller/admin/excel/convert/PolicyTypeConvert.java` | 新增 |
| `controller/admin/excel/convert/PolicyAchievementTypeConvert.java` | 新增 |
| `controller/admin/excel/convert/PolicyStatusConvert.java` | 新增 |
| `controller/admin/excel/convert/AchievementLevelConvert.java` | 新增 |
| `service/excel/OpsExcelImportService.java` | 修改（新增 3 个 import 方法） |
| `service/excel/OpsExcelImportServiceImpl.java` | 修改（新增 3 个 import 实现） |
| `controller/admin/excel/OpsExcelImportController.java` | 修改（type 枚举扩展） |
| `enums/ErrorCodeConstants.java` | 修改（新增错误码段） |

### 前端（yudao-ui/yudao-ui-admin-vue3）

| 文件 | 操作 |
|------|------|
| `src/views/opshub/policy/index.vue` | 新增 |
| `src/views/opshub/policy/components/PolicyFilterBar.vue` | 新增 |
| `src/views/opshub/policy/components/KpiPanel.vue` | 新增 |
| `src/views/opshub/policy/components/KpiPanelGrid.vue` | 新增 |
| `src/views/opshub/policy/components/IndicatorChart.vue` | 新增 |
| `src/views/opshub/policy/components/TargetDistribution.vue` | 新增 |
| `src/views/opshub/policy/components/PolicyDetailModal.vue` | 新增 |
| `src/views/opshub/policy/components/AchievementDrilldown.vue` | 新增 |
| `src/api/opshub/policy/index.ts` | 新增 |

### DDL/DML

| 文件 | 操作 |
|------|------|
| `db/branches/feature_step19-政策看板/feature_step19-政策看板_ddl.sql` | 新建 |
| `db/branches/feature_step19-政策看板/feature_step19-政策看板_dml.sql` | 新建 |

## 已知限制

1. 达成明细（省/医院/产品）数据需手动通过 Excel 导入或接口录入
2. 政策指标与签约合同中 `indicators` JSON 字段无自动同步机制，需手动维护一致性

## 剩余待做（P2/P3）

| 优先级 | 任务 | 说明 |
|--------|------|------|
| P2 | 政策数据从签约合同 indicators 自动同步 | 签署政策合同时自动创建政策记录 |
| P3 | 政策到期预警通知 | 结合通知系统实现 |
| P3 | 政策导出报表 | Excel 导出政策执行情况汇总 |
