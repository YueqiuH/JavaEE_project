<template>
  <div class="app-shell" :class="{ 'workspace-mode': workspaceMode }">
    <header class="app-header">
      <div class="header-inner">
        <router-link class="brand" to="/home" aria-label="返回智慧校园首页">
          <BrandMark />
          <span><strong>智慧校园</strong><small>服务平台</small></span>
        </router-link>

        <nav class="primary-nav" aria-label="主导航">
          <router-link to="/home"><el-icon><HomeFilled /></el-icon>首页</router-link>
          <router-link to="/services"><el-icon><Grid /></el-icon>服务中心</router-link>
          <router-link to="/workbench"><el-icon><Checked /></el-icon>工作台</router-link>
        </nav>

        <div class="header-actions">
          <el-tooltip content="搜索服务" placement="bottom">
            <button class="header-icon-button" type="button" aria-label="搜索服务" @click="router.push('/services')"><el-icon><Search /></el-icon></button>
          </el-tooltip>
          <el-badge :value="3" :max="9" class="notice-badge">
            <el-tooltip content="消息通知" placement="bottom">
              <button class="header-icon-button" type="button" aria-label="消息通知" @click="router.push({ name: 'meetingNotice' })"><el-icon><Bell /></el-icon></button>
            </el-tooltip>
          </el-badge>
          <el-dropdown ref="userDropdownRef" trigger="click" @command="handleUserCommand" @visible-change="dropdownOpen = $event">
            <button class="user-button" type="button">
              <span class="user-avatar">{{ avatarText }}</span>
              <span class="user-copy"><strong>{{ displayName }}</strong></span>
              <el-icon :class="{ rotated: dropdownOpen }"><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="User" command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item :icon="Setting" command="settings">修改密码</el-dropdown-item>
                <el-dropdown-item divided :icon="SwitchButton" command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <button class="mobile-menu-button" type="button" aria-label="打开导航" @click="mobileNavOpen = true"><el-icon><Menu /></el-icon></button>
        </div>
      </div>
    </header>

    <div class="app-body">
      <aside v-if="workspaceMode" class="domain-sidebar">
        <div class="sidebar-domain" :style="{ '--domain-color': currentDomain.color }">
          <span><el-icon><component :is="currentDomain.icon" /></el-icon></span>
          <div><strong>{{ sidebarDomainLabel }}</strong><small>{{ currentDomain.description }}</small></div>
        </div>
        <nav aria-label="当前业务域功能">
          <router-link v-for="service in sidebarServices" :key="service.key" :to="{ name: service.routeName }">
            <el-icon><component :is="service.icon" /></el-icon><span>{{ service.title }}</span>
          </router-link>
        </nav>
        <router-link class="all-services-link" to="/services"><el-icon><Grid /></el-icon>全部服务</router-link>
      </aside>

      <main class="app-main">
        <router-view />
      </main>
    </div>

    <el-drawer v-model="mobileNavOpen" direction="rtl" size="min(320px, 86vw)" :with-header="false">
      <div class="mobile-drawer">
        <div class="drawer-brand"><BrandMark /><div><strong>智慧校园</strong><small>服务平台</small></div><button type="button" aria-label="关闭导航" @click="mobileNavOpen=false"><el-icon><Close /></el-icon></button></div>
        <nav class="mobile-primary"><router-link to="/home" @click="mobileNavOpen=false"><el-icon><HomeFilled /></el-icon>首页</router-link><router-link to="/services" @click="mobileNavOpen=false"><el-icon><Grid /></el-icon>服务中心</router-link><router-link to="/workbench" @click="mobileNavOpen=false"><el-icon><Checked /></el-icon>工作台</router-link></nav>
        <template v-if="workspaceMode"><p class="drawer-label">{{ sidebarDomainLabel }}</p><nav class="mobile-domain"><router-link v-for="service in sidebarServices" :key="service.key" :to="{name:service.routeName}" @click="mobileNavOpen=false"><el-icon><component :is="service.icon" /></el-icon>{{service.title}}</router-link></nav></template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, provide, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Bell, Checked, Close, Grid, HomeFilled, Menu, Search, Setting, SwitchButton, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import BrandMark from '@/components/BrandMark.vue'
import { domainMap, getServicesByDomain, getBaseDomainLabel, canAccessService } from '@/config/navigation.js'
import { getCurrentUser, logoutUser } from '@/api/auth.js'
import { clearAccessToken } from '@/utils/authToken.js'
import { clearStoredCurrentUser, getStoredCurrentUser, setStoredCurrentUser } from '@/utils/authSession.js'

const route = useRoute()
const router = useRouter()
const userDropdownRef = ref(null)
const dropdownOpen = ref(false)
const previewUser = import.meta.env.DEV && import.meta.env.VITE_UI_PREVIEW === 'true'
  ? { user: { username: '600001', realName: '林同学' }, roles: ['STUDENT'], permissions: [], menus: [] }
  : null
const currentUser = ref(getStoredCurrentUser() || previewUser)
const currentUserType = computed(() => currentUser.value?.user?.userType ?? null)
const mobileNavOpen = ref(false)

provide('currentUser', currentUser)

const workspaceMode = computed(() => Boolean(route.meta.workspace))
const currentDomain = computed(() => domainMap[route.meta.domain] || domainMap.teaching)
const sidebarServices = computed(() => {
  const perms = getStoredCurrentUser()?.permissions || []
  const ut = getStoredCurrentUser()?.user?.userType
  return getServicesByDomain(currentDomain.value.key).filter(s => canAccessService(s, perms, ut))
})
const sidebarDomainLabel = computed(() => currentDomain.value.key === 'base' ? getBaseDomainLabel(getStoredCurrentUser()?.permissions || []) : currentDomain.value.label)
const displayName = computed(() => currentUser.value?.user?.realName || currentUser.value?.user?.username || '校园用户')
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
const roleLabel = computed(() => {
  const t = currentUser.value?.user?.userType
  return { 1: '学生', 2: '教师', 3: '教职工', 4: '教务处' }[t] || '用户'
})

const loadCurrentUser = async () => {
  const result = await getCurrentUser()
  currentUser.value = result.data
  setStoredCurrentUser(result.data)
}

const logout = async () => {
  try { await logoutUser() } finally {
    clearAccessToken()
    clearStoredCurrentUser()
    await router.push('/login')
  }
}

const handleUserCommand = async (command) => {
  if (command === 'logout') return logout()
  router.push({ name: 'profile', query: { tab: command === 'settings' ? 'password' : 'info' } })
}

const closeDropdown = () => { userDropdownRef.value?.handleClose?.() }
onMounted(() => {
  if (!currentUser.value) loadCurrentUser().catch(() => {})
  document.addEventListener('wheel', closeDropdown, true)
})
onUnmounted(() => { document.removeEventListener('wheel', closeDropdown, true) })
</script>

<style scoped>
.app-shell { min-height:100vh; background:var(--color-background); }
.app-header { position:sticky; z-index:100; top:0; height:var(--header-height); color:#fff; background:var(--color-brand-700); box-shadow:0 1px 0 rgba(255,255,255,.12); }
.header-inner { display:flex; width:min(var(--layout-width),calc(100% - 48px)); height:100%; align-items:center; margin:0 auto; }
.brand { display:flex; min-width:220px; align-items:center; gap:10px; color:#fff; text-decoration:none; }
.brand > span { display:flex; flex-direction:column; line-height:1.2; }
.brand strong { font-size:17px; font-weight:600; }
.brand small { margin-top:2px; color:rgba(255,255,255,.67); font-size:11px; }
.primary-nav { align-self:stretch; display:flex; gap:4px; }
.primary-nav a { position:relative; display:flex; min-width:104px; align-items:center; justify-content:center; gap:7px; color:rgba(255,255,255,.72); text-decoration:none; }
.primary-nav a:hover,.primary-nav a.router-link-exact-active { color:#fff; background:rgba(255,255,255,.06); }
.primary-nav a.router-link-exact-active::after { position:absolute; right:20px; bottom:0; left:20px; height:3px; content:''; background:#ffd77a; border-radius:3px 3px 0 0; }
.header-actions { display:flex; align-items:center; gap:6px; margin-left:auto; }
.header-icon-button,.mobile-menu-button { display:inline-flex; width:40px; height:40px; align-items:center; justify-content:center; color:rgba(255,255,255,.86); background:transparent; border:0; border-radius:4px; cursor:pointer; }
.header-icon-button:hover,.mobile-menu-button:hover { color:#fff; background:rgba(255,255,255,.1); }
.notice-badge { margin-right:4px; }
.notice-badge :deep(.el-badge__content) { border-color:var(--color-brand-700); }
.user-button { display:flex; min-width:166px; height:46px; align-items:center; gap:9px; padding:0 8px; color:#fff; text-align:left; background:transparent; border:0; border-radius:4px; cursor:pointer; }
.user-button:hover { background:rgba(255,255,255,.08); }
.user-avatar { display:inline-flex; width:32px; height:32px; flex:0 0 32px; align-items:center; justify-content:center; color:var(--color-brand-700); font-weight:700; background:#fff; border-radius:50%; }
.user-copy { display:flex; min-width:0; flex:1; flex-direction:column; line-height:1.25; }
.user-copy strong { overflow:hidden; font-size:13px; font-weight:600; text-overflow:ellipsis; white-space:nowrap; }
.user-copy small { color:rgba(255,255,255,.65); font-size:11px; }
.user-button .el-icon { transition: transform .25s ease }
.user-button .el-icon.rotated { transform: rotate(180deg) }
.mobile-menu-button { display:none; }
.app-body { display:flex; min-height:calc(100vh - var(--header-height)); }
.app-main { min-width:0; flex:1; }
.domain-sidebar { position:sticky; top:var(--header-height); display:flex; width:216px; height:calc(100vh - var(--header-height)); flex:0 0 216px; flex-direction:column; overflow-y:auto; background:#fff; border-right:1px solid var(--color-border); }
.sidebar-domain { display:flex; align-items:center; gap:10px; padding:20px 16px 16px; border-bottom:1px solid var(--color-border-light); }
.sidebar-domain > span { display:inline-flex; width:38px; height:38px; flex:0 0 38px; align-items:center; justify-content:center; color:var(--domain-color); background:color-mix(in srgb,var(--domain-color) 10%,white); border-radius:8px; }
.sidebar-domain div { display:flex; min-width:0; flex-direction:column; }
.sidebar-domain strong { font-weight:600; }
.sidebar-domain small { overflow:hidden; color:var(--color-text-tertiary); font-size:11px; text-overflow:ellipsis; white-space:nowrap; }
.domain-sidebar nav { display:flex; flex:1; flex-direction:column; gap:4px; padding:14px 10px; }
.domain-sidebar nav a,.all-services-link { display:flex; min-height:44px; align-items:center; gap:10px; padding:0 12px; color:var(--color-text-secondary); text-decoration:none; border-radius:4px; }
.domain-sidebar nav a:hover,.domain-sidebar nav a.router-link-active { color:var(--color-brand-700); background:var(--color-brand-50); }
.domain-sidebar nav a.router-link-active { font-weight:600; box-shadow:inset 3px 0 0 var(--color-brand-700); }
.domain-sidebar nav a .el-icon,.all-services-link .el-icon { flex:0 0 auto; font-size:18px; }
.all-services-link { margin:0 10px 14px; border-top:1px solid var(--color-border-light); border-radius:0; }
.all-services-link:hover { color:var(--color-brand-700); }
.mobile-drawer { min-height:100%; }
.drawer-brand { display:flex; align-items:center; gap:10px; padding-bottom:18px; border-bottom:1px solid var(--color-border); }
.drawer-brand > div { display:flex; flex:1; flex-direction:column; }
.drawer-brand strong { font-weight:600; }
.drawer-brand small { color:var(--color-text-tertiary); }
.drawer-brand button { display:inline-flex; width:40px; height:40px; align-items:center; justify-content:center; background:none; border:0; }
.mobile-primary,.mobile-domain { display:flex; flex-direction:column; gap:4px; padding:14px 0; }
.mobile-primary a,.mobile-domain a { display:flex; min-height:46px; align-items:center; gap:12px; padding:0 12px; color:var(--color-text-secondary); text-decoration:none; border-radius:4px; }
.mobile-primary a.router-link-active,.mobile-domain a.router-link-active { color:var(--color-brand-700); background:var(--color-brand-50); font-weight:600; }
.drawer-label { margin:10px 0 0; padding-top:16px; color:var(--color-text-tertiary); font-size:12px; border-top:1px solid var(--color-border); }
@media(max-width:991px){.header-inner{width:calc(100% - 32px)}.brand{min-width:auto}.brand small{display:none}.primary-nav a{min-width:90px}.user-copy{display:none}.user-button{min-width:auto}.domain-sidebar{width:64px;flex-basis:64px}.sidebar-domain{justify-content:center;padding:16px 10px}.sidebar-domain div,.domain-sidebar nav a span,.all-services-link:not(.el-icon){font-size:0}.domain-sidebar nav a,.all-services-link{justify-content:center;padding:0}.domain-sidebar nav a.router-link-active{box-shadow:inset 3px 0 0 var(--color-brand-700)}}
@media(max-width:767px){.primary-nav,.header-icon-button,.notice-badge,.user-button{display:none}.mobile-menu-button{display:inline-flex}.brand strong{font-size:15px}.domain-sidebar{display:none}.header-inner{width:calc(100% - 24px)}}
</style>
