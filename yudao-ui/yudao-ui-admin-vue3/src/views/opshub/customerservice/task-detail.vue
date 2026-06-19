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

        <!-- BPM 流程摘要区域 -->
        <div v-if="bpmSummary" class="bpm-summary-card">
          <el-divider />
          <div class="bpm-summary-content">
            <div class="bpm-summary-header">
              <span class="bpm-summary-title">流程状态</span>
              <el-tag :type="getBpmStatusTagType(bpmSummary.status)">
                {{ getBpmStatusLabel(bpmSummary.status) }}
              </el-tag>
            </div>
            <div v-if="bpmSummary.currentActivityName" class="bpm-summary-info">
              <span class="bpm-summary-label">当前节点：</span>
              <span class="bpm-summary-value">{{ bpmSummary.currentActivityName }}</span>
            </div>
            <div v-if="bpmSummary.currentAssigneeName" class="bpm-summary-info">
              <span class="bpm-summary-label">当前处理人：</span>
              <span class="bpm-summary-value">{{ bpmSummary.currentAssigneeName }}</span>
            </div>
          </div>
          <div class="bpm-summary-actions">
            <el-button type="primary" @click="goToBpmDetail">
              <Icon icon="ep:document" />查看审批进度
            </el-button>
          </div>
        </div>
      </template>
    </el-skeleton>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import * as CsTaskApi from '@/api/opshub/csTask'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'

defineOptions({ name: 'OpsHubCsTaskDetail' })

const { query } = useRoute()
const router = useRouter()

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

// BPM 流程摘要
interface BpmSummary {
  status: number
  currentActivityName?: string
  currentAssigneeName?: string
}
const bpmSummary = ref<BpmSummary | null>(null)

// BPM 状态标签映射
const getBpmStatusLabel = (status: number) => {
  const map: Record<number, string> = {
    1: '审批中',
    2: '已通过',
    3: '已退回',
    4: '已取消'
  }
  return map[status] || '未知'
}
const getBpmStatusTagType = (status: number): string => {
  const map: Record<number, string> = {
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'info'
  }
  return map[status] || ''
}

const getInfo = async () => {
  detailLoading.value = true
  try {
    detailData.value = await CsTaskApi.getCsTask(props.id || queryId)
    // 加载 BPM 流程摘要
    await loadBpmSummary()
  } finally {
    detailLoading.value = false
  }
}

// 加载 BPM 流程状态摘要
const loadBpmSummary = async () => {
  const processInstanceId = detailData.value?.processInstanceId
  if (!processInstanceId) {
    bpmSummary.value = null
    return
  }
  try {
    const data = await ProcessInstanceApi.getApprovalDetail({ processInstanceId })
    if (!data?.processInstance) {
      bpmSummary.value = null
      return
    }
    const pi = data.processInstance
    // 从 activityNodes 中找到当前正在执行的节点
    const currentActivity = data.activityNodes?.find(
      (node: any) => node.status === 1 // 运行中
    )
    const currentTask = currentActivity?.tasks?.find((t: any) => t.status === 1) // 待处理
    bpmSummary.value = {
      status: pi.status,
      currentActivityName: currentActivity?.name || '',
      currentAssigneeName: currentTask?.assigneeUser?.nickname || ''
    }
  } catch (e) {
    console.error('加载 BPM 流程摘要失败', e)
    bpmSummary.value = null
  }
}

// 跳转到 BPM 详情页
const goToBpmDetail = () => {
  const processInstanceId = detailData.value?.processInstanceId
  if (processInstanceId) {
    router.push({
      name: 'BpmProcessInstanceDetail',
      query: { id: processInstanceId }
    })
  }
}

defineExpose({ open: getInfo })

onMounted(() => {
  getInfo()
})
</script>

<style lang="scss" scoped>
.bpm-summary-card {
  .bpm-summary-content {
    .bpm-summary-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 10px;

      .bpm-summary-title {
        font-weight: 600;
        font-size: 14px;
      }
    }

    .bpm-summary-info {
      font-size: 13px;
      margin-bottom: 6px;

      .bpm-summary-label {
        color: #909399;
      }

      .bpm-summary-value {
        color: #303133;
      }
    }
  }

  .bpm-summary-actions {
    margin-top: 12px;
  }
}
</style>
