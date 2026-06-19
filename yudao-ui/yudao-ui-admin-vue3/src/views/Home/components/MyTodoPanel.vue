<template>
  <el-card shadow="hover" :body-style="{ padding: '20px' }">
    <template #header>
      <span class="text-15px font-600">我的待办</span>
    </template>
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8">
        <div class="todo-card todo-card--danger" @click="navigate('/dealer/service')">
          <div class="todo-label">待接单工单</div>
          <div class="todo-value text-red-500">{{ pendingTasks }}</div>
          <el-tag v-if="pendingTasks > 0" type="danger" effect="dark" round size="small" class="mt-4px">
            需要处理
          </el-tag>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="todo-card todo-card--warning" @click="navigate('/dealer/cs-workbench')">
          <div class="todo-label">待回复咨询</div>
          <div class="todo-value-row">
            <span class="todo-value text-orange-500">{{ pendingConsults }}</span>
            <span class="todo-unit text-gray-400">条</span>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="todo-card todo-card--info" @click="navigate('/dealer/service?tab=opreq')">
          <div class="todo-label">待处理操作请求</div>
          <div class="todo-value-row">
            <span class="todo-value text-blue-500">{{ pendingOpReqs }}</span>
            <span class="todo-unit text-gray-400">单</span>
          </div>
        </div>
      </el-col>
    </el-row>
  </el-card>
</template>

<script lang="ts" setup>
defineOptions({ name: 'MyTodoPanel' })

defineProps<{
  pendingTasks: number
  pendingConsults: number
  pendingOpReqs: number
}>()

const router = useRouter()
const navigate = (route: string) => router.push(route)
</script>

<style scoped>
.todo-card {
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  min-height: 110px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}
.todo-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.todo-card--danger { background: #fef2f2; }
.todo-card--warning { background: #fffbeb; }
.todo-card--info { background: #eff6ff; }
.todo-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}
.todo-value-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}
.todo-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}
.todo-unit {
  font-size: 14px;
}
</style>
