import axios from 'axios';
import { ElMessage, ElNotification } from 'element-plus';
import { clearAccessToken, getAccessToken } from '@/utils/authToken.js'
import { clearStoredCurrentUser } from '@/utils/authSession.js'
import Router from '@/router'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888'
const REQUEST_TIMEOUT = Number(import.meta.env.VITE_API_TIMEOUT || 15000)

const service = axios.create({
    baseURL: API_BASE_URL,
    timeout: REQUEST_TIMEOUT
})

// ---- 请求拦截器 ----
service.interceptors.request.use(
    config => {
        const token = getAccessToken()
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// ---- 响应拦截器 ----
service.interceptors.response.use(
    response => {
        const result = response.data
        if (result?.code === 0) {
            return result
        }
        // 业务错误（code !== 0）—— 用 message 通知，保留具体错误信息
        const errMsg = formatError(result)
        ElMessage.error({ message: errMsg, duration: 4000, showClose: true })
        return Promise.reject(new Error(result?.message || '请求失败'))
    },
    error => {
        const status = error.response?.status
        const result = error.response?.data
        const requestId = result?.requestId || ''

        // ---- 401: 未登录 ----
        if (status === 401) {
            clearAccessToken()
            clearStoredCurrentUser()
            ElMessage.warning('登录已过期，请重新登录')
            if (Router.currentRoute.value.path !== '/login') {
                Router.push({ path: '/login', query: { redirect: Router.currentRoute.value.fullPath } })
            }
            return Promise.reject(error)
        }

        // ---- 403: 无权限 ----
        if (status === 403) {
            ElMessage.error({
                message: '没有该操作权限，请联系管理员',
                duration: 5000, showClose: true
            })
            return Promise.reject(error)
        }

        // ---- 409: 业务冲突（排课冲突等）—— 弹框展示中文原因 ----
        if (status === 409 && result?.message) {
            ElMessage.error({
                message: result.message,
                duration: 6000, showClose: true
            })
            return Promise.reject(error)
        }

        // ---- 5xx: 服务器内部错误 —— 通知框 + 请求编号 ----
        if (status >= 500) {
            ElNotification({
                title: '服务器错误',
                message: result?.message || '服务暂时不可用，请稍后重试',
                type: 'error',
                duration: 8000
            })
            if (requestId) {
                console.error(`[请求编号] ${requestId} —— 如需排查请联系管理员并提供此编号`)
            }
            return Promise.reject(error)
        }

        // ---- 4xx: 客户端错误 ----
        if (status >= 400) {
            const msg = result?.message || '请求参数有误'
            ElMessage.error({ message: msg, duration: 4000, showClose: true })
            return Promise.reject(error)
        }

        // ---- 网络超时 / 断网 ----
        if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
            ElMessage.error('请求超时，请检查网络连接后重试')
            return Promise.reject(error)
        }

        // ---- 兜底 ----
        ElMessage.error({
            message: '网络异常，请检查连接后重试',
            duration: 4000, showClose: true
        })
        return Promise.reject(error)
    }
)

/**
 * 格式化业务错误消息，附带 requestId 便于排查。
 */
function formatError(result) {
    const base = result?.message || '操作失败'
    if (result?.requestId) {
        return `${base}（编号：${result.requestId}）`
    }
    return base
}

export default service
