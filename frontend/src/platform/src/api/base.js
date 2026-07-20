import request from '@/utils/request.js'
import { clearAccessToken, getAccessToken } from '@/utils/authToken.js'
import { clearStoredCurrentUser } from '@/utils/authSession.js'
import { ElMessage } from 'element-plus'
import Router from '@/router'

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

export const syncEnrollmentActual = (params) => request.post(`${BASE_API_PREFIX}/enrollments/sync-actual`, null, { params })

// ===== Excel 导出 =====

export const exportStudents= (params) => downloadFile(`${BASE_API_PREFIX}/students/export`, params, '学生档案.xlsx')

export const exportStaffs = () => downloadFile(`${BASE_API_PREFIX}/staffs/export`, null, '教职工档案.xlsx')

export const downloadStudentTemplate = () => downloadFile(`${BASE_API_PREFIX}/students/template`, null, '学生导入模板.xlsx')

export const downloadStaffTemplate = () => downloadFile(`${BASE_API_PREFIX}/staffs/template`, null, '教职工导入模板.xlsx')

export const importStudents = (formData) => request.post(`${BASE_API_PREFIX}/students/import`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })

export const importStaffs = (formData) => request.post(`${BASE_API_PREFIX}/staffs/import`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })

async function downloadFile(path, params, filename) {
  const token = getAccessToken()
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888'
  const query = params ? '?' + new URLSearchParams(
    Object.entries(params).filter(([, v]) => v != null)
  ).toString() : ''
  const res = await fetch(`${baseUrl}${path}${query}`, {
    headers: { 'Authorization': token ? `Bearer ${token}` : '' }
  })
  // 401 处理：与 axios 拦截器行为一致
  if (res.status === 401) {
    clearAccessToken()
    clearStoredCurrentUser()
    ElMessage.warning('登录已过期，请重新登录')
    if (Router.currentRoute.value.path !== '/login') {
      Router.push({ path: '/login', query: { redirect: Router.currentRoute.value.fullPath } })
    }
    throw new Error('未登录')
  }
  if (!res.ok) throw new Error('HTTP ' + res.status)
  const blob = await res.blob()
  if (blob.size < 100) {
    const text = await blob.text()
    console.warn('导出响应异常:', text)
    throw new Error('导出响应异常')
  }
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = filename
  a.click()
  URL.revokeObjectURL(a.href)
}

// ===== D3 学生特征多维统计 =====
export const getStudentStats = (params) => request.get(`${BASE_API_PREFIX}/students/stats`, { params })

// ===== D6 AI 自然语言报表（LLM 调用较慢，单独放宽超时） =====
export const generateAiReport = (data) => request.post(`${BASE_API_PREFIX}/ai/reports`, data, { timeout: 90000 })
