<template>
  <el-tabs v-model="activeTab" type="border-card">
    <!-- Tab 1: 工单管理 -->
    <el-tab-pane label="工单管理" name="task">
      <TaskTab side="initiator" />
    </el-tab-pane>

    <!-- Tab 2: 操作请求 -->
    <el-tab-pane label="操作请求" name="opreq">
      <OpReqTab />
    </el-tab-pane>

    <!-- Tab 3: 咨询队列 -->
    <el-tab-pane name="consult">
      <template #label>
        <el-badge :value="consultPendingCount" :hidden="!consultPendingCount" :max="99">咨询队列</el-badge>
      </template>
      <ConsultTab ref="consultTabRef" />
    </el-tab-pane>
  </el-tabs>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import TaskTab from '../customerservice/components/TaskTab.vue'
import OpReqTab from '../customerservice/components/OpReqTab.vue'
import ConsultTab from '../customerservice/components/ConsultTab.vue'

defineOptions({ name: 'OpshubWorkorderService' })

const activeTab = ref('task')

// 咨询队列 Tab 角标
const consultTabRef = ref<InstanceType<typeof ConsultTab>>()
const consultPendingCount = computed(() => consultTabRef.value?.statistics?.pendingCount ?? 0)
</script>
