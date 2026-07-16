import request from '@/utils/request.js'

export const BASE_API_PREFIX = '/api/v1/base'

export const getMainPageCountData = () => request.get(`${BASE_API_PREFIX}/dashboard/summary`)

export const getGradeStudentCountData = () => request.get(`${BASE_API_PREFIX}/dashboard/students-by-grade`)

export const getGradeAll = () => request.get(`${BASE_API_PREFIX}/grades`)

export const listStudentByConditionPage = (params) => request.get(`${BASE_API_PREFIX}/students`, { params })

export const addStudent = (data) => request.post(`${BASE_API_PREFIX}/students`, data)

export const updateStudent = (id, data) => request.put(`${BASE_API_PREFIX}/students/${id}`, data)

export const delStudent = (id) => request.delete(`${BASE_API_PREFIX}/students/${id}`)
