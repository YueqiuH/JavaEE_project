<template>
  <RouterView></RouterView>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'

// 滚动/滚轮/触摸滑动时关闭所有下拉（select/datepicker/cascader/菜单）
function closeAllPoppers() {
  document.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
}

onMounted(() => {
  window.addEventListener('scroll', closeAllPoppers, true)
  document.addEventListener('wheel', closeAllPoppers, true)
  document.addEventListener('touchmove', closeAllPoppers, true)
})
onUnmounted(() => {
  window.removeEventListener('scroll', closeAllPoppers, true)
  document.removeEventListener('wheel', closeAllPoppers, true)
  document.removeEventListener('touchmove', closeAllPoppers, true)
})
</script>
