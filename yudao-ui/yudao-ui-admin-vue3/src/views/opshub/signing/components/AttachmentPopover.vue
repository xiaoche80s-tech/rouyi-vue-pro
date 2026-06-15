<template>
  <div class="p-8px">
    <div class="text-12px font-bold mb-8px">附件列表</div>
    <div v-if="fileIdList.length === 0" class="text-12px text-gray-400">暂无附件</div>
    <div v-for="fid in fileIdList" :key="fid" class="flex items-center gap-8px mb-4px text-12px">
      <Icon icon="ep:document" class="text-gray-400" />
      <span>附件 #{{ fid }}</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
const props = defineProps<{ fileIds: string }>()

const fileIdList = computed(() => {
  if (!props.fileIds) return []
  return props.fileIds.split(',').filter(Boolean)
})
</script>
