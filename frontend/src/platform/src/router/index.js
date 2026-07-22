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
    { path: '/', name: 'landing', component: LandingView, meta: { name: 'Welcome' } },
    { path: '/login', name: 'login', component: LoginView, meta: { name: 'Login' } },
    {
      path: '/home',
      component: HomeView,
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'portalHome', component: PortalHome, meta: { name: 'Home' } },
        { path: '/services', name: 'serviceCenter', component: ServiceCenter, meta: { name: 'Service Center' } },
        { path: '/workbench', name: 'workbench', component: WorkbenchView, meta: { name: 'Workbench' } },

        { path: 'course-schedule', name: 'courseSchedule', component: () => import('@/views/teaching/CourseSchedule.vue'), meta: featureMeta('Course Schedule', 'teaching', 'course-schedule') },
        { path: 'course-selection', name: 'courseSelection', component: () => import('@/views/teaching/CourseSelection.vue'), meta: featureMeta('Course Selection', 'teaching', 'course-selection') },
        { path: 'score-management', name: 'scoreManagement', component: () => import('@/views/teaching/ScoreManagement.vue'), meta: featureMeta('Score Management', 'teaching', 'score-management') },
        { path: 'exam-arrangement', name: 'examArrangement', component: () => import('@/views/teaching/ExamArrangement.vue'), meta: featureMeta('Exam Arrangement', 'teaching', 'exam-arrangement') },
        { path: 'ai-learning', name: 'aiLearning', component: () => import('@/views/teaching/AiLearning.vue'), meta: featureMeta('AI Learning', 'teaching', 'ai-learning') },

        { path: 'student-status', name: 'studentStatus', component: () => import('@/views/student/StudentStatus.vue'), meta: featureMeta('Student Status', 'student', 'student-status') },
        { path: 'scholarship', name: 'scholarship', component: () => import('@/views/student/Scholarship.vue'), meta: featureMeta('Scholarship', 'student', 'scholarship') },
        { path: 'teaching-evaluation', name: 'teachingEvaluation', component: () => import('@/views/student/TeachingEvaluation.vue'), meta: featureMeta('Evaluation', 'student', 'teaching-evaluation') },
        { path: 'competition', name: 'competition', component: () => import('@/views/student/Competition.vue'), meta: featureMeta('Competition', 'student', 'competition') },
        { path: 'lab-booking', name: 'labBooking', component: () => import('@/views/student/LabBooking.vue'), meta: featureMeta('Lab Booking', 'student', 'lab-booking') },
        { path: 'fee-payment', name: 'feePayment', component: () => import('@/views/office/FeePayment.vue'), meta: featureMeta('Fee Payment', 'office', 'fee-payment') },
        { path: 'asset-management', name: 'assetManagement', component: () => import('@/views/office/AssetManagement.vue'), meta: featureMeta('Asset Mgmt', 'office', 'asset-management') },
        { path: 'work-plan', name: 'workPlan', component: () => import('@/views/office/WorkPlan.vue'), meta: featureMeta('Work Plan', 'office', 'work-plan') },
        { path: 'document-oa', name: 'documentOA', component: () => import('@/views/office/DocumentOA.vue'), meta: featureMeta('Document OA', 'office', 'document-oa') },
        { path: 'meeting-notice', name: 'meetingNotice', component: () => import('@/views/office/MeetingNotice.vue'), meta: featureMeta('Meeting Notice', 'office', 'meeting-notice') },
        { path: 'user-management', name: 'userManagement', component: () => import('@/views/base/UserManagement.vue'), meta: featureMeta('User Management', 'base', 'user-management') },
        { path: 'enrollment-stats', name: 'enrollmentStats', component: () => import('@/views/base/EnrollmentStats.vue'), meta: featureMeta('Enrollment Stats', 'base', 'enrollment-stats') },
        { path: 'student-analytics', name: 'studentAnalytics', component: () => import('@/views/base/StudentAnalytics.vue'), meta: featureMeta('Student Analytics', 'base', 'student-analytics') },
        { path: 'department-major', name: 'departmentMajor', component: () => import('@/views/base/DepartmentMajor.vue'), meta: featureMeta('Dept & Major', 'base', 'department-major') },
        { path: 'news-forum', name: 'newsForum', component: () => import('@/views/base/NewsForum.vue'), meta: featureMeta('News Forum', 'base', 'news-forum') },
        { path: 'ai-report', name: 'aiReport', component: () => import('@/views/base/AiReport.vue'), meta: featureMeta('AI Report', 'base', 'ai-report') },
        { path: 'profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { name: 'Profile' } },
      ],
    },
    { path: '/:pathMatch(.*)*', name: 'notFound', component: () => import('@/views/NotFoundView.vue') },
  ],
})

router.beforeEach((to) => {
  const token = getAccessToken()
  if (to.matched.some((record) => record.meta.requiresAuth) && !token && !UI_PREVIEW_MODE) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (token && isTokenExpired(token)) {
    clearAccessToken()
    clearStoredCurrentUser()
    if (to.matched.some((record) => record.meta.requiresAuth) && !UI_PREVIEW_MODE) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    return true
  }
  if ((to.path === '/login' || to.path === '/') && token && !isTokenExpired(token) && to.query.preview !== 'public') {
    return { path: '/home' }
  }
  const service = serviceMap[to.meta.serviceKey]
  const currentUser = getStoredCurrentUser()
  if (!UI_PREVIEW_MODE && service && currentUser && !canAccessService(service, currentUser.permissions || [], currentUser.user?.userType)) {
    return { path: '/home', query: { denied: service.key } }
  }
  return true
})

export default router
