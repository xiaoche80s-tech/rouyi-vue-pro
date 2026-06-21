# Step19 — 政策看板模块

## Context

基于 PRD-Step19-政策看板模块.md，构建独立的政策看板模块。当前政策数据嵌套在 `ops_signing_contract.indicators` JSON 字段中，没有独立实体。本 Step 新建 3 张数据表、后端 CRUD + KPI 查询接口、Excel 导入、前端看板页面。

PRD 文件：`docs/PRD-Step19-政策看板模块.md`

---

## Task 1: DDL/DML 变更

**新建文件**：
- `db/branches/feature_step19-政策看板/feature_step19-政策看板_ddl.sql`
- `db/branches/feature_step19-政策看板/feature_step19-政策看板_dml.sql`

DDL：创建 3 张表（按 PRD §DDL 变更节内容）：
- `ops_dealer_policy`（政策主表，含 policy_status、policy_desc、contract_code/name、product_line_code/name、achievement_type）
- `ops_dealer_policy_indicator`（指标表，含 policy_code 冗余字段、target_month）
- `ops_dealer_policy_achievement`（达成明细表，含 indicator_name、province_code、hospital_code、achieve_level）

DML：在 `system_menu` 中新增按钮权限：
- `dealer:policy:create`（父 ID 6004）
- `dealer:policy:update`（父 ID 6004）
- `dealer:policy:delete`（父 ID 6004）
- 已有权限参考：`dealer:policy:query`(6040)、`dealer:policy:consult`(6041)

---

## Task 2: DO + Mapper

**新建文件**（包路径：`cn.iocoder.yudao.module.opshub`）：
- `dal/dataobject/policy/DealerPolicyDO.java` — 对应 ops_dealer_policy，字段按 PRD 表定义，Java 字段用驼峰命名（policyStatus/policyDesc/achievementType/contractCode/contractName/productLineCode/productLineName）
- `dal/dataobject/policy/DealerPolicyIndicatorDO.java` — 对应 ops_dealer_policy_indicator（policyId/policyCode/indicatorName/targetMonth/targetValue/achievedValue/unit）
- `dal/dataobject/policy/DealerPolicyAchievementDO.java` — 对应 ops_dealer_policy_achievement（indicatorId/indicatorName/province/provinceCode/hospital/hospitalCode/productName/achievedValue/achieveLevel）
- `dal/mysql/policy/DealerPolicyMapper.java` — 继承 BaseMapperX，支持按 dealerCode/productLineCode 数据权限过滤，支持 policyType/achievementType/policyStatus/dealerId/keyword 分页查询
- `dal/mysql/policy/DealerPolicyIndicatorMapper.java` — 支持 selectListByPolicyId、selectDistinctIndicatorNames、按 policyId+indicatorName+targetMonth 查询
- `dal/mysql/policy/DealerPolicyAchievementMapper.java` — 支持按 indicatorId+achieveLevel 查询，按 province/hospital 分组汇总

---

## Task 3: 枚举 + 错误码

**新建文件**（包路径：`cn.iocoder.yudao.module.opshub.enums`）：
- `PolicyTypeEnum.java`：REBATE("rebate","返利"), PROMOTION("promotion","促销"), OTHER("other","其他")
- `PolicyStatusEnum.java`：EXECUTING("executing","执行中"), PENDING("pending","待执行"), COMPLETED("completed","已完成")
- `PolicyAchievementTypeEnum.java`：QUARTER("quarter","季度政策"), MONTH("month","月度政策")
- 不建 IndicatorNameEnum（动态值，通过接口获取）

**修改文件**：
- `enums/ErrorCodeConstants.java`：新增 `1-050-010-xxx` 段（政策不存在/政策编码已存在/指标数据不合法）

---

## Task 4: 政策 CRUD Service + Controller + VO

**新建文件**：

Service 层：
- `service/policy/DealerPolicyService.java`（接口）
- `service/policy/DealerPolicyServiceImpl.java`（实现）

Controller 层：
- `controller/admin/policy/DealerPolicyController.java`

VO 类（`controller/admin/policy/vo/`）：
- `DealerPolicySaveReqVO.java` — 创建/更新请求（含 indicators 列表，indicators 用内嵌 VO `IndicatorSaveVO`）
- `DealerPolicyRespVO.java` — 分页列表响应
- `DealerPolicyDetailRespVO.java` — 详情响应（含指标列表 + contractCode/contractName/productLineName）
- `DealerPolicyPageReqVO.java` — 分页查询请求（继承 PageParam，含 policyType/policyStatus/dealerId/keyword）

实现接口（对应 PRD 接口 1-5）：
- `POST /opshub/dealer-policy/create` — 创建政策+指标，policyCode 自动生成 POL-{年份}-{序号}
- `PUT /opshub/dealer-policy/update` — 更新政策+全量替换指标
- `DELETE /opshub/dealer-policy/delete` — 删除政策+级联删除指标
- `GET /opshub/dealer-policy/page` — 分页查询
- `GET /opshub/dealer-policy/get` — 详情（含指标列表）

权限码：`dealer:policy:create`/`update`/`delete`/`query`

---

## Task 5: 政策看板查询 Service + VO

**新建 VO 文件**（`controller/admin/policy/vo/`）：
- `PolicyKpiPanelRespVO.java` — KPI 面板响应（indicatorName/unit/policyCount + timeData 列表，timeData 含 label/achieved/target/rate）
- `PolicyKpiPanelPageReqVO.java` — 面板查询请求（policyTypes/indicatorNames/achievementTypes/months/productLineCodes/dealerIds/policyCode）
- `PolicyIndicatorChartRespVO.java` — 柱状图响应（indicatorName + timeLabels + bars 列表）
- `PolicyTargetGroupRespVO.java` — 目标分布响应（targetValue/policyCount + policies 简要列表）
- `PolicyAchievementRespVO.java` — 达成明细响应（name/achievedValue）

**在 DealerPolicyController 中新增接口**（对应 PRD 接口 6-9、11）：
- `GET /opshub/dealer-policy/kpi-panels` — 按 indicator_name 分组聚合，季度政策按 3/6/9/12 月汇总，月度政策按月汇总
- `GET /opshub/dealer-policy/indicator-chart` — 单个指标时间维度柱状图数据
- `GET /opshub/dealer-policy/target-distribution` — 按目标值分组统计政策数
- `GET /opshub/dealer-policy/achievement-drilldown` — 达成明细下钻（按 indicatorId + achieveLevel + province/hospital 参数）
- `GET /opshub/dealer-policy/indicator-names` — SELECT DISTINCT indicator_name FROM ops_dealer_policy_indicator

KPI 面板核心聚合逻辑（伪代码）：
```java
// 1. 查询符合条件的政策+指标
// 2. 按 indicator_name 分组
// 3. 对每组，按 target_month 聚合（季度政策映射到 Q1-Q4，月度政策保持月份）
// 4. 计算 achieved/target/rate
```

---

## Task 6: 达成明细 CRUD Controller

**新建文件**：
- `controller/admin/policy/DealerPolicyAchievementController.java`
- `controller/admin/policy/vo/DealerPolicyAchievementSaveReqVO.java`
- `controller/admin/policy/vo/DealerPolicyAchievementPageReqVO.java`

实现接口（对应 PRD 接口 10）：
- `POST /opshub/dealer-policy-achievement/create`
- `PUT /opshub/dealer-policy-achievement/update`
- `DELETE /opshub/dealer-policy-achievement/delete`
- `GET /opshub/dealer-policy-achievement/page`

权限码复用：`dealer:policy:create`/`update`/`delete`/`query`

---

## Task 7: Excel 导入 — VO + Convert

**新建文件**（包路径：`controller/admin/excel/`）：

Excel VO（`vo/`）：
- `PolicyImportExcelVO.java` — 政策导入（11 列，按 PRD §5 PolicyImportExcelVO 字段）
- `PolicyIndicatorImportExcelVO.java` — 指标导入（6 列）
- `PolicyAchievementImportExcelVO.java` — 达成明细导入（10 列）

Convert 转换器（`convert/`）：
- `PolicyTypeConvert.java` — 返利/促销/其他 ↔ rebate/promotion/other
- `PolicyAchievementTypeConvert.java` — 季度政策/月度政策 ↔ quarter/month
- `PolicyStatusConvert.java` — 执行中/待执行/已完成 ↔ executing/pending/completed
- `AchievementLevelConvert.java` — 省级/医院级/产品级 ↔ province/hospital/product

---

## Task 8: Excel 导入 — Service 实现

**修改文件**：
- `service/excel/OpsExcelImportService.java` — 新增 3 个方法声明
- `service/excel/OpsExcelImportServiceImpl.java` — 新增 3 个 import 实现

实现逻辑（参考已有 import*List 模式）：

`importPolicyList(List<PolicyImportExcelVO>)`：
1. 预加载 ops_dealer_policy 到 Map<policyCode, DO>
2. 预加载 ops_signing_contract 到 Map<contractCode, DO>
3. 遍历：校验必填 → 查找经销商(by dealerCode) → 若 contractCode 存在则查合同回填 sourceContractId → upsert
4. 失败行记录原因

`importPolicyIndicatorList(List<PolicyIndicatorImportExcelVO>)`：
1. 预加载 ops_dealer_policy 到 Map<policyCode, DO>（获取 policyId）
2. 预加载指标到 Map<policyCode+indicatorName+targetMonth, DO>
3. 遍历：校验 → 查 policyId → upsert，同步写入 policyCode 冗余字段

`importPolicyAchievementList(List<PolicyAchievementImportExcelVO>)`：
1. 预加载指标到 Map<policyCode+indicatorName+targetMonth, DO>（获取 indicatorId + indicatorName）
2. 预加载达成明细到复合 key Map
3. 遍历：校验 → 查 indicatorId → upsert

**修改文件**：
- `controller/admin/excel/OpsExcelImportController.java` — 在 type 枚举/switch 中新增 `policy`、`policy-indicator`、`policy-achievement` 三个分支

---

## Task 9: 前端 — API 层

**新建文件**：`src/api/opshub/policy/index.ts`

定义接口（对应 PRD 接口 6-11）：
- `getKpiPanels(params)` → `GET /opshub/dealer-policy/kpi-panels`
- `getIndicatorChart(params)` → `GET /opshub/dealer-policy/indicator-chart`
- `getTargetDistribution(params)` → `GET /opshub/dealer-policy/target-distribution`
- `getAchievementDrilldown(params)` → `GET /opshub/dealer-policy/achievement-drilldown`
- `getIndicatorNames()` → `GET /opshub/dealer-policy/indicator-names`

定义 TypeScript 类型：
- `PolicyKpiPanel` / `PolicyTimeData` / `PolicyIndicatorChart` / `PolicyTargetGroup` / `PolicyAchievementItem`

---

## Task 10: 前端 — 政策看板页面

**新建文件**（`src/views/opshub/policy/`）：
- `index.vue` — 主页面，组合以下子组件
- `components/PolicyFilterBar.vue` — 筛选器（政策类型/达成类型/指标类型/月度/产品线/经销商/政策编码），达成类型调用 `achievementTypes` 参数，指标类型调用 `getIndicatorNames()` 获取下拉选项
- `components/KpiPanelGrid.vue` — 按 indicatorName 分区网格布局
- `components/KpiPanel.vue` — 单个指标面板（标题+关联政策数+ECharts 季度/月度仪表盘+目标分布标签组），数据来自 timeData
- `components/IndicatorChart.vue` — ECharts 垂直柱状图（颜色编码：≥100% 绿、≥60% 黄、<60% 红），支持点击穿透
- `components/TargetDistribution.vue` — 目标值分组标签（降序/升序切换），点击弹出政策详情
- `components/PolicyDetailModal.vue` — 政策详情弹窗（policyCode/policyName/policyType/dealerName/productLineName/policyStatus/contractCode/contractName/achievementType + 指标列表含月份/目标/达成/达成率/单位 + 联系客服按钮）
- `components/AchievementDrilldown.vue` — 达成明细下钻弹窗（省→医院→产品三级列表）

路由注册：在 `src/router/modules/` 或动态路由中注册 `policy` 路由（菜单 ID 6004 已存在）

---

## Task 11: 前端 — Excel 导入卡片

**修改文件**：`src/views/opshub/excelImport/index.vue`

在已有的导入卡片列表中新增「政策看板」分组（L0 层级），含 3 张卡片：
- 政策数据导入（type: `policy`）
- 政策指标导入（type: `policy-indicator`）
- 达成明细导入（type: `policy-achievement`）

每张卡片复用已有的 `ImportCard.vue` 组件（下载模板 + 上传导入 + 结果展示）。

---

## Task 12: 编译验证

- 后端：`mvn compile -pl yudao-module-opshub` 通过
- 前端：`npm run type-check` 或 IDE 无 TypeScript 报错
- DDL：在测试环境执行 DDL 脚本无语法错误

---

## 实施顺序

| Task | 内容 | 依赖 |
|------|------|------|
| 1 | DDL/DML 变更 | 无 |
| 2 | DO + Mapper | Task 1 |
| 3 | 枚举 + 错误码 | 无 |
| 4 | CRUD Service + Controller + VO | Task 2, 3 |
| 5 | KPI 查询 Service + VO | Task 2, 3 |
| 6 | 达成明细 CRUD Controller | Task 2, 3 |
| 7 | Excel VO + Convert | Task 3 |
| 8 | Excel Service 实现 | Task 4, 5, 6, 7 |
| 9 | 前端 API 层 | Task 4, 5, 6 |
| 10 | 前端看板页面 | Task 9 |
| 11 | 前端 Excel 导入卡片 | Task 8 |
| 12 | 编译验证 | Task 1-11 |

---

## 验证方式

| 验证项 | 操作 | 预期 |
|--------|------|------|
| DDL 执行 | 在测试数据库执行 DDL 脚本 | 3 张表创建成功，索引正常 |
| 政策 CRUD | 通过 Postman/Swagger 调用接口 1-5 | 创建/更新/删除/分页/详情均正常 |
| KPI 面板 | 导入测试数据后调用接口 6 | 按指标分组返回 timeData，达成率计算正确 |
| 柱状图 | 调用接口 7 | 时间标签和颜色编码正确 |
| 目标分布 | 调用接口 8 | 按目标值分组统计正确 |
| 达成明细下钻 | 调用接口 9（三级） | 省→医院→产品逐级下钻正确 |
| 指标名称列表 | 调用接口 11 | 返回去重指标名称 |
| Excel 导入政策 | 下载模板 → 填数据 → 上传 | 成功导入，contractCode 自动关联 sourceContractId |
| Excel 导入指标 | 同上 | 按 policyCode 关联正确 |
| Excel 导入达成明细 | 同上 | 按复合键关联指标正确 |
| 看板页面 | 浏览器访问政策看板路由 | 筛选器/KPI 面板/柱状图/目标分布/钻取均正常渲染 |
| Excel 导入卡片 | 访问 Excel 导入页 | 3 张政策卡片显示正常，模板下载和导入均正常 |

---

## 风险与注意事项

| 风险 | 缓解措施 |
|------|---------|
| KPI 面板聚合查询性能 | 数据量不大（百条级），暂不优化；后续可加 Redis 缓存 |
| Excel 导入数据一致性 | 幂等 upsert + 事务回滚 + 失败行记录 |
| contractCode 关联失败 | 导入时 contractCode 找不到对应合同，记录到 failureRows，不中断其他行 |
| 指标名称动态值无校验 | 不限制指标名称枚举，允许任意输入，DISTINCT 查询供筛选 |
| 政策编码自动生成并发冲突 | 使用数据库序列或分布式锁，参考已有 contractCode 生成逻辑 |
| ECharts 依赖 | 检查项目是否已安装 echarts，若未安装需 `npm install echarts` |
