<template>
  <!-- 统计卡片 -->
  <ContentWrap>
    <div class="flex gap-4 mb-4">
      <el-card shadow="never" class="flex-1 text-center cursor-pointer" :class="{ 'border-blue-500': queryParams.status === undefined }" @click="filterByStatus(undefined)">
        <div class="text-2xl font-bold text-gray-800">{{ statistics.totalCount }}</div>
        <div class="text-xs text-gray-500">全部</div>
      </el-card>
      <el-card shadow="never" class="flex-1 text-center cursor-pointer" :class="{ 'border-orange-500': queryParams.status === '0' }" @click="filterByStatus('0')">
        <div class="text-2xl font-bold text-orange-500">{{ statistics.pendingCount }}</div>
        <div class="text-xs text-gray-500">待处理</div>
      </el-card>
      <el-card shadow="never" class="flex-1 text-center cursor-pointer" :class="{ 'border-blue-500': queryParams.status === '1' }" @click="filterByStatus('1')">
        <div class="text-2xl font-bold text-blue-500">{{ statistics.processingCount }}</div>
        <div class="text-xs text-gray-500">处理中</div>
      </el-card>
      <el-card shadow="never" class="flex-1 text-center cursor-pointer" :class="{ 'border-green-500': queryParams.status === '2' }" @click="filterByStatus('2')">
        <div class="text-2xl font-bold text-green-500">{{ statistics.completedCount }}</div>
        <div class="text-xs text-gray-500">已完成</div>
      </el-card>
      <el-card shadow="never" class="flex-1 text-center cursor-pointer" :class="{ 'border-gray-500': queryParams.status === '3' }" @click="filterByStatus('3')">
        <div class="text-2xl font-bold text-gray-400">{{ statistics.closedCount }}</div>
        <div class="text-xs text-gray-500">已关闭</div>
      </el-card>
    </div>
  </ContentWrap>

  <!-- 筛选栏 -->
  <ContentWrap>
    <el-form :model="queryParams" inline>
      <el-form-item label="咨询类型">
        <el-select v-model="queryParams.consultType" clearable placeholder="全部" style="width: 130px">
          <el-option v-for="t in consultTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源模块">
        <el-select v-model="queryParams.sourceModule" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="m in sourceModuleOptions" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="queryParams.keyword" clearable placeholder="搜索编号/内容/经销商" style="width: 220px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch"><Icon icon="ep:search" class="mr-4px" />搜索</el-button>
        <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-4px" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 表格 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" border stripe @row-click="handleRowClick" class="cursor-pointer">
      <el-table-column label="编号" prop="sessionNo" min-width="170" />
      <el-table-column label="咨询类型" prop="consultType" min-width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ getConsultTypeLabel(row.consultType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="经销商" prop="dealerName" min-width="140" show-overflow-tooltip />
      <el-table-column label="上下文" prop="context" min-width="200" show-overflow-tooltip />
      <el-table-column label="最后消息" prop="lastMessage" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.lastMessage || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="消息数" prop="messageCount" min-width="70" align="center" />
      <el-table-column label="状态" prop="status" min-width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" prop="assigneeName" min-width="80" align="center">
        <template #default="{ row }">
          {{ row.assigneeName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="最后更新" prop="lastMessageTime" min-width="160">
        <template #default="{ row }">
          {{ formatDate(row.lastMessageTime || row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" min-width="120" align="center">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" v-hasPermi="['dealer:cs-consult:reply']" link type="primary" @click.stop="handleOpenChat(row)">回复</el-button>
          <el-button v-if="row.status === 1" v-hasPermi="['dealer:cs-consult:reply']" link type="primary" @click.stop="handleOpenChat(row)">继续对话</el-button>
          <el-button v-if="row.status >= 2" link type="info" @click.stop="handleOpenChat(row)">查看归档</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>

  <!-- 聊天窗口 Drawer -->
  <ChatWindow
    v-model="chatDrawerVisible"
    :session-id="currentSessionId"
    mode="agent"
    @session-updated="getList"
  />
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import type { CsStatisticsVO } from '@/api/opshub/csSession'
import {
  getSessionPage,
  getSessionStatistics
} from '@/api/opshub/csSession'
import ChatWindow from '@/components/CsChatWindow/ChatWindow.vue'
import { useCsWebSocket } from '@/hooks/useCsWebSocket'
import { formatDate } from '@/utils/formatTime'

// ========== 枚举常量 ==========
const consultTypeOptions = [
  { value: 'signing', label: '签约咨询' }, { value: 'policy', label: '政策咨询' },
  { value: 'aftersale', label: '售后咨询' }, { value: 'order', label: '订单咨询' },
  { value: 'basedata', label: '基础数据咨询' }, { value: 'other', label: '其他咨询' }
]
const sourceModuleOptions = [
  { value: 'signing', label: '签约进度' }, { value: 'order', label: '订单' },
  { value: 'aftersale', label: '售后' }, { value: 'policy', label: '政策' },
  { value: 'basedata', label: '基础数据' }, { value: 'manual', label: '手动创建' }
]

const getStatusLabel = (val: number) => {
  const map: Record<number, string> = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return map[val] || ''
}
const getStatusTagType = (val: number) => (['warning', 'primary', 'success', 'info'] as any)[val] || ''
const getConsultTypeLabel = (val: string) =>
  consultTypeOptions.find(t => t.value === val)?.label || val || ''

// ========== 统计 ==========
const statistics = ref<CsStatisticsVO>({
  totalCount: 0, pendingCount: 0, processingCount: 0, completedCount: 0, closedCount: 0
})

const loadStatistics = async () => {
  statistics.value = await getSessionStatistics()
}

// ========== 列表查询 ==========
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1, pageSize: 20,
  consultType: undefined as string | undefined,
  status: undefined as string | undefined,
  sourceModule: undefined as string | undefined,
  keyword: ''
})

const getList = async () => {
  loading.value = true
  try {
    const data = await getSessionPage(queryParams)
    list.value = data.list; total.value = data.total
  } finally {
    loading.value = false
  }
  loadStatistics()
}

const handleSearch = () => { queryParams.pageNo = 1; getList() }
const handleReset = () => {
  queryParams.consultType = undefined; queryParams.status = undefined
  queryParams.sourceModule = undefined; queryParams.keyword = ''; handleSearch()
}

const filterByStatus = (status: string | undefined) => {
  queryParams.status = status; handleSearch()
}

// ========== 聊天窗口 ==========
const chatDrawerVisible = ref(false)
const currentSessionId = ref<number>()

const handleOpenChat = (row: any) => {
  currentSessionId.value = row.id
  chatDrawerVisible.value = true
}

const handleRowClick = (row: any) => {
  handleOpenChat(row)
}

// ========== WebSocket 实时监听 ==========
useCsWebSocket(
  // cs-chat-message: 更新对应行的 lastMessage
  (msg) => {
    const row = list.value.find((r: any) => r.id === msg.sessionId)
    if (row) {
      row.lastMessage = msg.content
      row.lastMessageTime = msg.createTime
      row.messageCount = (row.messageCount || 0) + 1
    }
  },
  // cs-session-event: 接单/完成/关闭 → 刷新行状态 + 统计
  () => {
    getList()
  },
  // cs-new-consult: 新咨询到达 → 刷新列表 + 统计
  () => {
    getList()
  }
)

// ========== 初始化 ==========
onMounted(() => { getList() })

// ========== 暴露给父组件（Tab 角标） ==========
defineExpose({ statistics })
</script>
