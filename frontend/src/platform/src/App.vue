<template>
  <RouterView></RouterView>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'

// 页面滚动时自动关闭所有打开的 el-select / el-cascader 等下拉
function closeAllPoppers() {
  document.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
}

onMounted(() => window.addEventListener('scroll', closeAllPoppers, true))
onUnmounted(() => window.removeEventListener('scroll', closeAllPoppers, true))
</script>
