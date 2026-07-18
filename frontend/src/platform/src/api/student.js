import request from '@/utils/request.js'

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

export const getMyEvaluationOverview = () => request.get(`${STUDENT_API_PREFIX}/evaluation-results/mine`)

export const getMyCourseEvaluationDetail = (courseId, semester) =>
  request.get(`${STUDENT_API_PREFIX}/evaluation-results/mine/courses/${courseId}`, { params: { semester } })
