
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
