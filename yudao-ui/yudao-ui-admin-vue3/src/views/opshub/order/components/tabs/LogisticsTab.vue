<template>
  <div v-if="detail">
    <el-timeline v-if="detail.logistics && detail.logistics.length > 0">
      <el-timeline-item
        v-for="node in detail.logistics"
        :key="node.id"
        :type="node.isCompleted ? 'success' : 'primary'"
        :hollow="!node.isCompleted"
        :timestamp="formatDate(node.nodeTime, 'YYYY-MM-DD HH:mm')"
        placement="top"
      >
        <div class="font-bold">{{ node.nodeDesc }}</div>
        <div class="text-12px text-gray-400 mt-4px" v-if="node.logisticsCompany || node.trackingNo">
          {{ node.logisticsCompany }} {{ node.trackingNo ? `| 单号: ${node.trackingNo}` : '' }}
        </div>
      </el-timeline-item>
    </el-timeline>

    <el-empty v-else description="暂无物流信息" />
  </div>
</template>

<script lang="ts" setup>
import type { OrderDetailVO } from '@/api/opshub/order'
import { formatDate } from '@/utils/formatTime'

defineProps<{ detail: OrderDetailVO | null }>()
</script>
