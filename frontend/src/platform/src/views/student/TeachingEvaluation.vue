<template>
  <section class="evaluation-workspace">
    <header class="workspace-header">
      <div>
        <p class="eyebrow">教学质量</p>
        <h1>{{ isResultViewer ? resultTitle : '课程评教' }}</h1>
        <p>{{ isResultViewer ? resultDescription : '对本学期授课教师和本人辅导员进行在线评分与匿名反馈' }}</p>
      </div>
      <div v-if="isStudent" class="privacy-badge"><el-icon><Lock /></el-icon><span>匿名反馈</span></div>
      <el-tooltip v-if="isResultViewer" content="刷新评教结果" placement="top">
        <el-button :icon="Refresh" circle aria-label="刷新评教结果" @click="loadTeacherOverview" />
      </el-tooltip>
    </header>

    <el-result v-if="roleResolved && !isStudent && !isResultViewer" icon="warning" title="当前账号无评教业务权限" />

    <template v-else-if="isStudent">
      <div class="student-summary" aria-label="评教任务概况">
        <div><span>待完成</span><strong>{{ pendingCount }}</strong><small>项评教任务</small></div>
        <div><span>已提交</span><strong>{{ submittedCount }}</strong><small>项匿名评价</small></div>
        <p><el-icon><InfoFilled /></el-icon>评价提交后不可修改，被评人员不会看到评价学生身份。</p>
      </div>

      <div class="surface-panel data-panel">
        <div class="panel-toolbar">
          <div><h2>我的评教任务</h2><span>共 {{ filteredTasks.length }} 项</span></div>
          <div class="toolbar-actions">
            <el-segmented v-model="studentFilter" :options="filterOptions" />
            <el-tooltip content="刷新任务" placement="top">
              <el-button :icon="Refresh" circle aria-label="刷新任务" @click="loadStudentTasks" />
            </el-tooltip>
          </div>
        </div>

        <div v-if="loadError" class="state-block">
          <el-result icon="error" title="评教任务加载失败" sub-title="请检查网络连接后重试">
            <template #extra><el-button type="primary" @click="loadStudentTasks">重新加载</el-button></template>
          </el-result>
        </div>

        <template v-else>
          <el-table v-if="loading || filteredTasks.length > 0" v-loading="loading" :data="filteredTasks" :row-key="taskRowKey" class="desktop-table" @row-click="openTask">
            <el-table-column label="评教项目" min-width="210">
              <template #default="{ row }"><strong>{{ row.courseName }}</strong><small class="cell-subtitle">{{ row.semester }}</small></template>
            </el-table-column>
            <el-table-column label="评教对象" min-width="150">
              <template #default="{ row }">{{ row.teacherName }}<small class="cell-subtitle">{{ row.targetType === 'COUNSELOR' ? '辅导员' : '授课教师' }}</small></template>
            </el-table-column>
            <el-table-column label="综合评分" min-width="180">
              <template #default="{ row }">
                <div v-if="row.status === 'SUBMITTED'" class="score-cell"><el-rate :model-value="Number(row.overallScore)" disabled /><b>{{ formatScore(row.overallScore) }}</b></div>
                <span v-else class="muted-text">待评价</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }"><el-tag :type="row.status === 'SUBMITTED' ? 'success' : 'warning'">{{ row.status === 'SUBMITTED' ? '已提交' : '待完成' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="132" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 'PENDING' && canSubmitEvaluation" link type="primary" :icon="EditPen" @click.stop="openEvaluation(row)">开始评教</el-button>
                <el-button v-else-if="row.status === 'SUBMITTED'" link :icon="View" @click.stop="openTask(row)">查看结果</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-loading="loading" class="mobile-records">
            <button v-for="row in filteredTasks" :key="taskRowKey(row)" type="button" class="mobile-record" @click="openTask(row)">
              <span class="record-top"><strong>{{ row.courseName }}</strong><el-tag :type="row.status === 'SUBMITTED' ? 'success' : 'warning'" size="small">{{ row.status === 'SUBMITTED' ? '已提交' : '待完成' }}</el-tag></span>
              <span>{{ row.teacherName }} · {{ row.semester }}</span>
              <small>{{ row.status === 'SUBMITTED' ? `综合评分 ${formatScore(row.overallScore)}` : (canSubmitEvaluation ? '点击开始匿名评教' : '当前账号仅可查看任务') }}</small>
            </button>
            <el-empty v-if="!loading && filteredTasks.length === 0" class="mobile-empty" :description="emptyTaskText" :image-size="72" />
          </div>
          <el-empty v-if="!loading && filteredTasks.length === 0" class="desktop-empty" :description="emptyTaskText" :image-size="88" />
        </template>
      </div>
    </template>

    <template v-else-if="isResultViewer">
      <div v-if="teacherError" class="surface-panel state-block">
        <el-result icon="error" title="评教结果加载失败" sub-title="请检查网络连接后重试">
          <template #extra><el-button type="primary" @click="loadTeacherOverview">重新加载</el-button></template>
        </el-result>
      </div>

      <template v-else>
        <div v-loading="loading" class="metric-strip">
          <div class="primary-metric"><span>综合评分</span><strong>{{ formatScore(overview.overallAverage) }}</strong><small>满分 5.00</small></div>
          <div><span>{{ overviewScoreLabels.teaching }}</span><strong>{{ formatScore(overview.teachingAverage) }}</strong><el-progress :percentage="scorePercentage(overview.teachingAverage)" :show-text="false" /></div>
          <div><span>{{ overviewScoreLabels.content }}</span><strong>{{ formatScore(overview.contentAverage) }}</strong><el-progress :percentage="scorePercentage(overview.contentAverage)" :show-text="false" color="#2f7d63" /></div>
          <div><span>{{ overviewScoreLabels.method }}</span><strong>{{ formatScore(overview.methodAverage) }}</strong><el-progress :percentage="scorePercentage(overview.methodAverage)" :show-text="false" color="#c38223" /></div>
          <div class="response-metric"><span>有效评价</span><strong>{{ overview.responseCount || 0 }}</strong><small>份匿名反馈</small></div>
        </div>

        <div class="surface-panel data-panel">
          <div class="panel-toolbar">
            <div><h2>评教表现</h2><span>按被评人员、项目与学期汇总</span></div>
          </div>

          <el-table v-if="loading || overview.courses.length > 0" v-loading="loading" :data="overview.courses" :row-key="summaryRowKey" class="desktop-table" @row-click="openCourseDetail">
            <el-table-column v-if="!isTeacher" label="被评人员" min-width="140"><template #default="{ row }">{{ row.teacherName }}<small class="cell-subtitle">{{ row.targetType === 'COUNSELOR' ? '辅导员' : '教师' }}</small></template></el-table-column>
            <el-table-column label="评教项目" min-width="210">
              <template #default="{ row }"><strong>{{ row.courseName }}</strong><small class="cell-subtitle">{{ row.semester }}</small></template>
            </el-table-column>
            <el-table-column label="综合得分" min-width="210">
              <template #default="{ row }"><div class="score-cell"><el-rate :model-value="Number(row.overallAverage)" disabled /><b>{{ formatScore(row.overallAverage) }}</b></div></template>
            </el-table-column>
            <el-table-column label="态度 / 内容 / 方法" min-width="210">
              <template #default="{ row }">{{ formatScore(row.teachingAverage) }} / {{ formatScore(row.contentAverage) }} / {{ formatScore(row.methodAverage) }}</template>
            </el-table-column>
            <el-table-column label="评价数" width="100">
              <template #default="{ row }">{{ row.responseCount }} 份</template>
            </el-table-column>
            <el-table-column label="操作" width="105" fixed="right">
              <template #default="{ row }"><el-button link :icon="View" @click.stop="openCourseDetail(row)">查看</el-button></template>
            </el-table-column>
          </el-table>

          <div v-loading="loading" class="mobile-records">
            <button v-for="row in overview.courses" :key="summaryRowKey(row)" type="button" class="mobile-record" @click="openCourseDetail(row)">
              <span class="record-top"><strong>{{ row.courseName }}</strong><b class="mobile-score">{{ formatScore(row.overallAverage) }}</b></span>
              <span>{{ row.semester }} · {{ row.responseCount }} 份评价</span>
              <small>态度 {{ formatScore(row.teachingAverage) }} · 内容 {{ formatScore(row.contentAverage) }} · 方法 {{ formatScore(row.methodAverage) }}</small>
            </button>
            <el-empty v-if="!loading && overview.courses.length === 0" class="mobile-empty" description="暂时还没有收到评教反馈" :image-size="72" />
          </div>
          <el-empty v-if="!loading && overview.courses.length === 0" class="desktop-empty" description="暂时还没有收到评教反馈" :image-size="88" />
        </div>
      </template>
    </template>

    <el-dialog v-model="evaluationOpen" title="提交匿名评教" width="min(580px, calc(100vw - 32px))" destroy-on-close>
      <div v-if="activeTask" class="dialog-target">
        <div><strong>{{ activeTask.courseName }}</strong><span>{{ activeTask.teacherName }} · {{ activeTask.semester }}</span></div>
        <el-tag type="info" effect="plain"><el-icon><Lock /></el-icon>匿名</el-tag>
      </div>
      <el-form ref="evaluationFormRef" :model="evaluationForm" :rules="evaluationRules" label-position="top" class="evaluation-form">
        <el-form-item :label="scoreLabels.teaching" prop="scoreTeaching"><el-rate v-model="evaluationForm.scoreTeaching" show-score score-template="{value} 分" /></el-form-item>
        <el-form-item :label="scoreLabels.content" prop="scoreContent"><el-rate v-model="evaluationForm.scoreContent" show-score score-template="{value} 分" /></el-form-item>
        <el-form-item :label="scoreLabels.method" prop="scoreMethod"><el-rate v-model="evaluationForm.scoreMethod" show-score score-template="{value} 分" /></el-form-item>
        <el-form-item label="意见与建议" prop="comment">
          <el-input v-model="evaluationForm.comment" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="可选，请客观描述课程体验与改进建议" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluationOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEvaluation">确认提交</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="taskDetailOpen" title="我的评教结果" size="min(500px, 100vw)" destroy-on-close>
      <div v-if="activeTask" class="detail-content">
        <div class="detail-heading"><div><small>{{ activeTask.semester }}</small><h2>{{ activeTask.courseName }}</h2><span>{{ activeTask.teacherName }}</span></div><strong>{{ formatScore(activeTask.overallScore) }}</strong></div>
        <dl class="score-details">
          <div><dt>{{ scoreLabels.teaching }}</dt><dd>{{ activeTask.scoreTeaching }}.00</dd></div>
          <div><dt>{{ scoreLabels.content }}</dt><dd>{{ activeTask.scoreContent }}.00</dd></div>
          <div><dt>{{ scoreLabels.method }}</dt><dd>{{ activeTask.scoreMethod }}.00</dd></div>
        </dl>
        <div class="comment-block"><h3>我的匿名反馈</h3><p>{{ activeTask.comment || '未填写文字反馈' }}</p></div>
        <p class="submitted-time">提交于 {{ formatDate(activeTask.submittedAt) }}</p>
      </div>
    </el-drawer>

    <el-drawer v-model="courseDetailOpen" title="课程评教详情" size="min(540px, 100vw)" destroy-on-close>
      <div v-loading="detailLoading" class="detail-content teacher-detail">
        <template v-if="courseDetail">
          <div class="detail-heading"><div><small>{{ courseDetail.semester }}</small><h2>{{ courseDetail.courseName }}</h2><span>{{ courseDetail.responseCount }} 份有效评价</span></div><strong>{{ formatScore(courseDetail.overallAverage) }}</strong></div>
          <dl class="score-details">
            <div><dt>{{ detailScoreLabels.teaching }}</dt><dd>{{ formatScore(courseDetail.teachingAverage) }}</dd></div>
            <div><dt>{{ detailScoreLabels.content }}</dt><dd>{{ formatScore(courseDetail.contentAverage) }}</dd></div>
            <div><dt>{{ detailScoreLabels.method }}</dt><dd>{{ formatScore(courseDetail.methodAverage) }}</dd></div>
          </dl>
          <div class="comments-section">
            <h3>匿名文字反馈 <span>{{ courseDetail.anonymousComments.length }}</span></h3>
            <blockquote v-for="(comment, index) in courseDetail.anonymousComments" :key="index">{{ comment }}</blockquote>
            <el-empty v-if="courseDetail.anonymousComments.length === 0" description="暂无文字反馈" :image-size="70" />
          </div>
        </template>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, inject, reactive, ref, watch } from 'vue'
import { EditPen, InfoFilled, Lock, Refresh, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCounselorEvaluationDetail, getMyCourseEvaluationDetail, getMyEvaluationOverview, listMyEvaluationTasks, submitCounselorEvaluation, submitEvaluation } from '@/api/student.js'

const currentUser = inject('currentUser', ref(null))
const roles = computed(() => new Set(currentUser.value?.roles || []))
const permissions = computed(() => new Set(currentUser.value?.permissions || []))
const roleResolved = computed(() => Boolean(currentUser.value))
const isStudent = computed(() => roles.value.has('STUDENT') && permissions.value.has('evaluation:task:read-self'))
const isTeacher = computed(() => roles.value.has('TEACHER') && permissions.value.has('evaluation:result:read-self'))
const isCounselor = computed(() => roles.value.has('COUNSELOR') && permissions.value.has('evaluation:result:read-counseled'))
const isAcademic = computed(() => roles.value.has('ADMIN') && permissions.value.has('evaluation:result:read-all'))
const isResultViewer = computed(() => isTeacher.value || isCounselor.value || isAcademic.value)
const resultTitle = computed(() => isTeacher.value ? '个人评教结果' : isCounselor.value ? '我的辅导员评教结果' : '全校评教结果')
const resultDescription = computed(() => isTeacher.value ? '查看本人授课课程的评价得分与匿名反馈' : isCounselor.value ? '查看所带学生对本人的匿名评价' : '查看全校辅导员和教师的评教汇总与匿名反馈')
const canSubmitEvaluation = computed(() => permissions.value.has('evaluation:submit-self'))

const tasks = ref([])
const loading = ref(false)
const saving = ref(false)
const loadError = ref(false)
const teacherError = ref(false)
const studentFilter = ref('ALL')
const filterOptions = [{ label: '全部', value: 'ALL' }, { label: '待完成', value: 'PENDING' }, { label: '已提交', value: 'SUBMITTED' }]
const filteredTasks = computed(() => studentFilter.value === 'ALL' ? tasks.value : tasks.value.filter((item) => item.status === studentFilter.value))
const pendingCount = computed(() => tasks.value.filter((item) => item.status === 'PENDING').length)
const submittedCount = computed(() => tasks.value.filter((item) => item.status === 'SUBMITTED').length)
const emptyTaskText = computed(() => ({ ALL: '当前没有可用的评教任务', PENDING: '所有评教任务均已完成', SUBMITTED: '暂未提交评教' }[studentFilter.value]))

const overview = reactive({ responseCount: 0, overallAverage: null, teachingAverage: null, contentAverage: null, methodAverage: null, courses: [] })
const evaluationOpen = ref(false)
const evaluationFormRef = ref()
const activeTask = ref(null)
const evaluationForm = reactive({ scoreTeaching: 0, scoreContent: 0, scoreMethod: 0, comment: '' })
const requiredScore = [{ required: true, type: 'number', min: 1, message: '请完成该项评分', trigger: 'change' }]
const evaluationRules = { scoreTeaching: requiredScore, scoreContent: requiredScore, scoreMethod: requiredScore }
const taskDetailOpen = ref(false)
const courseDetailOpen = ref(false)
const detailLoading = ref(false)
const courseDetail = ref(null)
const taskRowKey = (row) => row.targetType === 'COUNSELOR' ? `counselor-${row.teacherId}-${row.semester}` : `course-${row.selectionId}`
const summaryRowKey = (row) => `${row.targetType}-${row.teacherId}-${row.courseId || 0}-${row.semester}`
const scoreLabels = computed(() => activeTask.value?.targetType === 'COUNSELOR'
  ? { teaching: '工作态度', content: '指导内容', method: '沟通方式' }
  : { teaching: '教学态度', content: '教学内容', method: '教学方法' })
const overviewScoreLabels = computed(() => isCounselor.value
  ? { teaching: '工作态度', content: '指导内容', method: '沟通方式' }
  : isAcademic.value ? { teaching: '态度评分', content: '内容评分', method: '方式评分' }
    : { teaching: '教学态度', content: '教学内容', method: '教学方法' })
const detailScoreLabels = computed(() => courseDetail.value?.targetType === 'COUNSELOR'
  ? { teaching: '工作态度', content: '指导内容', method: '沟通方式' }
  : { teaching: '教学态度', content: '教学内容', method: '教学方法' })

const formatScore = (value) => value === null || value === undefined ? '--' : Number(value).toFixed(2)
const scorePercentage = (value) => value ? Math.round(Number(value) * 20) : 0
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '--'

const loadStudentTasks = async () => {
  loading.value = true
  loadError.value = false
  try {
    const result = await listMyEvaluationTasks()
    tasks.value = result.data || []
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

const loadTeacherOverview = async () => {
  loading.value = true
  teacherError.value = false
  try {
    const result = await getMyEvaluationOverview()
    Object.assign(overview, result.data || {}, { courses: result.data?.courses || [] })
  } catch {
    teacherError.value = true
  } finally {
    loading.value = false
  }
}

const openEvaluation = (task) => {
  activeTask.value = task
  Object.assign(evaluationForm, { scoreTeaching: 0, scoreContent: 0, scoreMethod: 0, comment: '' })
  evaluationOpen.value = true
}

const openTask = (task) => {
  if (task.status === 'PENDING') {
    if (canSubmitEvaluation.value) openEvaluation(task)
    else ElMessage.warning('当前账号没有提交评教权限')
    return
  }
  activeTask.value = task
  taskDetailOpen.value = true
}

const saveEvaluation = async () => {
  await evaluationFormRef.value.validate()
  await ElMessageBox.confirm('提交后不能修改或撤回，确认提交这份匿名评价？', '确认提交', { confirmButtonText: '提交评价', cancelButtonText: '继续填写', type: 'warning' })
  saving.value = true
  try {
    const payload = { ...evaluationForm, comment: evaluationForm.comment.trim() || null }
    if (activeTask.value.targetType === 'COUNSELOR') await submitCounselorEvaluation(payload)
    else await submitEvaluation(activeTask.value.selectionId, payload)
    ElMessage.success('匿名评教已提交')
    evaluationOpen.value = false
    await loadStudentTasks()
  } finally {
    saving.value = false
  }
}

const openCourseDetail = async (course) => {
  courseDetailOpen.value = true
  courseDetail.value = null
  detailLoading.value = true
  try {
    const result = course.targetType === 'COUNSELOR'
      ? await getCounselorEvaluationDetail(course.teacherId, course.semester)
      : await getMyCourseEvaluationDetail(course.courseId, course.semester, course.teacherId)
    courseDetail.value = { ...result.data, targetType: course.targetType, anonymousComments: result.data?.anonymousComments || [] }
  } catch {
    courseDetailOpen.value = false
  } finally {
    detailLoading.value = false
  }
}

watch([isStudent, isResultViewer], ([student, viewer]) => {
  if (student) loadStudentTasks()
  else if (viewer) loadTeacherOverview()
}, { immediate: true })
</script>

<style scoped>
.evaluation-workspace {
  display: grid;
  width: min(1180px, calc(100% - 56px));
  margin: 0 auto;
  padding: 32px 0 48px;
  gap: 18px;
  color: var(--color-text-primary);
}
.workspace-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 6px;
}
.workspace-header h1 {
  margin: 2px 0 4px;
  font-size: 26px;
  font-weight: 650;
  letter-spacing: 0;
}
.workspace-header p { margin: 0; color: var(--color-text-secondary); }
.workspace-header .eyebrow { color: var(--color-brand-600); font-size: 12px; font-weight: 700; }
.privacy-badge {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 9px 12px;
  color: var(--color-success);
  font-size: 14px;
  font-weight: 600;
  background: var(--color-brand-50);
  border: 1px solid var(--color-border);
  border-radius: 6px;
}
.student-summary {
  display: grid;
  grid-template-columns: 170px 170px minmax(0, 1fr);
  align-items: center;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}
.student-summary > div {
  display: grid;
  grid-template-columns: auto auto;
  align-items: baseline;
  padding: 18px 22px;
  column-gap: 8px;
  border-right: 1px solid var(--color-border-light);
}
.student-summary span { grid-column: 1 / -1; color: var(--color-text-secondary); font-size: 13px; }
.student-summary strong { font-size: 28px; }
.student-summary small { color: var(--color-text-tertiary); }
.student-summary p {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 18px 24px;
  color: var(--color-text-secondary);
  font-size: 14px;
}
.surface-panel {
  min-width: 0;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}
.data-panel { min-height: 360px; padding: 0 22px 20px; }
.panel-toolbar {
  display: flex;
  min-height: 68px;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}
.panel-toolbar > div:first-child { display: flex; align-items: baseline; gap: 10px; }
.panel-toolbar h2 { margin: 0; font-size: 17px; font-weight: 600; letter-spacing: 0; }
.panel-toolbar span { color: var(--color-text-tertiary); font-size: 13px; }
.toolbar-actions { display: flex; align-items: center; gap: 10px; }
.desktop-table { width: 100%; }
.desktop-table :deep(.el-table__row) { cursor: pointer; }
.desktop-table :deep(th.el-table__cell) {
  color: var(--color-text-secondary);
  background: #f8fafb;
  font-weight: 600;
}
.cell-subtitle { display: block; margin-top: 2px; color: var(--color-text-tertiary); }
.score-cell { display: flex; align-items: center; gap: 8px; }
.score-cell b { color: var(--color-warning); font-variant-numeric: tabular-nums; }
.muted-text { color: var(--color-text-tertiary); }
.desktop-empty { padding: 44px 0; }
.mobile-records { display: none; }
.state-block { min-height: 360px; place-items: center; }
.metric-strip {
  display: grid;
  grid-template-columns: 1.18fr repeat(3, 1fr) 1fr;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}
.metric-strip > div {
  min-width: 0;
  padding: 18px 20px;
  border-right: 1px solid var(--color-border-light);
}
.metric-strip > div:last-child { border-right: 0; }
.metric-strip span { display: block; color: var(--color-text-secondary); font-size: 13px; }
.metric-strip strong { display: block; margin: 5px 0 8px; font-size: 25px; font-variant-numeric: tabular-nums; }
.metric-strip small { color: var(--color-text-tertiary); }
.metric-strip .primary-metric { background: var(--color-brand-50); }
.metric-strip .primary-metric strong { color: var(--color-success); font-size: 34px; }
.mobile-score { color: var(--color-warning); font-size: 20px; }
.dialog-target {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding: 14px 16px;
  background: var(--color-brand-50);
  border-radius: 6px;
}
.dialog-target strong, .dialog-target span { display: block; }
.dialog-target span { margin-top: 4px; color: var(--color-text-secondary); font-size: 13px; }
.dialog-target .el-tag { display: flex; gap: 4px; }
.evaluation-form :deep(.el-form-item__content) { min-height: 32px; }
.evaluation-form :deep(.el-rate) { height: 32px; }
.detail-content { display: grid; gap: 22px; }
.detail-heading {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border-light);
}
.detail-heading small, .detail-heading span { color: var(--color-text-tertiary); }
.detail-heading h2 { margin: 5px 0; font-size: 22px; letter-spacing: 0; }
.detail-heading > strong { color: var(--color-warning); font-size: 36px; font-variant-numeric: tabular-nums; }
.score-details { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin: 0; }
.score-details div { padding: 14px; background: #f5f7f6; border-radius: 6px; }
.score-details dt { color: var(--color-text-secondary); font-size: 13px; }
.score-details dd { margin: 5px 0 0; font-size: 21px; font-weight: 700; }
.comment-block { padding: 16px; background: var(--color-brand-50); border-left: 3px solid var(--color-success); }
.comment-block h3, .comments-section h3 { margin: 0 0 10px; font-size: 15px; }
.comment-block p { margin: 0; line-height: 1.7; white-space: pre-wrap; }
.submitted-time { margin: 0; color: var(--color-text-tertiary); font-size: 13px; }
.comments-section h3 span { color: var(--color-text-tertiary); font-weight: 400; }
.comments-section blockquote {
  margin: 0 0 10px;
  padding: 14px 16px;
  line-height: 1.65;
  white-space: pre-wrap;
  border: 1px solid var(--color-border-light);
  border-left: 3px solid var(--color-warning);
  border-radius: 4px;
}
.teacher-detail { min-height: 220px; }

@media (max-width: 991px) {
  .evaluation-workspace { width: calc(100% - 40px); }
  .metric-strip { grid-template-columns: repeat(2, 1fr); }
  .metric-strip > div { border-bottom: 1px solid var(--color-border-light); }
  .metric-strip > div:nth-child(2n) { border-right: 0; }
  .metric-strip .response-metric { grid-column: 1 / -1; border-bottom: 0; }
  .student-summary { grid-template-columns: 1fr 1fr; }
  .student-summary p { grid-column: 1 / -1; border-top: 1px solid var(--color-border-light); }
}

@media (max-width: 767px) {
  .evaluation-workspace { width: calc(100% - 24px); padding: 22px 0 36px; gap: 14px; }
  .workspace-header { align-items: flex-start; margin-bottom: 4px; }
  .workspace-header h1 { font-size: 21px; }
  .workspace-header > div:first-child p:last-child { font-size: 13px; }
  .privacy-badge { flex: 0 0 auto; }
  .privacy-badge span { display: none; }
  .student-summary > div { padding: 14px 16px; }
  .student-summary p { padding: 14px 16px; }
  .data-panel { padding: 0; background: transparent; border: 0; }
  .panel-toolbar { min-height: 60px; }
  .panel-toolbar > div:first-child { display: block; }
  .toolbar-actions { align-items: flex-end; flex-direction: column; }
  .toolbar-actions :deep(.el-segmented) { max-width: 240px; }
  .desktop-table, .desktop-empty { display: none; }
  .mobile-records { display: flex; min-height: 160px; flex-direction: column; gap: 8px; }
  .mobile-record {
    display: flex;
    width: 100%;
    flex-direction: column;
    gap: 4px;
    padding: 14px;
    color: var(--color-text-secondary);
    text-align: left;
    background: #fff;
    border: 1px solid var(--color-border-light);
    border-radius: 6px;
  }
  .record-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 10px; color: var(--color-text-primary); }
  .record-top strong { min-width: 0; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .record-top .el-tag { flex: 0 0 auto; }
  .mobile-record small { color: var(--color-text-tertiary); }
  .mobile-empty { width: 100%; background: #fff; border: 1px solid var(--color-border-light); border-radius: 6px; }
  .metric-strip > div { padding: 14px; }
  .metric-strip strong { font-size: 22px; }
  .metric-strip .primary-metric strong { font-size: 29px; }
  .score-details { grid-template-columns: 1fr; }
  .detail-heading > strong { font-size: 30px; }
  .evaluation-form :deep(.el-rate__icon) { margin-right: 7px; font-size: 24px; }
}
</style>
