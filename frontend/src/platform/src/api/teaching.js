import request from '@/utils/request.js'

export const TEACHING_API_PREFIX = '/api/v1/teaching'

export const aiStudyApi = {
    uploadMaterial(data) {
        return request.post(`${TEACHING_API_PREFIX}/ai/materials`, data)
    },
    getMaterials(studentId) {
        return request.get(`${TEACHING_API_PREFIX}/ai/materials`, { params: { studentId } })
    },
    generateSummary(materialId) {
        return request.post(`${TEACHING_API_PREFIX}/ai/materials/${materialId}/summary`)
    },
    generateQuestions(materialId) {
        return request.post(`${TEACHING_API_PREFIX}/ai/materials/${materialId}/questions`)
    },
    recommendPath(studentId) {
        return request.post(`${TEACHING_API_PREFIX}/ai/learning-paths`, { studentId })
    },
    getRecords(studentId) {
        return request.get(`${TEACHING_API_PREFIX}/ai/records`, { params: { studentId } })
    }
}
