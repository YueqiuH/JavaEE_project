import request from '@/utils/request.js'
import { streamChatAPI } from '@/utils/streamChatAPI.js'

export const TEACHING_API_PREFIX = '/api/v1/teaching'

export const aiChatApi = {
    sendMessage(message, conversationId, onEvent, onError, onComplete) {
        return streamChatAPI.streamChat(
            `${TEACHING_API_PREFIX}/ai/chat`,
            { message, conversationId, semester: '2025-2026-2', stream: true },
            onEvent, onError, onComplete
        )
    },
    getHistory() {
        return request.get(`${TEACHING_API_PREFIX}/ai/history`)
    },
    getConversation(convId) {
        return request.get(`${TEACHING_API_PREFIX}/ai/history/${convId}`)
    },
    deleteConversation(convId) {
        return request.delete(`${TEACHING_API_PREFIX}/ai/history/${convId}`)
    }
}

// ==================== 课程管理 ====================
export const courseApi = {
    /** 搜索课程 */
    search(data) { return request.post(`${TEACHING_API_PREFIX}/course/search`, data) },

    /** 课程列表（含排课详情） */
    list(semester) { return request.get(`${TEACHING_API_PREFIX}/course/list`, { params: { semester } }) },

    /** 新增课程 */
    add(data) { return request.post(`${TEACHING_API_PREFIX}/course/add`, data) },

    /** 教师列表 */
    listTeachers() { return request.get(`${TEACHING_API_PREFIX}/course/teachers`) }
}

// ==================== 排课管理 ====================
export const scheduleApi = {
    /** 新增排课 */
    add(data) { return request.post(`${TEACHING_API_PREFIX}/schedule/add`, data) },

    /** 更新排课 */
    update(data) { return request.post(`${TEACHING_API_PREFIX}/schedule/update`, data) },

    /** 删除排课 */
    delete(scheduleId) { return request.post(`${TEACHING_API_PREFIX}/schedule/delete/${scheduleId}`) },

    /** 一键停开课程 */
    suspendCourse(courseId, semester) {
        return request.post(`${TEACHING_API_PREFIX}/schedule/suspend-course`, null, { params: { courseId, semester } })
    },

    /** 教师课表 */
    getTeacherSchedule(teacherId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/schedule/teacher/${teacherId}`, { params: { semester } })
    },

    /** 学生课表 */
    getStudentSchedule(studentId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/schedule/student/${studentId}`, { params: { semester } })
    },

    /** F4: 教师工作量 */
    getTeacherWorkload(teacherId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/schedule/workload/${teacherId}`, { params: { semester } })
    },

    /** 教室列表 */
    getClassrooms() {
        return request.get(`${TEACHING_API_PREFIX}/schedule/classrooms`)
    }
}

// ==================== 选课管理 ====================
export const selectCourse = (data) => request.post(`${TEACHING_API_PREFIX}/selection/select`, data)
export const dropCourse = (data) => request.post(`${TEACHING_API_PREFIX}/selection/drop`, data)
export const getMySelection = (studentId, semester) => request.get(`${TEACHING_API_PREFIX}/selection/my/${studentId}`, { params: { semester } })
export const getCourseList = (semester) => request.get(`${TEACHING_API_PREFIX}/course/list`, { params: { semester } })

// ==================== 成绩管理 ====================
export const scoreApi = {
    /** 教师录入/修改成绩 */
    input(data) { return request.post(`${TEACHING_API_PREFIX}/score/input`, data) },

    /** 教师暂存草稿 */
    saveDraft(data) { return request.post(`${TEACHING_API_PREFIX}/score/save-draft`, data) },

    /** 教师一键发布教学班成绩 */
    publish(scheduleId, teacherId, semester) {
        return request.post(`${TEACHING_API_PREFIX}/score/publish`, null, { params: { scheduleId, teacherId, semester } })
    },

    /** 教师查看自己的教学班列表 */
    getTeacherClasses(teacherId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/score/teacher-classes`, { params: { teacherId, semester } })
    },

    /** 教师查看某课程成绩列表 */
    getCourseScores(courseId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/score/course/${courseId}`, { params: { semester } })
    },

    /** 学生成绩单 */
    getStudentReport(studentId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/score/student/${studentId}`, { params: { semester } })
    },

    /** 辅导员查看预警列表 */
    getCounselorWarnings(counselorId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/score/counselor/warnings`, { params: { counselorId, semester } })
    },

    /** 辅导员查看学生成绩画像 */
    getStudentProfile(studentId) {
        return request.get(`${TEACHING_API_PREFIX}/score/counselor/student-profile/${studentId}`)
    },

    /** 管理员例外修改成绩 */
    adminModify(data) { return request.post(`${TEACHING_API_PREFIX}/score/admin/modify`, data) },

    /** 查看修改日志 */
    getModificationLogs(scoreId) {
        return request.get(`${TEACHING_API_PREFIX}/score/admin/logs/${scoreId}`)
    },

    /** 获取时间窗口状态 */
    getTimeWindow() { return request.get(`${TEACHING_API_PREFIX}/score/time-window`) },

    /** 获取当前教学周 */
    getCurrentWeek() { return request.get(`${TEACHING_API_PREFIX}/score/current-week`) },
}

// ==================== 考试与补考管理 ====================
export const examApi = {
    /** 第16周：统一编排考试 */
    scheduleExams(semester) {
        return request.post(`${TEACHING_API_PREFIX}/exam/schedule`, null, { params: { semester } })
    },

    /** 一键为所有考试分配考场 */
    assignAllRooms(semester) {
        return request.post(`${TEACHING_API_PREFIX}/exam/assign-all-rooms`, null, { params: { semester } })
    },

    /** 一键为所有考试指派监考 */
    assignAllInvigilators(semester) {
        return request.post(`${TEACHING_API_PREFIX}/exam/assign-all-invigilators`, null, { params: { semester } })
    },

    /** 为考试分配考场+座位 */
    assignRooms(examId) { return request.post(`${TEACHING_API_PREFIX}/exam/assign-rooms/${examId}`) },

    /** 指派监考教师 */
    assignInvigilators(data) { return request.post(`${TEACHING_API_PREFIX}/exam/assign-invigilators`, data) },

    /** 考试列表 */
    list(semester, examType) {
        return request.get(`${TEACHING_API_PREFIX}/exam/list`, { params: { semester, examType } })
    },

    /** 考试学生名单 */
    getStudents(examId) { return request.get(`${TEACHING_API_PREFIX}/exam/students/${examId}`) },

    /** 教师监考安排 */
    getInvigilations(teacherId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/exam/invigilations/${teacherId}`, { params: { semester } })
    },

    /** 补考/缓考报名 */
    applyResit(data) { return request.post(`${TEACHING_API_PREFIX}/exam/resit/apply`, data) },

    /** 自动撤销补考 */
    autoRevoke(studentId, courseId, semester) {
        return request.post(`${TEACHING_API_PREFIX}/exam/resit/auto-revoke`, null,
            { params: { studentId, courseId, semester } })
    },

    /** 冻结补考名单 */
    freezeResit(semester) {
        return request.post(`${TEACHING_API_PREFIX}/exam/resit/freeze`, null, { params: { semester } })
    },

    /** 查询补考状态 */
    getResitStatus(studentId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/exam/resit/status/${studentId}`, { params: { semester } })
    },

    /** 学生考试查询 */
    getStudentExams(studentId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/exam/student/${studentId}`, { params: { semester } })
    },

    /** 创建考试 */
    create(data) { return request.post(`${TEACHING_API_PREFIX}/exam/create`, data) },
}

// ==================== 自动排课引擎 ====================
export const autoScheduleApi = {
    /** 启动一键自动排课 */
    start(config) { return request.post(`${TEACHING_API_PREFIX}/auto-schedule/start`, config) },

    /** 查询排课进度 */
    getProgress(taskId) { return request.get(`${TEACHING_API_PREFIX}/auto-schedule/progress/${taskId}`) },

    /** 终止排课 */
    cancel(taskId) { return request.post(`${TEACHING_API_PREFIX}/auto-schedule/cancel/${taskId}`) },

    /** 查看诊断报告 */
    getDiagnostic(taskId) { return request.get(`${TEACHING_API_PREFIX}/auto-schedule/diagnostic/${taskId}`) },

    /** 获取槽位热力图 */
    getHeatmap(scheduleId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/auto-schedule/heatmap/${scheduleId}`, { params: { semester } })
    },

    /** AI推荐微调方案 */
    getRecommendations(scheduleId, semester) {
        return request.get(`${TEACHING_API_PREFIX}/auto-schedule/recommend/${scheduleId}`, { params: { semester } })
    },

    /** 已锁定排课列表 */
    getLockedSchedules(semester) {
        return request.get(`${TEACHING_API_PREFIX}/auto-schedule/locked`, { params: { semester } })
    },

    /** 锁定/解锁 */
    toggleLock(scheduleId, locked) {
        return request.post(`${TEACHING_API_PREFIX}/auto-schedule/toggle-lock/${scheduleId}`, null, { params: { locked } })
    },

    /** 质量评分 */
    getQualityScore(semester) {
        return request.get(`${TEACHING_API_PREFIX}/auto-schedule/quality-score`, { params: { semester } })
    },
}
