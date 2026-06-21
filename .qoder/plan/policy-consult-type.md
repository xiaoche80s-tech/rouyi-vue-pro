# 浮动咨询按钮路由感知 — 政策页面咨询类型为 policy

## Context

全局浮动咨询按钮（`Layout.vue`）的 `handleFloatingClick` 始终传 `consultType: 'other'`。用户反馈：在政策页面时，咨询类型应为 `'policy'` 而非 `'other'`。

## 方案

修改 `Layout.vue` 的 `setup()` 函数，根据当前路由路径自动匹配 `consultType`。

## 修改文件

**`yudao-ui/yudao-ui-admin-vue3/src/layout/Layout.vue`**

在 `setup()` 中：

1. 添加 `const route = useRoute()`（已通过 auto-import 可用）
2. 添加路由→咨询类型映射表：
   ```ts
   const routeConsultTypeMap: Record<string, string> = {
     '/opshub/signing': 'signing',
     '/opshub/policy': 'policy',
     '/opshub/aftersale': 'aftersale',
     '/opshub/order': 'order',
     '/opshub/basedata': 'basedata'
   }
   ```
3. 添加 computed 自动匹配：
   ```ts
   const consultType = computed(() => {
     const path = route.path
     for (const [prefix, type] of Object.entries(routeConsultTypeMap)) {
       if (path.startsWith(prefix)) return type
     }
     return 'other'
   })
   ```
4. 修改 `handleFloatingClick`：`openByCategory(consultType.value)`

## 验证

- 在政策页面 (`/opshub/policy`) 点击浮动按钮，创建的咨询会话 `consultType` 应为 `'policy'`
- 在签约页面 (`/opshub/signing`) 点击，`consultType` 应为 `'signing'`
- 在非 opshub 页面点击，`consultType` 应为 `'other'`
