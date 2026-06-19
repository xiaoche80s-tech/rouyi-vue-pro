# 基于立场（Side）抽象工单列表组件

_来源：ebb1cd2 → 9465c04 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
原有的客户服务页面同时服务经销商和执行员/管理员，导致 TaskTab.vue 内部通过 useUserStore 硬编码检测具体角色（dealer/executor/admin）来决定子标签和行为。这种耦合使得组件难以扩展（如新增“医院”角色需修改组件内部逻辑），且不利于多入口页面的复用。

## 决策驱动
- 组件解耦与复用性
- 扩展性（支持新发起方角色）
- 关注点分离（UI 行为与身份认证分离）

## 备选方案
- **保留内部角色检测** _（已否决）_ — 优点：父页面无需传参，使用简单；缺点：组件与具体业务角色强耦合，每增加一种新角色（如医院、供应商）都需修改组件内部判断逻辑，违反开闭原则
- **引入 side prop（initiator/handler/admin）** — 优点：组件只关心业务立场（发起方/处理方/管理方），不关心具体用户角色；新增发起方角色只需在新页面传入 side='initiator'，组件代码零改动；缺点：父页面需要明确知道当前用户的业务立场并传参

## 决策
重构 TaskTab.vue，移除内部的角色检测逻辑，改为接收 side prop（'initiator' | 'handler' | 'admin'）。父页面 customerservice/index.vue 传入 side='handler'，新建的 workorder-service/index.vue 传入 side='initiator'。子标签配置和操作按钮可见性完全由 side 决定。

## 影响
TaskTab 组件不再依赖 userStore 中的角色信息进行 UI 渲染，实现了业务逻辑与身份认证的解耦。未来新增其他发起方角色（如医院）时，只需创建新页面并传入 side='initiator'，无需修改共享组件。