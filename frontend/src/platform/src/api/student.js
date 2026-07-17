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
