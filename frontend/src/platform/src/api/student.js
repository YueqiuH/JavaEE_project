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

export const completeLabBooking = (id) => request.post(`${STUDENT_API_PREFIX}/lab-bookings/${id}/completions`)

export const listMyLabBookingNotices = (params) => request.get(`${STUDENT_API_PREFIX}/lab-booking-notices/mine`, { params })

export const markLabBookingNoticeRead = (id) => request.post(`${STUDENT_API_PREFIX}/lab-booking-notices/${id}/reads`)
