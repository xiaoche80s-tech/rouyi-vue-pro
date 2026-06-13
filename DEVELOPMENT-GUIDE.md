# 芋道（ruoyi-vue-pro）软件开发规范

> 本文档是芋道快速开发平台的完整开发规范，涵盖后端架构、命名、API 设计、数据库、测试及前端开发等方面，可直接指导日常开发。

---

## 目录

- [第一章：项目架构与分层规范](#第一章项目架构与分层规范)
- [第二章：命名规范](#第二章命名规范)
- [第三章：REST API 设计规范](#第三章rest-api-设计规范)
- [第四章：数据库与 ORM 规范](#第四章数据库与-orm-规范)
- [第五章：VO/DTO 设计规范](#第五章vodto-设计规范)
- [第六章：异常与错误码规范](#第六章异常与错误码规范)
- [第七章：Service 层规范](#第七章service-层规范)
- [第八章：测试规范](#第八章测试规范)
- [第九章：前端开发规范](#第九章前端开发规范)
- [第十章：构建与版本管理](#第十章构建与版本管理)

---

## 第一章：项目架构与分层规范

### 1.1 模块层次

项目采用 Maven 多模块架构，整体结构如下：

```
yudao-dependencies     — BOM 依赖版本管理（所有第三方版本在此锁定）
yudao-framework/       — 技术组件层（Spring Boot Starter 封装）
  yudao-common           — 通用 POJO、异常、工具类
  yudao-spring-boot-starter-web       — Web 层封装（全局异常、API 返回格式）
  yudao-spring-boot-starter-security  — 权限认证（Spring Security + Token）
  yudao-spring-boot-starter-mybatis   — MyBatis Plus 增强（BaseMapperX、BaseDO）
  yudao-spring-boot-starter-redis     — Redis + Redisson 封装
  yudao-spring-boot-starter-mq        — 消息队列（Redis Stream/Pub-Sub）
  yudao-spring-boot-starter-test      — 测试基础设施（BaseMockitoUnitTest 等）
  yudao-spring-boot-starter-biz-tenant         — 多租户
  yudao-spring-boot-starter-biz-data-permission — 数据权限
yudao-module-system    — 系统管理（用户、角色、菜单、租户、字典等）
yudao-module-infra     — 基础设施（代码生成、文件存储、API 日志等）
yudao-module-im        — 即时通讯（好友、群组、消息、频道）
yudao-module-bpm       — 工作流（Flowable）
yudao-server           — 应用入口（空壳容器，通过依赖组合启用模块）
```

### 1.2 业务模块内部包结构

每个 `yudao-module-xxx` 遵循统一的包结构，以 `cn.iocoder.yudao.module.xxx` 为根包：

```
controller/admin/    — REST API 控制器（admin 后台接口）
  └── vo/            — 请求/响应 VO 对象（XxxSaveReqVO, XxxxRespVO, XxxPageReqVO）
service/             — 业务逻辑层
dal/                 — 数据访问层
  ├── dataobject/    — DO 对象（继承 BaseDO）
  └── mysql/         — MyBatis Mapper 接口（继承 BaseMapperX）
convert/             — MapStruct 转换器（DO ↔ VO 转换）
enums/               — 枚举常量
api/                 — 模块间 API 接口定义（供其他模块调用）
framework/           — 模块级配置类
```

### 1.3 依赖方向

```
Controller → Service → DAL
```

**严格禁止反向依赖。** Controller 层调用 Service 层，Service 层调用 DAL 层，不允许下层反向引用上层。

### 1.4 核心基类与约定

| 基类/工具 | 说明 |
|-----------|------|
| `BaseDO` | 所有 DO 实体的基类，提供 `createTime`、`updateTime`、`creator`、`updater`、`deleted` 字段，自动填充 |
| `BaseMapperX<T>` | MyBatis Plus BaseMapper 增强，提供分页查询、批量插入等便捷方法 |
| `CommonResult<T>` | 统一 API 返回格式，包含 `code`、`msg`、`data` |
| `PageParam` / `PageResult` | 分页请求与响应 |
| `ErrorCode` | 错误码体系，每个模块定义自己的错误码常量 |

> **注意**：项目根目录 `lombok.config` 已启用 `chain=true`（链式 setter）和 `toString/equals` 调用 super。

---

## 第二章：命名规范

### 2.1 类命名

| 类型 | 命名模式 | 示例 |
|------|---------|------|
| Controller | `{Domain}Controller` | `DeptController` |
| Service 接口 | `{Domain}Service` | `DeptService` |
| Service 实现 | `{Domain}ServiceImpl` | `DeptServiceImpl` |
| Mapper | `{Domain}Mapper` | `DeptMapper` |
| DO 对象 | `{Domain}DO` | `DeptDO` |
| 保存请求 VO | `{Domain}SaveReqVO` | `DeptSaveReqVO` |
| 分页请求 VO | `{Domain}PageReqVO` | `PostPageReqVO` |
| 列表请求 VO | `{Domain}ListReqVO` | `DeptListReqVO` |
| 响应 VO | `{Domain}RespVO` | `DeptRespVO` |
| 精简响应 VO | `{Domain}SimpleRespVO` | `DeptSimpleRespVO` |
| 转换器 | `{Domain}Convert` | `AuthConvert` |
| 错误码常量 | `ErrorCodeConstants` | — |

### 2.2 方法命名

| 场景 | 命名模式 | 返回值 |
|------|---------|--------|
| 创建 | `create{Domain}` | `Long`（ID） |
| 修改 | `update{Domain}` | `void` |
| 删除单个 | `delete{Domain}` | `void` |
| 删除多个 | `delete{Domain}List` | `void` |
| 获取单个 | `get{Domain}` | `{Domain}DO` |
| 获取多个 | `get{Domain}List` | `List<{Domain}DO>` |
| 获取 Map | `get{Domain}Map` | `Map<Long, {Domain}DO>` |
| 分页查询 | `get{Domain}Page` | `PageResult<{Domain}DO>` |
| 校验存在 | `validate{Domain}Exists` | `void`（不存在抛异常） |

### 2.3 包命名

统一格式：`cn.iocoder.yudao.module.{moduleCode}.{layer}.{domain}`

示例：
- `cn.iocoder.yudao.module.system.controller.admin.dept`
- `cn.iocoder.yudao.module.system.service.dept`
- `cn.iocoder.yudao.module.system.dal.dataobject`

### 2.4 错误码编号规则

格式：`1_ABC_DEF_GHI`

| 段 | 含义 | 示例 |
|----|------|------|
| `1` | 固定业务码段前缀 | — |
| `ABC` | 模块编码 | system=002, infra=001, im=003, bpm=004 |
| `DEF` | 功能域编码 | dept=004, user=003 等 |
| `GHI` | 序列编号 | 从 000 递增 |

示例：`1_002_004_000` 表示 system 模块 dept 功能域的第 0 号错误。

---

## 第三章：REST API 设计规范

### 3.1 URL 路径设计

遵循 RESTful 风格，统一使用小写短横线分隔：

```
GET    /module/domain/list              # 获取列表
GET    /module/domain/page              # 分页查询
GET    /module/domain/get?id=1024       # 获取单个
GET    /module/domain/simple-list       # 获取精简列表
POST   /module/domain/create            # 创建
PUT    /module/domain/update            # 更新
DELETE /module/domain/delete?id=1024    # 删除
```

### 3.2 统一响应格式

所有接口统一使用 `CommonResult<T>` 包装返回结果：

```json
{
    "code": 0,
    "msg": "",
    "data": {}
}
```

- `code`：`0` 表示成功，非 `0` 表示错误码
- `msg`：错误时的提示信息
- `data`：实际业务数据

### 3.3 分页约定

**请求参数**（继承 `PageParam`）：

| 参数 | 类型 | 说明 |
|------|------|------|
| `pageNo` | Integer | 页码，从 1 开始，默认 1 |
| `pageSize` | Integer | 每页条数，默认 10，最大 200 |

**响应格式**（`PageResult<T>`）：

```json
{
    "total": 100,
    "list": [...]
}
```

### 3.4 权限注解

每个需要鉴权的接口使用 `@PreAuthorize` 注解：

```java
@PreAuthorize("@ss.hasPermission('module:domain:action')")
```

### 3.5 Controller 方法示例

```java
@RestController
@RequestMapping("/system/dept")
@Validated
@Tag(name = "管理后台 - 部门")
public class DeptController {

    @Resource
    private DeptService deptService;

    @PostMapping("create")
    @PreAuthorize("@ss.hasPermission('system:dept:create')")
    public CommonResult<Long> createDept(@Valid @RequestBody DeptSaveReqVO createReqVO) {
        Long deptId = deptService.createDept(createReqVO);
        return success(deptId);
    }

    @PutMapping("update")
    @PreAuthorize("@ss.hasPermission('system:dept:update')")
    public CommonResult<Boolean> updateDept(@Valid @RequestBody DeptSaveReqVO updateReqVO) {
        deptService.updateDept(updateReqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @PreAuthorize("@ss.hasPermission('system:dept:delete')")
    public CommonResult<Boolean> deleteDept(@RequestParam("id") Long id) {
        deptService.deleteDept(id);
        return success(true);
    }

    @GetMapping("get")
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public CommonResult<DeptRespVO> getDept(@RequestParam("id") Long id) {
        DeptDO dept = deptService.getDept(id);
        return success(BeanUtils.toBean(dept, DeptRespVO.class));
    }

    @GetMapping("list")
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public CommonResult<List<DeptRespVO>> getDeptList(DeptListReqVO listReqVO) {
        List<DeptDO> list = deptService.getDeptList(listReqVO);
        return success(BeanUtils.toBean(list, DeptRespVO.class));
    }
}
```

---

## 第四章：数据库与 ORM 规范

### 4.1 表命名

统一使用 `{module}_{domain}` 格式，小写下划线分隔：

- `system_dept`
- `system_user`
- `infra_file`
- `im_message`

### 4.2 DO 实体类

DO 类必须继承 `BaseDO`（普通场景）或 `TenantBaseDO`（需要多租户隔离的场景）：

```java
@TableName("system_dept")
@KeySequence("system_dept_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptDO extends TenantBaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    @TableId
    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    private Integer status;
}
```

**BaseDO 自动填充字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `createTime` | LocalDateTime | 创建时间，自动填充 |
| `updateTime` | LocalDateTime | 更新时间，自动填充 |
| `creator` | String | 创建者 |
| `updater` | String | 更新者 |
| `deleted` | Boolean | 逻辑删除标记 |

`TenantBaseDO` 额外提供 `tenantId` 字段。

### 4.3 Mapper 接口

所有 Mapper 继承 `BaseMapperX<T>`，查询方法使用 `default` 方法 + `LambdaQueryWrapperX` 构建：

```java
@Mapper
public interface DeptMapper extends BaseMapperX<DeptDO> {

    default List<DeptDO> selectList(DeptListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DeptDO>()
                .likeIfPresent(DeptDO::getName, reqVO.getName())
                .eqIfPresent(DeptDO::getStatus, reqVO.getStatus())
                .orderByAsc(DeptDO::getSort));
    }

    default PageResult<DeptDO> selectPage(DeptPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeptDO>()
                .likeIfPresent(DeptDO::getName, reqVO.getName())
                .eqIfPresent(DeptDO::getStatus, reqVO.getStatus())
                .orderByDesc(DeptDO::getId));
    }
}
```

> **提示**：`LambdaQueryWrapperX` 的 `xxxIfPresent` 方法会自动忽略 `null` 和空字符串参数，避免手动判空。

---

## 第五章：VO/DTO 设计规范

### 5.1 SaveReqVO（创建与修改共用）

创建和修改操作共用同一个 VO，通过 `id` 字段区分：修改时 `id` 必传，创建时为空。

```java
@Schema(description = "管理后台 - 部门创建/修改 Request VO")
@Data
public class DeptSaveReqVO {

    @Schema(description = "部门编号", example = "1024")
    private Long id;  // 修改时必传，创建时为空

    @Schema(description = "部门名称")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 30, message = "部门名称长度不能超过 30 个字符")
    private String name;

    @Schema(description = "显示顺序")
    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @Schema(description = "状态")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "修改状态必须是 {value}")
    private Integer status;
}
```

### 5.2 PageReqVO（分页请求，继承 PageParam）

```java
@Schema(description = "管理后台 - 岗位分页 Request VO")
@Data
public class PostPageReqVO extends PageParam {

    @Schema(description = "岗位编码，模糊匹配")
    private String code;

    @Schema(description = "岗位名称，模糊匹配")
    private String name;

    @Schema(description = "状态")
    private Integer status;
}
```

### 5.3 RespVO（响应对象）

```java
@Schema(description = "管理后台 - 部门信息 Response VO")
@Data
public class DeptRespVO {

    @Schema(description = "部门编号", example = "1024")
    private Long id;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "父部门 ID")
    private Long parentId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
```

### 5.4 校验注解使用规则

| 注解 | 适用场景 | 说明 |
|------|---------|------|
| `@NotNull` | 任何类型 | 非空校验 |
| `@NotBlank` | String | 非空且非空白字符串 |
| `@Size` | String/Collection | 长度范围 |
| `@Email` | String | 邮箱格式 |
| `@InEnum` | Integer/String | 枚举值范围（自定义注解） |
| `@Min` / `@Max` | 数值类型 | 数值范围 |
| `@Mobile` | String | 手机号格式（自定义注解） |

---

## 第六章：异常与错误码规范

### 6.1 错误码定义

每个模块在 `enums/` 包下定义 `ErrorCodeConstants` 接口：

```java
public interface ErrorCodeConstants {

    // ========== 部门模块 1-002-004-000 ==========
    ErrorCode DEPT_NAME_DUPLICATE  = new ErrorCode(1_002_004_000, "已经存在该名字的部门");
    ErrorCode DEPT_PARENT_NOT_EXITS = new ErrorCode(1_002_004_001, "父级部门不存在");
    ErrorCode DEPT_NOT_FOUND       = new ErrorCode(1_002_004_002, "当前部门不存在");

    // 支持参数化占位符
    ErrorCode DEPT_NOT_ENABLE = new ErrorCode(1_002_004_006, "部门({})不处于开启状态，不允许选择");
}
```

### 6.2 异常抛出方式

```java
// 方式一：直接 new ServiceException
throw new ServiceException(DEPT_NOT_FOUND);

// 方式二：便捷工具方法（推荐）
throw exception(DEPT_NOT_FOUND);

// 方式三：带参数填充
throw exception(DEPT_NOT_ENABLE, dept.getName());
```

### 6.3 校验方法模式

在 Service 层使用 `private` 校验方法，统一抛异常：

```java
private DeptDO validateDeptExists(Long id) {
    DeptDO dept = deptMapper.selectById(id);
    if (dept == null) {
        throw exception(DEPT_NOT_FOUND);
    }
    return dept;
}

private void validateDeptNameUnique(Long id, Long parentId, String name) {
    DeptDO dept = deptMapper.selectByParentIdAndName(parentId, name);
    if (dept == null) {
        return;
    }
    if (id == null || !id.equals(dept.getId())) {
        throw exception(DEPT_NAME_DUPLICATE);
    }
}
```

---

## 第七章：Service 层规范

### 7.1 接口定义

Service 接口定义核心方法，并提供 `default` 便捷方法：

```java
public interface DeptService {

    Long createDept(DeptSaveReqVO createReqVO);

    void updateDept(DeptSaveReqVO updateReqVO);

    void deleteDept(Long id);

    DeptDO getDept(Long id);

    List<DeptDO> getDeptList(Collection<Long> ids);

    default Map<Long, DeptDO> getDeptMap(Collection<Long> ids) {
        List<DeptDO> list = getDeptList(ids);
        return CollectionUtils.convertMap(list, DeptDO::getId);
    }
}
```

### 7.2 实现类编写步骤

每个写操作方法遵循以下固定步骤：

```java
@Override
public Long createDept(DeptSaveReqVO createReqVO) {
    // 1. 校验
    validateDeptNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

    // 2. 转换 VO → DO
    DeptDO dept = BeanUtils.toBean(createReqVO, DeptDO.class);

    // 3. 插入数据库
    deptMapper.insert(dept);

    // 4. 返回 ID
    return dept.getId();
}

@Override
public void updateDept(DeptSaveReqVO updateReqVO) {
    // 1. 校验存在
    validateDeptExists(updateReqVO.getId());
    validateDeptNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

    // 2. 转换 VO → DO
    DeptDO updateObj = BeanUtils.toBean(updateReqVO, DeptDO.class);

    // 3. 更新数据库
    deptMapper.updateById(updateObj);
}

@Override
public void deleteDept(Long id) {
    // 1. 校验存在
    validateDeptExists(id);
    // 2. 校验是否有子部门
    if (deptMapper.selectCountByParentId(id) > 0) {
        throw exception(DEPT_EXITS_CHILDREN);
    }
    // 3. 删除
    deptMapper.deleteById(id);
}
```

### 7.3 缓存策略

```java
// 写操作：清空缓存
@CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
public Long createXxx(XxxSaveReqVO reqVO) { ... }

// 读操作：缓存结果
@Cacheable(cacheNames = CACHE_NAME, key = "#id")
public XxxDO getXxx(Long id) { ... }
```

### 7.4 跨模块 API 调用

API 接口定义在被调用模块的 `api/` 包下，调用方通过 `@Resource` 注入：

```java
// 在被调用模块的 api/ 包下定义接口
public interface AdminUserApi {
    AdminUserRespDTO getUser(Long id);
    void validateUserList(Collection<Long> ids);
}

// 调用方注入使用
@Resource
private AdminUserApi adminUserApi;
```

---

## 第八章：测试规范

### 8.1 测试基类选择

| 基类 | 用途 | 特点 |
|------|------|------|
| `BaseMockitoUnitTest` | 纯 Mockito 单元测试 | 不启动 Spring 容器，速度最快 |
| `BaseDbUnitTest` | 数据库相关测试 | 使用 H2 内存数据库，自动建表，每个测试后自动清理 |
| `BaseRedisUnitTest` | Redis 相关测试 | 使用内嵌 jedis-mock |

### 8.2 测试方法模式（AAA）

```java
@Test
public void testCreateDept() {
    // 1. Arrange - 准备数据
    DeptSaveReqVO reqVO = randomPojo(DeptSaveReqVO.class, o -> {
        o.setId(null);
        o.setParentId(DeptDO.PARENT_ID_ROOT);
        o.setStatus(randomCommonStatus());
    });

    // 2. Act - 执行操作
    Long deptId = deptService.createDept(reqVO);

    // 3. Assert - 验证结果
    assertNotNull(deptId);
    DeptDO deptDO = deptMapper.selectById(deptId);
    assertPojoEquals(reqVO, deptDO, "id");
}
```

### 8.3 异常断言

使用 `assertServiceException` 验证业务异常：

```java
@Test
public void testDeleteDept_exitsChildren() {
    // 准备父部门
    DeptDO parentDept = randomPojo(DeptDO.class, o -> o.setParentId(DeptDO.PARENT_ID_ROOT));
    deptMapper.insert(parentDept);

    // 准备子部门
    DeptDO childDept = randomPojo(DeptDO.class, o -> o.setParentId(parentDept.getId()));
    deptMapper.insert(childDept);

    // 执行删除，期望抛出 DEPT_EXITS_CHILDREN 异常
    assertServiceException(
        () -> deptService.deleteDept(parentDept.getId()),
        DEPT_EXITS_CHILDREN
    );
}
```

### 8.4 测试数据准备

- 使用 `randomPojo(XxxDO.class)` 随机生成测试数据
- 通过 lambda 回调设置特定字段值
- 使用 `randomCommonStatus()`、`randomEmail()` 等工具方法生成常用随机值

### 8.5 运行测试

```bash
# 全量测试
mvn test

# 单模块测试
mvn test -pl yudao-module-system

# 单个测试类
mvn test -pl yudao-module-system -Dtest=DeptServiceImplTest

# 单个测试方法
mvn test -pl yudao-module-system -Dtest=DeptServiceImplTest#testCreateDept
```

---

## 第九章：前端开发规范

### 9.1 技术栈

Vue3 + TypeScript + Element Plus + Vite + Pinia + UnoCSS

### 9.2 API 文件组织

统一放置在 `src/api/{module}/{domain}/index.ts`：

```typescript
import request from '@/config/axios'

// 部门相关 API
export const DeptApi = {
  // 获取部门列表
  getDeptList: () => {
    return request.get({ url: '/system/dept/list' })
  },

  // 获取部门详情
  getDept: (id: number) => {
    return request.get({ url: '/system/dept/get', params: { id } })
  },

  // 创建部门
  createDept: (data: DeptVO) => {
    return request.post({ url: '/system/dept/create', data })
  },

  // 修改部门
  updateDept: (data: DeptVO) => {
    return request.put({ url: '/system/dept/update', data })
  },

  // 删除部门
  deleteDept: (id: number) => {
    return request.delete({ url: '/system/dept/delete', params: { id } })
  }
}
```

### 9.3 API 方法命名

| 场景 | 命名 |
|------|------|
| 获取列表 | `get{Domain}List()` |
| 分页查询 | `get{Domain}Page()` |
| 获取详情 | `get{Domain}(id)` |
| 创建 | `create{Domain}(data)` |
| 修改 | `update{Domain}(data)` |
| 删除 | `delete{Domain}(id)` |
| 导出 | `export{Domain}(params)` |

### 9.4 TypeScript 接口定义

```typescript
export interface DeptVO {
  id: number
  name: string
  parentId: number
  status: number
  sort: number
  createTime?: Date
}
```

### 9.5 页面组件结构

```
src/views/{module}/{domain}/
├── index.vue          # 主页面（搜索栏 + 列表 + 操作按钮）
└── components/
    └── {Domain}Form.vue  # 表单弹窗组件
```

### 9.6 页面编写规范

- 统一使用 **Composition API**（`<script setup>`）
- 查询参数使用 `reactive` 管理
- 列表数据和 loading 状态使用 `ref` 管理
- 使用 Element Plus 组件和 `<dict-tag>` 展示字典值

```vue
<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="部门名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入部门名称" clearable />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="部门编号" align="center" prop="id" />
      <el-table-column label="部门名称" align="center" prop="name" />
      <el-table-column label="状态" align="center" prop="key">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>
```

---

## 第十章：构建与版本管理

### 10.1 Maven 版本管理

- 项目版本通过根 `pom.xml` 的 `<revision>` 属性统一管理
- `flatten-maven-plugin` 在 `process-resources` 阶段将 `${revision}` 展开到 `.flattened-pom.xml`
- 所有第三方依赖版本在 `yudao-dependencies/pom.xml` 中集中锁定

### 10.2 模块启用规则

模块的启用需要**两处同步配置**，缺一不可：

1. **根 `pom.xml`** 的 `<modules>` 中取消注释对应 `<module>`
2. **`yudao-server/pom.xml`** 的 `<dependencies>` 中取消注释对应依赖

> **警告**：如果只在一处启用而另一处遗漏，会导致 Maven 构建失败（依赖找不到）。

### 10.3 后端构建命令

```bash
# 完整构建（跳过测试）
mvn clean install -DskipTests

# 仅编译不打包
mvn clean compile

# 运行某个模块的测试
mvn test -pl yudao-module-system

# 运行单个测试类
mvn test -pl yudao-module-im -Dtest=ImWebSocketServiceImplTest

# 运行单个测试方法
mvn test -pl yudao-module-im -Dtest=ImWebSocketServiceImplTest#testSendPrivateMessageAsync_exceptionSwallowed

# 启动应用（主入口）
mvn spring-boot:run -pl yudao-server

# 从某个失败模块恢复构建
mvn install -DskipTests -rf :yudao-server
```

### 10.4 前端构建命令

```bash
cd yudao-ui/yudao-ui-admin-vue3
pnpm install          # 安装依赖
pnpm dev              # 开发模式（热更新）
pnpm build:prod       # 生产构建
pnpm lint             # ESLint 检查
```

> **注意**：项目根目录 `yudao-ui/yudao-ui-admin-vue3/.npmrc` 已通过 `onlyBuiltDependencies` 放行了 `@parcel/watcher`、`@swc/core`、`core-js` 等原生编译包。如果 `pnpm install` 报 `ERR_PNPM_IGNORED_BUILDS`，需在该文件中添加对应包名。

### 10.5 编译配置

- 编译参数 `-parameters` 已启用（Spring Boot 3.2+ 参数名发现）
- 注解处理器链：`spring-boot-configuration-processor` → `lombok` → `lombok-mapstruct-binding` → `mapstruct-processor`
- JDK 版本：17+

---

## 附录：常用约定速查

### 项目默认配置

| 配置项 | 值 |
|--------|-----|
| 默认 Profile | `local`（`application-local.yaml`） |
| 循环依赖 | `allow-circular-references: true` |
| Lombok 链式 Setter | `chain=true` |
| 逻辑删除 | `deleted` 字段，自动填充 |

### WebSocket 推送

采用事务感知模式（`executeAfterTransaction`），事务提交后才异步发送，避免客户端收到消息时数据尚未落盘。

### Maven 仓库

使用华为云 + 阿里云镜像加速下载。
