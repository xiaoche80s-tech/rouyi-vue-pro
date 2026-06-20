# 新增流程管理员角色以统一跨模块转派权限

_来源：a7aeacb → 32a9ec7 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
当前 OpsHub 工单转派仅限 brand_admin/super_admin，而 BPM 模块转派仅限任务当前处理人。缺乏一个既能查看全局工单又能执行管理员级转派（绕过个人任务限制）的专用角色，导致流程运维灵活性不足。

## 决策驱动
- 满足流程运维人员跨模块（OpsHub + BPM）的管理需求
- 最小化对现有超级管理员角色的依赖
- 保持 BPM 模块原有权限校验逻辑的完整性

## 备选方案
- **复用 super_admin 或 brand_admin** _（已否决）_ — 优点：无需新增角色和代码；缺点：权限过大，不符合最小权限原则；无法区分业务管理员与流程运维人员
- **新增 process_admin 内置角色** — 优点：权限职责清晰；可在 BPM 和 OpsHub 中分别配置细粒度菜单权限；通过代码注入 PermissionApi 实现非侵入式权限增强；缺点：需同步修改 BPM 和 OpsHub 两个模块的权限校验逻辑；需维护新的角色常量和数据初始化脚本

## 决策
新增 code 为 process_admin 的内置角色（ID 161）。在 OpsHub 的 CsTaskServiceImpl 和 BPM 的 BpmTaskServiceImpl 中，通过注入 PermissionApi 检查该角色。若用户拥有 process_admin 或 super_admin 角色，则跳过“必须是当前处理人”的校验，允许执行转派操作。同时配置对应的菜单和数据权限。

## 影响
流程管理员获得了跨模块的工单干预能力；BPM 模块引入了对 PermissionApi 的依赖，增加了模块间的耦合度；需确保新角色的菜单授权在不同租户下正确初始化。