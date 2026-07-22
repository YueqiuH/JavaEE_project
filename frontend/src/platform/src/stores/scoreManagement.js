import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { scoreApi } from '@/api/teaching.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'

const DEFAULT_RATIOS = { regular: 0.3, exam: 0.7 }

export const useScoreStore = defineStore('scoreManagement', () => {
  // ==================== 身份 ====================
  const currentUser = computed(() => {
    try {
      const stored = getStoredCurrentUser()
      if (!stored?.user?.userId) return null
      return {
        userId: stored.user.userId,
        username: stored.user.username,
        userType: stored.user.userType || 1, // 1=学生 2=教师 3=教务 4=管理员
        roles: stored.roles || [],
      }
    } catch { return null }
  })

  const role = computed(() => {
    if (!currentUser.value) return 'guest'
    const t = currentUser.value.userType
    if (t === 4) return 'admin'        // 教务处
    if (t === 3) return 'teacher'      // 教职工
    if (t === 2) return 'counselor'    // 辅导员
    return 'student'                   // 学生
  })

  // ==================== 状态 ====================
  const semester = ref('2025-2026-1')
  const timeWindow = ref({ currentWeek: 1, phase: 'locked', teacherCanEdit: false, teacherCanPublish: false, studentCanView: false, adminOnly: false })
  const loading = ref(false)

  // 学生端
  const studentReport = ref(null)
  const studentScores = ref([])

  // 教师端
  const teacherClasses = ref([])
  const currentClassScores = ref([])
  const selectedClass = ref(null)

  // 辅导员端
  const warningList = ref([])
  const selectedStudentProfile = ref(null)

  // 管理员端
  const adminModifyLogs = ref([])

  // ==================== 计算属性 ====================

  /** 学生当前学期不及格学分 */
  const currentFailCredits = computed(() => {
    return studentScores.value
      .filter(s => s.status === 0 && s.semester === semester.value)
      .reduce((sum, s) => sum + (s.credit || 2), 0)
  })

  /** 学生累计不及格学分 */
  const totalFailCredits = computed(() => {
    return studentScores.value
      .filter(s => s.status === 0)
      .reduce((sum, s) => sum + (s.credit || 2), 0)
  })

  /** 学生已修学分(通过课程) */
  const earnedCredits = computed(() => {
    return studentScores.value
      .filter(s => s.status === 1)
      .reduce((sum, s) => sum + (s.credit || 2), 0)
  })

  /** 学生 GPA */
  const studentGpa = computed(() => studentReport.value?.gpa || 0)

  /** 预警等级 */
  const warningLevel = computed(() => {
    const cur = currentFailCredits.value
    const tot = totalFailCredits.value
    if (cur >= 15 || tot >= 20) return { level: 'red', label: '🔴 红色预警', desc: '退学高危警告' }
    if (cur >= 10) return { level: 'yellow', label: '🟡 黄色预警', desc: '轻度学业警示' }
    return { level: 'none', label: '✅ 正常', desc: '无学业预警' }
  })

  /** 教学班草稿/已发布统计 */
  const classStats = computed(() => {
    const sc = currentClassScores.value
    return {
      total: sc.length,
      draft: sc.filter(s => s.publishStatus === 1).length,
      published: sc.filter(s => s.publishStatus === 2).length,
      pass: sc.filter(s => s.status === 1).length,
      fail: sc.filter(s => s.status === 0).length,
    }
  })

  /** GPA 换算表（用于前端即时预览） */
  function scoreToGpa(score) {
    if (score >= 90) return 4.0
    if (score >= 85) return 3.7
    if (score >= 82) return 3.3
    if (score >= 78) return 3.0
    if (score >= 75) return 2.7
    if (score >= 72) return 2.3
    if (score >= 68) return 2.0
    if (score >= 64) return 1.5
    if (score >= 60) return 1.0
    return 0.0
  }

  /** 计算总评 = round(平时*regRatio + 期末*examRatio) */
  function calcTotal(regular, exam, regR, examR) {
    const rr = regR ?? DEFAULT_RATIOS.regular
    const er = examR ?? DEFAULT_RATIOS.exam
    return Math.round(regular * rr + exam * er)
  }

  // ==================== 操作 ====================

  /** 加载时间窗口 */
  async function loadTimeWindow() {
    try {
      const res = await scoreApi.getTimeWindow()
      if (res?.data) timeWindow.value = res.data
    } catch { /* 使用默认值 */ }
  }

  // --- 学生 ---

  /** 加载学生成绩单 */
  async function loadStudentReport(sem) {
    if (!currentUser.value) return
    loading.value = true
    try {
      const res = await scoreApi.getStudentReport(currentUser.value.userId, sem || semester.value)
      if (res?.data) {
        studentReport.value = res.data
        studentScores.value = res.data.courses || []
      }
    } catch { studentReport.value = null; studentScores.value = [] }
    finally { loading.value = false }
  }

  // --- 教师 ---

  /** 加载教师教学班列表(admin传0看全部) */
  async function loadTeacherClasses() {
    if (!currentUser.value) return
    loading.value = true
    try {
      const tid = role.value === 'admin' ? 0 : currentUser.value.userId
      const res = await scoreApi.getTeacherClasses(tid, semester.value)
      teacherClasses.value = res?.data || []
    } catch { teacherClasses.value = [] }
    finally { loading.value = false }
  }

  /** 加载某教学班成绩 */
  async function loadClassScores(courseId) {
    loading.value = true
    try {
      const res = await scoreApi.getCourseScores(courseId, semester.value)
      currentClassScores.value = (res?.data || []).map(s => ({
        ...s,
        studentName: s.student_name || s.studentName || '-',
        studentNo: s.student_no || s.studentNo || '-',
        courseName: s.course_name || s.courseName || '-',
      }))
    } catch { currentClassScores.value = [] }
    finally { loading.value = false }
  }

  /** 教师录入成绩 */
  async function inputScore(data) {
    try {
      const res = await scoreApi.input(data)
      return { ok: true, data: res?.data }
    } catch (e) {
      return { ok: false, msg: e?.response?.data?.msg || e?.message || '录入失败' }
    }
  }

  /** 教师暂存草稿 */
  async function saveDraft(data) {
    try {
      await scoreApi.saveDraft(data)
      return { ok: true }
    } catch (e) {
      return { ok: false, msg: e?.response?.data?.msg || e?.message || '暂存失败' }
    }
  }

  /** 教师一键发布 */
  async function publishClassScores(scheduleId) {
    if (!currentUser.value) return { ok: false, msg: '未登录' }
    try {
      const res = await scoreApi.publish(scheduleId, currentUser.value.userId, semester.value)
      return { ok: true, data: res?.data }
    } catch (e) {
      return { ok: false, msg: e?.response?.data?.msg || e?.message || '发布失败' }
    }
  }

  // --- 辅导员 ---

  /** 加载辅导员预警列表 */
  async function loadWarnings() {
    if (!currentUser.value) return
    loading.value = true
    try {
      const res = await scoreApi.getCounselorWarnings(currentUser.value.userId, semester.value)
      warningList.value = (res?.data || []).map(w => ({
        ...w,
        studentName: w.student_name || w.studentName || '-',
        studentNo: w.student_no || w.studentNo || '-',
        failCount: w.fail_count || 0,
        failCredits: w.fail_credits || 0,
        currentFailCredits: w.current_fail_credits || 0,
      }))
    } catch { warningList.value = [] }
    finally { loading.value = false }
  }

  /** 加载学生完整画像 */
  async function loadStudentProfile(studentId) {
    loading.value = true
    try {
      const res = await scoreApi.getStudentProfile(studentId)
      selectedStudentProfile.value = res?.data || null
    } catch { selectedStudentProfile.value = null }
    finally { loading.value = false }
  }

  // --- 管理员 ---

  /** 管理员例外修改成绩 */
  async function adminModifyScore(scoreId, newScore, reason, docNo) {
    if (!currentUser.value) return { ok: false, msg: '未登录' }
    try {
      const res = await scoreApi.adminModify({
        scoreId, newScore, adminId: currentUser.value.userId, reason, docNo
      })
      return { ok: true, data: res?.data }
    } catch (e) {
      return { ok: false, msg: e?.response?.data?.msg || e?.message || '修改失败' }
    }
  }

  /** 加载修改日志 */
  async function loadModificationLogs(scoreId) {
    try {
      const res = await scoreApi.getModificationLogs(scoreId)
      adminModifyLogs.value = res?.data || []
    } catch { adminModifyLogs.value = [] }
  }

  return {
    // 状态
    currentUser, role, semester, timeWindow, loading,
    studentReport, studentScores, teacherClasses, currentClassScores, selectedClass,
    warningList, selectedStudentProfile, adminModifyLogs,
    // 计算
    currentFailCredits, totalFailCredits, earnedCredits, studentGpa, warningLevel, classStats,
    // 工具
    scoreToGpa, calcTotal,
    // 方法
    loadTimeWindow, loadStudentReport, loadTeacherClasses, loadClassScores,
    inputScore, saveDraft, publishClassScores,
    loadWarnings, loadStudentProfile,
    adminModifyScore, loadModificationLogs,
  }
})
