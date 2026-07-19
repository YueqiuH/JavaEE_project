<template>
  <main class="service-center">
    <section class="center-intro">
      <div class="container">
        <p>统一服务目录</p>
        <h1>服务中心</h1>
        <span>检索并进入平台全部业务服务</span>
        <el-input v-model="keyword" size="large" clearable placeholder="搜索服务名称或功能说明" :prefix-icon="Search" />
      </div>
    </section>

    <section class="container center-content">
      <div class="filter-row">
        <div class="domain-filter" role="tablist" aria-label="服务分类">
          <button type="button" :class="{ active: activeDomain === 'all' }" @click="activeDomain = 'all'">全部 <span>{{ visibleTotal }}</span></button>
          <button v-for="domain in domains" :key="domain.key" type="button" :class="{ active: activeDomain === domain.key }" @click="activeDomain = domain.key">{{ domainLabels[domain.key] }} <span>{{ visibleDomainCounts[domain.key] || 0 }}</span></button>
        </div>
        <el-checkbox v-model="favoritesOnly">只看收藏</el-checkbox>
      </div>

      <div class="result-heading">
        <div><h2>{{ resultTitle }}</h2><p>找到 {{ filteredServices.length }} 项服务</p></div>
        <el-select v-model="sortMode" aria-label="排序方式" style="width: 132px"><el-option label="推荐排序" value="default" /><el-option label="按名称排序" value="name" /></el-select>
      </div>

      <div v-if="filteredServices.length" class="service-grid">
        <ServiceCard v-for="service in filteredServices" :key="service.key" :service="service" :favorite="isFavorite(service.key)" @favorite="toggleFavorite" @open="openService" />
      </div>
      <el-empty v-else description="没有找到符合条件的服务"><el-button @click="resetFilters">清除筛选</el-button></el-empty>
    </section>
  </main>
</template>

<script setup>
import { computed, inject, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import ServiceCard from '@/components/ServiceCard.vue'
import { domainMap, domains, services, getBaseDomainLabel } from '@/config/navigation.js'
import { useServicePreferences } from '@/utils/servicePreferences.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'

const route = useRoute()
const router = useRouter()
const currentUser = inject('currentUser', ref(null))
const { favoriteKeys, isFavorite, toggleFavorite, recordRecent } = useServicePreferences()
const userPermissions = computed(() => getStoredCurrentUser()?.permissions || [])
const canSeeSvc = (s) => !s.permission || userPermissions.value.includes(s.permission) || (s.broadPermission && userPermissions.value.includes(s.broadPermission))
const visibleServices = computed(() => services.filter(canSeeSvc))
const visibleTotal = computed(() => visibleServices.value.length)
const domainLabels = computed(() => {
  const labels = {}
  for (const d of domains) {
    labels[d.key] = d.key === 'base' ? getBaseDomainLabel(userPermissions.value) : d.label
  }
  return labels
})
const visibleDomainCounts = computed(() => {
  const counts = {}
  for (const d of domains) counts[d.key] = visibleServices.value.filter(s => s.domain === d.key).length
  return counts
})
const keyword = ref('')
const activeDomain = ref(domainMap[route.query.domain] ? route.query.domain : 'all')
const favoritesOnly = ref(false)
const sortMode = ref('default')

watch(() => route.query.domain, (domain) => { if (domainMap[domain]) activeDomain.value = domain })

const resultTitle = computed(() => activeDomain.value === 'all' ? '全部服务' : domainMap[activeDomain.value].label)
const filteredServices = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()
  const result = visibleServices.value.filter((service) => {
    const matchesDomain = activeDomain.value === 'all' || service.domain === activeDomain.value
    const matchesFavorite = !favoritesOnly.value || favoriteKeys.value.includes(service.key)
    const matchesKeyword = !normalizedKeyword || `${service.title}${service.description}`.toLowerCase().includes(normalizedKeyword)
    return matchesDomain && matchesFavorite && matchesKeyword
  })
  return sortMode.value === 'name' ? [...result].sort((a, b) => a.title.localeCompare(b.title, 'zh-CN')) : result
})

const openService = (service) => { recordRecent(service.key); router.push({ name: service.routeName }) }
const resetFilters = () => { keyword.value = ''; activeDomain.value = 'all'; favoritesOnly.value = false }
</script>

<style scoped>
.service-center { min-height: calc(100vh - var(--header-height)); padding-bottom: 56px; background: var(--color-background); }
.center-intro { padding: 42px 0 72px; color: #fff; background: var(--color-brand-700); }
.center-intro p { margin: 0; color: #ffd77a; font-size: 12px; font-weight: 700; letter-spacing: 1.5px; }
.center-intro h1 { margin: 4px 0 0; font-size: 30px; font-weight: 600; }
.center-intro span { color: rgba(255,255,255,.75); }
.center-intro .el-input { display: block; width: min(620px, 100%); margin-top: 22px; }
.center-intro :deep(.el-input__wrapper) { min-height: 48px; border-radius: 8px; box-shadow: 0 8px 26px rgba(0,0,0,.13); }
.center-content { min-height: 420px; margin-top: -34px; padding: 24px 28px 32px; background: #fff; border-radius: 8px; box-shadow: 0 10px 30px rgba(5,52,78,.08); }
.filter-row, .result-heading { display: flex; align-items: center; justify-content: space-between; gap: 24px; }
.filter-row { padding-bottom: 20px; border-bottom: 1px solid var(--color-border-light); }
.domain-filter { display: flex; flex-wrap: wrap; gap: 8px; }
.domain-filter button { min-height: 36px; padding: 0 13px; color: var(--color-text-secondary); background: #fff; border: 1px solid var(--color-border); border-radius: 4px; cursor: pointer; }
.domain-filter button.active { color: #fff; background: var(--color-brand-700); border-color: var(--color-brand-700); }
.domain-filter span { margin-left: 4px; opacity: .72; }
.result-heading { margin: 24px 0 18px; }
.result-heading h2 { margin: 0; font-size: 20px; font-weight: 600; }
.result-heading p { margin: 2px 0 0; color: var(--color-text-secondary); }
.service-grid { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 16px; }
@media (max-width: 991px) { .service-grid { grid-template-columns: repeat(2, minmax(0,1fr)); } }
@media (max-width: 767px) { .center-intro { padding-top: 28px; } .center-content { width: calc(100% - 24px); padding: 18px 14px 24px; } .filter-row { align-items: flex-start; flex-direction: column; } .domain-filter { flex-wrap: nowrap; width: 100%; overflow-x: auto; } .domain-filter button { flex: 0 0 auto; } .result-heading { align-items: flex-start; } .service-grid { grid-template-columns: 1fr; } }
</style>
