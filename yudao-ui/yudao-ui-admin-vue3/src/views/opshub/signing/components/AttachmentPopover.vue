<template>
  <div class="p-8px" style="min-width: 280px">
    <div class="flex items-center justify-between mb-8px">
      <span class="text-13px font-bold">📎 合同附件</span>
      <el-tag v-if="loaded" size="small" round>{{ files.length }}</el-tag>
    </div>

    <div v-if="loading" class="py-16px text-center">
      <Icon icon="ep:loading" class="animate-spin text-gray-400" />
      <div class="text-12px text-gray-400 mt-4px">加载中...</div>
    </div>

    <div v-else-if="files.length === 0" class="py-12px text-center">
      <Icon icon="ep:folder" class="text-24px text-gray-300" />
      <div class="text-12px text-gray-400 mt-4px">暂无附件</div>
    </div>

    <div v-else>
      <div
        v-for="file in files"
        :key="file.id"
        class="flex items-center justify-between py-6px px-4px rounded hover:bg-gray-50 mb-2px"
      >
        <div class="flex items-center gap-6px flex-1 min-w-0">
          <Icon icon="ep:document" class="text-blue-500 text-14px flex-shrink-0" />
          <div class="min-w-0 flex-1">
            <div class="text-12px truncate" :title="file.fileName">{{ file.fileName }}</div>
            <div class="flex items-center gap-6px mt-2px">
              <el-tag size="small" type="info" class="text-10px">{{ file.fileType }}</el-tag>
              <span class="text-11px text-gray-400">{{ formatFileSize(file.fileSize) }}</span>
            </div>
          </div>
        </div>
        <el-button link type="primary" size="small" class="flex-shrink-0 ml-4px" @click="handleDownload(file.id)">
          <Icon icon="ep:download" />
        </el-button>
      </div>

      <div class="mt-6px pt-6px border-t border-gray-100 text-11px text-gray-400 flex justify-between">
        <span>共 {{ files.length }} 个附件</span>
        <span>合计 {{ formatFileSize(totalSize) }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import * as SigningApi from '@/api/opshub/signing'
import * as BasedataApi from '@/api/opshub/basedata'

const props = defineProps<{ contractId: number }>()

const loading = ref(false)
const loaded = ref(false)
const files = ref<SigningApi.ContractAttachmentVO[]>([])

const totalSize = computed(() => {
  return files.value.reduce((sum, f) => sum + (f.fileSize || 0), 0)
})

const formatFileSize = (bytes: number) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let idx = 0
  let size = bytes
  while (size >= 1024 && idx < units.length - 1) {
    size /= 1024
    idx++
  }
  return size.toFixed(idx > 0 ? 1 : 0) + ' ' + units[idx]
}

const handleDownload = async (fileId: number) => {
  try {
    const url = await BasedataApi.getDownloadUrl(fileId)
    if (url) {
      window.open(url, '_blank')
    } else {
      ElMessage.warning('文件地址为空，无法下载')
    }
  } catch {
    ElMessage.error('获取下载链接失败')
  }
}

// 首次展开时加载
onMounted(async () => {
  if (loaded.value) return
  loading.value = true
  try {
    files.value = await SigningApi.getContractAttachments(props.contractId)
  } catch {
    // 静默处理
  } finally {
    loading.value = false
    loaded.value = true
  }
})
</script>
