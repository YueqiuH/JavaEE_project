import request from '@/utils/request.js'
import { getAccessToken } from '@/utils/authToken.js'
import { ElMessage } from 'element-plus'
import Router from '@/router'

export const STUDENT_API_PREFIX = '/api/v1/student'

export const listMyScholarshipApplications = (params) =>
  request.get(`${STUDENT_API_PREFIX}/scholarships/mine`, { params })

export const listScholarshipReviews = (params) =>
  request.get(`${STUDENT_API_PREFIX}/scholarship-reviews`, { params })

export const getScholarshipApplication = (id) =>
  request.get(`${STUDENT_API_PREFIX}/scholarships/${id}`)

export const createScholarshipApplication = (data) =>
  request.post(`${STUDENT_API_PREFIX}/scholarships`, data)

export const updateScholarshipApplication = (id, data) =>
  request.put(`${STUDENT_API_PREFIX}/scholarships/${id}`, data)

export const submitScholarshipApplication = (id) =>
  request.post(`${STUDENT_API_PREFIX}/scholarships/${id}/submissions`)

export const withdrawScholarshipApplication = (id) =>
  request.delete(`${STUDENT_API_PREFIX}/scholarships/${id}`)

export const submitScholarshipReview = (id, data) =>
  request.post(`${STUDENT_API_PREFIX}/scholarships/${id}/reviews`, data)

export const listScholarshipResults = (params) =>
  request.get(`${STUDENT_API_PREFIX}/scholarship-results`, { params })

export const generateScholarshipResults = (applicationIds) =>
  request.post(`${STUDENT_API_PREFIX}/scholarship-results`, { applicationIds })

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

export const listMyEvaluationTasks = () => request.get(`${STUDENT_API_PREFIX}/evaluation-tasks/mine`)

export const submitEvaluation = (selectionId, data) =>
  request.post(`${STUDENT_API_PREFIX}/evaluation-tasks/${selectionId}/submissions`, data)

export const submitCounselorEvaluation = (data) =>
  request.post(`${STUDENT_API_PREFIX}/evaluation-counselor-task/mine/submissions`, data)

export const getMyEvaluationOverview = () => request.get(`${STUDENT_API_PREFIX}/evaluation-results/mine`)

export const getMyCourseEvaluationDetail = (courseId, semester, teacherId) =>
  request.get(`${STUDENT_API_PREFIX}/evaluation-results/mine/courses/${courseId}`, { params: { semester, teacherId } })

export const getCounselorEvaluationDetail = (counselorId, semester) =>
  request.get(`${STUDENT_API_PREFIX}/evaluation-results/mine/counselors/${counselorId}`, { params: { semester } })

export const listCompetitions = (params) => request.get(`${STUDENT_API_PREFIX}/competitions`, { params })

export const getCompetition = (id) => request.get(`${STUDENT_API_PREFIX}/competitions/${id}`)

export const createCompetition = (data) => request.post(`${STUDENT_API_PREFIX}/competitions`, data)

export const updateCompetition = (id, data) => request.put(`${STUDENT_API_PREFIX}/competitions/${id}`, data)

export const publishCompetition = (id) => request.post(`${STUDENT_API_PREFIX}/competitions/${id}/publications`)

export const closeCompetition = (id) => request.post(`${STUDENT_API_PREFIX}/competitions/${id}/closures`)

export const listMyCompetitionTeams = (params) => request.get(`${STUDENT_API_PREFIX}/competition-teams/mine`, { params })

export const getCompetitionTeam = (id) => request.get(`${STUDENT_API_PREFIX}/competition-teams/${id}`)

export const createCompetitionTeam = (competitionId, data) => request.post(`${STUDENT_API_PREFIX}/competitions/${competitionId}/teams`, data)

export const updateCompetitionTeam = (id, data) => request.put(`${STUDENT_API_PREFIX}/competition-teams/${id}`, data)

export const uploadCompetitionMaterial = (id, file) => {
  const data = new FormData()
  data.append('file', file)
  return request.put(`${STUDENT_API_PREFIX}/competition-teams/${id}/material`, data)
}

export const downloadCompetitionMaterial = async (id) => {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888'
  const response = await fetch(`${apiBaseUrl}${STUDENT_API_PREFIX}/competition-teams/${id}/material`, {
    headers: { Authorization: `Bearer ${getAccessToken()}` },
  })
  if (response.status === 401) {
    ElMessage.warning('登录已过期，请重新登录')
    if (Router.currentRoute.value.path !== '/login') {
      Router.push({ path: '/login', query: { redirect: Router.currentRoute.value.fullPath } })
    }
    throw new Error('未登录')
  }
  if (!response.ok) {
    const result = await response.json().catch(() => ({}))
    throw new Error(result.message || '报名材料下载失败')
  }
  const disposition = response.headers.get('content-disposition') || ''
  const encodedName = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
  const fileName = encodedName ? decodeURIComponent(encodedName) : null
  return { blob: await response.blob(), fileName }
}

export const inviteCompetitionMember = (id, studentNo) => request.post(`${STUDENT_API_PREFIX}/competition-teams/${id}/invitations`, { studentNo })

export const removeCompetitionMember = (teamId, memberId) => request.delete(`${STUDENT_API_PREFIX}/competition-teams/${teamId}/members/${memberId}`)

export const submitCompetitionTeam = (id) => request.post(`${STUDENT_API_PREFIX}/competition-teams/${id}/submissions`)

export const listMyCompetitionInvitations = (params) => request.get(`${STUDENT_API_PREFIX}/competition-invitations/mine`, { params })

export const respondCompetitionInvitation = (memberId, decision) => request.post(`${STUDENT_API_PREFIX}/competition-invitations/${memberId}/responses`, { decision })

export const listCompetitionReviews = (params) => request.get(`${STUDENT_API_PREFIX}/competition-reviews`, { params })

export const listCompetitionTeams = (competitionId, params) =>
  request.get(`${STUDENT_API_PREFIX}/competitions/${competitionId}/teams`, { params })

export const reviewCompetitionTeam = (id, data) => request.post(`${STUDENT_API_PREFIX}/competition-teams/${id}/reviews`, data)

export const listLabs = (params) => request.get(`${STUDENT_API_PREFIX}/labs`, { params })

export const getLab = (id) => request.get(`${STUDENT_API_PREFIX}/labs/${id}`)

export const createLab = (data) => request.post(`${STUDENT_API_PREFIX}/labs`, data)

export const updateLab = (id, data) => request.put(`${STUDENT_API_PREFIX}/labs/${id}`, data)

export const listLabResources = (params) => request.get(`${STUDENT_API_PREFIX}/lab-resources`, { params })

export const createLabResource = (labId, data) => request.post(`${STUDENT_API_PREFIX}/labs/${labId}/resources`, data)

export const updateLabResource = (id, data) => request.put(`${STUDENT_API_PREFIX}/lab-resources/${id}`, data)

export const listLabOpenSlots = (params) => request.get(`${STUDENT_API_PREFIX}/lab-open-slots`, { params })

export const createLabOpenSlot = (labId, data) => request.post(`${STUDENT_API_PREFIX}/labs/${labId}/open-slots`, data)

export const updateLabOpenSlot = (id, data) => request.put(`${STUDENT_API_PREFIX}/lab-open-slots/${id}`, data)

export const deleteLabOpenSlot = (id) => request.delete(`${STUDENT_API_PREFIX}/lab-open-slots/${id}`)

export const listMyLabBookings = (params) => request.get(`${STUDENT_API_PREFIX}/lab-bookings/mine`, { params })

export const listManagedLabBookings = (params) => request.get(`${STUDENT_API_PREFIX}/lab-bookings/managed`, { params })

export const getLabBooking = (id) => request.get(`${STUDENT_API_PREFIX}/lab-bookings/${id}`)

export const createLabBooking = (data) => request.post(`${STUDENT_API_PREFIX}/lab-bookings`, data)

export const cancelLabBooking = (id) => request.delete(`${STUDENT_API_PREFIX}/lab-bookings/${id}`)

export const checkInLabBooking = (id) => request.post(`${STUDENT_API_PREFIX}/lab-bookings/${id}/check-ins`)

export const checkOutLabBooking = (id) => request.post(`${STUDENT_API_PREFIX}/lab-bookings/${id}/check-outs`)

export const completeLabBooking = (id) => request.post(`${STUDENT_API_PREFIX}/lab-bookings/${id}/completions`)

export const listMyLabBookingNotices = (params) => request.get(`${STUDENT_API_PREFIX}/lab-booking-notices/mine`, { params })

export const markLabBookingNoticeRead = (id) => request.post(`${STUDENT_API_PREFIX}/lab-booking-notices/${id}/reads`)
