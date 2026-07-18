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

export const inviteCompetitionMember = (id, studentNo) => request.post(`${STUDENT_API_PREFIX}/competition-teams/${id}/invitations`, { studentNo })

export const removeCompetitionMember = (teamId, memberId) => request.delete(`${STUDENT_API_PREFIX}/competition-teams/${teamId}/members/${memberId}`)

export const submitCompetitionTeam = (id) => request.post(`${STUDENT_API_PREFIX}/competition-teams/${id}/submissions`)

export const listMyCompetitionInvitations = (params) => request.get(`${STUDENT_API_PREFIX}/competition-invitations/mine`, { params })

export const respondCompetitionInvitation = (memberId, decision) => request.post(`${STUDENT_API_PREFIX}/competition-invitations/${memberId}/responses`, { decision })

export const listCompetitionReviews = (params) => request.get(`${STUDENT_API_PREFIX}/competition-reviews`, { params })

export const reviewCompetitionTeam = (id, data) => request.post(`${STUDENT_API_PREFIX}/competition-teams/${id}/reviews`, data)
