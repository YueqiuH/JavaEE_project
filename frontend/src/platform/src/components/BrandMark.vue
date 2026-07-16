<template>
  <span class="brand-mark" aria-hidden="true">
    <el-icon><School /></el-icon>
  </span>
</template>

<script setup>
import { School } from '@element-plus/icons-vue'
</script>

<style scoped>
.brand-mark {
  display: inline-flex;
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  align-items: center;
  justify-content: center;
  color: var(--color-brand-700);
  background: #fff;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 8px;
}

.brand-mark :deep(svg) {
  width: 23px;
  height: 23px;
}
</style>
