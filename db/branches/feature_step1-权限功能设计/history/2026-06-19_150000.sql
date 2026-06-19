-- 执行时间: 2026-06-19 15:00:00
-- 分支: feature/step1-权限功能设计
-- 操作说明: 将一级菜单"经销商管理 SaaS"重命名为"聚院通"

UPDATE system_menu SET name = '聚院通' WHERE id = 6000;
