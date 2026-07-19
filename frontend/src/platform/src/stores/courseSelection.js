import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { selectCourse, dropCourse, getMySelection, getCourseList } from '@/api/teaching.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'

const SPORTS_KEYWORDS = ['体育', '体育课']
const MAX_CREDIT_DEFAULT = 30
const MAX_SPORTS_DEFAULT = 1

export const useCourseSelectionStore = defineStore('courseSelection', () => {
  // ==================== 核心状态 ====================
  const semester = ref('2025-2026-1')
  const allCourses = ref([])          // 所有可选课程（教学班级别）
  const selectedCourses = ref([])     // 已选课程列表
  const creditLimit = ref(MAX_CREDIT_DEFAULT)
  const sportsLimit = ref(MAX_SPORTS_DEFAULT)
  const loading = ref(false)
  const queuePosition = ref(-1)       // -1=不在排队, >=0=排队位置
  const queueResult = ref(null)       // 排队结果
  const hoverTarget = ref(null)       // 当前悬停的教学班信息
  const selectionLogs = ref(loadLogs())

  // ==================== 用户信息（与登录认证保持一致） ====================
  // 从本地存储的 CurrentUserVo 中提取身份信息
  // CurrentUserVo 结构: { user: { userId, username, userType, status }, roles, permissions, menus }
  // 其中 username 即为学号/工号（如 "600001"），userId 为数据库主键
  const studentInfo = computed(() => {
    const stored = getStoredCurrentUser()
    if (!stored || !stored.user || !stored.user.userId) {
      // 未登录或登录态损坏 —— 返回空标识，API 层会因 401 跳转登录
      return null
    }
    const u = stored.user
    return {
      userId: u.userId,                   // 数据库 user_id，选课 API 使用的 studentId
      name: u.username,                   // 学号/工号（如 "600001"），同时也是显示名
      studentNo: u.username,              // 与 name 一致，都是学号
      userType: u.userType || 1,          // 1=学生, 2=教师, 3=教务, 4=管理员
    }
  })

  /** 当前登录用户的学生 ID（未登录时为 null，不应发生） */
  const currentUserId = computed(() => studentInfo.value?.userId ?? null)

  // ==================== 计算属性 ====================
  const currentCredits = computed(() =>
    selectedCourses.value.reduce((sum, c) => sum + Number(c.credit || 0), 0)
  )

  const creditsRemaining = computed(() => creditLimit.value - currentCredits.value)

  const creditProgress = computed(() =>
    Math.min(100, Math.round((currentCredits.value / creditLimit.value) * 100))
  )

  const creditBarStatus = computed(() => {
    const pct = creditProgress.value
    if (pct >= 100) return 'exception'
    if (pct >= 85) return 'warning'
    return 'success'
  })

  const sportsCount = computed(() =>
    selectedCourses.value.filter(c =>
      SPORTS_KEYWORDS.some(k => (c.courseName || '').includes(k) || (c.classification || '').includes(k))
    ).length
  )

  const sportsFull = computed(() => sportsCount.value >= sportsLimit.value)

  /** 按 courseCode 分组，1:N 结构 */
  const courseGroups = computed(() => {
    const map = new Map()
    for (const c of allCourses.value) {
      const code = c.courseCode || c.course_code || '__unknown__'
      if (!map.has(code)) map.set(code, [])
      map.get(code).push(c)
    }
    // 按课程代码排序
    const sorted = [...map.entries()].sort((a, b) => a[0].localeCompare(b[0]))
    return sorted.map(([code, classes]) => {
      const first = classes[0]
      return {
        courseCode: code,
        courseName: first.courseName || first.course_name || '-',
        classification: first.classification || '-',
        credit: Number(first.credit || 0),
        classes
      }
    })
  })

  /** 已选课程生成课表占用 */
  const scheduleOccupancy = computed(() => {
    return selectedCourses.value
      .filter(c => c.weekDay || c.week_day)
      .map(c => ({
        courseName: c.courseName || c.course_name,
        teacherName: c.teacherName || c.teacher_name || '-',
        classroomName: c.classroomName || c.classroom_name || '-',
        weekDay: c.weekDay || c.week_day,
        startPeriod: c.startPeriod || c.start_period,
        endPeriod: c.endPeriod || c.end_period,
        courseCode: c.courseCode || c.course_code,
      }))
  })

  // ==================== 辅助函数 ====================

  /** 判断两个时间段是否冲突 */
  function periodsOverlap(aStart, aEnd, bStart, bEnd) {
    return aStart < bEnd && aEnd > bStart
  }

  /** 检查新课程与已选课表是否时间冲突 */
  function hasTimeConflict(newCourse) {
    const nwd = newCourse.weekDay ?? newCourse.week_day
    const nsp = newCourse.startPeriod ?? newCourse.start_period
    const nep = newCourse.endPeriod ?? newCourse.end_period
    if (!nwd || !nsp || !nep) return { conflict: false }

    for (const s of selectedCourses.value) {
      const swd = s.weekDay ?? s.week_day
      const ssp = s.startPeriod ?? s.start_period
      const sep = s.endPeriod ?? s.end_period
      if (!swd || !ssp || !sep) continue
      if (swd === nwd && periodsOverlap(nsp, nep, ssp, sep)) {
        return {
          conflict: true,
          conflictWith: s.courseName || s.course_name || '未知课程',
          conflictDay: swd,
          conflictPeriod: `${ssp}-${sep}节`
        }
      }
    }
    return { conflict: false }
  }

  /** 判断 hover 目标与已选课表的冲突 */
  const hoverConflict = computed(() => {
    if (!hoverTarget.value) return null
    const result = hasTimeConflict(hoverTarget.value)
    return result.conflict ? result : null
  })

  /** 获取某天某节次的已选课程（用于课表渲染） */
  function getCourseAt(day, period) {
    return selectedCourses.value.find(s => {
      const wd = s.weekDay ?? s.week_day
      const sp = s.startPeriod ?? s.start_period
      const ep = s.endPeriod ?? s.end_period
      return wd === day && period >= sp && period <= ep
    }) || null
  }

  /** 获取 hover 预占信息 */
  function getHoverPreviewAt(day, period) {
    if (!hoverTarget.value) return null
    const nwd = hoverTarget.value.weekDay ?? hoverTarget.value.week_day
    const nsp = hoverTarget.value.startPeriod ?? hoverTarget.value.start_period
    const nep = hoverTarget.value.endPeriod ?? hoverTarget.value.end_period
    if (nwd === day && period >= nsp && period <= nep) {
      return {
        courseName: hoverTarget.value.courseName || hoverTarget.value.course_name,
        hasConflict: !!hoverConflict.value,
      }
    }
    return null
  }

  // ==================== 客户端前置校验 ====================
  function preValidate(course) {
    const credit = Number(course.credit || 0)
    const name = course.courseName || course.course_name || '课程'

    // 1. 学分超限
    if (currentCredits.value + credit > creditLimit.value) {
      return { ok: false, msg: `超出学分上限！（当前 ${currentCredits.value} / ${creditLimit.value}，无法再选 ${credit} 学分）` }
    }

    // 2. 时间冲突
    const conflict = hasTimeConflict(course)
    if (conflict.conflict) {
      return {
        ok: false,
        msg: `与已选课程「${conflict.conflictWith}」时间冲突！（周${['一','二','三','四','五','六','日'][conflict.conflictDay-1]} ${conflict.conflictPeriod}）`
      }
    }

    // 3. 体育课数量限制
    if (SPORTS_KEYWORDS.some(k => (course.classification || '').includes(k) || name.includes(k))) {
      if (sportsCount.value >= sportsLimit.value) {
        return { ok: false, msg: `体育课已达上限！最多可选 ${sportsLimit.value} 门` }
      }
    }

    // 4. 容量校验（本地兜底）
    const curCount = course.currentCount || 0
    const maxCap = course.maxCapacity || 60
    if (curCount >= maxCap) {
      return { ok: false, msg: '该教学班已满员' }
    }

    return { ok: true }
  }

  // ==================== 操作 ====================

  /** 加载所有数据 */
  async function loadAll() {
    // 确保已登录
    if (!currentUserId.value) {
      console.warn('[选课Store] 未登录，跳过数据加载')
      return
    }
    loading.value = true
    try {
      const [courseRes, selRes] = await Promise.all([
        getCourseList(semester.value),
        getMySelection(currentUserId.value, semester.value)
      ])
      allCourses.value = (courseRes?.data || []).filter(c => c.courseId)
      selectedCourses.value = (selRes?.data || []).map(s => ({
        ...s,
        courseName: s.course_name || s.courseName,
        courseCode: s.course_code || s.courseCode,
        teacherName: s.teacher_name || s.teacherName,
        classroomName: s.classroom_name || s.classroomName,
        weekDay: s.week_day || s.weekDay,
        startPeriod: s.start_period || s.startPeriod,
        endPeriod: s.end_period || s.endPeriod,
        classification: s.classification || '-',
        credit: Number(s.credit || 0),
        currentCount: s.currentCount || s.current_count || 0,
        maxCapacity: s.maxCapacity || s.max_capacity || 60,
      }))
    } catch (e) {
      allCourses.value = []
      selectedCourses.value = []
    } finally {
      loading.value = false
    }
  }

  /** 执行选课 */
  async function doSelect(course) {
    if (!currentUserId.value) {
      return { code: -1, msg: '登录已失效，请重新登录' }
    }
    const courseId = course.courseId || course.course_id
    const validate = preValidate(course)
    if (!validate.ok) {
      addLog({ action: '选课', course, ok: false, msg: validate.msg })
      return { code: -1, msg: validate.msg }
    }

    // 开始排队模拟
    queuePosition.value = 1
    queueResult.value = null

    try {
      const res = await selectCourse({
        studentId: currentUserId.value,
        courseId,
        semester: semester.value
      })
      queuePosition.value = -1
      if (res?.code === 0) {
        addLog({ action: '选课', course, ok: true, msg: '选课成功' })
        await loadAll()
        return { code: 0, msg: '选课成功' }
      } else {
        addLog({ action: '选课', course, ok: false, msg: res?.msg || '选课失败' })
        return { code: -1, msg: res?.msg || '选课失败' }
      }
    } catch (e) {
      queuePosition.value = -1
      const errMsg = e?.response?.data?.msg || e?.message || '网络错误'
      addLog({ action: '选课', course, ok: false, msg: errMsg })
      return { code: -1, msg: errMsg }
    }
  }

  /** 执行退选 */
  async function doDrop(course) {
    if (!currentUserId.value) {
      return { code: -1, msg: '登录已失效，请重新登录' }
    }
    const courseId = course.courseId || course.course_id
    const name = course.courseName || course.course_name || '课程'

    try {
      const res = await dropCourse({
        studentId: currentUserId.value,
        courseId,
        semester: semester.value
      })
      if (res?.code === 0) {
        addLog({ action: '退选', course, ok: true, msg: `退选「${name}」成功` })
        await loadAll()
        return { code: 0, msg: '退选成功' }
      } else {
        addLog({ action: '退选', course, ok: false, msg: res?.msg || '退选失败' })
        return { code: -1, msg: res?.msg || '退选失败' }
      }
    } catch (e) {
      const errMsg = e?.response?.data?.msg || e?.message || '网络错误'
      addLog({ action: '退选', course, ok: false, msg: errMsg })
      return { code: -1, msg: errMsg }
    }
  }

  /** 模拟排队进度（高并发演示） */
  function simulateQueuePosition(total) {
    queuePosition.value = total
    const timer = setInterval(() => {
      if (queuePosition.value > 1) {
        queuePosition.value--
      } else if (queuePosition.value === 1) {
        queuePosition.value = -1
        clearInterval(timer)
      }
    }, 500 + Math.random() * 300)
    return () => clearInterval(timer)
  }

  // ==================== 日志 ====================
  function loadLogs() {
    try {
      return JSON.parse(localStorage.getItem('selection_logs') || '[]')
    } catch { return [] }
  }

  function addLog({ action, course, ok, msg }) {
    const log = {
      time: new Date().toLocaleString('zh-CN'),
      action,
      courseName: course.courseName || course.course_name || '-',
      courseCode: course.courseCode || course.course_code || '-',
      teacherName: course.teacherName || course.teacher_name || '-',
      ok,
      msg
    }
    selectionLogs.value.unshift(log)
    if (selectionLogs.value.length > 200) selectionLogs.value.length = 200
    localStorage.setItem('selection_logs', JSON.stringify(selectionLogs.value))
  }

  function clearLogs() {
    selectionLogs.value = []
    localStorage.removeItem('selection_logs')
  }

  // ==================== 初始化 ====================
  // loadAll 由组件 onMounted 调用

  return {
    // 状态
    semester, allCourses, selectedCourses, creditLimit, sportsLimit,
    loading, queuePosition, queueResult, hoverTarget, selectionLogs,
    // 计算
    studentInfo, currentUserId, currentCredits, creditsRemaining, creditProgress, creditBarStatus,
    sportsCount, sportsFull, courseGroups, scheduleOccupancy,
    hoverConflict,
    // 方法
    loadAll, doSelect, doDrop, preValidate,
    getCourseAt, getHoverPreviewAt,
    simulateQueuePosition, clearLogs,
  }
})
