<template>
  <div class="d-crumb d-rise" style="--rise: 0">
    <router-link to="/home">首页</router-link>
    <el-icon><ArrowRight /></el-icon>
    <router-link :to="{ path: '/services', query: { domain } }">{{ domainLabel }}</router-link>
    <el-icon><ArrowRight /></el-icon>
    <span>{{ title }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { domainMap, getBaseDomainLabel } from '@/config/navigation.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'

const props = defineProps({
  domain: { type: String, required: true },
  title: { type: String, required: true },
})

const domainLabel = computed(() => {
  const d = domainMap[props.domain]
  if (!d) return props.domain
  if (props.domain === 'base') {
    const perms = getStoredCurrentUser()?.permissions || []
    return getBaseDomainLabel(perms)
  }
  return d.label
})
</script>

<style scoped>
.d-crumb {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 20px;
  color: var(--color-text-tertiary);
  font-size: 12.5px;
}
.d-crumb a {
  color: inherit;
  text-decoration: none;
  transition: color 0.22s;
}
.d-crumb a:hover {
  color: var(--d-accent, var(--color-brand-700));
}
.d-crumb .el-icon {
  font-size: 11px;
  color: #c4b5fd;
}

@keyframes d-rise-kf {
  from { opacity: 0; transform: translateY(14px); }
  to   { opacity: 1; transform: none; }
}
.d-rise {
  animation: d-rise-kf 0.5s cubic-bezier(0.22, 0.61, 0.36, 1) both;
  animation-delay: calc(var(--rise, 0) * 70ms);
}
@media (prefers-reduced-motion: reduce) {
  .d-rise { animation: none; }
}
</style>
