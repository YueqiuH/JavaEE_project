import request from '@/utils/request.js'

export const STUDENT_API_PREFIX = '/api/v1/student'

export const getStudentProfile = () => request.get(`${STUDENT_API_PREFIX}/student-profile`)

export const updateStudentProfile = (data) => request.put(`${STUDENT_API_PREFIX}/student-profile`, data)

export const listStatusChangeMajors = () => request.get(`${STUDENT_API_PREFIX}/status-change-majors`)

export const listMyStatusChanges = (params) => request.get(`${STUDENT_API_PREFIX}/status-changes/mine`, { params })

export const listStatusChangeReviews = (params) => request.get(`${STUDENT_API_PREFIX}/status-change-reviews`, { params })

export const getStatusChange = (id) => request.get(`${STUDENT_API_PREFIX}/status-changes/${id}`)

export const createStatusChange = (data) => request.post(`${STUDENT_API_PREFIX}/status-changes`, data)

export const updateStatusChange = (id, data) => request.put(`${STUDENT_API_PREFIX}/status-changes/${id}`, data)

export const submitStatusChange = (id) => request.post(`${STUDENT_API_PREFIX}/status-changes/${id}/submissions`)

export const withdrawStatusChange = (id) => request.delete(`${STUDENT_API_PREFIX}/status-changes/${id}`)

export const reviewStatusChange = (id, data) => request.post(`${STUDENT_API_PREFIX}/status-changes/${id}/reviews`, data)
