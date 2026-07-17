import request from '@/utils/request.js'

export const TEACHING_API_PREFIX = '/api/v1/teaching'

// ==================== AI 学习助理 ====================
export const aiStudyApi = {
    uploadMaterial(data) {
        return request.post(`${TEACHING_API_PREFIX}/ai/materials`, data)
    },
    getMaterials(studentId) {
        return request.get(`${TEACHING_API_PREFIX}/ai/materials`, { params: { studentId } })
    },
    generateSummary(materialId) {
        return request.post(`${TEACHING_API_PREFIX}/ai/materials/${materialId}/summary`)
    },
    generateQuestions(materialId) {
        return request.post(`${TEACHING_API_PREFIX}/ai/materials/${materialId}/questions`)
    },
    recommendPath(studentId) {
        return request.post(`${TEACHING_API_PREFIX}/ai/learning-paths`, { studentId })
    },
    getRecords(studentId) {
        return request.get(`${TEACHING_API_PREFIX}/ai/records`, { params: { studentId } })
    }
}

// ==================== 课程管理 ====================
export const courseApi = {
    /** 搜索课程 */
    search(data) { return request.post('/course/search', data) },

    /** 课程列表（含排课详情） */
    list(semester) { return request.get('/course/list', { params: { semester } }) },

    /** 新增课程 */
    add(data) { return request.post('/course/add', data) }
}

// ==================== 排课管理 ====================
export const scheduleApi = {
    /** 新增排课 */
    add(data) { return request.post('/teaching/schedule/add', data) },

    /** 更新排课 */
    update(data) { return request.post('/teaching/schedule/update', data) },

    /** 删除排课 */
    delete(scheduleId) { return request.post(`/teaching/schedule/delete/${scheduleId}`) },

    /** 一键停开课程 */
    suspendCourse(courseId, semester) {
        return request.post('/teaching/schedule/suspend-course', null, { params: { courseId, semester } })
    },

    /** 教师课表 */
    getTeacherSchedule(teacherId, semester) {
        return request.get(`/teaching/schedule/teacher/${teacherId}`, { params: { semester } })
    },

    /** 学生课表 */
    getStudentSchedule(studentId, semester) {
        return request.get(`/teaching/schedule/student/${studentId}`, { params: { semester } })
    },

    /** F4: 教师工作量 */
    getTeacherWorkload(teacherId, semester) {
        return request.get(`/teaching/schedule/workload/${teacherId}`, { params: { semester } })
    }
}

// ==================== 选课管理 ====================
export const selectCourse = (data) => request.post('/teaching/selection/select', data)
export const dropCourse = (data) => request.post('/teaching/selection/drop', data)
export const getMySelection = (studentId, semester) => request.get(`/teaching/selection/my/${studentId}`, { params: { semester } })
export const getCourseList = (semester) => request.get('/course/list', { params: { semester } })

// ==================== 成绩管理 ====================
export const scoreApi = {
    /** 教师录入/修改成绩 */
    input(data) { return request.post('/teaching/score/input', data) },

    /** 教师暂存草稿 */
    saveDraft(data) { return request.post('/teaching/score/save-draft', data) },

    /** 教师一键发布教学班成绩 */
    publish(scheduleId, teacherId, semester) {
        return request.post('/teaching/score/publish', null, { params: { scheduleId, teacherId, semester } })
    },

    /** 教师查看自己的教学班列表 */
    getTeacherClasses(teacherId, semester) {
        return request.get('/teaching/score/teacher-classes', { params: { teacherId, semester } })
    },

    /** 教师查看某课程成绩列表 */
    getCourseScores(courseId, semester) {
        return request.get(`/teaching/score/course/${courseId}`, { params: { semester } })
    },

    /** 学生成绩单 */
    getStudentReport(studentId, semester) {
        return request.get(`/teaching/score/student/${studentId}`, { params: { semester } })
    },

    /** 辅导员查看预警列表 */
    getCounselorWarnings(counselorId, semester) {
        return request.get('/teaching/score/counselor/warnings', { params: { counselorId, semester } })
    },

    /** 辅导员查看学生成绩画像 */
    getStudentProfile(studentId) {
        return request.get(`/teaching/score/counselor/student-profile/${studentId}`)
    },

    /** 管理员例外修改成绩 */
    adminModify(data) { return request.post('/teaching/score/admin/modify', data) },

    /** 查看修改日志 */
    getModificationLogs(scoreId) {
        return request.get(`/teaching/score/admin/logs/${scoreId}`)
    },

    /** 获取时间窗口状态 */
    getTimeWindow() { return request.get('/teaching/score/time-window') },

    /** 获取当前教学周 */
    getCurrentWeek() { return request.get('/teaching/score/current-week') },
}
