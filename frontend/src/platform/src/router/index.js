import { createRouter, createWebHistory } from 'vue-router'
import { clearAccessToken, getAccessToken } from '@/utils/authToken.js'
import { clearStoredCurrentUser, getStoredCurrentUser } from '@/utils/authSession.js'
import { isTokenExpired } from '@/utils/jwt.js'
import { canAccessService, serviceMap } from '@/config/navigation.js'

const UI_PREVIEW_MODE = import.meta.env.DEV && import.meta.env.VITE_UI_PREVIEW === 'true'

const LandingView = () => import('@/views/LandingView.vue')
const LoginView = () => import('@/views/LoginView.vue')
const HomeView = () => import('@/views/HomeView.vue')
const PortalHome = () => import('@/views/dashboard/PortalHome.vue')
const ServiceCenter = () => import('@/views/dashboard/ServiceCenter.vue')
const WorkbenchView = () => import('@/views/dashboard/WorkbenchView.vue')

const featureMeta = (name, domain, serviceKey) => ({ name, domain, serviceKey, workspace: true })

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/', name: 'landing', component: LandingView, meta: { name: '欢迎页' } },
    { path: '/login', name: 'login', component: LoginView, meta: { name: '登录' } },
    {
      path: '/home',
      component: HomeView,
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'portalHome', component: PortalHome, meta: { name: '首页' } },
        { path: '/services', name: 'serviceCenter', component: ServiceCenter, meta: { name: '服务中心' } },
        { path: '/workbench', name: 'workbench', component: WorkbenchView, meta: { name: '工作台' } },

        { path: 'course-schedule', name: 'courseSchedule', component: () => import('@/views/teaching/CourseSchedule.vue'), meta: featureMeta('排课与课表', 'teaching', 'course-schedule') },
        { path: 'auto-schedule', name: 'autoSchedule', component: () => import('@/views/teaching/AutoSchedule.vue'), meta: featureMeta('自动排课引擎', 'teaching', 'auto-schedule') },
        { path: 'course-selection', name: 'courseSelection', component: () => import('@/views/teaching/CourseSelection.vue'), meta: featureMeta('选课与容量', 'teaching', 'course-selection') },
        { path: 'score-management', name: 'scoreManagement', component: () => import('@/views/teaching/ScoreManagement.vue'), meta: featureMeta('成绩评定与预警', 'teaching', 'score-management') },
        { path: 'exam-arrangement', name: 'examArrangement', component: () => import('@/views/teaching/ExamArrangement.vue'), meta: featureMeta('考试与补考', 'teaching', 'exam-arrangement') },
        { path: 'ai-learning', name: 'aiLearning', component: () => import('@/views/teaching/AiLearning.vue'), meta: featureMeta('AI 智能学习助理', 'teaching', 'ai-learning') },

        { path: 'student-status', name: 'studentStatus', component: () => import('@/views/student/StudentStatus.vue'), meta: featureMeta('学籍变动', 'student', 'student-status') },
        { path: 'scholarship', name: 'scholarship', component: () => import('@/views/student/Scholarship.vue'), meta: featureMeta('奖助贷评审', 'student', 'scholarship') },
        { path: 'teaching-evaluation', name: 'teachingEvaluation', component: () => import('@/views/student/TeachingEvaluation.vue'), meta: featureMeta('评教反馈', 'student', 'teaching-evaluation') },
        { path: 'competition', name: 'competition', component: () => import('@/views/student/Competition.vue'), meta: featureMeta('学科竞赛', 'student', 'competition') },
        { path: 'competition/:competitionId/teams', name: 'competitionTeams', component: () => import('@/views/student/CompetitionTeams.vue'), meta: featureMeta('竞赛队伍', 'student', 'competition') },
        { path: 'lab-booking', name: 'labBooking', component: () => import('@/views/student/LabBooking.vue'), meta: featureMeta('实验室预约', 'student', 'lab-booking') },
        { path: 'fee-payment', name: 'feePayment', component: () => import('@/views/office/FeePayment.vue'), meta: featureMeta('学杂费交纳', 'office', 'fee-payment') },
        { path: 'asset-management', name: 'assetManagement', component: () => import('@/views/office/AssetManagement.vue'), meta: featureMeta('固定资产管理', 'office', 'asset-management') },
        { path: 'work-plan', name: 'workPlan', component: () => import('@/views/office/WorkPlan.vue'), meta: featureMeta('工作计划', 'office', 'work-plan') },
        { path: 'document-oa', name: 'documentOA', component: () => import('@/views/office/DocumentOA.vue'), meta: featureMeta('公文流转 OA', 'office', 'document-oa') },
        { path: 'meeting-notice', name: 'meetingNotice', component: () => import('@/views/office/MeetingNotice.vue'), meta: featureMeta('会议与通知', 'office', 'meeting-notice') },
        { path: 'user-management', name: 'userManagement', component: () => import('@/views/base/UserManagement.vue'), meta: featureMeta('师生信息库', 'base', 'user-management') },
        { path: 'enrollment-stats', name: 'enrollmentStats', component: () => import('@/views/base/EnrollmentStats.vue'), meta: featureMeta('招生统计', 'base', 'enrollment-stats') },
        { path: 'student-analytics', name: 'studentAnalytics', component: () => import('@/views/base/StudentAnalytics.vue'), meta: featureMeta('学生多维统计', 'base', 'student-analytics') },
        { path: 'department-major', name: 'departmentMajor', component: () => import('@/views/base/DepartmentMajor.vue'), meta: featureMeta('院系专业管理', 'base', 'department-major') },
        { path: 'news-forum', name: 'newsForum', component: () => import('@/views/base/NewsForum.vue'), meta: featureMeta('新闻与论坛', 'base', 'news-forum') },
        { path: 'ai-report', name: 'aiReport', component: () => import('@/views/base/AiReport.vue'), meta: featureMeta('AI 智能报表', 'base', 'ai-report') },
        { path: 'profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { name: '个人中心' } },
      ],
    },
    { path: '/:pathMatch(.*)*', name: 'notFound', component: () => import('@/views/NotFoundView.vue') },
  ],
})

router.beforeEach((to) => {
  const token = getAccessToken()

  // ---- 需要认证但无 token → 跳登录 ----
  if (to.matched.some((record) => record.meta.requiresAuth) && !token && !UI_PREVIEW_MODE) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // ---- 有 token 但已过期 → 清理会话，跳登录 ----
  if (token && isTokenExpired(token)) {
    clearAccessToken()
    clearStoredCurrentUser()
    if (to.matched.some((record) => record.meta.requiresAuth) && !UI_PREVIEW_MODE) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    // 非认证页面也清理过期 token，但不强制跳转
    return true
  }

  // ---- 已登录用户访问登录页/首页 → 重定向到 home ----
  if ((to.path === '/login' || to.path === '/') && token && !isTokenExpired(token) && to.query.preview !== 'public') {
    return { path: '/home' }
  }

  // ---- 服务权限校验 ----
  const service = serviceMap[to.meta.serviceKey]
  const currentUser = getStoredCurrentUser()
  if (!UI_PREVIEW_MODE && service && currentUser && !canAccessService(service, currentUser.permissions || [], currentUser.user?.userType)) {
    return { path: '/home', query: { denied: service.key } }
  }
  return true
})

export default router
