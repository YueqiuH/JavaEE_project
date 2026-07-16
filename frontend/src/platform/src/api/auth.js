import request from '@/utils/request.js'

const AUTH_API_PREFIX = '/api/v1/auth'

export const loginUser = (data) => request.post(`${AUTH_API_PREFIX}/login`, data)

export const logoutUser = () => request.post(`${AUTH_API_PREFIX}/logout`)

export const getCurrentUser = () => request.get(`${AUTH_API_PREFIX}/me`)
