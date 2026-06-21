---
kind: design
name: 采用 Service 层角色可见性过滤与数据权限叠加机制
source: session
category: adr
---

# 采用 Service 层角色可见性过滤与数据权限叠加机制

_来源：3722ab2 → 11502cc 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
客户服务模块（工单 ops_cs_task 和操作请求 ops_cs_opreq）需要实现细粒度的数据可见性控制：经销商仅看自己发起的，执行员仅看待接单或指派给自己的，而管理员/销售员可查看范围内全部。现有的 DealerDataPermissionRule 仅基于 dealer_code 和 product_line_code 进行 SQL 拦截，无法满足基于 creator_user_id 或 assignee_id 的用户级过滤需求。

## 决策驱动
- 职责分离：数据权限（租户/组织维度）与业务可见性（个人维度）解耦
- 实现清晰度：避免在通用的 DataPermission 拦截器中硬编码特定业务的用户级逻辑
- 灵活性：允许不同角色拥有完全不同的查询逻辑（如 OR 条件 vs AND 条件）

## 备选方案
- **在 Service 层动态注入可见性过滤条件** — 优点：逻辑清晰，与现有的 DealerDataPermissionRule 正交叠加；可针对不同角色定制复杂 SQL 条件（如执行员的 status=0 OR assignee_id=me）；不污染通用基础设施代码。；缺点：需要在每个涉及分页查询的 Service 方法中手动添加角色判断逻辑；需确保前端或 Service 层正确传递当前用户上下文。
- **扩展 DealerDataPermissionRule 支持用户级过滤** _（已否决）_ — 优点：统一在 SQL 拦截器层处理，对 Service 层透明。；缺点：DataPermission 规则通常设计为通用的组织/租户维度，混入 userId 维度的业务逻辑会导致规则复杂化且难以复用；难以处理执行员那种“未接单 OR 我的”这种非单纯的数据归属逻辑。

## 决策
采用双层过滤机制：底层由 DealerDataPermissionRule 通过 SQL 拦截器强制约束 dealer_code 和 product_line_code 的数据权限；上层在 CsTaskServiceImpl 和 CsOpReqServiceImpl 的分页查询方法中，根据当前用户角色（brand_admin/dealer/service_executor 等）动态构建额外的 WHERE 条件（如 creator_user_id = ? 或 (status = 0 OR assignee_id = ?)），两者通过 AND 逻辑叠加生效。

## 影响
实现了灵活且安全的角色数据可见性控制。开发人员需在 Service 层维护角色过滤逻辑，但保持了数据权限基础设施的通用性。需注意在执行员查询时，SQL 条件需正确处理 OR 逻辑与数据权限 AND 逻辑的结合，确保不会因数据权限过滤掉本应看到的待接单公海工单（若待接单工单无明确 dealer_code 归属则需特殊处理，但计划中假设均有 dealer_code 约束）。