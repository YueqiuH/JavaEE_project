<template>
  <div class="landing-page">
    <header class="landing-header">
      <div class="container header-content">
        <router-link class="landing-brand" to="/">
          <BrandMark />
          <span><strong>智慧校园</strong><small>服务平台</small></span>
        </router-link>
        <nav aria-label="欢迎页导航">
          <a href="#capabilities">平台能力</a>
          <a href="#services">服务目录</a>
          <el-button type="primary" @click="router.push('/login')">登录平台</el-button>
        </nav>
      </div>
    </header>

    <main>
      <section class="landing-hero" aria-labelledby="landing-title">
        <img :src="campusHero" alt="秋日校园建筑与树木" />
        <div class="hero-overlay"></div>
        <div class="container hero-inner">
          <div class="hero-copy">
            <p>SMART CAMPUS SERVICE</p>
            <h1 id="landing-title">智慧校园服务平台</h1>
            <h2>一个入口，连接校园里的每一项服务</h2>
            <span>面向学生、教师和教职工，统一提供教务教学、学生事务、协同办公与数据服务。</span>
            <div class="hero-actions"><el-button type="primary" size="large" :icon="Right" @click="router.push('/login')">进入平台</el-button><el-button size="large" :icon="Grid" @click="scrollToCapabilities">浏览平台能力</el-button></div>
          </div>
          <div class="hero-facts" aria-label="平台概览">
            <div><strong>24</strong><span>项校园服务</span></div>
            <div><strong>4</strong><span>个业务领域</span></div>
            <div><strong>1</strong><span>个统一入口</span></div>
          </div>
        </div>
      </section>

      <section id="capabilities" class="capability-band">
        <div class="container">
          <div class="intro-heading"><p>平台能力</p><h2>围绕校园日常工作构建完整服务体系</h2><span>清晰的业务入口、统一的办理流程和面向角色的个人工作台。</span></div>
          <div class="capability-grid">
            <article v-for="domain in domains" :key="domain.key" :style="{'--domain-color':domain.color,'--domain-soft':domain.softColor}">
              <span class="domain-icon"><el-icon><component :is="domain.icon" /></el-icon></span>
              <div><small>0{{ domains.findIndex(item => item.key === domain.key) + 1 }}</small><h3>{{domain.label}}</h3><p>{{domain.description}}</p></div>
              <ul><li v-for="service in getServicesByDomain(domain.key).slice(0,3)" :key="service.key">{{service.title}}</li></ul>
            </article>
          </div>
        </div>
      </section>

      <section id="services" class="service-preview">
        <div class="container preview-grid">
          <div class="preview-copy"><p>统一服务门户</p><h2>所有功能都能被看见，也能快速抵达</h2><span>登录后，平台会根据角色整理推荐服务、最近使用、收藏与待办事项。进入具体业务后，页面自动切换为更紧凑的工作区。</span><el-button type="primary" :icon="Right" @click="router.push('/login')">登录查看个人门户</el-button></div>
          <div class="preview-board" aria-label="服务入口示意">
            <div class="preview-nav"><span></span><strong>常用服务</strong><small>8 项</small></div>
            <div class="preview-cards"><div v-for="service in previewServices" :key="service.key"><span :style="{color:domainMap[service.domain].color,background:domainMap[service.domain].softColor}"><el-icon><component :is="service.icon" /></el-icon></span><p><strong>{{service.title}}</strong><small>{{domainMap[service.domain].label}}</small></p></div></div>
          </div>
        </div>
      </section>
    </main>

    <footer class="landing-footer"><div class="container"><div><strong>智慧校园服务平台</strong><p>统一入口 · 协同办理 · 数据驱动</p></div><div><p>演示账号：600001 / 123321</p></div></div></footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Grid, Right } from '@element-plus/icons-vue'
import BrandMark from '@/components/BrandMark.vue'
import campusHero from '@/assets/images/campus-hero.jpg'
import { domainMap, domains, getServicesByDomain, services } from '@/config/navigation.js'

const router = useRouter()
const previewServices = ['course-schedule','course-selection','lab-booking','document-oa','meeting-notice','student-analytics'].map(key=>services.find(service=>service.key===key))
const scrollToCapabilities=()=>document.querySelector('#capabilities')?.scrollIntoView({behavior:'smooth'})
</script>

<style scoped>
.landing-page{background:#fff}.landing-header{position:absolute;z-index:10;top:0;right:0;left:0;height:72px;color:#fff;border-bottom:1px solid rgba(255,255,255,.18)}.header-content{display:flex;height:100%;align-items:center;justify-content:space-between}.landing-brand{display:flex;align-items:center;gap:10px;color:#fff;text-decoration:none}.landing-brand>span{display:flex;flex-direction:column;line-height:1.2}.landing-brand strong{font-size:17px;font-weight:600}.landing-brand small{margin-top:2px;color:rgba(255,255,255,.68);font-size:11px}.landing-header nav{display:flex;align-items:center;gap:28px}.landing-header nav>a{position:relative;color:rgba(255,255,255,.78);text-decoration:none}.landing-header nav>a:hover{color:#fff}.landing-header .el-button{background:#fff;border-color:#fff;color:var(--color-brand-700)}
.landing-hero{position:relative;display:flex;height:min(820px,88vh);min-height:660px;align-items:center;overflow:hidden;color:#fff;background:var(--color-brand-800)}.landing-hero>img{position:absolute;inset:0;width:100%;height:100%;object-fit:cover;object-position:center 55%}.hero-overlay{position:absolute;inset:0;background:linear-gradient(90deg,rgba(1,35,55,.94) 0%,rgba(1,48,72,.72) 46%,rgba(1,48,72,.16) 78%)}.hero-inner{position:relative;display:flex;align-items:flex-end;justify-content:space-between;gap:48px;padding-top:72px}.hero-copy{max-width:670px}.hero-copy>p,.intro-heading>p,.preview-copy>p{margin:0 0 8px;color:#ffd77a;font-size:12px;font-weight:700;letter-spacing:1.5px}.hero-copy h1{margin:0;font-size:42px;font-weight:600;letter-spacing:0}.hero-copy h2{margin:10px 0 0;font-size:24px;font-weight:500}.hero-copy>span{display:block;max-width:610px;margin-top:14px;color:rgba(255,255,255,.75);font-size:16px}.hero-actions{display:flex;gap:12px;margin-top:28px}.hero-actions :deep(.el-button--default){color:#fff;background:rgba(255,255,255,.1);border-color:rgba(255,255,255,.6)}.hero-facts{display:grid;min-width:230px;overflow:hidden;background:rgba(1,52,78,.7);border:1px solid rgba(255,255,255,.2);border-radius:8px;backdrop-filter:blur(8px)}.hero-facts>div{display:flex;align-items:baseline;gap:12px;padding:14px 18px;border-bottom:1px solid rgba(255,255,255,.16)}.hero-facts>div:last-child{border-bottom:0}.hero-facts strong{font-size:26px;font-weight:600}.hero-facts span{color:rgba(255,255,255,.7)}
.capability-band{padding:74px 0 80px;background:var(--color-background)}.intro-heading{text-align:center}.intro-heading>p,.preview-copy>p{color:var(--color-warning)}.intro-heading h2{margin:0;font-size:30px;font-weight:600}.intro-heading>span{display:block;margin-top:8px;color:var(--color-text-secondary)}.capability-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:16px;margin-top:36px}.capability-grid article{min-height:280px;padding:22px;background:#fff;border:1px solid var(--color-border);border-radius:8px}.domain-icon{display:inline-flex;width:48px;height:48px;align-items:center;justify-content:center;color:var(--domain-color);background:var(--domain-soft);border-radius:8px;font-size:25px}.capability-grid article>div{margin-top:28px}.capability-grid small{color:var(--domain-color);font-size:11px;font-weight:700}.capability-grid h3{margin:2px 0 6px;font-size:20px;font-weight:600}.capability-grid p{margin:0;color:var(--color-text-secondary)}.capability-grid ul{padding:0;margin:22px 0 0;list-style:none}.capability-grid li{position:relative;padding:5px 0 5px 14px;color:var(--color-text-secondary);font-size:13px}.capability-grid li::before{position:absolute;top:13px;left:0;width:5px;height:5px;content:'';background:var(--domain-color);border-radius:50%}
.service-preview{padding:80px 0;background:#fff}.preview-grid{display:grid;grid-template-columns:minmax(300px,.7fr) minmax(540px,1.3fr);align-items:center;gap:64px}.preview-copy h2{margin:0;font-size:30px;font-weight:600}.preview-copy>span{display:block;margin:14px 0 26px;color:var(--color-text-secondary);font-size:15px}.preview-board{padding:22px;background:var(--color-background);border:1px solid var(--color-border);border-radius:8px;box-shadow:0 20px 55px rgba(12,42,61,.12)}.preview-nav{display:flex;align-items:center;gap:10px;padding-bottom:16px;border-bottom:1px solid var(--color-border)}.preview-nav>span{width:10px;height:10px;background:var(--color-success);border-radius:50%}.preview-nav strong{font-weight:600}.preview-nav small{margin-left:auto;color:var(--color-text-tertiary)}.preview-cards{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px;margin-top:16px}.preview-cards>div{display:flex;min-width:0;align-items:center;gap:10px;padding:12px;background:#fff;border:1px solid var(--color-border);border-radius:6px}.preview-cards>div>span{display:inline-flex;width:38px;height:38px;flex:0 0 38px;align-items:center;justify-content:center;border-radius:6px;font-size:20px}.preview-cards p{display:flex;min-width:0;flex-direction:column;margin:0}.preview-cards strong{overflow:hidden;font-size:13px;font-weight:600;text-overflow:ellipsis;white-space:nowrap}.preview-cards small{color:var(--color-text-tertiary)}
.landing-footer{padding:30px 0;color:rgba(255,255,255,.68);background:var(--color-brand-800)}.landing-footer>.container{display:flex;align-items:center;justify-content:space-between;gap:24px}.landing-footer strong{color:#fff;font-size:17px;font-weight:600}.landing-footer p{margin:3px 0}.landing-footer>div>div:last-child{text-align:right}.landing-footer small{font-size:11px}
@media(max-width:991px){.capability-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.preview-grid{grid-template-columns:1fr;gap:36px}.hero-facts{display:none}}
@media(max-width:767px){.landing-header nav>a{display:none}.landing-header nav{gap:0}.landing-hero{height:760px;min-height:680px}.hero-inner{align-items:flex-end;padding-bottom:70px}.hero-copy h1{font-size:32px}.hero-copy h2{font-size:20px}.hero-copy>span{font-size:14px}.hero-actions{align-items:stretch;flex-direction:column}.capability-band,.service-preview{padding:56px 0}.intro-heading h2,.preview-copy h2{font-size:24px}.capability-grid{grid-template-columns:1fr}.capability-grid article{min-height:auto}.preview-cards{grid-template-columns:1fr}.landing-footer>.container{align-items:flex-start;flex-direction:column}.landing-footer>div>div:last-child{text-align:left}}
</style>
