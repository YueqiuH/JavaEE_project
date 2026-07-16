<template>
    <div class="landing">
        <!-- ===== 导航栏 ===== -->
        <header class="navbar" :class="{ 'navbar-solid': isScrolled }">
            <div class="nav-inner">
                <a href="/" class="logo-link">
                    <img class="logo-white" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' style='stop-color:%233B82F6'/%3E%3Cstop offset='100%25' style='stop-color:%2306B6D4'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect x='4' y='4' width='32' height='32' rx='8' fill='url(%23g)'/%3E%3Ctext x='20' y='27' text-anchor='middle' fill='white' font-size='18' font-weight='bold' font-family='sans-serif'%3E智%3C/text%3E%3C/svg%3E" alt="智驭校园" width="40" height="40" />
                    <span class="logo-name">智驭校园</span>
                </a>
                <nav class="nav-menu">
                    <a href="/" class="nav-item active" @click.prevent>首页</a>
                    <a href="/login" class="nav-btn-login" @click.prevent="goLogin">登录</a>
                </nav>
            </div>
        </header>

        <!-- ===== Hero Banner ===== -->
        <section class="hero">
            <div class="hero-media">
                <img
                    class="hero-bg-img"
                    src="https://cloudcache.tencent-cloud.com/qcloud/ui/static/static_source_business/bc0b2c5e-5aff-4e1a-a3a1-7a1c9bc2c19e.jpg"
                    alt=""
                />
                <div class="hero-overlay"></div>
            </div>
            <div class="hero-text">
                <h1 class="hero-title">智驭校园</h1>
                <p class="hero-subtitle">共创智慧教育时代</p>
            </div>
        </section>

        <!-- ===== 核心方案区 ===== -->
        <section class="solutions-section">
            <div class="solutions-inner">
                <h4 class="section-heading">核心解决方案</h4>
                <p class="section-lead">致力于做好教育信息化升级的"数字助手"</p>

                <!-- Tab 切换 -->
                <div class="tab-bar">
                    <button
                        v-for="tab in tabs"
                        :key="tab.key"
                        class="tab-btn"
                        :class="{ active: activeTab === tab.key }"
                        @click="activeTab = tab.key"
                    >{{ tab.label }}</button>
                </div>

                <!-- 卡片网格 -->
                <div class="card-grid">
                    <a
                        v-for="card in currentCards"
                        :key="card.id"
                        href="javascript:void(0)"
                        class="sol-card"
                        @click.prevent
                    >
                        <div class="card-img-wrap">
                            <div class="card-img-placeholder" :style="{ background: card.bg }">
                                <el-icon :size="40" color="rgba(255,255,255,0.7)"><component :is="card.icon" /></el-icon>
                            </div>
                        </div>
                        <h5 class="card-name">{{ card.title }}</h5>
                        <p class="card-desc">{{ card.desc }}</p>
                    </a>
                </div>
            </div>
        </section>

        <!-- ===== 页脚 ===== -->
        <footer class="site-footer">
            <div class="footer-inner">
                <div class="footer-main">
                    <div class="footer-brand">
                        <div class="footer-logo-row">
                            <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 40 40'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' style='stop-color:%233B82F6'/%3E%3Cstop offset='100%25' style='stop-color:%2306B6D4'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect x='4' y='4' width='32' height='32' rx='8' fill='url(%23g)'/%3E%3Ctext x='20' y='27' text-anchor='middle' fill='white' font-size='18' font-weight='bold' font-family='sans-serif'%3E智%3C/text%3E%3C/svg%3E" alt="" width="32" height="32" />
                            <span class="footer-brand-name">智驭校园</span>
                        </div>
                    </div>
                    <div class="footer-links">
                        <dl class="footer-col">
                            <dt>产品方案</dt>
                            <dd><a href="#">教务管理系统</a></dd>
                            <dd><a href="#">学工管理系统</a></dd>
                            <dd><a href="#">协同办公系统</a></dd>
                            <dd><a href="#">数据决策系统</a></dd>
                        </dl>
                        <dl class="footer-col">
                            <dt>AI 引擎</dt>
                            <dd><a href="#">智能学习助理</a></dd>
                            <dd><a href="#">心理预警助手</a></dd>
                            <dd><a href="#">公文审批助手</a></dd>
                            <dd><a href="#">智能报表助手</a></dd>
                        </dl>
                        <dl class="footer-col">
                            <dt>关于</dt>
                            <dd><a href="#">关于我们</a></dd>
                            <dd><a href="#">联系我们</a></dd>
                        </dl>
                    </div>
                </div>
                <div class="footer-copy">
                    <p>Copyright © 2024-2026 智驭校园. All Rights Reserved.</p>
                </div>
            </div>
        </footer>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { School, UserFilled, OfficeBuilding, DataAnalysis } from '@element-plus/icons-vue'

const router = useRouter()
const isScrolled = ref(false)
const activeTab = ref('teaching')

const tabs = [
    { key: 'teaching', label: '教务教学' },
    { key: 'student', label: '学工管理' },
    { key: 'office', label: '协同办公' },
    { key: 'data', label: '数据决策' }
]

const allCards = {
    teaching: [
        { id: 't1', icon: 'School', bg: 'linear-gradient(135deg, #3B82F6, #2563EB)', title: '排课与课表管理系统', desc: '智能排课引擎秒级生成课表，支持调课申请与审批流转，学生教师日历式查询' },
        { id: 't2', icon: 'School', bg: 'linear-gradient(135deg, #6366F1, #4F46E5)', title: '选课与容量控制系统', desc: '实时库存校验与时间冲突检测，动态调整选课名额，教师查看选课名单' },
        { id: 't3', icon: 'DataAnalysis', bg: 'linear-gradient(135deg, #10B981, #059669)', title: '成绩评定与预警系统', desc: '在线录入成绩自动计算绩点，不及格标注与学业预警自动触发推送' },
        { id: 't4', icon: 'School', bg: 'linear-gradient(135deg, #F59E0B, #D97706)', title: '考试安排与重修补考系统', desc: '期末考试发布与监考安排，座位自动分配，补考重修在线报名' },
        { id: 't5', icon: 'School', bg: 'linear-gradient(135deg, #EC4899, #DB2777)', title: '毕业设计过程管理系统', desc: '教师发布课题学生在线选题，中期报告提交与教师批注反馈全流程' },
        { id: 't6', icon: 'School', bg: 'linear-gradient(135deg, #8B5CF6, #7C3AED)', title: 'AI 智能学习助理', desc: '上传课件自动生成知识点摘要与复习题，基于历史成绩推荐学习路径' }
    ],
    student: [
        { id: 's1', icon: 'UserFilled', bg: 'linear-gradient(135deg, #3B82F6, #2563EB)', title: '学籍变动与信息审核系统', desc: '休学复学转专业在线申请，辅导员教务二级审核流程' },
        { id: 's2', icon: 'UserFilled', bg: 'linear-gradient(135deg, #10B981, #059669)', title: '奖助贷资助与评审系统', desc: '奖学金困难补助在线申请，名额评定与材料审核自动汇总' },
        { id: 's3', icon: 'DataAnalysis', bg: 'linear-gradient(135deg, #8B5CF6, #7C3AED)', title: '评教反馈与教学质量评估', desc: '学生在线匿名评教打分，教师查询个人得分，院系统计整体数据' },
        { id: 's4', icon: 'UserFilled', bg: 'linear-gradient(135deg, #F59E0B, #D97706)', title: '学科竞赛与组队系统', desc: '竞赛信息发布，学生在线组队报名，教师审核队伍资质' },
        { id: 's5', icon: 'UserFilled', bg: 'linear-gradient(135deg, #6366F1, #4F46E5)', title: '实验室与实践预约系统', desc: '实验室开放时间管理，在线预约工位设备，自动冲突校验' },
        { id: 's6', icon: 'UserFilled', bg: 'linear-gradient(135deg, #EC4899, #DB2777)', title: 'AI 心理咨询与行为预警', desc: '24h AI心理陪伴对话，问卷智能分析，红橙黄三级预警推送辅导员' }
    ],
    office: [
        { id: 'o1', icon: 'OfficeBuilding', bg: 'linear-gradient(135deg, #3B82F6, #2563EB)', title: '学杂费交纳与流水查询', desc: '财务导入账单，学生在线支付，一卡通充值消费明细查询' },
        { id: 'o2', icon: 'OfficeBuilding', bg: 'linear-gradient(135deg, #10B981, #059669)', title: '固定资产管理与申领', desc: '设备购置领用申请，部门审批，资产台账自动更新' },
        { id: 'o3', icon: 'OfficeBuilding', bg: 'linear-gradient(135deg, #8B5CF6, #7C3AED)', title: '教职工工作计划与协同', desc: '周月计划在线制定，部门负责人查询点评与任务指派' },
        { id: 'o4', icon: 'OfficeBuilding', bg: 'linear-gradient(135deg, #F59E0B, #D97706)', title: '官方公文流转系统', desc: '公文会签请示报告，逐级审批链条，支持退回催办' },
        { id: 'o5', icon: 'OfficeBuilding', bg: 'linear-gradient(135deg, #6366F1, #4F46E5)', title: '校园会议与通知发布', desc: '会议通知发布，自动推送，参会人在线反馈参会/请假' },
        { id: 'o6', icon: 'OfficeBuilding', bg: 'linear-gradient(135deg, #EC4899, #DB2777)', title: 'AI 公文摘要与审批助手', desc: '长篇公文一键提炼要点，AI推荐审批意见草稿' }
    ],
    data: [
        { id: 'd1', icon: 'DataAnalysis', bg: 'linear-gradient(135deg, #3B82F6, #2563EB)', title: '教职工与学生信息库', desc: '全校师生基础数字档案，多条件组合检索与批量导入' },
        { id: 'd2', icon: 'DataAnalysis', bg: 'linear-gradient(135deg, #10B981, #059669)', title: '招生与迎新数据统计', desc: '招生计划导入，实时汇总报到人数报到率生源地分布' },
        { id: 'd3', icon: 'DataAnalysis', bg: 'linear-gradient(135deg, #8B5CF6, #7C3AED)', title: '学生特征多维统计', desc: '在校生总量按院系专业班级穿透，选课偏好与生源地分析' },
        { id: 'd4', icon: 'DataAnalysis', bg: 'linear-gradient(135deg, #F59E0B, #D97706)', title: '院系与专业资源管理', desc: '院系介绍专业设置培养方案教研室分布等基础数据维护' },
        { id: 'd5', icon: 'DataAnalysis', bg: 'linear-gradient(135deg, #6366F1, #4F46E5)', title: '新闻公告与校园论坛', desc: '系统公告学校新闻发布，师生互动论坛发帖回复点赞' },
        { id: 'd6', icon: 'DataAnalysis', bg: 'linear-gradient(135deg, #EC4899, #DB2777)', title: 'AI 自然语言报表助手', desc: '自然语言输入查询需求，AI自动查询数据库生成图表' }
    ]
}

const currentCards = computed(() => allCards[activeTab.value])

const goLogin = () => router.push('/login')

let scrollTimer = null
window.addEventListener('scroll', () => {
    if (scrollTimer) return
    scrollTimer = setTimeout(() => {
        isScrolled.value = window.scrollY > 60
        scrollTimer = null
    }, 50)
}, { passive: true })
</script>

<style scoped>
/* === Reset === */
.landing {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
    color: #333;
    line-height: 1.6;
    -webkit-font-smoothing: antialiased;
}

/* === Navbar === */
.navbar {
    position: fixed;
    top: 0; left: 0; right: 0;
    z-index: 100;
    height: 64px;
    transition: background 0.3s, box-shadow 0.3s;
}
.navbar-solid {
    background: #fff;
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.nav-inner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 40px;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
}
.logo-link {
    display: flex;
    align-items: center;
    gap: 10px;
    text-decoration: none;
}
.logo-name {
    font-size: 18px;
    font-weight: 700;
    color: #fff;
    letter-spacing: 2px;
    transition: color 0.3s;
}
.navbar-solid .logo-name { color: #1a1a1a; }

.nav-menu {
    display: flex;
    align-items: center;
    gap: 28px;
}
.nav-item {
    font-size: 15px;
    color: rgba(255,255,255,0.9);
    text-decoration: none;
    padding: 4px 0;
    position: relative;
    transition: color 0.2s;
}
.nav-item:hover, .nav-item.active { color: #fff; }
.nav-item.active::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0; right: 0;
    height: 2px;
    background: #fff;
}
.navbar-solid .nav-item { color: #555; }
.navbar-solid .nav-item:hover, .navbar-solid .nav-item.active { color: #0052D9; }
.navbar-solid .nav-item.active::after { background: #0052D9; }

.nav-btn-login {
    display: inline-block;
    padding: 6px 20px;
    border: 1px solid rgba(255,255,255,0.6);
    border-radius: 20px;
    color: #fff;
    font-size: 14px;
    text-decoration: none;
    transition: all 0.2s;
}
.nav-btn-login:hover { background: rgba(255,255,255,0.15); border-color: #fff; }
.navbar-solid .nav-btn-login {
    border-color: #0052D9;
    color: #0052D9;
}
.navbar-solid .nav-btn-login:hover { background: #0052D9; color: #fff; }

/* === Hero === */
.hero {
    position: relative;
    width: 100%;
    height: 100vh;
    min-height: 600px;
    display: flex;
    align-items: center;
    justify-content: center;
}
.hero-media {
    position: absolute;
    inset: 0;
    overflow: hidden;
}
.hero-bg-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}
.hero-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0,0,0,0.35);
}
.hero-text {
    position: relative;
    z-index: 2;
    text-align: center;
}
.hero-title {
    font-size: 56px;
    font-weight: 700;
    color: #fff;
    letter-spacing: 12px;
    margin: 0 0 12px;
    text-shadow: 0 2px 16px rgba(0,0,0,0.3);
}
.hero-subtitle {
    font-size: 24px;
    color: rgba(255,255,255,0.85);
    letter-spacing: 6px;
    margin: 0;
    font-weight: 300;
}

/* === Solutions Section === */
.solutions-section {
    padding: 80px 40px;
    background: #f5f7fa;
}
.solutions-inner {
    max-width: 1200px;
    margin: 0 auto;
}
.section-heading {
    text-align: center;
    font-size: 28px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 8px;
}
.section-lead {
    text-align: center;
    font-size: 15px;
    color: #888;
    margin: 0 0 36px;
}

/* Tab bar */
.tab-bar {
    display: flex;
    justify-content: center;
    gap: 0;
    margin-bottom: 36px;
}
.tab-btn {
    padding: 10px 28px;
    font-size: 15px;
    color: #666;
    background: none;
    border: none;
    border-bottom: 2px solid transparent;
    cursor: pointer;
    transition: all 0.2s;
}
.tab-btn:hover { color: #0052D9; }
.tab-btn.active {
    color: #0052D9;
    border-bottom-color: #0052D9;
    font-weight: 600;
}

/* Card grid */
.card-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
}
.sol-card {
    display: block;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    text-decoration: none;
    color: inherit;
    transition: box-shadow 0.3s, transform 0.3s;
}
.sol-card:hover {
    box-shadow: 0 6px 24px rgba(0,0,0,0.1);
    transform: translateY(-4px);
}
.card-img-wrap {
    width: 100%;
    height: 180px;
    overflow: hidden;
}
.card-img-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}
.card-name {
    font-size: 16px;
    font-weight: 600;
    margin: 16px 20px 8px;
    color: #1a1a1a;
}
.card-desc {
    font-size: 13px;
    color: #999;
    margin: 0 20px 20px;
    line-height: 1.6;
}

/* === Footer === */
.site-footer {
    background: #1a1f2e;
    padding: 48px 40px 20px;
}
.footer-inner { max-width: 1200px; margin: 0 auto; }
.footer-main { display: flex; justify-content: space-between; gap: 60px; margin-bottom: 36px; }
.footer-logo-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.footer-brand-name { color: #ccc; font-size: 16px; font-weight: 600; }
.footer-links { display: flex; gap: 64px; }
.footer-col { margin: 0; }
.footer-col dt { color: #ddd; font-size: 14px; font-weight: 600; margin-bottom: 12px; }
.footer-col dd { margin: 0 0 8px; }
.footer-col a { color: #888; font-size: 13px; text-decoration: none; line-height: 2; }
.footer-col a:hover { color: #bbb; }
.footer-copy { border-top: 1px solid #2a2f3e; padding-top: 16px; text-align: center; }
.footer-copy p { color: #666; font-size: 12px; margin: 0; }

/* === Responsive === */
@media (max-width: 900px) {
    .card-grid { grid-template-columns: repeat(2, 1fr); }
    .hero-title { font-size: 36px; letter-spacing: 6px; }
    .hero-subtitle { font-size: 18px; }
    .footer-main { flex-direction: column; gap: 32px; }
}
@media (max-width: 600px) {
    .card-grid { grid-template-columns: 1fr; }
    .nav-inner { padding: 0 20px; }
    .footer-links { gap: 24px; flex-wrap: wrap; }
}
</style>
