<template>
  <el-dialog
    :model-value="modelValue"
    title="选择经销商"
    width="420px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="onOpen"
  >
    <div class="mb-4 text-sm text-gray-500">请选择本次咨询关联的经销商</div>
    <el-radio-group v-model="selectedCode" class="w-full">
      <div
        v-for="d in dealers"
        :key="d.dealerCode"
        class="dealer-radio-item mb-3 p-3 rounded-lg border cursor-pointer transition-all"
        :class="{
          'border-blue-500 bg-blue-50': selectedCode === d.dealerCode,
          'border-gray-200 bg-white hover:border-gray-300': selectedCode !== d.dealerCode
        }"
        @click="selectedCode = d.dealerCode"
      >
        <el-radio :value="d.dealerCode" class="w-full !mr-0">
          <div>
            <div class="text-sm font-medium">{{ d.dealerName }}</div>
            <div class="text-xs text-gray-400 mt-0.5">编码：{{ d.dealerCode }}</div>
          </div>
        </el-radio>
      </div>
    </el-radio-group>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :disabled="!selectedCode" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import type { DealerItemVO } from '@/api/opshub/dealerScope'

const props = defineProps<{
  modelValue: boolean
  dealers: DealerItemVO[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  select: [dealer: DealerItemVO]
}>()

const selectedCode = ref('')

const onOpen = () => {
  // 打开时若仅 1 项自动选中
  if (props.dealers.length === 1) {
    selectedCode.value = props.dealers[0].dealerCode
  } else {
    selectedCode.value = ''
  }
}

// 监听 dealers 变化，自动选中唯一项
watch(
  () => props.dealers,
  (val) => {
    if (props.modelValue && val.length === 1) {
      selectedCode.value = val[0].dealerCode
    }
  }
)

const handleConfirm = () => {
  const selected = props.dealers.find((d) => d.dealerCode === selectedCode.value)
  if (selected) {
    emit('select', selected)
  }
  emit('update:modelValue', false)
}
</script>

<style scoped>
.dealer-radio-item :deep(.el-radio__label) {
  width: 100%;
  padding-left: 8px;
}
</style>
