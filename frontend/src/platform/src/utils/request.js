import axios from 'axios';
import { ElMessage } from 'element-plus';
import { clearAccessToken, getAccessToken } from '@/utils/authToken.js'
import { clearStoredCurrentUser } from '@/utils/authSession.js'
import Router from '@/router'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888'
const REQUEST_TIMEOUT = Number(import.meta.env.VITE_API_TIMEOUT || 15000)

// create an axios instance
const service = axios.create({
    baseURL: API_BASE_URL,
    timeout: REQUEST_TIMEOUT
})

service.interceptors.request.use(
    config => {
        const token = getAccessToken()
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

service.interceptors.response.use(
    response => {
        const result = response.data
        if (result?.code === 0) {
            return result
        }

        const error = new Error(result?.message || '请求失败')
        error.code = result?.code
        error.requestId = result?.requestId
        ElMessage.error(formatMessage(error.message, error.requestId))
        return Promise.reject(error)
    },
    error => {
        const status = error.response?.status
        const result = error.response?.data

        if (status === 401) {
            clearAccessToken()
            clearStoredCurrentUser()
            if (Router.currentRoute.value.path !== '/login') {
                Router.push({ path: '/login', query: { redirect: Router.currentRoute.value.fullPath } })
            }
        }

        const message = result?.message || (status === 403 ? '没有该操作权限' : '网络请求失败')
        ElMessage.error(formatMessage(message, result?.requestId))
        return Promise.reject(error)
    }
)

const formatMessage = (message, requestId) => {
    return requestId ? `${message}（请求编号：${requestId}）` : message
}

export default service
