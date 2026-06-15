<template>
  <el-dialog v-model="dialogVisible" title="AI 智能解读" width="500px">
    <div class="ai-interpret-skeleton">
      <el-empty description="AI 解读功能开发中">
        <template #image>
          <Icon icon="ep:magic-stick" :size="60" color="#909399" />
        </template>
        <p class="text-gray-400 mt-4">
          该功能将对接 OCR + NLP 服务，自动识别文件内容并提取关键信息。
        </p>
        <p class="text-gray-400">
          当前文件：<span class="text-gray-600 font-medium">{{ fileData.fileName }}</span>
        </p>
      </el-empty>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import type { BasedataFileVO } from '@/api/opshub/basedata'

const dialogVisible = ref(false)
const fileData = ref<BasedataFileVO>({} as BasedataFileVO)

/** 打开弹窗 */
const open = (file: BasedataFileVO) => {
  fileData.value = file
  dialogVisible.value = true
}

defineExpose({ open })
</script>

<style scoped>
.ai-interpret-skeleton {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
