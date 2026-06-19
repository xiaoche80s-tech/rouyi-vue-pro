<template>
  <div class="dashboard-header">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-4">
        <el-avatar :src="avatar" :size="56" class="shadow-md">
          <img src="@/assets/imgs/avatar.gif" alt="" />
        </el-avatar>
        <div>
          <h2 class="text-20px font-600 mb-2px">{{ greeting }}，{{ username }}</h2>
          <div class="flex items-center gap-2">
            <el-tag :type="roleTagType" effect="dark" round size="small">{{ roleLabel }}</el-tag>
            <el-tag v-if="readonlyMode" type="warning" effect="plain" round size="small" class="ml-2">
              只读模式
            </el-tag>
            <span class="text-13px text-gray-400">{{ today }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useUserStore } from '@/store/modules/user'

defineOptions({ name: 'DashboardHeader' })

const props = withDefaults(
  defineProps<{
    role: string
    readonlyMode?: boolean
  }>(),
  { readonlyMode: false }
)

const userStore = useUserStore()
const avatar = userStore.getUser.avatar
const username = userStore.getUser.nickname

const roleMap: Record<string, { label: string; type: 'primary' | 'success' | 'warning' | 'danger' | 'info' }> = {
  super_admin: { label: '超级管理员', type: 'danger' },
  brand_admin: { label: '品牌管理员', type: 'primary' },
  brand_sales: { label: '品牌销售', type: 'success' },
  service_executor: { label: '服务执行员', type: 'warning' },
  dealer: { label: '经销商', type: 'info' }
}

const roleLabel = computed(() => roleMap[props.role]?.label || '未知角色')
const roleTagType = computed(() => roleMap[props.role]?.type || 'info')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const today = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekdays[d.getDay()]}`
})
</script>

<style scoped>
.dashboard-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: #fff;
}
.dashboard-header h2 {
  color: #fff;
}
.dashboard-header .text-gray-400 {
  color: rgba(255, 255, 255, 0.7);
}
</style>
