<template>
  <div class="d-page sm-console">
    <PageBreadcrumb domain="teaching" title="成绩评定与预警" />
    <!-- ==================== 顶部标题 ==================== -->
    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <h1>成绩评定与预警</h1>
        <p class="d-head-desc">成绩录入、绩点计算与学业预警处理</p>
      </div>
    </header>
    <!-- ==================== 操作栏 ==================== -->
    <div class="d-toolbar">
      <div class="top-left">
        <span class="role-badge" :class="store.role">{{ roleLabel }}</span>
        <span class="top-user">{{ store.currentUser?.username || '-' }}</span>
        <el-tag size="small" effect="plain" type="info">学期: {{ store.semester }}</el-tag>
      </div>
      <div class="top-center">
        <el-tag size="small" :type="timePhaseType">
          第 {{ store.timeWindow.currentWeek }} 周 ·
          {{ timePhaseLabel }}
        </el-tag>
        <el-tag v-if="store.timeWindow.teacherCanEdit" size="small" type="success" effect="dark">📝 教师可录入</el-tag>
        <el-tag v-if="store.timeWindow.adminOnly" size="small" type="danger" effect="dark">🔒 已归档（仅管理员可修改）</el-tag>
      </div>
      <div class="top-right">
        <el-button size="small" :icon="Refresh" @click="refreshAll" :loading="store.loading">刷新</el-button>
      </div>
    </div>

    <!-- ==================== 角色视图路由 ==================== -->
    <div class="sm-body">

      <!-- ========== 学生视图 ========== -->
      <template v-if="store.role === 'student'">
        <div class="view-student">
          <!-- GPA 概览卡片 -->
          <div class="gpa-overview">
            <div class="gpa-main-card">
              <div class="gpa-big">{{ store.studentGpa }}</div>
              <div class="gpa-label">学期 GPA</div>
              <div class="gpa-max">/ 4.0</div>
            </div>
            <div class="stat-cards">
              <div class="stat-card pass">
                <span class="sc-num">{{ store.studentReport?.pass || 0 }}</span>
                <span class="sc-label">已通过</span>
              </div>
              <div class="stat-card fail">
                <span class="sc-num">{{ store.studentReport?.fail || 0 }}</span>
                <span class="sc-label">未通过</span>
              </div>
              <div class="stat-card credit">
                <span class="sc-num">{{ store.studentReport?.totalCredits || 0 }}</span>
                <span class="sc-label">已修学分</span>
              </div>
            </div>
            <div class="warning-card" :class="'wl-' + store.warningLevel.level">
              <span>{{ store.warningLevel.label }}</span>
              <small>{{ store.warningLevel.desc }}</small>
              <small v-if="store.warningLevel.level !== 'none'">
                当前学期不及格: {{ store.currentFailCredits }} 学分 | 累计: {{ store.totalFailCredits }} 学分
              </small>
            </div>
          </div>

          <!-- 成绩列表 -->
          <div class="score-section">
            <div class="section-title">
              📋 成绩单
              <span class="section-hint" v-if="store.timeWindow.studentCanReview">
                💡 第20周核对期：如对成绩有异议，可申请复核
              </span>
            </div>
            <el-table :data="store.studentScores" stripe size="small" max-height="calc(100vh - 340px)" empty-text="暂无成绩数据">
              <el-table-column type="index" width="40" />
              <el-table-column prop="courseCode" label="课程代码" width="90" />
              <el-table-column prop="courseName" label="课程名称" width="130" />
              <el-table-column label="性质" width="60">
                <template #default="{row}">
                  <el-tag :type="row.classification==='必修'?'danger':'success'" size="small">{{ row.classification }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="credit" label="学分" width="50" />
              <el-table-column label="平时" width="55">
                <template #default="{row}">{{ row.regularScore ?? '-' }}</template>
              </el-table-column>
              <el-table-column label="期末" width="55">
                <template #default="{row}">{{ row.examScore ?? '-' }}</template>
              </el-table-column>
              <el-table-column label="总评" width="60">
                <template #default="{row}">
                  <span :class="{'fail-score': row.scoreScore < 60}">{{ row.scoreScore ?? '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="绩点" width="55">
                <template #default="{row}">{{ row.gpa ?? '-' }}</template>
              </el-table-column>
              <el-table-column label="结果" width="55">
                <template #default="{row}">
                  <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'通过':'未通过' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="学期" width="100" prop="semester" />
              <el-table-column label="操作" width="70" v-if="store.timeWindow.studentCanReview">
                <template #default="{row}">
                  <el-button v-if="row.scoreScore < 60" size="small" type="warning" @click="applyReview(row)">复核</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </template>

      <!-- ========== 教师视图 ========== -->
      <template v-if="store.role === 'teacher'">
        <div class="view-teacher">
          <!-- 左侧：教学班列表 -->
          <div class="teacher-left">
            <div class="section-title">📚 我的教学班</div>
            <div class="class-list" v-loading="store.loading">
              <div
                v-for="cls in store.teacherClasses"
                :key="cls.schedule_id"
                class="class-item"
                :class="{ active: store.selectedClass?.schedule_id === cls.schedule_id }"
                @click="selectTeacherClass(cls)"
              >
                <strong>{{ cls.course_name }}</strong>
                <small>{{ cls.course_code }} · {{ cls.classification }}</small>
                <span class="ci-meta">
                  已选 {{ cls.enrolled_count || 0 }}/{{ cls.max_capacity || '-' }} ·
                  已录 {{ cls.graded_count || 0 }} ·
                  <el-tag v-if="(cls.draft_count||0) > 0" size="small" type="warning" effect="plain">{{ cls.draft_count }} 草稿</el-tag>
                </span>
              </div>
              <el-empty v-if="store.teacherClasses.length===0" description="暂无教学班" :image-size="60" />
            </div>
          </div>

          <!-- 右侧：成绩录入 -->
          <div class="teacher-right">
            <div class="section-title" v-if="store.selectedClass">
              📝 {{ store.selectedClass.course_name }} · 成绩录入
              <span class="ci-subtitle">{{ store.selectedClass.course_code }} · {{ store.selectedClass.classification }} · {{ store.selectedClass.credit }}学分</span>
            </div>

            <div v-if="!store.selectedClass" class="empty-hint">
              <el-empty description="请从左侧选择一个教学班" :image-size="80" />
            </div>

            <div v-else class="score-input-area">
              <!-- 工具栏 -->
              <div class="input-toolbar">
                <div class="toolbar-info">
                  已录: {{ store.classStats.published }} 已发布 ·
                  <span v-if="store.classStats.draft>0" style="color:#d97706">{{ store.classStats.draft }} 草稿</span>
                  <span v-else>0 草稿</span> ·
                  通过率: {{ store.classStats.total ? Math.round(store.classStats.pass/store.classStats.total*100) : 0 }}%
                </div>
                <div class="toolbar-actions">
                  <el-button
                    v-if="store.timeWindow.teacherCanEdit"
                    size="small"
                    type="primary"
                    @click="publishAll"
                    :disabled="store.classStats.draft === 0"
                  >
                    一键发布 ({{ store.classStats.draft }})
                  </el-button>
                </div>
              </div>

              <!-- 成绩表格 -->
              <el-table :data="store.currentClassScores" stripe size="small" max-height="calc(100vh - 380px)">
                <el-table-column type="index" width="35" />
                <el-table-column prop="studentName" label="姓名" width="80" />
                <el-table-column prop="studentNo" label="学号" width="90" />
                <el-table-column label="平时成绩" width="110">
                  <template #default="{row}">
                    <el-input-number
                      v-if="store.timeWindow.teacherCanEdit && row.publishStatus !== 2"
                      v-model="row.regularScore"
                      :min="0" :max="100" size="small"
                      controls-position="right"
                      style="width:90px"
                      @change="onScoreChange(row)"
                    />
                    <span v-else>{{ row.regularScore ?? '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="期末成绩" width="110">
                  <template #default="{row}">
                    <el-input-number
                      v-if="store.timeWindow.teacherCanEdit && row.publishStatus !== 2"
                      v-model="row.examScore"
                      :min="0" :max="100" size="small"
                      controls-position="right"
                      style="width:90px"
                      @change="onScoreChange(row)"
                    />
                    <span v-else>{{ row.examScore ?? '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="总评" width="65">
                  <template #default="{row}">
                    <span :class="{'fail-score': row.scoreScore != null && row.scoreScore < 60}">
                      {{ row.scoreScore ?? '-' }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="绩点" width="55">
                  <template #default="{row}">{{ row.gpa ?? '-' }}</template>
                </el-table-column>
                <el-table-column label="状态" width="70">
                  <template #default="{row}">
                    <el-tag v-if="row.publishStatus===1" type="warning" size="small">草稿</el-tag>
                    <el-tag v-else-if="row.publishStatus===2" type="success" size="small">已发布</el-tag>
                    <el-tag v-else type="info" size="small">待录入</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="110" fixed="right">
                  <template #default="{row}">
                    <template v-if="store.timeWindow.teacherCanEdit && row.publishStatus !== 2">
                      <el-button size="small" type="primary" link @click="saveRowAsDraft(row)">暂存</el-button>
                      <el-button size="small" type="success" link @click="publishRow(row)">发布</el-button>
                    </template>
                    <span v-else-if="row.publishStatus===2" style="font-size:11px;color:#888">已发布</span>
                  </template>
                </el-table-column>
              </el-table>

              <el-empty v-if="store.currentClassScores.length===0" description="该班暂无成绩数据" :image-size="60" style="margin-top:40px" />
            </div>
          </div>
        </div>
      </template>

      <!-- ========== 辅导员视图 ========== -->
      <template v-if="store.role === 'counselor'">
        <div class="view-counselor">
          <!-- 左侧：预警面板 -->
          <div class="counselor-left">
            <div class="section-title">⚠️ 学业预警看板</div>

            <!-- 统计摘要 -->
            <div class="warning-summary">
              <div class="ws-card red">
                <span class="wsc-num">{{ redCount }}</span>
                <span class="wsc-label">🔴 红色预警</span>
                <small>退学高危</small>
              </div>
              <div class="ws-card yellow">
                <span class="wsc-num">{{ yellowCount }}</span>
                <span class="wsc-label">🟡 黄色预警</span>
                <small>轻度警示</small>
              </div>
            </div>

            <!-- 预警列表 -->
            <div class="warning-list" v-loading="store.loading">
              <div
                v-for="w in store.warningList"
                :key="w.student_id"
                class="warning-item"
                :class="{ 'wl-red': w.failCredits >= 20 || w.currentFailCredits >= 15, 'wl-yellow': w.failCredits < 20 && w.currentFailCredits < 15 }"
                @click="viewStudentProfile(w.student_id)"
              >
                <div class="wi-left">
                  <span :class="w.failCredits >= 20 || w.currentFailCredits >= 15 ? 'dot-red' : 'dot-yellow'"></span>
                  <div>
                    <strong>{{ w.studentName }}</strong>
                    <small>{{ w.studentNo }}</small>
                  </div>
                </div>
                <div class="wi-right">
                  <span>累计 {{ w.failCredits }} 学分</span>
                  <span>本学期 {{ w.currentFailCredits }} 学分</span>
                  <el-tag :type="w.failCredits >= 20 || w.currentFailCredits >= 15 ? 'danger' : 'warning'" size="small">
                    {{ w.failCredits >= 20 || w.currentFailCredits >= 15 ? '红牌' : '黄牌' }}
                  </el-tag>
                </div>
              </div>
              <el-empty v-if="store.warningList.length===0" description="暂无学业预警" :image-size="60" />
            </div>
          </div>

          <!-- 右侧：学生画像 / 帮扶记录 -->
          <div class="counselor-right">
            <template v-if="!store.selectedStudentProfile">
              <div class="empty-hint">
                <el-empty description="点击左侧学生查看成绩画像与帮扶记录" :image-size="80" />
              </div>
            </template>
            <template v-else>
              <div class="section-title">
                📊 {{ store.selectedStudentProfile.studentName || '学生' }} · 成绩画像
              </div>
              <div class="profile-content">
                <div class="profile-stats">
                  <div class="ps-item">
                    <span>累计不及格学分</span>
                    <strong :class="store.selectedStudentProfile.totalFailCredits >= 20 ? 'text-danger' : ''">
                      {{ store.selectedStudentProfile.totalFailCredits || 0 }}
                    </strong>
                  </div>
                  <div class="ps-item">
                    <span>本学期不及格学分</span>
                    <strong :class="store.selectedStudentProfile.currentSemesterFailCredits >= 15 ? 'text-danger' : ''">
                      {{ store.selectedStudentProfile.currentSemesterFailCredits || 0 }}
                    </strong>
                  </div>
                  <div class="ps-item">
                    <span>预警等级</span>
                    <strong>{{ store.selectedStudentProfile.warningLevel === 'red' ? '🔴 红牌' : store.selectedStudentProfile.warningLevel === 'yellow' ? '🟡 黄牌' : '无' }}</strong>
                  </div>
                </div>

                <!-- 成绩列表 -->
                <el-table
                  :data="store.selectedStudentProfile.allScores || []"
                  stripe size="small" max-height="300"
                  style="margin-top:12px"
                >
                  <el-table-column prop="courseName" label="课程" width="110" />
                  <el-table-column label="分类" width="55">
                    <template #default="{row}">
                      <el-tag :type="row.classification==='必修'?'danger':'success'" size="small">{{ row.classification }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="scoreScore" label="总评" width="55" />
                  <el-table-column label="结果" width="55">
                    <template #default="{row}">
                      <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'通过':'未通过' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="semester" label="学期" width="100" />
                  <el-table-column prop="publishStatus" label="状态" width="65">
                    <template #default="{row}">
                      {{ row.publishStatus===1?'草稿':row.publishStatus===2?'已发布':'已归档' }}
                    </template>
                  </el-table-column>
                </el-table>

                <!-- 帮扶记录 -->
                <div class="intervention-section">
                  <div class="section-title">📝 帮扶约谈记录</div>
                  <el-input
                    v-model="interventionNote"
                    type="textarea" :rows="3"
                    placeholder="记录谈话时间、谈话内容、帮扶人、家校联系情况..."
                    style="margin-bottom:8px"
                  />
                  <el-button type="primary" size="small" @click="saveIntervention">保存记录</el-button>
                </div>
              </div>
            </template>
          </div>
        </div>
      </template>

      <!-- ========== 管理员视图 ========== -->
      <template v-if="store.role === 'admin'">
        <div class="view-admin">
          <div class="section-title">🔧 成绩例外修改（归档期管理）</div>
          <div class="admin-warn" v-if="!store.timeWindow.adminOnly">
            ⚠️ 当前为第 {{ store.timeWindow.currentWeek }} 周，尚在常规修改期内。教师可直接修改。管理员特权修改应在第21周后使用。
          </div>

          <div class="admin-form">
            <el-form label-width="100px" size="default">
              <el-form-item label="成绩记录ID">
                <el-input-number v-model="adminForm.scoreId" :min="1" style="width:200px" placeholder="输入 score_id" />
                <el-button size="small" style="margin-left:8px" @click="searchScore" :loading="store.loading">查询</el-button>
              </el-form-item>
              <el-form-item label="当前分数">
                <span>{{ adminForm.currentScore ?? '-' }}</span>
              </el-form-item>
              <el-form-item label="新分数">
                <el-input-number v-model="adminForm.newScore" :min="0" :max="100" style="width:150px" />
              </el-form-item>
              <el-form-item label="修改缘由">
                <el-input v-model="adminForm.reason" type="textarea" :rows="2" placeholder="修改的具体原因" />
              </el-form-item>
              <el-form-item label="公文号">
                <el-input v-model="adminForm.docNo" placeholder="审批单号（如 SP-2026-001）" style="width:250px" />
              </el-form-item>
              <el-form-item>
                <el-button type="danger" @click="executeAdminModify" :disabled="!adminForm.scoreId || !adminForm.newScore">
                  提交修改（记录永久日志）
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </template>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useScoreStore } from '@/stores/scoreManagement.js'
import PageBreadcrumb from '@/components/business/PageBreadcrumb.vue'
import './teaching-d.css'

const store = useScoreStore()

// ==================== 角色标签 ====================
const roleLabel = computed(() => ({
  student: '🧑‍🎓 学生', teacher: '👨‍🏫 教师',
  counselor: '📋 辅导员', admin: '🔧 管理员', guest: '❓ 访客'
}[store.role]))

const timePhaseLabel = computed(() => ({
  locked: '日常授课期（锁定）', exam: '期末考试期（可录入）',
  buffer: '核对缓冲期', archived: '归档校对期'
}[store.timeWindow.phase] || '未知'))

const timePhaseType = computed(() => ({
  locked: 'info', exam: 'success', buffer: 'warning', archived: 'danger'
}[store.timeWindow.phase] || 'info'))

// ==================== 辅导员 ====================
const redCount = computed(() =>
  store.warningList.filter(w => w.failCredits >= 20 || w.currentFailCredits >= 15).length
)
const yellowCount = computed(() =>
  store.warningList.filter(w => w.failCredits < 20 && w.currentFailCredits < 15).length
)

async function viewStudentProfile(studentId) {
  await store.loadStudentProfile(studentId)
}

const interventionNote = ref('')
function saveIntervention() {
  if (!interventionNote.value.trim()) { ElMessage.warning('请输入记录内容'); return }
  ElMessage.success('帮扶记录已保存')
  interventionNote.value = ''
}

// ==================== 教师 ====================
async function selectTeacherClass(cls) {
  store.selectedClass = cls
  await store.loadClassScores(cls.course_id)
}

function onScoreChange(row) {
  if (row.regularScore != null && row.examScore != null) {
    // 即时预览总评
    const total = store.calcTotal(row.regularScore, row.examScore)
    row.scoreScore = total
    row.gpa = store.scoreToGpa(total)
    row.status = total >= 60 ? 1 : 0
  }
}

async function saveRowAsDraft(row) {
  const data = {
    studentId: row.studentId || row.student_id,
    courseId: row.courseId || row.course_id,
    scheduleId: store.selectedClass?.schedule_id,
    semester: store.semester,
    regularScore: row.regularScore,
    examScore: row.examScore,
    scoreScore: row.scoreScore,
    gpa: row.gpa,
    status: row.status,
    publishStatus: 1,
    teacherId: store.currentUser?.userId,
    regularRatio: 0.3,
    examRatio: 0.7,
  }
  const result = await store.saveDraft(data)
  if (result.ok) {
    ElMessage.success('草稿已保存')
    row.publishStatus = 1
  } else {
    ElMessage.error(result.msg)
  }
}

async function publishRow(row) {
  const data = {
    studentId: row.studentId || row.student_id,
    courseId: row.courseId || row.course_id,
    scheduleId: store.selectedClass?.schedule_id,
    semester: store.semester,
    regularScore: row.regularScore,
    examScore: row.examScore,
    scoreScore: row.scoreScore,
    gpa: row.gpa,
    status: row.status,
    publishStatus: 2,
    teacherId: store.currentUser?.userId,
    regularRatio: 0.3,
    examRatio: 0.7,
  }
  const result = await store.inputScore(data)
  if (result.ok) {
    ElMessage.success('成绩已发布')
    row.publishStatus = 2
  } else {
    ElMessage.error(result.msg)
  }
}

async function publishAll() {
  if (!store.selectedClass) return
  ElMessageBox.confirm(
    `确认一键发布「${store.selectedClass.course_name}」的全部 ${store.classStats.draft} 条草稿成绩吗？发布后学生端可见。`,
    '一键发布', { type: 'warning' }
  ).then(async () => {
    const result = await store.publishClassScores(store.selectedClass.schedule_id)
    if (result.ok) {
      ElMessage.success(`已发布 ${result.data?.published || 0} 条成绩`)
      await store.loadClassScores(store.selectedClass.course_id)
    } else {
      ElMessage.error(result.msg)
    }
  }).catch(() => {})
}

// ==================== 学生 ====================
function applyReview(row) {
  ElMessageBox.confirm(
    `确认对「${row.courseName}」（${row.scoreScore}分）申请成绩复核吗？`,
    '成绩复核申请', { type: 'warning' }
  ).then(() => {
    ElMessage.success('复核申请已提交，请等待教师审核')
  }).catch(() => {})
}

// ==================== 管理员 ====================
const adminForm = reactive({ scoreId: null, currentScore: null, newScore: null, reason: '', docNo: '' })

async function searchScore() {
  ElMessage.info('查询成绩记录功能（需 scoreId）')
}

async function executeAdminModify() {
  if (!adminForm.scoreId || adminForm.newScore == null) {
    ElMessage.warning('请填写完整信息'); return
  }
  ElMessageBox.confirm(
    `确认将记录 ${adminForm.scoreId} 的分数修改为 ${adminForm.newScore}？此操作将记录永久审计日志。`,
    '管理员特权修改', { type: 'danger' }
  ).then(async () => {
    const result = await store.adminModifyScore(
      adminForm.scoreId, adminForm.newScore, adminForm.reason, adminForm.docNo
    )
    if (result.ok) {
      ElMessage.success(`已修改：${result.data?.oldScore} → ${result.data?.newScore}`)
    } else {
      ElMessage.error(result.msg)
    }
  }).catch(() => {})
}

// ==================== 刷新 ====================
async function refreshAll() {
  await store.loadTimeWindow()
  if (store.role === 'student') await store.loadStudentReport()
  else if (store.role === 'teacher') await store.loadTeacherClasses()
  else if (store.role === 'counselor') await store.loadWarnings()
}

// ==================== 初始化 ====================
onMounted(async () => {
  await store.loadTimeWindow()
  if (store.role === 'student') await store.loadStudentReport()
  else if (store.role === 'teacher') await store.loadTeacherClasses()
  else if (store.role === 'counselor') await store.loadWarnings()
})
</script>

<style scoped>
/* ========== 整体 ========== */
.sm-console { display: flex; flex-direction: column; overflow: hidden; }
.sm-topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 16px; flex-shrink: 0; gap: 12px; flex-wrap: wrap;
}
.top-left { display: flex; align-items: center; gap: 10px; }
.role-badge {
  padding: 2px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; color: #fff;
}
.role-badge.student { background: #3B82F6; }
.role-badge.teacher { background: #059669; }
.role-badge.counselor { background: #d97706; }
.role-badge.admin { background: #7c3aed; }
.top-user { font-weight: 600; font-size: 14px; }
.top-center { display: flex; align-items: center; gap: 8px; }
.top-right { display: flex; gap: 8px; }

.sm-body { flex: 1; overflow: hidden; display: flex; flex-direction: column; }

/* ========== 学生视图 ========== */
.view-student { flex: 1; overflow-y: auto; padding: 12px; display: flex; flex-direction: column; gap: 12px; }
.gpa-overview { display: flex; gap: 12px; flex-shrink: 0; }
.gpa-main-card {
  background: linear-gradient(135deg, #3B82F6, #60A5FA); color: #fff;
  border-radius: 8px; padding: 20px 28px; text-align: center; min-width: 120px;
}
.gpa-big { font-size: 36px; font-weight: 700; line-height: 1.2; }
.gpa-label { font-size: 13px; opacity: .85; }
.gpa-max { font-size: 11px; opacity: .65; }
.stat-cards { display: flex; gap: 8px; flex: 1; }
.stat-card {
  flex: 1; background: #fff; border-radius: 8px; padding: 14px 18px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.stat-card.pass { border-left: 3px solid #059669; }
.stat-card.fail { border-left: 3px solid #EF4444; }
.stat-card.credit { border-left: 3px solid #d97706; }
.sc-num { font-size: 28px; font-weight: 700; }
.sc-label { font-size: 11px; color: #888; }
.warning-card {
  border-radius: 8px; padding: 14px 18px; display: flex; flex-direction: column; gap: 2px;
  min-width: 180px;
}
.warning-card.wl-none { background: #f0fdf4; border: 1px solid #86efac; }
.warning-card.wl-yellow { background: #fefce8; border: 1px solid #fde68a; }
.warning-card.wl-red { background: #fef2f2; border: 1px solid #fecaca; }
.warning-card span { font-weight: 600; font-size: 14px; }
.warning-card small { font-size: 11px; color: #555; }
.score-section { flex: 1; overflow: hidden; display: flex; flex-direction: column; background: #fff; border-radius: 6px; padding: 12px; }

.section-title {
  font-size: 14px; font-weight: 600; padding-bottom: 8px; margin-bottom: 8px;
  border-bottom: 1px solid #f0f0f0; display: flex; align-items: center; gap: 12px; flex-shrink: 0;
}
.section-hint { font-size: 11px; color: #d97706; font-weight: 400; }
.ci-subtitle { font-size: 11px; color: #888; font-weight: 400; }
.fail-score { color: #EF4444; font-weight: 600; }
.text-danger { color: #EF4444; }

/* ========== 教师视图 ========== */
.view-teacher { flex: 1; display: flex; gap: 8px; padding: 8px; overflow: hidden; }
.teacher-left { width: 260px; background: #fff; border-radius: 6px; display: flex; flex-direction: column; overflow: hidden; }
.teacher-left .section-title { padding: 10px 12px; margin: 0; }
.class-list { flex: 1; overflow-y: auto; padding: 4px 8px; display: flex; flex-direction: column; gap: 4px; }
.class-item {
  padding: 10px 12px; border: 1px solid #e4e7ed; border-radius: 6px; cursor: pointer;
  transition: all 0.15s; display: flex; flex-direction: column; gap: 2px;
}
.class-item:hover { border-color: #3B82F6; background: #f8faff; }
.class-item.active { border-color: #3B82F6; background: #eff6ff; }
.class-item strong { font-size: 13px; }
.class-item small { font-size: 11px; color: #888; }
.ci-meta { font-size: 11px; color: #666; margin-top: 2px; }

.teacher-right { flex: 1; background: #fff; border-radius: 6px; display: flex; flex-direction: column; overflow: hidden; padding: 0 12px 12px; }
.teacher-right .section-title { padding-top: 10px; }

.empty-hint { flex: 1; display: flex; align-items: center; justify-content: center; }

.score-input-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.input-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; flex-shrink: 0;
}
.toolbar-info { font-size: 12px; color: #666; }

/* ========== 辅导员视图 ========== */
.view-counselor { flex: 1; display: flex; gap: 8px; padding: 8px; overflow: hidden; }
.counselor-left { width: 340px; background: #fff; border-radius: 6px; display: flex; flex-direction: column; overflow: hidden; }
.counselor-left .section-title { padding: 10px 12px; margin: 0; }
.warning-summary { display: flex; gap: 8px; padding: 8px 12px; flex-shrink: 0; }
.ws-card {
  flex: 1; padding: 12px; border-radius: 6px; text-align: center; display: flex; flex-direction: column;
}
.ws-card.red { background: #fef2f2; border: 1px solid #fecaca; }
.ws-card.yellow { background: #fefce8; border: 1px solid #fde68a; }
.wsc-num { font-size: 32px; font-weight: 700; }
.wsc-label { font-size: 12px; font-weight: 600; }
.ws-card small { font-size: 10px; color: #888; }

.warning-list { flex: 1; overflow-y: auto; padding: 4px 8px; display: flex; flex-direction: column; gap: 4px; }
.warning-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 12px; border: 1px solid #e4e7ed; border-radius: 6px; cursor: pointer;
  transition: all 0.15s;
}
.warning-item:hover { border-color: #3B82F6; }
.warning-item.wl-red { border-left: 3px solid #EF4444; }
.warning-item.wl-yellow { border-left: 3px solid #d97706; }
.wi-left { display: flex; align-items: center; gap: 8px; }
.dot-red, .dot-yellow { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.dot-red { background: #EF4444; }
.dot-yellow { background: #d97706; }
.wi-left div { display: flex; flex-direction: column; }
.wi-left strong { font-size: 13px; }
.wi-left small { font-size: 11px; color: #888; }
.wi-right { display: flex; flex-direction: column; align-items: flex-end; font-size: 11px; color: #666; gap: 2px; }

.counselor-right { flex: 1; background: #fff; border-radius: 6px; overflow-y: auto; padding: 0 12px 12px; }
.counselor-right .section-title { padding-top: 10px; }
.profile-content { display: flex; flex-direction: column; }
.profile-stats { display: flex; gap: 12px; margin-bottom: 8px; }
.ps-item { flex: 1; background: #f8f9fb; padding: 12px; border-radius: 6px; text-align: center; }
.ps-item span { font-size: 11px; color: #888; display: block; }
.ps-item strong { font-size: 20px; }
.intervention-section { margin-top: 16px; padding-top: 12px; border-top: 1px solid #f0f0f0; }

/* ========== 管理员视图 ========== */
.view-admin { flex: 1; overflow-y: auto; padding: 12px; background: #fff; margin: 8px; border-radius: 6px; }
.admin-warn { padding: 10px 14px; margin-bottom: 16px; background: #fefce8; border: 1px solid #fde68a; border-radius: 6px; font-size: 13px; }
.admin-form { max-width: 600px; }

/* ========== 响应式 ========== */
@media (max-width: 1100px) {
  .view-teacher { flex-direction: column; }
  .teacher-left { width: 100%; max-height: 200px; }
  .view-counselor { flex-direction: column; }
  .counselor-left { width: 100%; max-height: 300px; }
}
@media (max-width: 640px) {
  .gpa-overview { flex-wrap: wrap; }
  .warning-summary { flex-direction: column; }
  .profile-stats { flex-direction: column; }
}
</style>
