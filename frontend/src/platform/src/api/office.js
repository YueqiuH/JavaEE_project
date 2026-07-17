import request from '@/utils/request.js'

export const OFFICE_API_PREFIX = '/api/v1/office'

export const feeAPI = {
  importFees: (data) => request.post(`${OFFICE_API_PREFIX}/fee/import`, data),
  getMyFees: () => request.get(`${OFFICE_API_PREFIX}/fee/mine`),
  pay: (feeId) => request.post(`${OFFICE_API_PREFIX}/fee/pay/${feeId}`),
  getRecentPayments: () => request.get(`${OFFICE_API_PREFIX}/fee/payment/recent`),
}

export const assetAPI = {
  list: (deptId) => request.get(`${OFFICE_API_PREFIX}/asset/list`, { params: deptId ? { deptId } : {} }),
  save: (data) => request.post(`${OFFICE_API_PREFIX}/asset/save`, data),
  apply: (data) => request.post(`${OFFICE_API_PREFIX}/asset/apply`, data),
  approve: (assetId, approved) => request.post(`${OFFICE_API_PREFIX}/asset/approve/${assetId}`, null, { params: { approved } }),
  remove: (assetId) => request.delete(`${OFFICE_API_PREFIX}/asset/${assetId}`),
}

export const workPlanAPI = {
  getMine: () => request.get(`${OFFICE_API_PREFIX}/work-plan/mine`),
  list: () => request.get(`${OFFICE_API_PREFIX}/work-plan/list`),
  save: (data) => request.post(`${OFFICE_API_PREFIX}/work-plan/save`, data),
  comment: (planId, comment) => request.post(`${OFFICE_API_PREFIX}/work-plan/comment/${planId}`, comment, { headers: { 'Content-Type': 'text/plain' } }),
  remove: (planId) => request.delete(`${OFFICE_API_PREFIX}/work-plan/${planId}`),
}

export const documentAPI = {
  approvers: () => request.get(`${OFFICE_API_PREFIX}/document/approvers`),
  start: (data) => request.post(`${OFFICE_API_PREFIX}/document/start`, data),
  resubmit: (docId, data) => request.post(`${OFFICE_API_PREFIX}/document/${docId}/resubmit`, data),
  initiated: () => request.get(`${OFFICE_API_PREFIX}/document/mine`),
  pending: () => request.get(`${OFFICE_API_PREFIX}/document/pending`),
  history: (docId) => request.get(`${OFFICE_API_PREFIX}/document/${docId}/history`),
  approve: (docId, data) => request.post(`${OFFICE_API_PREFIX}/document/${docId}/approve`, data),
  remind: (docId) => request.post(`${OFFICE_API_PREFIX}/document/${docId}/remind`),
}

export const meetingAPI = {
  publish: (data) => request.post(`${OFFICE_API_PREFIX}/meeting/publish`, data),
  list: () => request.get(`${OFFICE_API_PREFIX}/meeting/list`),
  mine: () => request.get(`${OFFICE_API_PREFIX}/meeting/mine`),
  reply: (meetingId, status) => request.post(`${OFFICE_API_PREFIX}/meeting/${meetingId}/reply`, null, { params: { status } }),
  summary: (meetingId) => request.get(`${OFFICE_API_PREFIX}/meeting/${meetingId}/summary`),
  notifications: () => request.get(`${OFFICE_API_PREFIX}/meeting/notifications`),
  readNotification: (notifyId) => request.post(`${OFFICE_API_PREFIX}/meeting/notifications/${notifyId}/read`),
}

export const aiApprovalAPI = {
  summary: (docId) => request.post(`${OFFICE_API_PREFIX}/ai-approval/summary/${docId}`),
  recommend: (docId, data) => request.post(`${OFFICE_API_PREFIX}/ai-approval/recommend/${docId}`, data),
}
