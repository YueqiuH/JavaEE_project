<template>
  <div class="not-found">
    <div class="not-found__card">
      <h1>404</h1>
      <p>页面未找到</p>
      <el-button type="primary" @click="$router.push('/home')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { ElButton } from 'element-plus'
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - var(--header-height));
  background: var(--color-background);
}
.not-found__card {
  text-align: center;
  padding: 48px 40px;
}
.not-found__card h1 {
  font-size: 72px;
  color: var(--color-brand-700);
  margin: 0 0 12px;
  font-weight: 700;
  line-height: 1;
}
.not-found__card p {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin: 0 0 28px;
}
</style>
