# 「其他咨询」经销商下拉选择

## Context

当前经销商用户点击右下角浮动按钮发起「其他咨询」时，后端 `CsSessionServiceImpl.createSession()` 自动取该用户授权的第一个经销商（`iterator().next()`），用户无法选择。当用户被授权多个经销商时，无法指定以哪个经销商身份发起咨询。

**目标**：浮动按钮点击时，先获取当前用户授权的经销商列表，若仅 1 个则直接创建，若多个则弹出对话框让用户选择后再创建。

**界面原型**：Canvas `dealer-select-dialog-prototype.canvas.tsx`

---

## Task 1: 后端新增 `/my-dealers` 接口

**修改文件**: `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/controller/admin/dealer/DealerUserScopeController.java`

在 `DealerUserScopeController` 中新增端点：

```java
@GetMapping("/my-dealers")
@Operation(summary = "获取当前登录用户授权的经销商列表")
@PreAuthorize("@ss.hasPermission('dealer:cs-consult:create')")
public CommonResult<List<DealerItemVO>> getMyDealers()
```

逻辑：
1. `SecurityFrameworkUtils.getLoginUserId()` 获取当前用户
2. `dealerUserScopeService.getDealerCodesByUserId(userId)` 获取授权编码集合
3. `dealerInfoService.getSimpleList()` 获取全部开启状态经销商，内存过滤出授权的
4. 组装 `List<DealerItemVO>` 返回（复用已有 `DealerItemVO`）

需在 Controller 中新增 `import SecurityFrameworkUtils`。

权限 `dealer:cs-consult:create` 经销商角色已有，无需额外配置。

---

## Task 2: 前端新增 `getMyDealers()` API

**修改文件**: `yudao-ui/yudao-ui-admin-vue3/src/api/opshub/dealerScope/index.ts`

在文件末尾新增：

```ts
// 获取当前登录用户授权的经销商列表
export const getMyDealers = async (): Promise<DealerItemVO[]> => {
  return await request.get({ url: '/opshub/dealer-scope/my-dealers' })
}
```

复用同文件已有的 `DealerItemVO` 类型。

---

## Task 3: 新建 `DealerSelectDialog.vue` 组件

**新建文件**: `yudao-ui/yudao-ui-admin-vue3/src/components/CsChatWindow/DealerSelectDialog.vue`

- `el-dialog` 标题"选择经销商"，宽度 420px
- 提示文字"请选择本次咨询关联的经销商"
- `el-radio-group` + `el-radio` 展示经销商列表（名称 + 编码）
- Props: `modelValue`(boolean), `dealers`(DealerItemVO[])
- Emits: `update:modelValue`, `select`(DealerItemVO)
- 打开时若仅 1 项自动选中；确认按钮未选时禁用

---

## Task 4: 修改 `useCsConsult.ts`

**修改文件**: `yudao-ui/yudao-ui-admin-vue3/src/hooks/useCsConsult.ts`

改造内容：
1. 导入 `getMyDealers` API 和 `DealerItemVO` 类型
2. 新增 `dealerDialogVisible = ref(false)` 和 `myDealerList = ref<DealerItemVO[]>([])`
3. 改造 `openByCategory` 方法：
   - 调用 `getMyDealers()` 获取列表
   - 0 个 → `ElMessage.warning('当前无授权经销商')`
   - 1 个 → 直接 `openConsult({ consultType, sourceModule: 'manual', dealerCode, dealerName })`
   - 多个 → `myDealerList = dealers; dealerDialogVisible = true`
4. 新增 `onDealerSelected(dealer: DealerItemVO)` 回调 → `openConsult` 带 dealerCode
5. return 导出 `dealerDialogVisible`, `myDealerList`, `onDealerSelected`

---

## Task 5: 修改 `Layout.vue`

**修改文件**: `yudao-ui/yudao-ui-admin-vue3/src/layout/Layout.vue`

1. import `DealerSelectDialog` 组件
2. 从 `useCsConsult()` 解构新增的 `dealerDialogVisible`, `myDealerList`, `onDealerSelected`
3. 在经销商角色的 JSX 区域（`isDealer.value` 分支内），在 `<ChatWindow>` 之后添加：

```tsx
<DealerSelectDialog
  modelValue={dealerDialogVisible.value}
  onUpdate:modelValue={(v: boolean) => { dealerDialogVisible.value = v }}
  dealers={myDealerList.value}
  onSelect={(dealer: any) => onDealerSelected(dealer)}
/>
```

---

## 不需要改动的文件

- `CsSessionServiceImpl.createSession()` — 前端传入 dealerCode 后跳过自动补全，该逻辑保留兜底
- `ChatHeader.vue` — 通过 `session?.dealerName` 显示，无需改动
- 业务模块（签约/订单/售后等）— 通过 `openConsult`/`openBatchConsult` 直接传 dealerCode，不受影响

---

## 验证方式

1. 后端编译：`mvn clean compile -pl yudao-module-opshub`
2. 前端编译：`cd yudao-ui/yudao-ui-admin-vue3 && pnpm build:prod`
3. E2E 测试：
   - 授权 1 个经销商的账号 → 点击浮动按钮 → 直接创建会话，ChatHeader 显示正确经销商
   - 授权多个经销商的账号 → 点击浮动按钮 → 弹出选择对话框 → 选择后创建会话
   - 从业务模块（签约/订单等）发起咨询 → 行为不变，不弹选择框
