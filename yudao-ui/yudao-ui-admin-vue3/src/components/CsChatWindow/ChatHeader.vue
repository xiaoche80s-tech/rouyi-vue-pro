<template>
  <!-- ========== 经销商视图 ========== -->
  <div v-if="mode === 'dealer'" class="chat-header">
    <!-- 咨询信息卡片 -->
    <div class="px-4 pt-3 pb-2 bg-gradient-to-r from-blue-50 to-indigo-50 border-b border-gray-100">
      <div class="flex items-center justify-between mb-2">
        <span class="text-base font-semibold text-gray-800">{{ consultTypeLabel }}</span>
        <el-tag :type="statusTagType" size="small" effect="light">{{ statusText }}</el-tag>
      </div>
      <div class="grid grid-cols-2 gap-x-4 gap-y-1 text-xs text-gray-500">
        <div class="flex">
          <span class="w-14 shrink-0 text-gray-400">咨询ID</span>
          <span class="text-gray-600">{{ session?.sessionNo || '-' }}</span>
        </div>
        <div class="flex">
          <span class="w-14 shrink-0 text-gray-400">经销商</span>
          <span class="text-gray-600">{{ session?.dealerName || '-' }}</span>
        </div>
        <div v-if="session?.context" class="flex col-span-2">
          <span class="w-14 shrink-0 text-gray-400">上下文</span>
          <span class="text-gray-600 truncate">{{ session.context }}</span>
        </div>
        <div v-if="session?.productLineName" class="flex">
          <span class="w-14 shrink-0 text-gray-400">产品线</span>
          <span class="text-gray-600">{{ session.productLineName }}</span>
        </div>
      </div>
    </div>

    <!-- 状态条：待处理 → 正在转接人工 -->
    <div v-if="session?.status === 0" class="transfer-status px-4 py-3 bg-amber-50 border-b border-amber-100">
      <div class="flex items-center gap-2">
        <el-icon class="is-loading text-amber-500 text-lg"><Loading /></el-icon>
        <div class="flex-1">
          <div class="text-sm font-medium text-amber-700">正在为您转接人工客服...</div>
          <div class="text-xs text-amber-500 mt-0.5">请稍候，客服正在赶来</div>
        </div>
        <el-button size="small" type="danger" plain @click="$emit('closeSession')">关闭对话</el-button>
      </div>
    </div>

    <!-- 状态条：处理中 → 已转接成功 -->
    <div v-else-if="session?.status === 1" class="px-4 py-2 bg-green-50 border-b border-green-100">
      <div class="flex items-center gap-2">
        <el-icon class="text-green-500"><CircleCheck /></el-icon>
        <span class="text-sm text-green-700 flex-1">已转接人工客服</span>
        <span v-if="session?.assigneeName" class="text-xs text-green-600">
          客服：{{ session.assigneeName }}
        </span>
        <el-button size="small" type="danger" plain @click="$emit('closeSession')">关闭对话</el-button>
      </div>
    </div>

    <!-- 状态条：已完成 -->
    <div v-else-if="session?.status === 2" class="px-4 py-2 bg-gray-50 border-b border-gray-100">
      <div class="flex items-center gap-2">
        <el-icon class="text-green-500"><CircleCheck /></el-icon>
        <span class="text-sm text-gray-600">该咨询已完成处理</span>
        <span v-if="session?.assigneeName" class="text-xs text-gray-400 ml-auto">
          客服：{{ session.assigneeName }}
        </span>
      </div>
    </div>

    <!-- 状态条：已关闭 -->
    <div v-else-if="session?.status === 3" class="px-4 py-2 bg-gray-50 border-b border-gray-100">
      <div class="flex items-center gap-2">
        <el-icon class="text-gray-400"><CircleClose /></el-icon>
        <span class="text-sm text-gray-500">该咨询已关闭</span>
      </div>
    </div>
  </div>

  <!-- ========== 客服执行员视图：操作按钮 ========== -->
  <div v-else class="chat-header px-4 py-3 border-b border-gray-200">
    <div class="flex items-center justify-between mb-2">
      <div>
        <el-tag :type="statusTagType" size="small" class="mr-2">{{ statusText }}</el-tag>
        <span class="text-sm text-gray-500">{{ consultTypeLabel }}</span>
      </div>
      <div class="flex items-center gap-1">
        <el-tag v-if="isConnected" type="success" size="small" effect="plain">
          <el-icon class="mr-1"><CircleCheck /></el-icon>已连接
        </el-tag>
        <el-tag v-else type="danger" size="small" effect="plain">未连接</el-tag>
      </div>
    </div>
    <div class="text-sm text-gray-600 mb-1">
      <span>{{ session?.dealerName || '未知经销商' }}</span>
      <span v-if="session?.productLineName" class="ml-2 text-gray-400">| {{ session.productLineName }}</span>
    </div>
    <div v-if="session?.context" class="text-xs text-gray-400 mb-2 truncate">{{ session.context }}</div>

    <!-- 操作按钮 -->
    <div class="flex gap-2">
      <el-button v-if="session?.status === 0" type="primary" size="small" @click="$emit('accept')">
        接单
      </el-button>
      <el-button v-if="session?.status === 1" type="success" size="small" @click="$emit('complete')">
        完成处理
      </el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { CircleCheck, CircleClose, Loading } from '@element-plus/icons-vue'
import type { CsSessionVO } from '@/api/opshub/csSession'

const props = withDefaults(defineProps<{
  session: CsSessionVO | null
  isConnected: boolean
  mode?: 'dealer' | 'agent'
}>(), {
  mode: 'dealer'
})

defineEmits<{
  accept: []
  complete: []
  closeSession: []
}>()

/** 咨询类型中文映射 */
const consultTypeMap: Record<string, string> = {
  signing: '签约咨询',
  policy: '政策咨询',
  aftersale: '售后咨询',
  order: '订单咨询',
  basedata: '基础数据咨询',
  other: '其他咨询'
}

const consultTypeLabel = computed(() => {
  const t = props.session?.consultType
  return t ? (consultTypeMap[t] || t) : '在线咨询'
})

const statusText = computed(() => {
  const map: Record<number, string> = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return props.session ? map[props.session.status] || '未知' : '加载中'
})

const statusTagType = computed(() => {
  const map: Record<number, string> = { 0: 'warning', 1: '', 2: 'success', 3: 'info' }
  return (props.session ? map[props.session.status] : 'info') as any
})
</script>
