import request from '@/utils/request.js'

export const BASE_API_PREFIX = '/api/v1/base'

export const getMainPageCountData = () => request.get(`${BASE_API_PREFIX}/dashboard/summary`)

export const getGradeStudentCountData = () => request.get(`${BASE_API_PREFIX}/dashboard/students-by-grade`)

export const getGradeAll = () => request.get(`${BASE_API_PREFIX}/grades`)

export const listStudentByConditionPage = (params) => request.get(`${BASE_API_PREFIX}/students`, { params })

export const addStudent = (data) => request.post(`${BASE_API_PREFIX}/students`, data)

export const updateStudent = (id, data) => request.put(`${BASE_API_PREFIX}/students/${id}`, data)

export const delStudent = (id) => request.delete(`${BASE_API_PREFIX}/students/${id}`)

// ===== D1 教职工信息库 =====
export const listStaffPage = (params) => request.get(`${BASE_API_PREFIX}/staffs`, { params })

export const addStaff = (data) => request.post(`${BASE_API_PREFIX}/staffs`, data)

export const updateStaff = (id, data) => request.put(`${BASE_API_PREFIX}/staffs/${id}`, data)

export const disableStaff = (id) => request.delete(`${BASE_API_PREFIX}/staffs/${id}`)

// ===== D4 院系与专业资源管理 =====
export const listDepartmentPage = (params) => request.get(`${BASE_API_PREFIX}/departments`, { params })

export const addDepartment = (data) => request.post(`${BASE_API_PREFIX}/departments`, data)

export const updateDepartment = (id, data) => request.put(`${BASE_API_PREFIX}/departments/${id}`, data)

export const delDepartment = (id) => request.delete(`${BASE_API_PREFIX}/departments/${id}`)

export const listMajorPage = (params) => request.get(`${BASE_API_PREFIX}/majors`, { params })

export const addMajor = (data) => request.post(`${BASE_API_PREFIX}/majors`, data)

export const updateMajor = (id, data) => request.put(`${BASE_API_PREFIX}/majors/${id}`, data)

export const delMajor = (id) => request.delete(`${BASE_API_PREFIX}/majors/${id}`)

// ===== D5 新闻公告与校园论坛 =====
export const listNewsPage = (params) => request.get(`${BASE_API_PREFIX}/news`, { params })

export const addNews = (data) => request.post(`${BASE_API_PREFIX}/news`, data)

export const updateNews = (id, data) => request.put(`${BASE_API_PREFIX}/news/${id}`, data)

export const delNews = (id) => request.delete(`${BASE_API_PREFIX}/news/${id}`)

export const listForumPostPage = (params) => request.get(`${BASE_API_PREFIX}/forum/posts`, { params })

export const getForumPost = (id) => request.get(`${BASE_API_PREFIX}/forum/posts/${id}`)

export const addForumPost = (data) => request.post(`${BASE_API_PREFIX}/forum/posts`, data)

export const likeForumPost = (id) => request.post(`${BASE_API_PREFIX}/forum/posts/${id}/likes`)

export const delForumPost = (id) => request.delete(`${BASE_API_PREFIX}/forum/posts/${id}`)

export const moderateForumPost = (id, status) => request.put(`${BASE_API_PREFIX}/forum/posts/${id}/status`, { status })

export const listForumComments = (postId) => request.get(`${BASE_API_PREFIX}/forum/posts/${postId}/comments`)

export const addForumComment = (postId, data) => request.post(`${BASE_API_PREFIX}/forum/posts/${postId}/comments`, data)

export const delForumComment = (id) => request.delete(`${BASE_API_PREFIX}/forum/comments/${id}`)

// ===== D2 招生与迎新统计 =====
export const listEnrollmentPage = (params) => request.get(`${BASE_API_PREFIX}/enrollments`, { params })

export const addEnrollment = (data) => request.post(`${BASE_API_PREFIX}/enrollments`, data)

export const updateEnrollment = (id, data) => request.put(`${BASE_API_PREFIX}/enrollments/${id}`, data)

export const delEnrollment = (id) => request.delete(`${BASE_API_PREFIX}/enrollments/${id}`)

export const getEnrollmentStats = (params) => request.get(`${BASE_API_PREFIX}/enrollments/stats`, { params })

// ===== D3 学生特征多维统计 =====
export const getStudentStats = (params) => request.get(`${BASE_API_PREFIX}/students/stats`, { params })

// ===== D6 AI 自然语言报表（LLM 调用较慢，单独放宽超时） =====
export const generateAiReport = (data) => request.post(`${BASE_API_PREFIX}/ai/reports`, data, { timeout: 90000 })
