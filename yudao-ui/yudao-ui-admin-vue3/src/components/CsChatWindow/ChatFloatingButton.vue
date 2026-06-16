<template>
  <div class="cs-chat-floating-button fixed right-6 bottom-6 z-50">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <el-button
        type="primary"
        circle
        size="large"
        class="!w-14 !h-14 !text-2xl shadow-lg"
        @click="$emit('click')"
      >
        💬
      </el-button>
    </el-badge>
  </div>
</template>

<script lang="ts" setup>
defineProps<{
  unreadCount: number
}>()

defineEmits<{
  click: []
}>()
</script>
