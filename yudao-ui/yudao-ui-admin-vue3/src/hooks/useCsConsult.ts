import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createSession } from '@/api/opshub/csSession'

/**
 * 咨询参数接口
 */
export interface ConsultParams {
  consultType: string // signing/policy/aftersale/order/basedata/other
  sourceModule: string // signing/order/aftersale/basedata/manual
  context?: string // 例："合同 MC-2026-001 签署流程咨询"
  contextId?: number // 业务记录 ID
  contextCode?: string // 合同号/订单号/售后单号/文件编号
  productLineCode?: string
  productLineName?: string
  dealerCode?: string
  dealerName?: string
}

/** 批量咨询单次上限 */
const BATCH_LIMIT = 20

/**
 * 共享咨询 composable
 *
 * 统一封装「创建/复用会话 → 打开 ChatWindow」逻辑，各业务模块复用。
 *
 * 使用方式：
 * ```vue
 * <script setup>
 * const { chatVisible, currentSessionId, openConsult, openBatchConsult, openByCategory } = useCsConsult()
 * </script>
 * <template>
 *   <!-- 各模块内容 -->
 *   <ChatWindow v-model="chatVisible" :session-id="currentSessionId" />
 * </template>
 * ```
 */
export function useCsConsult() {
  const chatVisible = ref(false)
  const currentSessionId = ref<number>()

  /**
   * 单条咨询：创建（或复用已有）会话 → 打开 ChatWindow Drawer
   */
  const openConsult = async (params: ConsultParams) => {
    try {
      const sessionId: number = await createSession(params)
      currentSessionId.value = sessionId
      chatVisible.value = true
    } catch (e: any) {
      ElMessage.error(e.message || '创建咨询会话失败')
    }
  }

  /**
   * 批量咨询：为每条记录并行创建会话，全部完成后打开第一个
   *
   * @param rows 选中的行数据数组
   * @param builder 每行数据 → ConsultParams 的映射函数
   */
  const openBatchConsult = async <T>(rows: T[], builder: (row: T) => ConsultParams) => {
    if (!rows || rows.length === 0) {
      ElMessage.warning('请先选择记录')
      return
    }
    const targetRows = rows.length > BATCH_LIMIT ? rows.slice(0, BATCH_LIMIT) : rows
    if (rows.length > BATCH_LIMIT) {
      ElMessage.warning(`单次批量咨询最多支持 ${BATCH_LIMIT} 条，已截取前 ${BATCH_LIMIT} 条`)
    }
    try {
      const sessionIds: number[] = await Promise.all(
        targetRows.map((row) => createSession(builder(row)))
      )
      // 打开第一个会话
      if (sessionIds.length > 0) {
        currentSessionId.value = sessionIds[0]
        chatVisible.value = true
      }
      ElMessage.success(`已为 ${sessionIds.length} 条记录创建咨询会话`)
    } catch (e: any) {
      ElMessage.error(e.message || '批量创建咨询会话失败')
    }
  }

  /**
   * 浮动按钮 / 分类入口：创建无上下文的「other」类型会话
   */
  const openByCategory = async (consultType: string = 'other') => {
    await openConsult({
      consultType,
      sourceModule: 'manual'
    })
  }

  return {
    chatVisible,
    currentSessionId,
    openConsult,
    openBatchConsult,
    openByCategory
  }
}
