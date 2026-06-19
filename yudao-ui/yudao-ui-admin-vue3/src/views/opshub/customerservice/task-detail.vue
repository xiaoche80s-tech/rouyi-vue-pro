<template>
  <ContentWrap>
    <el-skeleton :loading="detailLoading" animated :rows="6">
      <template #default>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单编号">{{ detailData.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="工单状态">
            <el-tag :type="getStatusTagType(detailData.status)">{{ getStatusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务内容" :span="2">{{ detailData.content }}</el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag :type="getUrgencyTagType(detailData.urgency)">{{ getUrgencyLabel(detailData.urgency) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="工单分类">{{ getCategoryLabel(detailData.category) }}</el-descriptions-item>
          <el-descriptions-item label="经销商">{{ detailData.dealerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产品线">{{ detailData.productLineName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源模块">{{ getSourceModuleLabel(detailData.sourceModule) }}</el-descriptions-item>
          <el-descriptions-item label="SLA 截止时间">{{ detailData.slaDeadline || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detailData.creatorUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="执行员">{{ detailData.assigneeName || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="接单时间">{{ detailData.acceptTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="交付时间">{{ detailData.deliverTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="特殊备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-skeleton>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import * as CsTaskApi from '@/api/opshub/csTask'

defineOptions({ name: 'OpsHubCsTaskDetail' })

const { query } = useRoute()

const props = defineProps({
  id: propTypes.number.def(undefined)
})

// ========== 枚举常量 ==========
const statusOptions = [
  { value: 0, label: '待接单' }, { value: 1, label: '处理中' },
  { value: 2, label: '已交付' }, { value: 3, label: '已关闭' }, { value: 4, label: '已退回' }
]
const urgencyOptions = [
  { value: 0, label: '紧急' }, { value: 1, label: '高' }, { value: 2, label: '中' }, { value: 3, label: '低' }
]
const categoryOptions = [
  { value: 0, label: '签约' }, { value: 1, label: '政策' }, { value: 2, label: '售后' },
  { value: 3, label: '订单' }, { value: 4, label: '数据' }, { value: 5, label: '其他' }
]
const sourceModuleOptions = [
  { value: 'aftersale', label: '售后' }, { value: 'order', label: '订单' },
  { value: 'signing', label: '签约进度' }, { value: 'basedata', label: '基础数据' }, { value: 'manual', label: '手动创建' }
]

const getStatusLabel = (val: number) => statusOptions.find(s => s.value === val)?.label || ''
const getStatusTagType = (val: number) => (['warning', 'primary', 'success', 'info', 'danger'] as any)[val] || ''
const getUrgencyLabel = (val: number) => urgencyOptions.find(u => u.value === val)?.label || ''
const getUrgencyTagType = (val: number) => (['danger', 'warning', '', 'info'] as any)[val] || ''
const getCategoryLabel = (val: number) => categoryOptions.find(c => c.value === val)?.label || ''
const getSourceModuleLabel = (val: string) => sourceModuleOptions.find(m => m.value === val)?.label || val || ''

// ========== 数据加载 ==========
const detailLoading = ref(false)
const detailData = ref<any>({})
const queryId = query.id as unknown as number

const getInfo = async () => {
  detailLoading.value = true
  try {
    detailData.value = await CsTaskApi.getCsTask(props.id || queryId)
  } finally {
    detailLoading.value = false
  }
}
defineExpose({ open: getInfo })

onMounted(() => {
  getInfo()
})
</script>
