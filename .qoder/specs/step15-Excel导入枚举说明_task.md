# Step15 — Excel 导入枚举说明

## Context

PRD: `docs/PRD-Step15-Excel导入枚举说明.md`
为 13 张 Excel 导入模板增加枚举字段说明：VO 添加 @ExcelColumnSelect → 注册 Function Bean → 模板增加字段说明 Sheet → 前端卡片增加折叠面板。

---

## Task 1: 注册 ExcelColumnSelectFunction Bean（14 个实现）

**新建文件**：`yudao-module-opshub/.../framework/excel/function/OpsExcelSelectFunctions.java`

- 创建 14 个 @Component 静态内部类，实现 ExcelColumnSelectFunction
- 每个类的 getName() 返回约定 functionName
- 每个类的 getOptions() 返回对应中文标签列表

---

## Task 2: ImportExcelVO 枚举字段添加 @ExcelColumnSelect（11 个 VO）

**修改文件**：11 个 `*ImportExcelVO.java`

- 在每个有 converter 的字段上追加 `@ExcelColumnSelect(functionName = "xxx")`
- 添加 import 语句

---

## Task 3: Excel 模板增加「字段说明」Sheet 页

**修改文件**：`OpsExcelImportController.java`

- 改用 FastExcelFactory.write() 多 Sheet 写入
- Sheet 1「数据」：表头 + 示例数据 + SelectSheetWriteHandler
- Sheet 2「字段说明」：字段名称 / 字段类型 / 可选值

---

## Task 4: 前端卡片增加枚举说明折叠面板

**修改文件**：`yudao-ui/yudao-ui-admin-vue3/src/views/opshub/excelImport/index.vue`

- ImportCardItem 接口增加 enumFields
- 11 张卡片配置补充 enumFields 数据
- ImportCard 组件增加 el-collapse 折叠面板

---

## Task 5: 编译验证

- 后端：`mvn compile -pl yudao-module-opshub` 通过
- 前端：无 TypeScript 报错

---

## 实施顺序

| Task | 内容 | 依赖 |
|------|------|------|
| 1 | 注册 SelectFunction Bean | 无 |
| 2 | VO 添加 @ExcelColumnSelect | Task 1 |
| 3 | 模板增加字段说明 Sheet | Task 1, 2 |
| 4 | 前端折叠面板 | 无 |
| 5 | 编译验证 | Task 1-4 |
