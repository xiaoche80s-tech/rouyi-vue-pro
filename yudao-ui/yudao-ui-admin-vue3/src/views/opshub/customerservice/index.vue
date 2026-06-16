<template>
  <el-tabs v-model="activeTab" type="border-card">
    <!-- Tab 1: 工单管理 -->
    <el-tab-pane label="工单管理" name="task">
      <TaskTab />
    </el-tab-pane>

    <!-- Tab 2: 操作请求 -->
    <el-tab-pane label="操作请求" name="opreq">
      <OpReqTab />
    </el-tab-pane>

    <!-- Tab 3: 咨询队列 -->
    <el-tab-pane label="咨询队列" name="consult">
      <ConsultTab />
    </el-tab-pane>
  </el-tabs>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import TaskTab from './components/TaskTab.vue'
import OpReqTab from './components/OpReqTab.vue'
import ConsultTab from './components/ConsultTab.vue'

defineOptions({ name: 'OpshubCustomerService' })

const activeTab = ref('task')
</script>
