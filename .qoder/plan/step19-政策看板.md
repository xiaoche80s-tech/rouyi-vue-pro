# 执行计划 — Step19 政策看板模块

## Context

基于已确认的 PRD（`docs/PRD-Step19-政策看板模块.md`）和 Task 分解（`.qoder/specs/step19-政策看板模块_task.md`），构建独立的政策看板模块。包含 3 张新表、后端 CRUD + KPI 查询接口、Excel 导入、前端看板页面。

### 关键代码模式（已验证）

| 模式 | 参考文件 | 要点 |
|------|---------|------|
| DO | `SigningContractDO.java` | 继承 `TenantBaseDO`，`@TableName` + `@KeySequence`，`@Data` + `@EqualsAndHashCode(callSuper = true)` |
| Mapper | `SigningContractMapper.java` | 继承 `BaseMapperX`，`@Mapper`，`LambdaQueryWrapperX` 的 `inIfPresent` |
| Service | `SigningContractService.java` | 纯接口，创建返回 Long，分页返回 `PageResult<DO>` |
| ServiceImpl | `SigningContractServiceImpl.java` | `@Service` + `@Validated`，`BeanUtils.toBean()`，静态导入 `exception()` |
| Controller | `SigningContractController.java` | `@Tag` + `@RestController`，`@PreAuthorize`，`CommonResult` 包装 |
| Excel VO | `SigningContractImportExcelVO.java` | `@ExcelProperty` + `@ExcelColumnSelect` + `converter`，`@Data @Builder` |
| Excel Service | `OpsExcelImportServiceImpl.java` | `@Transactional`，预加载 Map → 遍历 → try/catch 每行 → 统计 |
| Excel Controller | `OpsExcelImportController.java` | `switch` 按 type 分发，模板双 Sheet |
| 前端 API | `src/api/opshub/signing/index.ts` | TS 接口定义 + `request.get/post` |
| 前端页面 | `src/views/opshub/signing/index.vue` | Composition API，`reactive` queryParams，`ImportCard` 组件 |
| Excel 导入页 | `src/views/opshub/excelImport/index.vue` | L0-L4 分组，`ImportCardItem` 类型，三列布局 |

### 关键常量

- **错误码段**：`1-050-010-xxx`（下一个可用子模块编号）
- **菜单 ID 起点**：`6302`（当前最大 6301）
- **已有菜单**：6004（政策看板）、6040（dealer:policy:query）、6041（dealer:policy:consult）
- **ECharts**：已安装 `^6.0.0`
- **路由**：动态路由，后端菜单系统加载，无需前端静态注册

---

## Task 1: DDL/DML 变更

**新建文件**：
- `db/branches/feature_step19-政策看板/feature_step19-政策看板_ddl.sql` — 3 张表的 CREATE TABLE（按 PRD §DDL 变更）
- `db/branches/feature_step19-政策看板/feature_step19-政策看板_dml.sql` — 菜单权限 INSERT

DML 内容（菜单 ID 从 6302 起）：
```sql
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, ...) VALUES
(6302, '创建政策', 'dealer:policy:create', 3, 3, 6004, ...),
(6303, '更新政策', 'dealer:policy:update', 3, 4, 6004, ...),
(6304, '删除政策', 'dealer:policy:delete', 3, 5, 6004, ...);
-- 角色菜单关联：4 个角色均关联 6302-6304
```

**执行后验证**：在测试 DB 执行 DDL 无报错。

---

## Task 2: DO + Mapper（6 个文件）

**基础包路径**：`cn.iocoder.yudao.module.opshub`
**根目录**：`yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/`

新建：
1. `dal/dataobject/policy/DealerPolicyDO.java`
   - `@TableName("ops_dealer_policy")`，`@KeySequence("ops_dealer_policy_seq")`
   - 字段：id, dealerId, dealerCode, productLineCode, productLineName, policyCode, policyName, policyType, achievementType, policyStatus, contractCode, contractName, policyDesc, sourceContractId, remark

2. `dal/dataobject/policy/DealerPolicyIndicatorDO.java`
   - `@TableName("ops_dealer_policy_indicator")`
   - 字段：id, policyId, policyCode, indicatorName, targetMonth, targetValue, achievedValue, unit

3. `dal/dataobject/policy/DealerPolicyAchievementDO.java`
   - `@TableName("ops_dealer_policy_achievement")`
   - 字段：id, indicatorId, indicatorName, province, provinceCode, hospital, hospitalCode, productName, achievedValue, achieveLevel

4. `dal/mysql/policy/DealerPolicyMapper.java`
   - 继承 `BaseMapperX<DealerPolicyDO>`
   - `selectPage(reqVO)`: 支持 policyType/achievementType/policyStatus/dealerId/keyword 过滤
   - `selectByPolicyCode(code)`: 按编码查单条

5. `dal/mysql/policy/DealerPolicyIndicatorMapper.java`
   - `selectListByPolicyId(policyId)`: 查政策下全部指标
   - `selectDistinctIndicatorNames()`: `SELECT DISTINCT indicator_name`
   - `selectByPolicyCodeAndNameAndMonth(policyCode, indicatorName, targetMonth)`: 幂等查询
   - `deleteByPolicyId(policyId)`: 删除政策时级联删除指标

6. `dal/mysql/policy/DealerPolicyAchievementMapper.java`
   - `selectByIndicatorIdAndLevel(indicatorId, achieveLevel, province, hospital)`: 下钻查询
   - `selectByIndicatorCodeAndNameAndMonth(policyCode, indicatorName, targetMonth, achieveLevel, provinceCode, hospitalCode, productName)`: 幂等查询

---

## Task 3: 枚举 + 错误码（4 个文件）

新建：
1. `enums/PolicyTypeEnum.java` — REBATE/PROMOTION/OTHER（参考 ContractTypeEnum 模式）
2. `enums/PolicyStatusEnum.java` — EXECUTING/PENDING/COMPLETED
3. `enums/PolicyAchievementTypeEnum.java` — QUARTER/MONTH

修改：
4. `enums/ErrorCodeConstants.java` — 新增：
```java
// ========== 政策看板 1-050-010-xxx ==========
ErrorCode POLICY_NOT_EXISTS = new ErrorCode(1_050_010_000, "政策不存在");
ErrorCode POLICY_CODE_EXISTS = new ErrorCode(1_050_010_001, "政策编码已存在");
ErrorCode POLICY_INDICATOR_INVALID = new ErrorCode(1_050_010_002, "指标数据不合法");
```

---

## Task 4: CRUD Service + Controller + VO（~8 个文件）

新建：
1. `service/policy/DealerPolicyService.java` — 接口
2. `service/policy/DealerPolicyServiceImpl.java` — 实现（含 policyCode 自动生成、指标级联维护）
3. `controller/admin/policy/DealerPolicyController.java` — 5 个接口
4. `controller/admin/policy/vo/DealerPolicySaveReqVO.java` — 含内嵌 `IndicatorSaveVO` 列表
5. `controller/admin/policy/vo/DealerPolicyRespVO.java`
6. `controller/admin/policy/vo/DealerPolicyDetailRespVO.java` — 含指标列表
7. `controller/admin/policy/vo/DealerPolicyPageReqVO.java` — 继承 PageParam

**policyCode 自动生成逻辑**：参考 `SigningContractMapper` 中的合同编码生成，`POL-{年份}-{序号}`。

**事务**：create/update 方法加 `@Transactional`（政策+指标同步）。

---

## Task 5: KPI 查询（5 个 VO + Controller 扩展）

新建 VO：
1. `vo/PolicyKpiPanelRespVO.java` — indicatorName/unit/policyCount + `List<TimeDataVO> timeData`（label/achieved/target/rate）
2. `vo/PolicyKpiPanelPageReqVO.java` — policyTypes/indicatorNames/achievementTypes/months/productLineCodes/dealerIds/policyCode（逗号分隔 String）
3. `vo/PolicyIndicatorChartRespVO.java` — indicatorName + timeLabels + bars（achieved/target/rate）
4. `vo/PolicyTargetGroupRespVO.java` — targetValue/policyCount + `List<PolicyBriefVO> policies`
5. `vo/PolicyAchievementRespVO.java` — name/achievedValue

在 `DealerPolicyController` 中新增 5 个 GET 接口（kpi-panels/indicator-chart/target-distribution/achievement-drilldown/indicator-names）。

**KPI 聚合核心逻辑**：
```java
// 1. 构建查询条件，查 ops_dealer_policy_indicator JOIN ops_dealer_policy
// 2. 按 indicator_name 分组
// 3. 季度政策：target_month 3→Q1, 6→Q2, 9→Q3, 12→Q4
// 4. 月度政策：直接按 target_month 展示
// 5. rate = achieved / target * 100（target=0 时返回 0）
```

---

## Task 6: 达成明细 CRUD Controller（3 个文件）

新建：
1. `controller/admin/policy/DealerPolicyAchievementController.java`
2. `controller/admin/policy/vo/DealerPolicyAchievementSaveReqVO.java`
3. `controller/admin/policy/vo/DealerPolicyAchievementPageReqVO.java`

4 个接口：create/update/delete/page，权限码复用 `dealer:policy:*`。

---

## Task 7: Excel 导入 VO + Convert（7 个文件）

新建 VO（`controller/admin/excel/vo/`）：
1. `PolicyImportExcelVO.java` — 11 列，4 个枚举字段用 converter
2. `PolicyIndicatorImportExcelVO.java` — 6 列
3. `PolicyAchievementImportExcelVO.java` — 10 列

新建 Convert（`controller/admin/excel/convert/`）：
4. `PolicyTypeConvert.java` — 返利↔rebate, 促销↔promotion, 其他↔other
5. `PolicyAchievementTypeConvert.java` — 季度政策↔quarter, 月度政策↔month
6. `PolicyStatusConvert.java` — 执行中↔executing, 待执行↔pending, 已完成↔completed
7. `AchievementLevelConvert.java` — 省级↔province, 医院级↔hospital, 产品级↔product

---

## Task 8: Excel 导入 Service（修改 3 个文件）

修改：
1. `service/excel/OpsExcelImportService.java` — 新增 3 个方法签名
2. `service/excel/OpsExcelImportServiceImpl.java` — 实现 3 个 import 方法
3. `controller/admin/excel/OpsExcelImportController.java` — switch 中新增 3 个 type 分支

**importPolicyList 核心逻辑**：
```java
@Transactional
public ExcelImportRespVO importPolicyList(List<PolicyImportExcelVO> list) {
    Map<String, DealerPolicyDO> existMap = // policyCode → DO
    Map<String, SigningContractDO> contractMap = // contractCode → DO
    for (each vo) {
        try {
            validate(vo);
            DealerInfoDO dealer = dealerMap.get(vo.getDealerCode()); // 查经销商
            Long sourceContractId = null;
            if (vo.getContractCode() != null) {
                SigningContractDO contract = contractMap.get(vo.getContractCode());
                if (contract == null) throw "合同编码不存在";
                sourceContractId = contract.getId();
            }
            // upsert
        } catch { failureRows.put(rowNo, e.getMessage()); }
    }
}
```

**importPolicyIndicatorList / importPolicyAchievementList**：类似模式，按复合键 upsert。

---

## Task 9: 前端 API（1 个文件）

新建 `src/api/opshub/policy/index.ts`：
- TypeScript 类型定义：`PolicyKpiPanel`, `PolicyTimeData`, `PolicyIndicatorChart`, `PolicyTargetGroup`, `PolicyAchievementItem`
- 6 个 API 函数：getKpiPanels, getIndicatorChart, getTargetDistribution, getAchievementDrilldown, getIndicatorNames, + CRUD（getPage, get, create, update, delete）

---

## Task 10: 前端看板页面（8 个文件）

新建 `src/views/opshub/policy/`：
1. `index.vue` — 主页面，组合子组件，defineOptions({ name: 'OpshubPolicy' })
2. `components/PolicyFilterBar.vue` — 筛选器（复用 ms-dropdown 多选组件）
3. `components/KpiPanelGrid.vue` — CSS Grid 布局，每行 2-3 个面板
4. `components/KpiPanel.vue` — 标题 + ECharts gauge 仪表盘 + TargetDistribution
5. `components/IndicatorChart.vue` — ECharts bar 柱状图（颜色：≥100% #52c41a, ≥60% #faad14, <60% #ff4d4f）
6. `components/TargetDistribution.vue` — el-tag 标签组 + 排序切换
7. `components/PolicyDetailModal.vue` — el-dialog 政策详情 + 指标 el-table
8. `components/AchievementDrilldown.vue` — el-dialog 三级面包屑下钻

---

## Task 11: 前端 Excel 导入卡片（修改 1 个文件）

修改 `src/views/opshub/excelImport/index.vue`：
- 新增 L5 分组「政策看板」(tag: `primary`)
- 3 个 ImportCardItem：policy / policy-indicator / policy-achievement

---

## Task 12: 编译验证

- 后端：`mvn compile -pl yudao-module-opshub -am`
- 前端：无 TypeScript 报错

---

## 实施顺序（按依赖编排）

| 批次 | Task | 内容 | 可并行 |
|------|------|------|--------|
| 1 | T1 | DDL/DML | — |
| 2 | T2+T3 | DO+Mapper / 枚举+错误码 | 并行 |
| 3 | T4+T5+T6 | CRUD / KPI 查询 / 达成明细 CRUD | 并行（均依赖 T2+T3） |
| 4 | T7+T8 | Excel VO+Convert / Excel Service | 串行（T8 依赖 T7+T4） |
| 5 | T9 | 前端 API | 依赖 T4+T5+T6 |
| 6 | T10+T11 | 看板页面 / Excel 导入卡片 | 并行 |
| 7 | T12 | 编译验证 | — |

## 验证方式

1. DDL 在测试 DB 执行成功
2. 后端 `mvn compile` 通过
3. Swagger 调用 CRUD 接口正常
4. Excel 导入模板下载 + 上传导入正常
5. 前端看板页面渲染正常（筛选/面板/柱状图/钻取）
