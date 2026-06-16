# 关联表采用业务编码（Code）替代ID进行外键关联

_来源：e1e55ae → c750ce2 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
在 opshub 模块的经销商与产品线关联设计中，原方案使用数据库自增 ID（dealer_id, product_line_id）作为关联字段。为提升业务可读性、支持跨环境数据迁移以及简化前端交互（直接使用业务唯一标识），决定重构关联逻辑。

## 决策驱动
- 业务语义清晰性
- 数据迁移与集成便利性
- 前端交互简化

## 备选方案
- **使用业务编码（Code）关联** — 优点：字段具有业务含义，便于调试和人工核对；不依赖特定环境的自增ID序列，利于多租户或跨库数据同步；前端可直接传递 code 无需额外查 ID。；缺点：关联字段由 int8 变为 varchar(50)，索引体积略增；需确保 Code 的唯一性和稳定性。
- **使用数据库主键 ID 关联** _（已否决）_ — 优点：传统关系型数据库标准做法，索引效率高，存储空间小。；缺点：无业务含义，调试困难；不同环境 ID 不一致导致数据迁移复杂；前端需维护 ID 映射。

## 决策
在 `ops_dealer_product_line_relation`、`ops_dealer_user_scope` 和 `ops_executor_product_line_scope` 表中，将关联字段从 `dealer_id`/`product_line_id` (int8) 改为 `dealer_code`/`product_line_code` (varchar(50))。同时，将主表 `ops_dealer_info` 和 `ops_dealer_product_line` 中的 `code` 字段重命名为语义更明确的 `dealer_code` 和 `product_line_code`，并建立唯一索引。后端 DO/Mapper/Service/VO 及前端 API 全链路同步修改类型与字段名。

## 影响
提升了系统的业务可维护性和数据集成能力。但增加了字符串比较的性能开销（虽通过索引优化可控），且要求业务编码一旦生成不可随意变更。物理删除策略被应用于部分关联表（如移除 `deleted` 字段）以简化状态管理。