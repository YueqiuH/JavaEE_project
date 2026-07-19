<template>
  <div class="cs-console">
    <!-- ========== 1. 顶部状态与统计栏 ========== -->
    <div class="cs-topbar">
      <div class="top-left">
        <span class="top-avatar">{{ (store.studentInfo?.name || '?').slice(0,1) }}</span>
        <div class="top-info">
          <strong>{{ store.studentInfo?.name || '未登录' }}</strong>
          <small>学号 {{ store.studentInfo?.studentNo || '-' }} · {{ userTypeLabel }}</small>
        </div>
        <el-tag size="small" effect="plain" type="info">学期: {{ store.semester }}</el-tag>
      </div>
      <div class="top-center">
        <div class="credit-bar-wrap">
          <span class="credit-label">已选学分</span>
          <el-progress
            :percentage="store.creditProgress"
            :status="store.creditBarStatus"
            :stroke-width="10"
            :text-inside="true"
            style="width:200px"
          />
          <span class="credit-text" :class="{ 'text-warn': store.creditProgress>=85, 'text-danger': store.creditProgress>=100 }">
            {{ store.currentCredits }} / {{ store.creditLimit }}
          </span>
        </div>
        <el-tag v-if="store.sportsFull" size="small" type="warning" effect="dark">
          ⚠ 体育课已选{{ store.sportsCount }}门，不可再选
        </el-tag>
        <el-tag v-if="store.creditProgress>=100" size="small" type="danger" effect="dark">
          🔒 学分已满，选课已锁定
        </el-tag>
      </div>
      <div class="top-right">
        <el-button size="small" :icon="Refresh" @click="store.loadAll()" :loading="store.loading">刷新</el-button>
        <el-button size="small" @click="showLog=true">📝 选课日志 ({{ store.selectionLogs.length }})</el-button>
      </div>
    </div>

    <!-- ========== 主体：左二右一三栏布局 ========== -->
    <div class="cs-body">
      <!-- ===== 左侧面板 ===== -->
      <div class="cs-left">
        <!-- 2. 高级检索过滤区 -->
        <div class="filter-panel">
          <el-input
            v-model="filter.keyword"
            placeholder="课程代码 / 名称 / 教师"
            :prefix-icon="Search"
            clearable
            size="default"
            class="filter-input"
          />
          <el-select v-model="filter.department" placeholder="开课院系" clearable size="default" style="width:130px">
            <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
          </el-select>
          <el-select v-model="filter.classification" placeholder="课程性质" clearable size="default" style="width:110px">
            <el-option label="必修" value="必修" /><el-option label="选修" value="选修" /><el-option label="限选" value="限选" />
          </el-select>
          <div class="quick-tags">
            <el-tag v-for="t in quickTags" :key="t.key" size="small"
              :effect="filter.quickTag===t.key?'dark':'plain'"
              :type="filter.quickTag===t.key?'primary':'info'"
              @click="filter.quickTag = filter.quickTag===t.key ? '' : t.key"
              style="cursor:pointer">
              {{ t.label }}
            </el-tag>
          </div>
        </div>

        <!-- 3. 双层折叠面板（1:N 课程列表） -->
        <div class="course-list-panel">
          <div class="list-header">
            <span>📋 共 {{ filteredGroups.length }} 门课程，{{ totalClasses }} 个教学班</span>
            <el-switch v-model="expandAll" size="small" active-text="展开全部" inactive-text="收起全部" />
          </div>
          <div class="list-body" v-loading="store.loading">
            <el-empty v-if="filteredGroups.length===0" description="暂无匹配课程" :image-size="80" />
            <el-collapse v-else v-model="expandedGroups" @change="onExpandChange">
              <el-collapse-item
                v-for="group in filteredGroups"
                :key="group.courseCode"
                :name="group.courseCode"
              >
                <!-- 第一层：课程级别 -->
                <template #title>
                  <div class="course-header">
                    <span class="ch-icon">📚</span>
                    <span class="ch-name">{{ group.courseName }}</span>
                    <span class="ch-code">({{ group.courseCode }})</span>
                    <el-tag :type="group.classification==='必修'?'danger':group.classification==='限选'?'warning':'success'" size="small" effect="light">
                      {{ group.classification }}
                    </el-tag>
                    <span class="ch-credit">学分: {{ group.credit }}</span>
                    <span class="ch-count">{{ group.classes.length }} 个教学班</span>
                  </div>
                </template>

                <!-- 第二层：教学班级别 -->
                <div class="class-list">
                  <div
                    v-for="cls in group.classes"
                    :key="cls.courseId || cls.course_id"
                    class="class-card"
                    :class="{
                      'class-selected': isSelected(cls),
                      'class-full': (cls.currentCount||0) >= (cls.maxCapacity||60),
                      'class-disabled': store.creditProgress>=100 || isSameCodeSelected(cls)
                    }"
                  >
                    <div class="cc-info">
                      <div class="cc-main">
                        <span class="cc-teacher">👨‍🏫 {{ cls.teacherName || cls.teacher_name || '待定' }}</span>
                        <span class="cc-room">📍 {{ cls.classroomName || cls.classroom_name || '待定' }}</span>
                        <span class="cc-time">
                          🕐 {{ fmtSchedule(cls) }}
                        </span>
                      </div>
                      <div class="cc-capacity">
                        <el-progress
                          :percentage="capacityPct(cls)"
                          :stroke-width="5"
                          :status="(cls.currentCount||0)>=(cls.maxCapacity||60)?'exception':capacityPct(cls)>80?'warning':''"
                          style="width:80px"
                        />
                        <span class="cc-cap-text" :class="{'text-danger':(cls.currentCount||0)>=(cls.maxCapacity||60)}">
                          {{ cls.currentCount||0 }} / {{ cls.maxCapacity||60 }}
                        </span>
                      </div>
                    </div>
                    <div class="cc-actions">
                      <!-- 已选教学班：显示退选 -->
                      <el-button
                        v-if="isSelected(cls)"
                        size="small"
                        type="danger"
                        @click.stop="confirmDrop(cls)"
                      >
                        退选
                      </el-button>
                      <!-- 同课程代码已选其他班：禁用 -->
                      <el-button
                        v-else-if="isSameCodeSelected(cls)"
                        size="small"
                        disabled
                      >
                        已选其他班
                      </el-button>
                      <!-- 满员：禁用 -->
                      <el-button
                        v-else-if="(cls.currentCount||0) >= (cls.maxCapacity||60)"
                        size="small"
                        disabled
                      >
                        已满
                      </el-button>
                      <!-- 学分满：禁用 -->
                      <el-button
                        v-else-if="store.creditProgress >= 100"
                        size="small"
                        disabled
                      >
                        学分已满
                      </el-button>
                      <!-- 正常可选 -->
                      <el-button
                        v-else
                        size="small"
                        type="primary"
                        @click.stop="handleSelect(cls)"
                        @mouseenter="store.hoverTarget = cls"
                        @mouseleave="store.hoverTarget = null"
                      >
                        选课
                      </el-button>
                    </div>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>
      </div>

      <!-- ===== 右侧面板 ===== -->
      <div class="cs-right">
        <!-- 4. 周历课表矩阵（核心冲突预览区） -->
        <div class="schedule-panel">
          <div class="sp-title">📅 我的课表（周历视图）</div>
          <div class="sp-grid">
            <!-- 表头 -->
            <div class="sg-header">
              <div class="sgh-corner">节次</div>
              <div v-for="d in 7" :key="d" class="sgh-day" :class="{weekend:d>=6}">
                {{ weekLabels[d-1] }}
              </div>
            </div>
            <!-- 课表主体 -->
            <div v-for="p in 13" :key="p" class="sg-row" :class="periodZone(p)">
              <div class="sgr-label">
                <span class="period-num">{{ p }}</span>
                <span v-if="p===1" class="period-zone-name">上午</span>
                <span v-if="p===6" class="period-zone-name">下午</span>
                <span v-if="p===11" class="period-zone-name">晚上</span>
              </div>
              <div
                v-for="d in 7"
                :key="d"
                class="sg-cell"
                :class="cellClass(d, p)"
                @mouseenter="onCellHover(d, p)"
              >
                <!-- 已选课程卡片 -->
                <div
                  v-if="hasCourseAt(d, p) && isCourseStart(d, p)"
                  class="sg-course-block"
                  :style="courseBlockStyle(d, p)"
                  @click="onCourseClick(d, p)"
                >
                  <div class="scb-name">{{ getCourseNameAt(d, p) }}</div>
                  <div class="scb-detail">{{ getCourseTeacherAt(d, p) }} · {{ getCourseRoomAt(d, p) }}</div>
                </div>
                <!-- Hover 预占指示 -->
                <div
                  v-else-if="hoverPreview && hoverPreview.day===d && hoverPreview.period===p && hoverPreview.isStart"
                  class="sg-hover-preview"
                  :class="{ 'preview-ok': !hoverPreview.conflict, 'preview-bad': hoverPreview.conflict }"
                  :style="hoverPreview.style"
                >
                  <div class="shp-name">{{ hoverPreview.name }}</div>
                  <div class="shp-tag">{{ hoverPreview.conflict ? '❌ 与已有课程冲突' : '✅ 可预占' }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="sp-legend">
            <span class="legend-dot ok"></span>已选课程
            <span class="legend-dot hover-ok"></span>预占可用
            <span class="legend-dot hover-bad"></span>时间冲突
          </div>
        </div>

        <!-- 5. 选课购物车 / 已选列表 -->
        <div class="cart-panel">
          <div class="cp-title">
            <span>🛒 已选课程购物车（{{ store.selectedCourses.length }} 门）</span>
            <el-switch v-model="viewMode" size="small" active-text="周课表" inactive-text="表格" />
          </div>

          <!-- 表格模式 -->
          <el-table
            v-if="viewMode==='table'"
            :data="store.selectedCourses"
            stripe
            size="small"
            max-height="220"
            style="width:100%"
            empty-text="暂未选课"
          >
            <el-table-column prop="courseCode" label="代码" width="75" />
            <el-table-column prop="courseName" label="课程" width="100" />
            <el-table-column label="性质" width="55">
              <template #default="{row}">
                <el-tag :type="row.classification==='必修'?'danger':'success'" size="small">{{ row.classification }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="teacherName" label="教师" width="65" />
            <el-table-column label="时间" width="120">
              <template #default="{row}">{{ fmtSchedule(row) }}</template>
            </el-table-column>
            <el-table-column prop="classroomName" label="地点" width="70" />
            <el-table-column label="操作" width="55" fixed="right">
              <template #default="{row}">
                <el-button size="small" type="danger" @click="confirmDrop(row)">退选</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 周课表模式 -->
          <div v-else class="cart-weekly">
            <div class="cw-grid">
              <div class="cwg-header"><div class="cwgh-corner"></div><div v-for="d in 7" :key="d" class="cwgh-day" :class="{weekend:d>=6}">{{ weekLabels[d-1] }}</div></div>
              <div v-for="p in 13" :key="p" class="cwg-row" :class="periodZone(p)">
                <div class="cwgr-label">{{ p }}</div>
                <div v-for="d in 7" :key="d" class="cwgr-cell" :class="{weekend:d>=6}">
                  <div
                    v-if="hasCourseAt(d, p) && isCourseStart(d, p)"
                    class="cw-course-dot"
                    :style="{height: courseDotHeight(d, p)}"
                  >
                    <span class="cwd-name">{{ getCourseNameAt(d, p) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 排队模态框 ========== -->
    <el-dialog
      v-model="queueVisible"
      title="选课排队中"
      width="420px"
      :close-on-click-modal="false"
      :show-close="false"
      center
    >
      <div class="queue-modal">
        <div class="qm-icon">⏳</div>
        <div class="qm-text">
          <template v-if="store.queuePosition > 0">
            正在排队中，当前队列位置：
            <strong class="qm-pos">第 {{ store.queuePosition }} 位</strong>
          </template>
          <template v-else-if="store.queuePosition === 0">
            正在处理您的选课请求...
          </template>
        </div>
        <el-progress :percentage="queueProgress" :stroke-width="8" :indeterminate="store.queuePosition===0" />
        <p class="qm-tip">请耐心等待，选课结果将自动通知您</p>
      </div>
    </el-dialog>

    <!-- ========== 退选确认弹框 ========== -->
    <el-dialog
      v-model="dropDialog.visible"
      title="确认退选"
      width="420px"
      center
    >
      <div class="drop-confirm">
        <p class="dc-warn">⚠️ 您确定要退选以下课程吗？</p>
        <div class="dc-course">
          <strong>{{ dropDialog.course?.courseName || dropDialog.course?.course_name }}</strong>
          <span>{{ fmtSchedule(dropDialog.course || {}) }}</span>
        </div>
        <p class="dc-note">退选后名额将立即释放，可能无法重新选入！</p>
      </div>
      <template #footer>
        <el-button @click="dropDialog.visible=false">取消</el-button>
        <el-button type="danger" @click="executeDrop" :loading="dropDialog.loading">确认退选</el-button>
      </template>
    </el-dialog>

    <!-- ========== 选课日志弹框 ========== -->
    <el-dialog v-model="showLog" title="📝 选课操作日志" width="700px" top="5vh">
      <div style="margin-bottom:10px;text-align:right">
        <el-button size="small" @click="store.clearLogs()">清空日志</el-button>
      </div>
      <el-table :data="store.selectionLogs" stripe size="small" max-height="450" empty-text="暂无记录">
        <el-table-column label="时间" width="155">
          <template #default="{row}">{{ row.time }}</template>
        </el-table-column>
        <el-table-column label="操作" width="55">
          <template #default="{row}">
            <el-tag :type="row.action==='选课'?'success':'danger'" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程" width="110" />
        <el-table-column prop="courseCode" label="代码" width="75" />
        <el-table-column prop="teacherName" label="教师" width="65" />
        <el-table-column label="结果" width="55">
          <template #default="{row}">
            <span :style="{color:row.ok?'#059669':'#EF4444'}">{{ row.ok?'✅ 成功':'❌ 失败' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="备注" min-width="140" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { useCourseSelectionStore } from '@/stores/courseSelection.js'

const store = useCourseSelectionStore()

// ==================== 用户类型标签 ====================
const userTypeLabel = computed(() => {
  const t = store.studentInfo?.userType
  return { 1: '学生', 2: '教师', 3: '教务', 4: '管理员' }[t] || '用户'
})

// ==================== 常量 ====================
const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const departments = ['计算机学院', '数学学院', '物理学院', '外语学院', '经管学院', '体育部', '马克思主义学院']
const quickTags = [
  { key: 'required', label: '必修课' },
  { key: 'elective', label: '选修课' },
  { key: 'retake', label: '重修推荐' },
  { key: 'major', label: '本专业推荐' },
]

// ==================== 过滤器 ====================
const filter = reactive({
  keyword: '',
  department: '',
  classification: '',
  quickTag: '',
})

const filteredGroups = computed(() => {
  let groups = store.courseGroups

  if (filter.keyword) {
    const kw = filter.keyword.toLowerCase()
    groups = groups.filter(g => {
      if (g.courseName.toLowerCase().includes(kw)) return true
      if (g.courseCode.toLowerCase().includes(kw)) return true
      if (g.classes.some(c => (c.teacherName||c.teacher_name||'').toLowerCase().includes(kw))) return true
      return false
    })
  }
  if (filter.department) {
    groups = groups.filter(g =>
      g.classes.some(c => (c.department||'').includes(filter.department))
    )
  }
  if (filter.classification) {
    groups = groups.filter(g => g.classification === filter.classification)
  }
  if (filter.quickTag === 'required') {
    groups = groups.filter(g => g.classification === '必修')
  }
  if (filter.quickTag === 'elective') {
    groups = groups.filter(g => g.classification === '选修' || g.classification === '限选')
  }

  return groups
})

const totalClasses = computed(() =>
  filteredGroups.value.reduce((s, g) => s + g.classes.length, 0)
)

// ==================== 折叠面板 ====================
const expandAll = ref(false)
const expandedGroups = ref([])

watch(expandAll, (v) => {
  expandedGroups.value = v ? filteredGroups.value.map(g => g.courseCode) : []
})

watch(filteredGroups, () => {
  if (expandAll.value) {
    expandedGroups.value = filteredGroups.value.map(g => g.courseCode)
  }
})

function onExpandChange() {}

// ==================== 辅助函数 ====================
function fmtSchedule(cls) {
  const wd = cls.weekDay ?? cls.week_day
  const sp = cls.startPeriod ?? cls.start_period
  const ep = cls.endPeriod ?? cls.end_period
  if (!wd) return '时间待定'
  return `周${weekLabels[wd-1]?.charAt(1)||wd} ${sp}-${ep}节`
}

function capacityPct(cls) {
  return Math.round((cls.currentCount || 0) / (cls.maxCapacity || 60) * 100)
}

function isSelected(cls) {
  const cid = cls.courseId || cls.course_id
  return store.selectedCourses.some(s => (s.courseId || s.course_id) === cid)
}

function isSameCodeSelected(cls) {
  if (isSelected(cls)) return false
  const code = cls.courseCode || cls.course_code
  return store.selectedCourses.some(s => (s.courseCode || s.course_code) === code)
}

// ==================== 课表渲染辅助 ====================
function hasCourseAt(day, period) {
  return !!store.getCourseAt(day, period)
}

function isCourseStart(day, period) {
  const c = store.getCourseAt(day, period)
  if (!c) return false
  const sp = c.startPeriod ?? c.start_period
  return sp === period
}

function getCourseNameAt(day, period) {
  return store.getCourseAt(day, period)?.courseName || ''
}

function getCourseTeacherAt(day, period) {
  return store.getCourseAt(day, period)?.teacherName || '-'
}

function getCourseRoomAt(day, period) {
  return store.getCourseAt(day, period)?.classroomName || '-'
}

function courseBlockStyle(day, period) {
  const c = store.getCourseAt(day, period)
  if (!c) return {}
  const sp = c.startPeriod ?? c.start_period
  const ep = c.endPeriod ?? c.end_period
  const span = ep - sp + 1
  return { height: (span * 36 - 2) + 'px' }
}

function courseDotHeight(day, period) {
  const c = store.getCourseAt(day, period)
  if (!c) return 'auto'
  const sp = c.startPeriod ?? c.start_period
  const ep = c.endPeriod ?? c.end_period
  return (ep - sp + 1) * 20 - 2 + 'px'
}

function periodZone(p) {
  if (p <= 5) return 'zone-morning'
  if (p <= 10) return 'zone-afternoon'
  return 'zone-evening'
}

// ==================== Hover 预占计算 ====================
const hoverPreview = computed(() => {
  if (!store.hoverTarget) return null
  const t = store.hoverTarget
  const wd = t.weekDay ?? t.week_day
  const sp = t.startPeriod ?? t.start_period
  const ep = t.endPeriod ?? t.end_period
  const name = t.courseName || t.course_name || '课程'
  if (!wd || !sp || !ep) return null

  const conflict = store.hoverConflict
  const span = ep - sp + 1
  return {
    day: wd,
    period: sp,
    isStart: true,
    name,
    conflict: !!conflict,
    style: { height: (span * 36 - 2) + 'px' },
  }
})

function cellClass(day, period) {
  const classes = []
  if (day >= 6) classes.push('weekend')

  // 如果有已选课程
  if (hasCourseAt(day, period)) classes.push('cell-occupied')

  // hover 预占高亮
  if (hoverPreview.value && hoverPreview.value.day === day) {
    const hp = hoverPreview.value
    const ep = (store.hoverTarget?.endPeriod || store.hoverTarget?.end_period || hp.period)
    if (period >= hp.period && period <= ep) {
      classes.push(hp.conflict ? 'cell-conflict' : 'cell-available')
    }
  }

  return classes
}

function onCellHover(day, period) {}
function onCourseClick(day, period) {
  const c = store.getCourseAt(day, period)
  if (c) confirmDrop(c)
}

// ==================== 选课 / 退选交互 ====================
const queueVisible = ref(false)
const queueProgress = ref(0)
let queueTimer = null

async function handleSelect(cls) {
  // 前置校验
  const validate = store.preValidate(cls)
  if (!validate.ok) {
    ElMessage.warning(validate.msg)
    return
  }

  // 显示排队模态框
  queueVisible.value = true
  queueProgress.value = 0

  // 模拟排队进度
  let pos = Math.floor(Math.random() * 15) + 3
  store.queuePosition = pos
  const timer = setInterval(() => {
    if (pos > 1) {
      pos--
      store.queuePosition = pos
      queueProgress.value = Math.round((1 - pos / (Math.floor(Math.random() * 15) + 3)) * 80)
    }
  }, 400)

  // 执行选课
  const result = await store.doSelect(cls)
  clearInterval(timer)
  store.queuePosition = -1

  if (result.code === 0) {
    queueProgress.value = 100
    setTimeout(() => {
      queueVisible.value = false
      ElMessage.success('选课成功！课表已更新')
    }, 500)
  } else {
    queueVisible.value = false
    ElMessage.error(result.msg || '选课失败')
  }

  // 清理 hover
  store.hoverTarget = null
}

const dropDialog = reactive({
  visible: false,
  course: null,
  loading: false,
})

function confirmDrop(course) {
  dropDialog.course = course
  dropDialog.visible = true
  dropDialog.loading = false
}

async function executeDrop() {
  dropDialog.loading = true
  const result = await store.doDrop(dropDialog.course)
  dropDialog.loading = false
  dropDialog.visible = false
  if (result.code === 0) {
    ElMessage.success('退选成功')
  } else {
    ElMessage.error(result.msg || '退选失败')
  }
}

// ==================== 视图切换 ====================
const viewMode = ref('table')

// ==================== 日志展示 ====================
const showLog = ref(false)

// ==================== 初始化 ====================
onMounted(() => {
  store.loadAll()
})
</script>

<style scoped>
/* ========== 整体布局 ========== */
.cs-console {
  height: calc(100vh - var(--header-height, 64px));
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  overflow: hidden;
}

/* ========== 1. 顶部状态栏 ========== */
.cs-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
  gap: 16px;
  flex-wrap: wrap;
}
.top-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.top-avatar {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  color: #fff; border-radius: 50%; font-weight: 700; font-size: 16px;
}
.top-info { display: flex; flex-direction: column; }
.top-info strong { font-size: 14px; }
.top-info small { font-size: 11px; color: #888; }
.top-center {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  justify-content: center;
}
.credit-bar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.credit-label { font-size: 12px; color: #666; white-space: nowrap; }
.credit-text { font-size: 13px; font-weight: 600; color: #059669; }
.credit-text.text-warn { color: #d97706; }
.credit-text.text-danger { color: #EF4444; }
.top-right { display: flex; gap: 8px; }

/* ========== 主体三栏 ========== */
.cs-body {
  flex: 1;
  display: flex;
  gap: 8px;
  padding: 8px;
  overflow: hidden;
  min-height: 0;
}

/* ========== 左侧面板 ========== */
.cs-left {
  flex: 1;
  min-width: 360px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}

/* 2. 过滤区 */
.filter-panel {
  background: #fff;
  border-radius: 6px;
  padding: 10px 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}
.filter-input { flex: 1; min-width: 180px; }
.quick-tags { display: flex; gap: 6px; flex-wrap: wrap; width: 100%; }

/* 3. 课程列表 */
.course-list-panel {
  flex: 1;
  background: #fff;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
.list-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 4px;
}

/* 课程头 */
.course-header {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding-right: 12px;
}
.ch-icon { font-size: 18px; }
.ch-name { font-weight: 600; font-size: 14px; }
.ch-code { font-size: 12px; color: #888; }
.ch-credit { font-size: 12px; color: #3B82F6; margin-left: auto; }
.ch-count { font-size: 11px; color: #999; }

/* 教学班列表 */
.class-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 4px 0 8px;
}
.class-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  transition: all 0.2s;
  gap: 12px;
}
.class-card:hover { border-color: #3B82F6; background: #f8faff; }
.class-card.class-selected { border-color: #059669; background: #f0fdf4; }
.class-card.class-full { opacity: 0.7; }
.class-card.class-disabled { opacity: 0.6; }

.cc-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
  flex-wrap: wrap;
}
.cc-main {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  font-size: 12px;
  color: #555;
}
.cc-teacher { font-weight: 500; }
.cc-room, .cc-time { color: #777; }
.cc-capacity {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}
.cc-cap-text { font-size: 11px; color: #888; }
.cc-cap-text.text-danger { color: #EF4444; font-weight: 600; }
.cc-actions { flex-shrink: 0; }

/* ========== 右侧面板 ========== */
.cs-right {
  width: 420px;
  min-width: 380px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}

/* 4. 课表面板 */
.schedule-panel {
  flex: 1;
  background: #fff;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}
.sp-title {
  font-size: 13px;
  font-weight: 600;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.sp-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

/* 课表表头 */
.sg-header {
  display: flex;
  background: #f8f9fb;
  position: sticky;
  top: 0;
  z-index: 2;
  flex-shrink: 0;
}
.sgh-corner {
  width: 42px;
  text-align: center;
  padding: 4px 0;
  font-size: 10px;
  color: #888;
  border-right: 1px solid #e4e7ed;
  flex-shrink: 0;
}
.sgh-day {
  flex: 1;
  text-align: center;
  padding: 4px 0;
  font-size: 11px;
  font-weight: 600;
  border-right: 1px solid #e4e7ed;
}
.sgh-day:last-child { border-right: none; }
.sgh-day.weekend { color: #e9a824; }

/* 课表行 */
.sg-row {
  display: flex;
  flex: 1;
}
.sg-row:last-child .sg-cell { border-bottom: none; }
.sgr-label {
  width: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #e4e7ed;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
  position: relative;
}
.period-num {
  font-size: 10px;
  font-weight: 600;
  color: #666;
}
.period-zone-name {
  position: absolute;
  left: 4px;
  font-size: 8px;
  color: #999;
  writing-mode: vertical-rl;
}
.zone-morning .sgr-label { color: #d97706; background: #fffbeb; }
.zone-afternoon .sgr-label { color: #059669; background: #f0fdf4; }
.zone-evening .sgr-label { color: #2563eb; background: #eff6ff; }

/* 课表单元格 */
.sg-cell {
  flex: 1;
  min-height: 34px;
  border-right: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
  position: relative;
  transition: background 0.15s;
}
.sg-cell:last-child { border-right: none; }
.sg-cell.weekend { background: #fffef8; }
.sg-cell.cell-occupied { background: #e8f0fe; }

/* hover 高亮 */
.sg-cell.cell-available { background: #dcfce7 !important; }
.sg-cell.cell-conflict { background: #fee2e2 !important; }

/* 已选课程块 */
.sg-course-block {
  position: absolute;
  top: 1px; left: 1px; right: 1px;
  z-index: 1;
  border-radius: 3px;
  padding: 2px 4px;
  color: #fff;
  font-size: 9px;
  overflow: hidden;
  cursor: pointer;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  transition: filter 0.15s, opacity 0.3s;
}
.sg-course-block:hover { filter: brightness(1.1); }
.scb-name { font-weight: 600; line-height: 1.3; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.scb-detail { opacity: .85; font-size: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* hover 预占 */
.sg-hover-preview {
  position: absolute;
  top: 1px; left: 1px; right: 1px;
  z-index: 1;
  border-radius: 3px;
  padding: 2px 4px;
  font-size: 9px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.sg-hover-preview.preview-ok {
  background: rgba(5, 150, 105, 0.25);
  border: 2px dashed #059669;
}
.sg-hover-preview.preview-bad {
  background: rgba(239, 68, 68, 0.2);
  border: 2px dashed #EF4444;
}
.shp-name { font-weight: 600; color: #333; }
.shp-tag { font-size: 8px; font-weight: 600; }
.preview-ok .shp-tag { color: #059669; }
.preview-bad .shp-tag { color: #EF4444; }

/* 图例 */
.sp-legend {
  display: flex;
  gap: 16px;
  padding: 4px 12px;
  font-size: 11px;
  color: #888;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  align-items: center;
}
.legend-dot {
  width: 10px; height: 10px; border-radius: 2px;
  display: inline-block; margin-right: 3px;
}
.legend-dot.ok { background: linear-gradient(135deg, #3B82F6, #60A5FA); }
.legend-dot.hover-ok { background: rgba(5, 150, 105, 0.25); border: 1.5px dashed #059669; }
.legend-dot.hover-bad { background: rgba(239, 68, 68, 0.2); border: 1.5px dashed #EF4444; }

/* 5. 购物车面板 */
.cart-panel {
  background: #fff;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
  max-height: 300px;
}
.cp-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

/* 购物车周课表 */
.cart-weekly {
  overflow-y: auto;
  max-height: 250px;
  padding: 4px;
}
.cw-grid {
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}
.cwg-header {
  display: flex;
  background: #f8f9fb;
}
.cwgh-corner { width: 28px; flex-shrink: 0; }
.cwgh-day {
  flex: 1; text-align: center; padding: 2px 0;
  font-size: 9px; font-weight: 600; border-right: 1px solid #e4e7ed;
}
.cwgh-day:last-child { border-right: none; }
.cwgh-day.weekend { color: #e9a824; }
.cwg-row { display: flex; }
.cwgr-label {
  width: 28px; display: flex; align-items: center; justify-content: center;
  font-size: 8px; color: #888; border-right: 1px solid #e4e7ed; border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.cwgr-cell {
  flex: 1; min-height: 18px;
  border-right: 1px solid #f5f5f5; border-bottom: 1px solid #f5f5f5;
  position: relative;
}
.cwgr-cell:last-child { border-right: none; }
.cwgr-cell.weekend { background: #fffef8; }
.cw-course-dot {
  position: absolute; top: 1px; left: 1px; right: 1px; z-index: 1;
  background: linear-gradient(135deg, #3B82F6, #60A5FA); color: #fff;
  border-radius: 2px; overflow: hidden;
  display: flex; align-items: center; padding: 0 2px;
}
.cwd-name { font-size: 7px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* ========== 排队模态框 ========== */
.queue-modal { text-align: center; padding: 12px 0; }
.qm-icon { font-size: 48px; margin-bottom: 12px; animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%,100% { opacity:1; transform:scale(1); } 50% { opacity:.6; transform:scale(.95); } }
.qm-text { font-size: 15px; margin-bottom: 16px; color: #555; }
.qm-pos { color: #3B82F6; font-size: 20px; }
.qm-tip { font-size: 12px; color: #999; margin-top: 12px; }

/* ========== 退选确认 ========== */
.drop-confirm { text-align: center; }
.dc-warn { font-size: 15px; color: #d97706; margin-bottom: 12px; }
.dc-course {
  background: #fefce8; border: 1px solid #fde68a; border-radius: 6px;
  padding: 10px 16px; margin-bottom: 12px; display: flex; flex-direction: column; gap: 4px;
}
.dc-course strong { font-size: 16px; }
.dc-course span { font-size: 12px; color: #888; }
.dc-note { font-size: 12px; color: #999; }

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .cs-body { flex-direction: column; }
  .cs-left { flex: 1; min-width: 0; }
  .cs-right { width: 100%; min-width: 0; flex-direction: row; max-height: 50%; }
  .schedule-panel { flex: 1; }
  .cart-panel { width: 50%; max-height: none; }
}
@media (max-width: 768px) {
  .cs-right { flex-direction: column; max-height: none; }
  .cart-panel { width: 100%; }
  .cs-topbar { flex-direction: column; align-items: flex-start; }
  .top-center { justify-content: flex-start; }
}
</style>
