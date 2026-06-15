<template>
  <el-dialog v-model="visible" title="申请退货" width="680px" destroy-on-close>
    <div class="mb-12px text-gray-500">
      订单号：<span class="font-bold text-gray-800">{{ orderCode }}</span>
    </div>

    <el-table :data="returnItems" border size="small">
      <el-table-column type="selection" width="55" />
      <el-table-column align="center" label="产品名称" prop="productName" min-width="140" show-overflow-tooltip />
      <el-table-column align="center" label="产品编码" prop="productCode" width="100" />
      <el-table-column align="center" label="规格型号" prop="specModel" width="100" />
      <el-table-column align="right" label="单价" width="90">
        <template #default="{ row }">¥{{ formatAmount(row.unitPrice) }}</template>
      </el-table-column>
      <el-table-column align="center" label="可退数量" width="90">
        <template #default="{ row }">
          <span class="text-green-600 font-bold">{{ row.returnableQty }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="退货数量" width="140">
        <template #default="{ row }">
          <el-input-number
            v-model="row.returnQty"
            :min="0"
            :max="row.returnableQty"
            :step="1"
            size="small"
            controls-position="right"
            class="!w-full"
          />
        </template>
      </el-table-column>
      <el-table-column align="right" label="退货金额" width="110">
        <template #default="{ row }">
          ¥{{ formatAmount(row.returnQty * row.unitPrice) }}
        </template>
      </el-table-column>
    </el-table>

    <div class="mt-12px text-right text-14px">
      退货合计：<span class="font-bold text-red-600 text-18px">¥{{ formatAmount(totalReturnAmount) }}</span>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="warning" :loading="submitting" :disabled="validItems.length === 0" @click="handleSubmit">
        确认退货
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import * as OrderApi from '@/api/opshub/order'
import type { ReturnableProduct } from '@/api/opshub/order'

const emit = defineEmits<{ (e: 'success'): void }>()
const message = useMessage()

const visible = ref(false)
const submitting = ref(false)
const orderId = ref<number>(0)
const orderCode = ref('')
const returnItems = ref<(ReturnableProduct & { returnQty: number })[]>([])

const open = (id: number, code: string, products: ReturnableProduct[]) => {
  orderId.value = id
  orderCode.value = code
  returnItems.value = products
    .filter((p) => p.returnableQty > 0)
    .map((p) => ({ ...p, returnQty: 0 }))
  visible.value = true
}

const validItems = computed(() => returnItems.value.filter((i) => i.returnQty > 0))

const totalReturnAmount = computed(() =>
  validItems.value.reduce((sum, i) => sum + i.returnQty * i.unitPrice, 0)
)

const formatAmount = (val: number | undefined) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const handleSubmit = async () => {
  if (validItems.value.length === 0) {
    message.warning('请至少选择一件退货商品并填写数量')
    return
  }
  submitting.value = true
  try {
    await OrderApi.applyReturn({
      orderId: orderId.value,
      items: validItems.value.map((i) => ({
        productId: i.productId,
        returnQty: i.returnQty
      }))
    })
    message.success('退货申请已提交')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('退货申请失败', e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
