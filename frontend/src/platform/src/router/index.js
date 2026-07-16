import { createRouter, createWebHistory } from 'vue-router'
import { getAccessToken } from '@/utils/authToken.js'

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
        { path: 'course-selection', name: 'courseSelection', component: () => import('@/views/teaching/CourseSelection.vue'), meta: featureMeta('选课与容量', 'teaching', 'course-selection') },
        { path: 'score-management', name: 'scoreManagement', component: () => import('@/views/teaching/ScoreManagement.vue'), meta: featureMeta('成绩评定与预警', 'teaching', 'score-management') },
        { path: 'exam-arrangement', name: 'examArrangement', component: () => import('@/views/teaching/ExamArrangement.vue'), meta: featureMeta('考试与补考', 'teaching', 'exam-arrangement') },
        { path: 'graduation-design', name: 'graduationDesign', component: () => import('@/views/teaching/GraduationDesign.vue'), meta: featureMeta('毕业设计管理', 'teaching', 'graduation-design') },
        { path: 'ai-learning', name: 'aiLearning', component: () => import('@/views/teaching/AiLearning.vue'), meta: featureMeta('AI 智能学习助理', 'teaching', 'ai-learning') },

        { path: 'student-status', name: 'studentStatus', component: () => import('@/views/student/StudentStatus.vue'), meta: featureMeta('学籍变动', 'student', 'student-status') },
        { path: 'scholarship', name: 'scholarship', component: () => import('@/views/student/Scholarship.vue'), meta: featureMeta('奖助贷评审', 'student', 'scholarship') },
        { path: 'teaching-evaluation', name: 'teachingEvaluation', component: () => import('@/views/student/TeachingEvaluation.vue'), meta: featureMeta('评教反馈', 'student', 'teaching-evaluation') },
        { path: 'competition', name: 'competition', component: () => import('@/views/student/Competition.vue'), meta: featureMeta('学科竞赛', 'student', 'competition') },
        { path: 'lab-booking', name: 'labBooking', component: () => import('@/views/student/LabBooking.vue'), meta: featureMeta('实验室预约', 'student', 'lab-booking') },
        { path: 'ai-psychology', name: 'aiPsychology', component: () => import('@/views/student/AiPsychology.vue'), meta: featureMeta('AI 心理预警', 'student', 'ai-psychology') },

        { path: 'fee-payment', name: 'feePayment', component: () => import('@/views/office/FeePayment.vue'), meta: featureMeta('学杂费交纳', 'office', 'fee-payment') },
        { path: 'asset-management', name: 'assetManagement', component: () => import('@/views/office/AssetManagement.vue'), meta: featureMeta('固定资产管理', 'office', 'asset-management') },
        { path: 'work-plan', name: 'workPlan', component: () => import('@/views/office/WorkPlan.vue'), meta: featureMeta('工作计划', 'office', 'work-plan') },
        { path: 'document-oa', name: 'documentOA', component: () => import('@/views/office/DocumentOA.vue'), meta: featureMeta('公文流转 OA', 'office', 'document-oa') },
        { path: 'meeting-notice', name: 'meetingNotice', component: () => import('@/views/office/MeetingNotice.vue'), meta: featureMeta('会议与通知', 'office', 'meeting-notice') },
        { path: 'ai-approval', name: 'aiApproval', component: () => import('@/views/office/AiApproval.vue'), meta: featureMeta('AI 审批助手', 'office', 'ai-approval') },

        { path: 'user-management', name: 'userManagement', component: () => import('@/views/base/UserManagement.vue'), meta: featureMeta('师生信息库', 'base', 'user-management') },
        { path: 'enrollment-stats', name: 'enrollmentStats', component: () => import('@/views/base/EnrollmentStats.vue'), meta: featureMeta('招生统计', 'base', 'enrollment-stats') },
        { path: 'student-analytics', name: 'studentAnalytics', component: () => import('@/views/base/StudentAnalytics.vue'), meta: featureMeta('学生多维统计', 'base', 'student-analytics') },
        { path: 'department-major', name: 'departmentMajor', component: () => import('@/views/base/DepartmentMajor.vue'), meta: featureMeta('院系专业管理', 'base', 'department-major') },
        { path: 'news-forum', name: 'newsForum', component: () => import('@/views/base/NewsForum.vue'), meta: featureMeta('新闻与论坛', 'base', 'news-forum') },
        { path: 'ai-report', name: 'aiReport', component: () => import('@/views/base/AiReport.vue'), meta: featureMeta('AI 智能报表', 'base', 'ai-report') },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  if (to.matched.some((record) => record.meta.requiresAuth) && !getAccessToken() && !UI_PREVIEW_MODE) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if ((to.path === '/login' || to.path === '/') && getAccessToken() && to.query.preview !== 'public') {
    return { path: '/home' }
  }
  return true
})

export default router
