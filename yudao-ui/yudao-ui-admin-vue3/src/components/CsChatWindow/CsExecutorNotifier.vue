<template>
  <!-- 全局通知组件，不渲染任何 UI，仅监听 WebSocket 推送 -->
  <div></div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { useCsWebSocket, type CsChatMessagePayload } from '@/hooks/useCsWebSocket'

const router = useRouter()

/** 咨询类型中文映射 */
const consultTypeMap: Record<string, string> = {
  signing: '签约咨询', policy: '政策咨询', aftersale: '售后咨询',
  order: '订单咨询', basedata: '基础数据咨询', other: '其他咨询'
}

useCsWebSocket(
  undefined, // onChatMessage - 不需要
  undefined, // onSessionEvent - 不需要
  (msg: CsChatMessagePayload) => {
    // cs-new-consult: 显示 Toast 通知
    const typeName = consultTypeMap[msg.consultType] || '在线咨询'
    const dealerName = msg.dealerName || '经销商'
    const context = msg.context || ''

    ElNotification({
      title: `新咨询：${typeName}`,
      message: `${dealerName}${context ? ' - ' + context : ''}`,
      type: 'info',
      duration: 8000,
      position: 'bottom-right',
      onClick: () => {
        // 点击跳转到咨询工作台
        router.push('/dealer/cs-workbench')
      }
    })
  }
)
</script>
