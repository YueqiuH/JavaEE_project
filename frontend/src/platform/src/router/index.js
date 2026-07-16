// ===== 公共页面 =====
import LandingView from "@/views/LandingView.vue";
import LoginView from "@/views/LoginView.vue";
import HomeView from "@/views/HomeView.vue";

// ===== 成员A：教务核心 =====
import CourseSchedule from "@/views/teaching/CourseSchedule.vue";
import CourseSelection from "@/views/teaching/CourseSelection.vue";
import ScoreManagement from "@/views/teaching/ScoreManagement.vue";
import ExamArrangement from "@/views/teaching/ExamArrangement.vue";
import GraduationDesign from "@/views/teaching/GraduationDesign.vue";
import AiLearning from "@/views/teaching/AiLearning.vue";

// ===== 成员B：学工事务 =====
import StudentStatus from "@/views/student/StudentStatus.vue";
import Scholarship from "@/views/student/Scholarship.vue";
import TeachingEvaluation from "@/views/student/TeachingEvaluation.vue";
import Competition from "@/views/student/Competition.vue";
import LabBooking from "@/views/student/LabBooking.vue";
import AiPsychology from "@/views/student/AiPsychology.vue";

// ===== 成员C：协同办公 =====
import FeePayment from "@/views/office/FeePayment.vue";
import AssetManagement from "@/views/office/AssetManagement.vue";
import WorkPlan from "@/views/office/WorkPlan.vue";
import DocumentOA from "@/views/office/DocumentOA.vue";
import MeetingNotice from "@/views/office/MeetingNotice.vue";
import AiApproval from "@/views/office/AiApproval.vue";

// ===== 成员D：基础数据 =====
import UserManagement from "@/views/base/UserManagement.vue";
import EnrollmentStats from "@/views/base/EnrollmentStats.vue";
import StudentAnalytics from "@/views/base/StudentAnalytics.vue";
import DepartmentMajor from "@/views/base/DepartmentMajor.vue";
import NewsForum from "@/views/base/NewsForum.vue";
import AiReport from "@/views/base/AiReport.vue";

import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        // 公共
        { path: '/', name: 'landing', component: LandingView, meta: { name: "欢迎页" } },
        { path: '/login', name: 'login', component: LoginView, meta: { name: "登录" } },
        {
            path: '/home', name: 'home', component: HomeView, meta: { name: "主页" },
            children: [
                // 成员A：教务核心
                { path: '/home/course-schedule', name: 'courseSchedule', component: CourseSchedule, meta: { name: "排课与课表", member: "A" } },
                { path: '/home/course-selection', name: 'courseSelection', component: CourseSelection, meta: { name: "选课与容量", member: "A" } },
                { path: '/home/score-management', name: 'scoreManagement', component: ScoreManagement, meta: { name: "成绩评定与预警", member: "A" } },
                { path: '/home/exam-arrangement', name: 'examArrangement', component: ExamArrangement, meta: { name: "考试与补考", member: "A" } },
                { path: '/home/graduation-design', name: 'graduationDesign', component: GraduationDesign, meta: { name: "毕业设计管理", member: "A" } },
                { path: '/home/ai-learning', name: 'aiLearning', component: AiLearning, meta: { name: "AI智能学习助理", member: "A" } },
                // 成员B：学工事务
                { path: '/home/student-status', name: 'studentStatus', component: StudentStatus, meta: { name: "学籍变动", member: "B" } },
                { path: '/home/scholarship', name: 'scholarship', component: Scholarship, meta: { name: "奖助贷评审", member: "B" } },
                { path: '/home/teaching-evaluation', name: 'teachingEvaluation', component: TeachingEvaluation, meta: { name: "评教反馈", member: "B" } },
                { path: '/home/competition', name: 'competition', component: Competition, meta: { name: "学科竞赛", member: "B" } },
                { path: '/home/lab-booking', name: 'labBooking', component: LabBooking, meta: { name: "实验室预约", member: "B" } },
                { path: '/home/ai-psychology', name: 'aiPsychology', component: AiPsychology, meta: { name: "AI心理预警", member: "B" } },
                // 成员C：协同办公
                { path: '/home/fee-payment', name: 'feePayment', component: FeePayment, meta: { name: "学杂费交纳", member: "C" } },
                { path: '/home/asset-management', name: 'assetManagement', component: AssetManagement, meta: { name: "固定资产管理", member: "C" } },
                { path: '/home/work-plan', name: 'workPlan', component: WorkPlan, meta: { name: "工作计划", member: "C" } },
                { path: '/home/document-oa', name: 'documentOA', component: DocumentOA, meta: { name: "公文流转OA", member: "C" } },
                { path: '/home/meeting-notice', name: 'meetingNotice', component: MeetingNotice, meta: { name: "会议与通知", member: "C" } },
                { path: '/home/ai-approval', name: 'aiApproval', component: AiApproval, meta: { name: "AI审批助手", member: "C" } },
                // 成员D：基础数据
                { path: '/home/user-management', name: 'userManagement', component: UserManagement, meta: { name: "师生信息库", member: "D" } },
                { path: '/home/enrollment-stats', name: 'enrollmentStats', component: EnrollmentStats, meta: { name: "招生统计", member: "D" } },
                { path: '/home/student-analytics', name: 'studentAnalytics', component: StudentAnalytics, meta: { name: "学生多维统计", member: "D" } },
                { path: '/home/department-major', name: 'departmentMajor', component: DepartmentMajor, meta: { name: "院系专业管理", member: "D" } },
                { path: '/home/news-forum', name: 'newsForum', component: NewsForum, meta: { name: "新闻与论坛", member: "D" } },
                { path: '/home/ai-report', name: 'aiReport', component: AiReport, meta: { name: "AI智能报表", member: "D" } },
            ]
        }
    ]
})

export default router
