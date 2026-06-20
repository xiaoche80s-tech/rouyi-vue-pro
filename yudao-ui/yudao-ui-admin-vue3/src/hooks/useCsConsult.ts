import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createSession } from '@/api/opshub/csSession'
import { getMyDealers, type DealerItemVO } from '@/api/opshub/dealerScope'

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
 * const { chatVisible, currentSessionId, openConsult, openBatchConsult, openByCategory,
 *   dealerDialogVisible, myDealerList, onDealerSelected } = useCsConsult()
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
  // 经销商选择对话框状态
  const dealerDialogVisible = ref(false)
  const myDealerList = ref<DealerItemVO[]>([])
  // 暂存的咨询类型（多经销商弹框时使用）
  let pendingConsultType = 'other'

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
   * 浮动按钮 / 分类入口：先获取授权经销商列表，再决定行为
   * - 0 个 → 提示无授权
   * - 1 个 → 直接创建
   * - 多个 → 弹框选择
   */
  const openByCategory = async (consultType: string = 'other') => {
    try {
      const dealers = await getMyDealers()
      if (dealers.length === 0) {
        ElMessage.warning('当前无授权经销商，无法发起咨询')
        return
      }
      if (dealers.length === 1) {
        // 仅 1 个 → 直接创建
        await openConsult({
          consultType,
          sourceModule: 'manual',
          dealerCode: dealers[0].dealerCode,
          dealerName: dealers[0].dealerName
        })
      } else {
        // 多个 → 弹框选择
        pendingConsultType = consultType
        myDealerList.value = dealers
        dealerDialogVisible.value = true
      }
    } catch (e: any) {
      ElMessage.error(e.message || '获取经销商列表失败')
    }
  }

  /**
   * 经销商选择确认回调
   */
  const onDealerSelected = async (dealer: DealerItemVO) => {
    dealerDialogVisible.value = false
    await openConsult({
      consultType: pendingConsultType,
      sourceModule: 'manual',
      dealerCode: dealer.dealerCode,
      dealerName: dealer.dealerName
    })
  }

  return {
    chatVisible,
    currentSessionId,
    openConsult,
    openBatchConsult,
    openByCategory,
    // 经销商选择对话框
    dealerDialogVisible,
    myDealerList,
    onDealerSelected
  }
}
