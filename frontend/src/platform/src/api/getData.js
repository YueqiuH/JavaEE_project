
import request from '@/utils/request.js'

// ========== 登录/退出 ==========
//登录
export const loginUser = (data) => {
    return request({
        url: `/login`,
        method: 'post',
        data
    })
}

//用户退出登录
export const logoutUser = () => {
    return request({
        url: `/logout`,
        method: 'get',
    })
}

// ========== 首页统计 ==========
export const getMainPageCountData = () => {
    return request({
        url: `/data/getMainPageCountData`,
        method: 'get'
    })
}

export const getGradeStudentCountData = () => {
    return request({
        url: `/data/getGradeStudentCountData`,
        method: 'get'
    })
}

// ========== 年级 ==========
export const getGradeAll = () => {
    return request({
        url: `/grade/all`,
        method: 'get',
    })
}

// ========== 学生管理 ==========
export const listStudentByConditionPage = (data) => {
    return request({
        url: `/student/getStudentListByCondition`,
        method: 'post',
        data
    })
}

export const addStudent = (data) => {
    return request({
        url: `/student/add`,
        method: 'post',
        data
    })
}

export const updateStudent = (data) => {
    return request({
        url: `/student/update`,
        method: 'post',
        data
    })
}

export const delStudent = (data) => {
    return request({
        url: `/student/delete`,
        method: 'post',
        data
    })
}

// ========== AI API ==========
export const languageModelAPI = {
    generateText(prompt) {
        return request.post('/ai/text/generate', { prompt })
    },
    generateTextWithTemplate(topic, style) {
        return request.post('/ai/text/generate-template', { topic, style })
    },
    generateTextWithChatClient(prompt) {
        return request.post('/ai/text/generate-chat', { prompt })
    }
}

export const chatModelAPI = {
    singleChat(sessionId, message) {
        return request.post('/ai/chat/stream', { sessionId, message })
    },
    chatWithSystem(sessionId, systemPrompt, message) {
        return request.post('/ai/chat/with-system', { sessionId, systemPrompt, message })
    },
    clearHistory(sessionId) {
        return request.delete(`/ai/chat/history/${sessionId}`)
    },
    getHistory(sessionId) {
        return request.get(`/ai/chat/history/${sessionId}`)
    }
}

export const imageAPI = {
    generateImage(prompt, size, number) {
        return request.post('/ai/image/generate', { prompt, size, number })
    },
    generateMultipleImages(prompt, size, number) {
        return request.post('/ai/image/generate-multiple', { prompt, size, number })
    }
}

// ========== 成员C：协同办公 API ==========
export const feeAPI = {
    importFees(data) { return request({ url: '/office/fee/import', method: 'post', data }) },
    getStudentFees(studentId) { return request({ url: `/office/fee/student/${studentId}`, method: 'get' }) },
    pay(feeId) { return request({ url: `/office/fee/pay/${feeId}`, method: 'post' }) },
    getRecentPayments(studentId) { return request({ url: `/office/fee/payment/recent/${studentId}`, method: 'get' }) }
}

export const assetAPI = {
    list(deptId) { return request({ url: '/office/asset/list', method: 'get', params: deptId ? { deptId } : {} }) },
    save(data) { return request({ url: '/office/asset/save', method: 'post', data }) },
    apply(data) { return request({ url: '/office/asset/apply', method: 'post', data }) },
    approve(assetId, approved) { return request({ url: `/office/asset/approve/${assetId}`, method: 'post', params: { approved } }) },
    remove(assetId) { return request({ url: `/office/asset/${assetId}`, method: 'delete' }) }
}

export const workPlanAPI = {
    getUserPlans(userId) { return request({ url: `/office/work-plan/user/${userId}`, method: 'get' }) },
    list() { return request({ url: '/office/work-plan/list', method: 'get' }) },
    save(data) { return request({ url: '/office/work-plan/save', method: 'post', data }) },
    comment(planId, comment) { return request({ url: `/office/work-plan/comment/${planId}`, method: 'post', data: comment, headers: { 'Content-Type': 'text/plain' } }) },
    remove(planId) { return request({ url: `/office/work-plan/${planId}`, method: 'delete' }) }
}

export const documentAPI = {
    start(data) { return request({ url: '/office/document/start', method: 'post', data }) },
    initiated(userId) { return request({ url: `/office/document/initiator/${userId}`, method: 'get' }) },
    pending(userId) { return request({ url: `/office/document/pending/${userId}`, method: 'get' }) },
    history(docId) { return request({ url: `/office/document/${docId}/history`, method: 'get' }) },
    approve(docId, data) { return request({ url: `/office/document/${docId}/approve`, method: 'post', data }) },
    remind(docId) { return request({ url: `/office/document/${docId}/remind`, method: 'post' }) }
}

export const meetingAPI = {
    publish(data) { return request({ url: '/office/meeting/publish', method: 'post', data }) },
    list() { return request({ url: '/office/meeting/list', method: 'get' }) },
    userMeetings(userId) { return request({ url: `/office/meeting/user/${userId}`, method: 'get' }) },
    reply(meetingId, userId, status) { return request({ url: `/office/meeting/${meetingId}/reply`, method: 'post', params: { userId, status } }) },
    summary(meetingId) { return request({ url: `/office/meeting/${meetingId}/summary`, method: 'get' }) },
    notifications(userId) { return request({ url: `/office/meeting/notification/${userId}`, method: 'get' }) },
    readNotification(notifyId) { return request({ url: `/office/meeting/notification/${notifyId}/read`, method: 'post' }) }
}

export const aiApprovalAPI = {
    summary(docId) { return request({ url: `/office/ai-approval/summary/${docId}`, method: 'post' }) },
    recommend(docId, data) { return request({ url: `/office/ai-approval/recommend/${docId}`, method: 'post', data }) }
}
