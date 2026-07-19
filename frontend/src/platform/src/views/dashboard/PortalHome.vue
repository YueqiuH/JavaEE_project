<template>
  <div class="portal-home">
    <section class="campus-hero" aria-labelledby="portal-title">
      <img :src="campusHero" alt="秋日校园建筑与树木" />
      <div class="hero-shade"></div>
      <div class="container hero-content">
        <div class="hero-copy">
          <p class="hero-kicker">SMART CAMPUS</p>
          <h1 id="portal-title">智慧校园服务平台</h1>
          <p>连接教学、学生服务与校园办公，让每项事务都有清晰入口。</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" :icon="Grid" @click="router.push('/services')">进入服务中心</el-button>
            <el-button size="large" :icon="Checked" @click="router.push('/workbench')">查看我的待办</el-button>
          </div>
        </div>
        <div class="hero-status" aria-label="今日概览">
          <span>{{ formattedDate }}</span>
          <strong>{{ greeting }}，{{ displayName }}</strong>
          <p>今天有 4 项日程，2 项业务需要处理</p>
        </div>
      </div>
    </section>

    <main class="portal-content">
      <section class="quick-section container" aria-labelledby="quick-title">
        <div class="quick-header">
          <div>
            <h2 id="quick-title">常用服务</h2>
            <p>根据你的角色、收藏和最近使用进行整理</p>
          </div>
          <el-button text :icon="Right" @click="router.push('/services')">查看全部服务</el-button>
        </div>

        <div class="segmented-tabs" role="tablist" aria-label="常用服务分类">
          <button
            v-for="tab in quickTabs"
            :key="tab.key"
            type="button"
            :class="{ active: quickTab === tab.key }"
            @click="quickTab = tab.key"
          >
            {{ tab.label }}
            <span v-if="tab.key === 'favorites'">{{ favoriteKeys.length }}</span>
          </button>
        </div>

        <div v-if="quickServices.length" class="service-grid">
          <ServiceCard
            v-for="service in quickServices"
            :key="service.key"
            :service="service"
            :favorite="isFavorite(service.key)"
            @favorite="toggleFavorite"
            @open="openService"
          />
        </div>
        <el-empty v-else description="还没有收藏服务">
          <el-button type="primary" @click="router.push('/services')">去服务中心添加</el-button>
        </el-empty>
      </section>

      <section class="domain-section">
        <div class="container">
          <div class="section-heading">
            <div>
              <h2>全部服务</h2>
              <p>四个业务域覆盖平台全部 {{ visibleTotal }} 项功能</p>
            </div>
            <span class="service-total">{{ visibleTotal }} 项服务</span>
          </div>

          <div class="domain-tabs" role="tablist" aria-label="业务域">
            <button
              v-for="domain in domains"
              :key="domain.key"
              type="button"
              :class="{ active: activeDomain === domain.key }"
              :style="{ '--domain-color': domain.color }"
              @click="activeDomain = domain.key"
            >
              <span>{{ domainLabels[domain.key] }}</span>
              <small>{{ visibleDomainCounts[domain.key] || 0 }}</small>
            </button>
          </div>

          <p class="domain-description">
            <span :style="{ background: activeDomainInfo.color }"></span>
            {{ activeDomainInfo.description }}
          </p>

          <div class="service-grid">
            <ServiceCard
              v-for="service in domainServices"
              :key="service.key"
              :service="service"
              :favorite="isFavorite(service.key)"
              @favorite="toggleFavorite"
              @open="openService"
            />
          </div>
        </div>
      </section>

      <section class="ai-section container">
        <div class="section-heading">
          <div>
            <h2>AI 智能服务</h2>
            <p>在演示模式下预览 {{ aiServices.length }} 类校园智能助手</p>
          </div>
          <el-tag effect="plain" type="warning">可选能力</el-tag>
        </div>
        <div class="ai-strip" :style="{ gridTemplateColumns: `repeat(${aiServices.length}, minmax(0, 1fr))` }">
          <button v-for="(service, idx) in aiServices" :key="service.key" type="button" @click="openService(service)">
            <span class="ai-strip-icon"><el-icon><component :is="service.icon" /></el-icon></span>
            <span><strong>{{ service.title }}</strong><small>{{ service.description }}</small></span>
            <el-icon v-if="idx !== aiServices.length - 1" class="ai-arrow"><Right /></el-icon>
          </button>
        </div>
      </section>

      <section class="shortcut-section">
        <div class="container">
          <div class="section-heading">
            <div>
              <h2>业务直通车</h2>
              <p>按业务域快速浏览全部校园服务</p>
            </div>
          </div>
          <div class="shortcut-grid">
            <button
              v-for="domain in domains"
              :key="domain.key"
              type="button"
              :style="{ '--domain-color': domain.color, '--domain-soft': domain.softColor }"
              @click="goDomain(domain.key)"
            >
              <span><small>业务域</small><strong>{{ domainLabels[domain.key] }}</strong><em>{{ visibleDomainCounts[domain.key] || 0 }} 项服务</em></span>
              <el-icon><component :is="domain.icon" /></el-icon>
            </button>
          </div>
        </div>
      </section>
    </main>

    <footer class="portal-footer">
      <div class="container footer-content">
        <div>
          <strong>智慧校园服务平台</strong>
          <p>统一入口 · 协同办理 · 数据驱动</p>
        </div>
        <p>校园图片：note thanun / Unsplash</p>
      </div>
    </footer>

    <div class="floating-actions" aria-label="快捷工具">
      <el-tooltip content="我的收藏" placement="left"><button type="button" @click="showFavorites"><el-icon><Star /></el-icon></button></el-tooltip>
      <el-tooltip content="意见反馈" placement="left"><button type="button" @click="feedbackVisible = true"><el-icon><ChatDotRound /></el-icon></button></el-tooltip>
      <el-tooltip content="返回顶部" placement="left"><button type="button" @click="scrollTop"><el-icon><Top /></el-icon></button></el-tooltip>
    </div>

    <el-dialog v-model="feedbackVisible" title="意见反馈" width="min(480px, calc(100vw - 32px))">
      <el-form label-position="top">
        <el-form-item label="反馈类型"><el-select v-model="feedbackType" style="width: 100%"><el-option label="功能建议" value="feature" /><el-option label="使用问题" value="issue" /><el-option label="数据问题" value="data" /></el-select></el-form-item>
        <el-form-item label="反馈内容"><el-input v-model="feedbackText" type="textarea" :rows="4" maxlength="300" show-word-limit placeholder="请描述你遇到的问题或建议" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="feedbackVisible = false">取消</el-button><el-button type="primary" @click="submitFeedback">提交反馈</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, inject, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Checked, Grid, Right, Star, Top } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import campusHero from '@/assets/images/campus-hero.jpg'
import ServiceCard from '@/components/ServiceCard.vue'
import { canAccessService, domains, getServicesByDomain, services, getBaseDomainLabel } from '@/config/navigation.js'
import { useServicePreferences } from '@/utils/servicePreferences.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'

const router = useRouter()
const currentUser = inject('currentUser', ref(null))
const { favoriteKeys, recentKeys, isFavorite, toggleFavorite, recordRecent } = useServicePreferences()

const quickTab = ref('recommended')
const activeDomain = ref('teaching')
const feedbackVisible = ref(false)
const feedbackType = ref('feature')
const feedbackText = ref('')

const quickTabs = [
  { key: 'recommended', label: '为你推荐' },
  { key: 'recent', label: '最近使用' },
  { key: 'favorites', label: '我的收藏' },
]

const defaultRecommended = ['course-schedule', 'course-selection', 'exam-arrangement', 'lab-booking', 'fee-payment', 'meeting-notice', 'document-oa', 'student-analytics']
const displayName = computed(() => currentUser.value?.user?.realName || currentUser.value?.user?.username || '同学')
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 11) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})
const formattedDate = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())
const userPerms = computed(() => getStoredCurrentUser()?.permissions || [])
const domainLabels = computed(() => {
  const labels = {}
  for (const d of domains) {
    labels[d.key] = d.key === 'base' ? getBaseDomainLabel(userPerms.value) : d.label
  }
  return labels
})
const canSee = (service) => canAccessService(service, userPerms.value)
const visibleTotal = computed(() => services.filter(canSee).length)
const visibleDomainCounts = computed(() => {
  const counts = {}
  for (const d of domains) counts[d.key] = services.filter(s => s.domain === d.key && canSee(s)).length
  return counts
})

const quickServices = computed(() => {
  let keys = defaultRecommended
  if (quickTab.value === 'recent') keys = recentKeys.value
  if (quickTab.value === 'favorites') keys = favoriteKeys.value
  return keys.map((key) => services.find((service) => service.key === key)).filter(Boolean).filter(canSee)
})
const domainServices = computed(() => getServicesByDomain(activeDomain.value).filter(canSee))
const activeDomainInfo = computed(() => domains.find((domain) => domain.key === activeDomain.value))
const aiServices = computed(() => services.filter((service) => service.ai && canSee(service)))

const openService = (service) => {
  recordRecent(service.key)
  router.push({ name: service.routeName })
}

const goDomain = (domain) => router.push({ path: '/services', query: { domain } })
const showFavorites = () => {
  quickTab.value = 'favorites'
  document.querySelector('.quick-section')?.scrollIntoView({ behavior: 'smooth' })
}
const scrollTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })
const submitFeedback = () => {
  if (!feedbackText.value.trim()) {
    ElMessage.warning('请填写反馈内容')
    return
  }
  ElMessage.success('反馈已记录，感谢你的建议')
  feedbackText.value = ''
  feedbackVisible.value = false
}
</script>

<style scoped>
.portal-home { min-height: 100vh; background: var(--color-background); }
.campus-hero { position: relative; height: 330px; overflow: hidden; color: #fff; background: var(--color-brand-800); }
.campus-hero > img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; object-position: center 58%; }
.hero-shade { position: absolute; inset: 0; background: linear-gradient(90deg, rgba(1, 42, 66, .9) 0%, rgba(1, 42, 66, .62) 42%, rgba(1, 42, 66, .12) 75%); }
.hero-content { position: relative; display: flex; height: 100%; align-items: center; justify-content: space-between; gap: 48px; padding-bottom: 34px; }
.hero-copy { max-width: 640px; }
.hero-kicker { margin: 0 0 8px; color: #ffd77a; font-size: 12px; font-weight: 700; letter-spacing: 2px; }
.hero-copy h1 { margin: 0; font-size: 36px; font-weight: 600; letter-spacing: 0; }
.hero-copy > p:not(.hero-kicker) { margin: 10px 0 22px; color: rgba(255,255,255,.82); font-size: 16px; }
.hero-actions { display: flex; gap: 12px; }
.hero-actions :deep(.el-button--default) { color: #fff; background: rgba(255,255,255,.1); border-color: rgba(255,255,255,.6); }
.hero-status { min-width: 300px; padding: 20px 22px; background: rgba(1, 53, 82, .72); border: 1px solid rgba(255,255,255,.22); border-radius: 8px; backdrop-filter: blur(8px); }
.hero-status span, .hero-status strong, .hero-status p { display: block; }
.hero-status span { color: rgba(255,255,255,.72); font-size: 13px; }
.hero-status strong { margin-top: 5px; font-size: 20px; font-weight: 600; }
.hero-status p { margin: 10px 0 0; color: rgba(255,255,255,.78); }
.portal-content { position: relative; margin-top: -40px; }
.quick-section { position: relative; z-index: 2; padding: 28px 32px 34px; background: #fff; border-radius: 8px 8px 0 0; box-shadow: 0 -8px 24px rgba(6, 48, 70, .08); }
.quick-header { display: flex; align-items: center; justify-content: space-between; gap: 20px; }
.quick-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.quick-header p { margin: 3px 0 0; color: var(--color-text-secondary); }
.segmented-tabs { display: flex; gap: 28px; margin: 22px 0 20px; overflow: hidden; border-bottom: 1px solid var(--color-border-light); scrollbar-width: none; }
.segmented-tabs::-webkit-scrollbar { display: none; }
.segmented-tabs button { position: relative; min-height: 40px; padding: 0 2px 12px; color: var(--color-text-secondary); background: none; border: 0; cursor: pointer; }
.segmented-tabs button.active { color: var(--color-brand-700); font-weight: 600; }
.segmented-tabs button.active::after { position: absolute; right: 0; bottom: -1px; left: 0; height: 3px; content: ''; background: var(--color-brand-700); border-radius: 3px 3px 0 0; }
.segmented-tabs button span { display: inline-flex; min-width: 18px; height: 18px; align-items: center; justify-content: center; margin-left: 5px; color: var(--color-brand-700); font-size: 11px; background: var(--color-brand-100); border-radius: 9px; }
.service-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.domain-section { padding: 42px 0 46px; background: #fff; border-top: 1px solid var(--color-border-light); }
.service-total { color: var(--color-text-secondary); }
.domain-tabs { display: flex; gap: 30px; margin-bottom: 0; overflow-x: auto; overflow-y: hidden; border-bottom: 1px solid var(--color-border-light); scrollbar-width: none; }
.domain-tabs::-webkit-scrollbar { display: none; }
.domain-tabs button { position: relative; display: inline-flex; min-height: 44px; flex: 0 0 auto; align-items: center; gap: 7px; padding: 0 2px 12px; color: var(--color-text-secondary); background: transparent; border: 0; cursor: pointer; }
.domain-tabs button:hover { color: var(--color-text-primary); }
.domain-tabs button span { font-size: 15px; }
.domain-tabs button small { display: inline-flex; min-width: 20px; height: 20px; align-items: center; justify-content: center; padding: 0 6px; color: var(--color-text-tertiary); font-size: 11px; background: var(--color-background); border-radius: 10px; }
.domain-tabs button.active { color: var(--domain-color); font-weight: 600; }
.domain-tabs button.active::after { position: absolute; right: 0; bottom: -1px; left: 0; height: 3px; content: ''; background: var(--domain-color); border-radius: 3px 3px 0 0; }
.domain-tabs button.active small { color: var(--domain-color); background: color-mix(in srgb, var(--domain-color) 10%, white); }
.domain-description { display: flex; align-items: center; gap: 8px; min-height: 42px; margin: 0 0 14px; color: var(--color-text-tertiary); font-size: 13px; }
.domain-description span { width: 6px; height: 6px; flex: 0 0 6px; border-radius: 50%; }
.ai-section { padding-top: 42px; padding-bottom: 46px; }
.ai-strip { display: grid; overflow: hidden; grid-template-columns: repeat(4, minmax(0, 1fr)); background: #fff; border: 1px solid var(--color-border); border-radius: 8px; }
.ai-strip button { display: grid; min-height: 116px; grid-template-columns: 42px minmax(0, 1fr); align-items: center; gap: 12px; padding: 18px; color: inherit; text-align: left; background: #fff; border: 0; border-right: 1px solid var(--color-border); cursor: pointer; }
.ai-strip button:has(.ai-arrow) { grid-template-columns: 42px minmax(0, 1fr) 18px; }
.ai-strip button:last-child { border-right: 0; }
.ai-strip button:hover { background: #fffaf0; }
.ai-strip-icon { display: inline-flex; width: 42px; height: 42px; align-items: center; justify-content: center; color: #9a6900; background: #fff2c9; border-radius: 8px; }
.ai-strip button > span:nth-child(2) { display: flex; min-width: 0; flex-direction: column; }
.ai-strip strong { font-weight: 600; }
.ai-strip small { margin-top: 4px; overflow: hidden; color: var(--color-text-secondary); text-overflow: ellipsis; white-space: nowrap; }
.ai-arrow { color: var(--color-text-tertiary); }
.shortcut-section { padding: 42px 0 50px; background: #fff; }
.shortcut-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.shortcut-grid button { display: flex; min-height: 128px; align-items: center; justify-content: space-between; padding: 22px; color: var(--color-text-primary); text-align: left; background: var(--domain-soft); border: 1px solid transparent; border-radius: 8px; cursor: pointer; }
.shortcut-grid button:hover { border-color: var(--domain-color); }
.shortcut-grid button span { display: flex; flex-direction: column; }
.shortcut-grid small { color: var(--color-text-secondary); }
.shortcut-grid strong { margin: 4px 0 12px; font-size: 18px; font-weight: 600; }
.shortcut-grid em { color: var(--domain-color); font-size: 13px; font-style: normal; }
.shortcut-grid .el-icon { color: var(--domain-color); font-size: 36px; }
.portal-footer { padding: 32px 0; color: rgba(255,255,255,.74); background: var(--color-brand-800); }
.footer-content { display: flex; align-items: center; justify-content: space-between; gap: 24px; }
.footer-content strong { color: #fff; font-size: 17px; font-weight: 600; }
.footer-content p { margin: 4px 0 0; font-size: 13px; }
.floating-actions { position: fixed; z-index: 20; right: 18px; bottom: 80px; display: flex; overflow: hidden; flex-direction: column; background: #fff; border: 1px solid var(--color-border); border-radius: 8px; box-shadow: var(--shadow-overlay); }
.floating-actions button { display: inline-flex; width: 44px; height: 44px; align-items: center; justify-content: center; color: var(--color-text-secondary); background: #fff; border: 0; border-bottom: 1px solid var(--color-border-light); cursor: pointer; }
.floating-actions button:last-child { border-bottom: 0; }
.floating-actions button:hover { color: var(--color-brand-700); background: var(--color-brand-50); }
@media (max-width: 1199px) { .service-grid { grid-template-columns: repeat(3, minmax(0,1fr)); } .ai-strip { grid-template-columns: repeat(2, minmax(0,1fr)); } .ai-strip button:nth-child(2) { border-right: 0; } .ai-strip button:nth-child(-n+2) { border-bottom: 1px solid var(--color-border); } }
@media (max-width: 991px) { .hero-status { display: none; } .shortcut-grid { grid-template-columns: repeat(2, minmax(0,1fr)); } }
@media (max-width: 767px) { .campus-hero { height: 360px; } .hero-content { align-items: flex-end; padding-bottom: 74px; } .hero-copy h1 { font-size: 28px; } .hero-copy > p:not(.hero-kicker) { font-size: 14px; } .hero-actions { align-items: stretch; flex-direction: column; } .quick-section { width: calc(100% - 24px); padding: 22px 18px 28px; } .quick-header { align-items: flex-start; } .quick-header > .el-button { display: none; } .segmented-tabs { gap: 18px; overflow-x: auto; overflow-y: hidden; } .service-grid { grid-template-columns: 1fr; } .domain-tabs { gap: 22px; } .domain-description { margin-bottom: 12px; } .ai-strip { grid-template-columns: 1fr; } .ai-strip button { border-right: 0; border-bottom: 1px solid var(--color-border); } .ai-strip button:last-child { border-bottom: 0; } .shortcut-grid { grid-template-columns: 1fr; } .footer-content { align-items: flex-start; flex-direction: column; } .floating-actions { display: none; } }
</style>
