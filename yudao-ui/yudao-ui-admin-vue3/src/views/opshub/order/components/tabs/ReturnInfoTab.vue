<template>
  <div v-if="detail">
    <!-- 退货说明 -->
    <el-alert
      v-if="!['signed', 'completed'].includes(detail.progressStatus)"
      type="warning"
      :closable="false"
      class="mb-12px"
    >
      仅已签收或已完成的订单可申请退货
    </el-alert>

    <!-- 申请退货按钮 -->
    <div class="mb-12px" v-if="['signed', 'completed'].includes(detail.progressStatus)">
      <el-button
        v-hasPermi="['dealer:order:return']"
        type="warning"
        :disabled="!detail.returnableProducts || detail.returnableProducts.length === 0"
        @click="emit('applyReturn')"
      >
        <Icon class="mr-5px" icon="ep:refresh-left" />申请退货
      </el-button>
    </div>

    <!-- 可退货商品明细 -->
    <el-table :data="detail.returnableProducts" border size="small">
      <el-table-column align="center" label="产品名称" prop="productName" min-width="150" show-overflow-tooltip />
      <el-table-column align="center" label="产品编码" prop="productCode" width="120" />
      <el-table-column align="center" label="规格型号" prop="specModel" width="120" />
      <el-table-column align="right" label="单价" width="100">
        <template #default="{ row }">¥{{ formatAmount(row.unitPrice) }}</template>
      </el-table-column>
      <el-table-column align="center" label="购买数量" prop="quantity" width="90" />
      <el-table-column align="center" label="可退货数量" width="100">
        <template #default="{ row }">
          <span :class="row.returnableQty > 0 ? 'text-green-600 font-bold' : 'text-gray-400'">
            {{ row.returnableQty }}
          </span>
        </template>
      </el-table-column>
      <el-table-column align="right" label="金额" width="120">
        <template #default="{ row }">¥{{ formatAmount(row.amount) }}</template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!detail.returnableProducts || detail.returnableProducts.length === 0"
      description="暂无可退货商品"
    />
  </div>
</template>

<script lang="ts" setup>
import type { OrderDetailVO } from '@/api/opshub/order'

defineProps<{ detail: OrderDetailVO | null }>()
const emit = defineEmits<{ (e: 'applyReturn'): void }>()

const formatAmount = (val: number | undefined) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>
