# 授权管理页面重构方案

## Context

当前授权管理页面（`/dealer/scope-mgmt`）采用"下拉选用户 → 管理授权"的交互模式，两个 Tab 共用全量用户列表，无法直观看到已维护的授权数据，且用户选择未按角色过滤。

**目标**：重构为"表格展示已有授权数据 + 抽屉新增授权"模式，Tab 只展示有授权记录的用户，新增抽屉按角色过滤可选用户。

---

## Task 1: 后端 — system 模块增加按 roleCode 过滤用户能力

### 1.1 AdminUserService 新增方法

**文件**: `yudao-module-system/.../service/user/AdminUserService.java`

```java
/** 根据角色编码获取启用状态的用户列表 */
List<AdminUserDO> getUserListByRoleCode(String roleCode);
```

### 1.2 AdminUserServiceImpl 实现

**文件**: `yudao-module-system/.../service/user/impl/AdminUserServiceImpl.java`

- 新增注入 `RoleMapper`
- 实现逻辑：`RoleMapper.selectByCode(roleCode)` → roleId → `permissionService.getUserRoleIdListByRoleId(singleton(roleId))` → userIds → `userMapper.selectByIds(userIds)` → 过滤 status=ENABLE

已有可复用方法链：
- `RoleMapper.selectByCode(String)` ✅
- `PermissionService.getUserRoleIdListByRoleId(Collection<Long>)` ✅
- `AdminUserMapper.selectByIds(Collection<Long>)` ✅ (继承自 MyBatis Plus)

### 1.3 UserController 增加 roleCode 参数

**文件**: `yudao-module-system/.../controller/admin/user/UserController.java` (第133-149行)

在 `getSimpleUserList` 端点增加可选参数 `roleCode`：

```java
@GetMapping({"/list-all-simple", "/simple-list"})
public CommonResult<List<UserSimpleRespVO>> getSimpleUserList(
        @RequestParam(value = "deptId", required = false) Long deptId,
        @RequestParam(value = "roleCode", required = false) String roleCode) {
    List<AdminUserDO> list;
    if (StrUtil.isNotBlank(roleCode)) {
        list = userService.getUserListByRoleCode(roleCode);
    } else if (deptId != null) {
        list = userService.getDeptUsers(Collections.singletonList(deptId));
    } else {
        list = userService.getUserListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }
    Map<Long, DeptDO> deptMap = deptService.getDeptMap(
            convertList(list, AdminUserDO::getDeptId));
    return success(UserConvert.INSTANCE.convertSimpleList(list, deptMap));
}
```

不传 roleCode 时行为不变，向后兼容。

---

## Task 2: 后端 — opshub 模块新增"获取所有授权用户"API

### 2.1 新增两个 VO

**新文件 1**: `yudao-module-opshub/.../controller/admin/dealer/vo/ExecutorScopeUserRespVO.java`

```java
@Schema(description = "管理后台 - 执行员产品线授权用户 Response VO")
@Data
public class ExecutorScopeUserRespVO {
    @Schema(description = "用户ID") private Long userId;
    @Schema(description = "用户昵称") private String nickname;
    @Schema(description = "已授权的产品线编码列表") private Set<String> productLineCodes;
}
```

**新文件 2**: `yudao-module-opshub/.../controller/admin/dealer/vo/DealerScopeUserRespVO.java`

```java
@Schema(description = "管理后台 - 经销商授权用户 Response VO")
@Data
public class DealerScopeUserRespVO {
    @Schema(description = "用户ID") private Long userId;
    @Schema(description = "用户昵称") private String nickname;
    @Schema(description = "已授权的经销商编码列表") private Set<String> dealerCodes;
}
```

### 2.2 Mapper 层

- `ExecutorProductLineScopeMapper` — 无需新增方法，BaseMapperX 的 `selectList()` 即可查全表
- `DealerUserScopeMapper` — 同上

### 2.3 Service 层

**`ExecutorProductLineScopeService`** 新增接口：
```java
Map<Long, Set<String>> getAllUserProductLineScopeMap();
```

**`ExecutorProductLineScopeServiceImpl`** 实现：查全表 → `stream().collect(groupingBy(userId, mapping(productLineCode, toSet())))`

**`DealerUserScopeService`** 新增接口：
```java
Map<Long, Set<String>> getAllUserDealerScopeMap();
```

**`DealerUserScopeServiceImpl`** 实现：同理。

### 2.4 Controller 层

**`DealerProductLineScopeController`** 新增端点：

```
GET /opshub/product-line-scope/scope-users
```
- 调用 `getAllUserProductLineScopeMap()` 获取 userId → codes 映射
- 通过 `AdminUserApi.getUserMap(userIds)` 批量获取用户信息
- 组装 `List<ExecutorScopeUserRespVO>` 返回

**`DealerUserScopeController`** 新增端点：

```
GET /opshub/dealer-scope/scope-users
```
- 同上，组装 `List<DealerScopeUserRespVO>`

两个 Controller 需新增注入 `AdminUserApi`（opshub pom 已依赖 system 模块 ✅）。

### 2.5 API 端点汇总

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/system/user/simple-list?roleCode=xxx` | 按角色过滤用户（修改） |
| GET | `/opshub/product-line-scope/scope-users` | 所有有产品线授权的用户（新增） |
| GET | `/opshub/dealer-scope/scope-users` | 所有有经销商授权的用户（新增） |
| POST | `/opshub/product-line-scope/assign` | 分配产品线授权（已有不变） |
| POST | `/opshub/dealer-scope/assign` | 分配经销商授权（已有不变） |

---

## Task 3: 前端 — API 层更新

### 3.1 `src/api/system/user/index.ts`

```typescript
export const getSimpleUserList = (roleCode?: string): Promise<UserVO[]> => {
  return request.get({ url: '/system/user/simple-list', params: { roleCode } })
}
```

### 3.2 `src/api/opshub/dealerScope/index.ts`

新增：
```typescript
export const getExecutorScopeUsers = async () => {
  return await request.get({ url: '/opshub/product-line-scope/scope-users' })
}
export const getDealerScopeUsers = async () => {
  return await request.get({ url: '/opshub/dealer-scope/scope-users' })
}
```

---

## Task 4: 前端 — 页面重构

**文件**: `src/views/opshub/scope/index.vue`（整体重写）

### 4.1 布局结构

```
ContentWrap
  el-tabs
    Tab "服务单执行员"
      工具栏：[新增] 按钮
      el-table（已有授权用户列表）
        列：用户昵称 | 已授权产品线（el-tag） | 操作（移除）
      el-drawer（新增授权）
        表单：用户下拉（service_executor 角色） + 产品线多选 + 提交

    Tab "经销商"
      工具栏：[新增] 按钮
      el-table（已有授权用户列表）
        列：用户昵称 | 已授权经销商（el-tag） | 操作（移除）
      el-drawer（新增授权）
        表单：用户下拉（dealer 角色） + 经销商多选 + 提交
```

### 4.2 数据加载

- Tab 主表格数据：调用 `getExecutorScopeUsers()` / `getDealerScopeUsers()`
- 抽屉用户下拉：懒加载，首次打开抽屉时调用 `getSimpleUserList('service_executor')` / `getSimpleUserList('dealer')`
- 产品线/经销商名称映射：初始化时加载 `getSimpleProductLineList()` / `getSimpleDealerList()` 构建 code→name map

### 4.3 Tag 展示与操作

- 每个用户的授权以 `el-tag` 展示，tag 支持 `closable` 可单独移除
- 移除单个：从现有 codes 中去除该 code，调用 `assign` 接口提交
- 移除全部：调用 `assign` 接口传空数组

### 4.4 抽屉新增交互

1. 选择用户（按角色过滤）
2. 选择要授权的产品线/经销商（多选，排除该用户已有的）
3. 提交：获取用户已有 codes + 新增 codes，调用 `assign` 接口
4. 关闭抽屉，刷新 Tab 列表

---

## 约束

- `ops_` 前缀表**不建唯一索引**
- assign 接口采用"先删后插"模式，通过 `@Transactional` 保证原子性

## 验证

1. 后端编译：`mvn clean compile -pl yudao-module-system,yudao-module-opshub`
2. 前端编译：`cd yudao-ui/yudao-ui-admin-vue3 && pnpm lint`
3. 页面验证：
   - 访问 `/dealer/scope-mgmt`，两个 Tab 分别只展示有授权记录的用户
   - 执行员 Tab 表格展示用户 + 产品线 Tags
   - 经销商 Tab 表格展示用户 + 经销商 Tags
   - 点击"新增"打开抽屉，用户下拉仅显示对应角色的用户
   - 提交新增后列表自动刷新
   - 单个 Tag 可关闭移除授权

## 涉及文件

### 修改
| 文件 | 模块 |
|------|------|
| `UserController.java` | system |
| `AdminUserService.java` | system |
| `AdminUserServiceImpl.java` | system |
| `ExecutorProductLineScopeService.java` | opshub |
| `ExecutorProductLineScopeServiceImpl.java` | opshub |
| `DealerUserScopeService.java` | opshub |
| `DealerUserScopeServiceImpl.java` | opshub |
| `DealerProductLineScopeController.java` | opshub |
| `DealerUserScopeController.java` | opshub |
| `src/api/system/user/index.ts` | 前端 |
| `src/api/opshub/dealerScope/index.ts` | 前端 |
| `src/views/opshub/scope/index.vue` | 前端 |

### 新增
| 文件 | 说明 |
|------|------|
| `ExecutorScopeUserRespVO.java` | 执行员授权用户响应 VO |
| `DealerScopeUserRespVO.java` | 经销商授权用户响应 VO |
