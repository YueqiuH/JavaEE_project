<template>
  <section class="scholarship-workspace">
    <header class="workspace-header">
      <div>
        <p class="eyebrow">学生事务</p>
        <h1>{{ isReviewer ? '奖助贷审核工作台' : '奖助贷申请' }}</h1>
        <p>{{ isReviewer ? (isCounselor ? '完成所带学生申请的辅导员初审' : '完成教务终审并形成最终资助名单') : '提交奖学金、困难补助和生源地助学贷款申请' }}</p>
      </div>
      <el-button v-if="isStudent" type="primary" :icon="DocumentAdd" @click="openCreateDialog">新建申请</el-button>
      <el-button v-if="isAcademic && activeTeacherTab === 'approved'" type="primary" :icon="Finished" :disabled="selectedApproved.length === 0" @click="generateResults">
        生成名单<span v-if="selectedApproved.length">（{{ selectedApproved.length }}）</span>
      </el-button>
    </header>

    <el-result v-if="roleResolved && !isStudent && !isReviewer" icon="warning" title="当前账号无奖助贷业务权限" />

    <template v-else-if="isStudent">
      <div class="summary-strip" aria-label="申请流程">
        <div><span>1</span><strong>填写申请</strong><small>保存草稿并完善材料</small></div>
        <i></i>
        <div><span>2</span><strong>两级审核</strong><small>辅导员初审、教务终审</small></div>
        <i></i>
        <div><span>3</span><strong>结果公示</strong><small>查看最终资助结果</small></div>
      </div>

      <div class="surface-panel data-panel">
        <div class="panel-toolbar">
          <div>
            <h2>我的申请</h2>
            <span>共 {{ total }} 项</span>
          </div>
          <div class="filter-actions">
            <el-select v-model="studentStatus" aria-label="申请状态" placeholder="全部状态" clearable @change="resetAndLoad">
              <el-option v-for="item in statusFilterOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-tooltip content="刷新列表" placement="top">
              <el-button :icon="Refresh" circle aria-label="刷新列表" @click="loadApplications" />
            </el-tooltip>
          </div>
        </div>

        <el-table v-if="loading || applications.length" v-loading="loading" :data="applications" class="desktop-table" row-key="scholarshipId" @row-click="openDetail">
          <el-table-column label="申请编号" min-width="190">
            <template #default="{ row }"><span class="application-no">{{ row.applicationNo }}</span></template>
          </el-table-column>
          <el-table-column prop="title" label="申请事项" min-width="220">
            <template #default="{ row }"><strong>{{ row.title }}</strong><small class="cell-subtitle">{{ typeLabel(row.scholarshipType) }}</small></template>
          </el-table-column>
          <el-table-column label="提交时间" min-width="160">
            <template #default="{ row }">{{ formatDate(row.applyTime || row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }"><el-tag :type="statusMeta(row.status).type" effect="light">{{ statusMeta(row.status).label }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{ row }">
              <div class="row-actions" @click.stop>
                <el-button link :icon="View" @click="openDetail(row)">查看</el-button>
                <el-button v-if="canEdit(row)" link type="primary" :icon="EditPen" @click="openEditDialog(row)">修改</el-button>
                <el-button v-if="canSubmit(row)" link type="primary" :icon="Promotion" @click="submitApplication(row)">提交</el-button>
                <el-button v-if="canWithdraw(row)" link type="danger" :icon="Close" @click="withdrawApplication(row)">撤回</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-loading="loading" class="mobile-records">
          <button v-for="row in applications" :key="row.scholarshipId" type="button" class="mobile-record" @click="openDetail(row)">
            <span class="record-top"><strong>{{ row.title }}</strong><el-tag :type="statusMeta(row.status).type" size="small">{{ statusMeta(row.status).label }}</el-tag></span>
            <span>{{ typeLabel(row.scholarshipType) }} · {{ formatDate(row.applyTime || row.createdAt) }}</span>
            <small>{{ row.applicationNo }}</small>
          </button>
          <el-empty v-if="!loading && applications.length === 0" description="暂无申请记录" :image-size="72" />
        </div>

        <el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadApplications" />
      </div>
    </template>

    <template v-else-if="isReviewer">
      <div class="surface-panel data-panel teacher-panel">
        <el-tabs v-model="activeTeacherTab" class="review-tabs" @tab-change="handleTeacherTabChange">
          <el-tab-pane name="review">
            <template #label><span class="tab-label"><el-icon><List /></el-icon>待评审</span></template>
          </el-tab-pane>
          <el-tab-pane v-if="isAcademic" name="approved">
            <template #label><span class="tab-label"><el-icon><CircleCheck /></el-icon>已通过</span></template>
          </el-tab-pane>
          <el-tab-pane v-if="isAcademic" name="results">
            <template #label><span class="tab-label"><el-icon><Files /></el-icon>资助名单</span></template>
          </el-tab-pane>
        </el-tabs>

        <div class="panel-toolbar teacher-toolbar">
          <div>
            <h2>{{ teacherPanelTitle }}</h2>
            <span>共 {{ total }} 项</span>
          </div>
          <el-tooltip content="刷新列表" placement="top">
            <el-button :icon="Refresh" circle aria-label="刷新列表" @click="loadApplications" />
          </el-tooltip>
        </div>

        <el-table
          v-if="loading || applications.length"
          v-loading="loading"
          :data="applications"
          class="desktop-table"
          row-key="scholarshipId"
          @row-click="openDetail"
          @selection-change="selectedApproved = $event"
        >
          <el-table-column v-if="activeTeacherTab === 'approved'" type="selection" width="48" />
          <el-table-column label="学生" min-width="150">
            <template #default="{ row }"><strong>{{ row.studentName }}</strong><small class="cell-subtitle">{{ row.studentNo }}</small></template>
          </el-table-column>
          <el-table-column prop="title" label="申请事项" min-width="220">
            <template #default="{ row }"><strong>{{ row.title }}</strong><small class="cell-subtitle">{{ typeLabel(row.scholarshipType) }}</small></template>
          </el-table-column>
          <el-table-column label="提交时间" min-width="165">
            <template #default="{ row }">{{ formatDate(row.applyTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }"><el-tag :type="statusMeta(row.status).type">{{ statusMeta(row.status).label }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="155" fixed="right">
            <template #default="{ row }">
              <div class="row-actions" @click.stop>
                <el-button link :icon="View" @click="openDetail(row)">查看</el-button>
                <el-button v-if="activeTeacherTab === 'review'" link type="primary" :icon="EditPen" @click="openReviewDialog(row)">评审</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-loading="loading" class="mobile-records">
          <button v-for="row in applications" :key="row.scholarshipId" type="button" class="mobile-record" @click="openDetail(row)">
            <span class="record-top"><strong>{{ row.studentName }} · {{ row.title }}</strong><el-tag :type="statusMeta(row.status).type" size="small">{{ statusMeta(row.status).label }}</el-tag></span>
            <span>{{ typeLabel(row.scholarshipType) }} · {{ formatDate(row.applyTime) }}</span>
            <small>学号 {{ row.studentNo }}</small>
          </button>
          <el-empty v-if="!loading && applications.length === 0" :description="emptyDescription" :image-size="72" />
        </div>

        <el-empty v-if="!loading && applications.length === 0" class="desktop-empty" :description="emptyDescription" :image-size="88" />
        <el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadApplications" />
      </div>
    </template>

    <el-dialog v-model="applicationDialogOpen" :title="editingApplication ? '修改申请' : '新建申请'" width="min(560px, calc(100vw - 32px))" destroy-on-close>
      <el-form ref="applicationFormRef" :model="applicationForm" :rules="applicationRules" label-position="top">
        <el-form-item label="申请类型" prop="scholarshipType">
          <el-select v-model="applicationForm.scholarshipType" placeholder="请选择申请类型" class="full-width">
            <el-option v-for="item in scholarshipTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请标题" prop="title">
          <el-input v-model="applicationForm.title" maxlength="128" show-word-limit placeholder="例如：2026 学年国家奖学金申请" />
        </el-form-item>
        <el-form-item label="申请理由" prop="reason">
          <el-input v-model="applicationForm.reason" type="textarea" :rows="6" maxlength="2000" show-word-limit placeholder="请说明个人情况、申请依据及相关经历" />
        </el-form-item>
        <el-form-item label="证明材料链接" prop="attachmentUrl">
          <el-input v-model="applicationForm.attachmentUrl" placeholder="https://..." clearable>
            <template #prefix><el-icon><Link /></el-icon></template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applicationDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveApplication">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogOpen" title="提交评审结论" width="min(520px, calc(100vw - 32px))" destroy-on-close>
      <div v-if="reviewingApplication" class="review-target">
        <strong>{{ reviewingApplication.studentName }} · {{ reviewingApplication.title }}</strong>
        <span>{{ typeLabel(reviewingApplication.scholarshipType) }} / {{ reviewingApplication.applicationNo }}</span>
      </div>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-position="top">
        <el-form-item label="评审结论" prop="decision">
          <el-radio-group v-model="reviewForm.decision">
            <el-radio-button value="APPROVE">通过</el-radio-button>
            <el-radio-button value="RETURN">退回修改</el-radio-button>
            <el-radio-button value="REJECT">拒绝</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="评审意见" prop="opinion">
          <el-input v-model="reviewForm.opinion" type="textarea" :rows="4" maxlength="256" show-word-limit placeholder="通过时可选填，退回或拒绝时必填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveReview">确认提交</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailOpen" title="申请详情" size="min(520px, 100vw)" destroy-on-close>
      <div v-if="detail" class="detail-content">
        <div class="detail-title">
          <div><span>{{ typeLabel(detail.scholarshipType) }}</span><h2>{{ detail.title }}</h2><small>{{ detail.applicationNo }}</small></div>
          <el-tag :type="statusMeta(detail.status).type">{{ statusMeta(detail.status).label }}</el-tag>
        </div>
        <dl>
          <template v-if="isReviewer"><dt>申请学生</dt><dd>{{ detail.studentName }}（{{ detail.studentNo }}）</dd></template>
          <dt>提交时间</dt><dd>{{ formatDate(detail.applyTime) }}</dd>
          <dt>申请理由</dt><dd class="reason-text">{{ detail.reason }}</dd>
          <dt>证明材料</dt><dd><a v-if="detail.attachmentUrl" :href="detail.attachmentUrl" target="_blank" rel="noopener">打开材料链接</a><span v-else>未上传</span></dd>
          <template v-if="detail.counselorReviewedAt"><dt>辅导员初审</dt><dd class="review-opinion">{{ detail.counselorOpinion || '通过' }} · {{ detail.counselorName }} · {{ formatDate(detail.counselorReviewedAt) }}</dd></template>
          <template v-if="detail.academicReviewedAt"><dt>教务终审</dt><dd class="review-opinion">{{ detail.academicOpinion || '通过' }} · {{ detail.academicReviewerName }} · {{ formatDate(detail.academicReviewedAt) }}</dd></template>
        </dl>
        <div class="drawer-actions">
          <el-button v-if="isStudent && canEdit(detail)" :icon="EditPen" @click="openEditDialog(detail); detailOpen = false">修改申请</el-button>
          <el-button v-if="isStudent && canSubmit(detail)" type="primary" :icon="Promotion" @click="submitApplication(detail)">提交申请</el-button>
          <el-button v-if="isReviewer && isPendingReview(detail)" type="primary" :icon="EditPen" @click="openReviewDialog(detail); detailOpen = false">开始审核</el-button>
        </div>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, inject, reactive, ref, watch } from 'vue'
import { CircleCheck, Close, DocumentAdd, EditPen, Files, Finished, Link, List, Promotion, Refresh, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createScholarshipApplication,
  generateScholarshipResults,
  getScholarshipApplication,
  listMyScholarshipApplications,
  listScholarshipResults,
  listScholarshipReviews,
  submitScholarshipApplication,
  submitScholarshipReview,
  updateScholarshipApplication,
  withdrawScholarshipApplication,
} from '@/api/student.js'

const currentUser = inject('currentUser', ref(null))
const roles = computed(() => new Set(currentUser.value?.roles || []))
const roleResolved = computed(() => Boolean(currentUser.value))
const isStudent = computed(() => roles.value.has('STUDENT'))
const isCounselor = computed(() => roles.value.has('COUNSELOR'))
const isAcademic = computed(() => roles.value.has('ADMIN'))
const isReviewer = computed(() => isCounselor.value || isAcademic.value)

const scholarshipTypes = [
  { value: 'SCHOLARSHIP', label: '奖学金' },
  { value: 'DIFFICULTY_GRANT', label: '困难补助' },
  { value: 'STUDENT_LOAN', label: '生源地助学贷款' },
]
const statusMap = {
  DRAFT: { label: '草稿', type: 'info' },
  SUBMITTED: { label: '待辅导员初审', type: 'warning' },
  ACADEMIC_REVIEW: { label: '待教务终审', type: 'warning' },
  RETURNED: { label: '已退回', type: 'danger' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '未通过', type: 'danger' },
  WITHDRAWN: { label: '已撤回', type: 'info' },
  SELECTED: { label: '已入选', type: 'success' },
}
const statusFilterOptions = Object.entries(statusMap).map(([value, meta]) => ({ value, label: meta.label }))

const applications = ref([])
const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const studentStatus = ref('')
const activeTeacherTab = ref('review')
const selectedApproved = ref([])
const detailOpen = ref(false)
const detail = ref(null)

const applicationDialogOpen = ref(false)
const applicationFormRef = ref()
const editingApplication = ref(null)
const applicationForm = reactive({ scholarshipType: '', title: '', reason: '', attachmentUrl: '' })
const applicationRules = {
  scholarshipType: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
  title: [{ required: true, message: '请填写申请标题', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写申请理由', trigger: 'blur' }],
  attachmentUrl: [{ type: 'url', message: '请输入完整的 http(s) 链接', trigger: 'blur' }],
}

const reviewDialogOpen = ref(false)
const reviewFormRef = ref()
const reviewingApplication = ref(null)
const reviewForm = reactive({ decision: 'APPROVE', opinion: '' })
const reviewRules = computed(() => ({
  decision: [{ required: true, message: '请选择评审结论', trigger: 'change' }],
  opinion: reviewForm.decision === 'APPROVE' ? [] : [{ required: true, message: '退回或拒绝时必须填写评审意见', trigger: 'blur' }],
}))

const teacherPanelTitle = computed(() => ({ review: '待评审申请', approved: '已通过申请', results: '已生成资助名单' }[activeTeacherTab.value]))
const emptyDescription = computed(() => ({ review: '暂无待评审申请', approved: '暂无已通过申请', results: '尚未生成资助名单' }[activeTeacherTab.value]))

const statusMeta = (status) => statusMap[status] || { label: status || '未知', type: 'info' }
const typeLabel = (type) => scholarshipTypes.find((item) => item.value === type)?.label || type
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '未提交'
const canEdit = (row) => ['DRAFT', 'RETURNED'].includes(row.status)
const canSubmit = (row) => ['DRAFT', 'RETURNED'].includes(row.status)
const canWithdraw = (row) => ['DRAFT', 'SUBMITTED', 'RETURNED'].includes(row.status)
const isPendingReview = (row) => isCounselor.value ? row.status === 'SUBMITTED' : row.status === 'ACADEMIC_REVIEW'

const loadApplications = async () => {
  if (!isStudent.value && !isReviewer.value) return
  loading.value = true
  try {
    let result
    if (isStudent.value) {
      result = await listMyScholarshipApplications({ page: page.value, size: pageSize, status: studentStatus.value || undefined })
    } else if (activeTeacherTab.value === 'results') {
      result = await listScholarshipResults({ page: page.value, size: pageSize })
    } else {
      const stage = activeTeacherTab.value === 'review' ? (isCounselor.value ? 'COUNSELOR' : 'ACADEMIC') : 'ALL'
      const status = activeTeacherTab.value === 'approved' ? 'APPROVED' : undefined
      result = await listScholarshipReviews({ page: page.value, size: pageSize, stage, status })
    }
    applications.value = result.data.records
    total.value = result.data.total
    selectedApproved.value = []
  } finally {
    loading.value = false
  }
}

const resetAndLoad = () => {
  page.value = 1
  loadApplications()
}

const handleTeacherTabChange = () => resetAndLoad()

const openCreateDialog = () => {
  editingApplication.value = null
  Object.assign(applicationForm, { scholarshipType: '', title: '', reason: '', attachmentUrl: '' })
  applicationDialogOpen.value = true
}

const openEditDialog = (row) => {
  editingApplication.value = row
  Object.assign(applicationForm, {
    scholarshipType: row.scholarshipType,
    title: row.title,
    reason: row.reason,
    attachmentUrl: row.attachmentUrl || '',
  })
  applicationDialogOpen.value = true
}

const saveApplication = async () => {
  await applicationFormRef.value.validate()
  saving.value = true
  try {
    const payload = { ...applicationForm, attachmentUrl: applicationForm.attachmentUrl || null }
    if (editingApplication.value) await updateScholarshipApplication(editingApplication.value.scholarshipId, payload)
    else await createScholarshipApplication(payload)
    ElMessage.success(editingApplication.value ? '申请已更新' : '草稿已保存')
    applicationDialogOpen.value = false
    await loadApplications()
  } finally {
    saving.value = false
  }
}

const submitApplication = async (row) => {
  await ElMessageBox.confirm('提交后将先进入辅导员初审，确认提交该申请？', '提交申请', { confirmButtonText: '确认提交', cancelButtonText: '取消', type: 'warning' })
  await submitScholarshipApplication(row.scholarshipId)
  ElMessage.success('申请已提交')
  detailOpen.value = false
  await loadApplications()
}

const withdrawApplication = async (row) => {
  await ElMessageBox.confirm('撤回后该申请将结束，确认继续？', '撤回申请', { confirmButtonText: '确认撤回', cancelButtonText: '取消', type: 'warning' })
  await withdrawScholarshipApplication(row.scholarshipId)
  ElMessage.success('申请已撤回')
  await loadApplications()
}

const openDetail = async (row) => {
  const result = await getScholarshipApplication(row.scholarshipId)
  detail.value = result.data
  detailOpen.value = true
}

const openReviewDialog = (row) => {
  reviewingApplication.value = row
  Object.assign(reviewForm, { decision: 'APPROVE', opinion: '' })
  reviewDialogOpen.value = true
}

const saveReview = async () => {
  await reviewFormRef.value.validate()
  saving.value = true
  try {
    await submitScholarshipReview(reviewingApplication.value.scholarshipId, { stage: isCounselor.value ? 'COUNSELOR' : 'ACADEMIC', ...reviewForm, opinion: reviewForm.opinion || null })
    ElMessage.success(isCounselor.value && reviewForm.decision === 'APPROVE' ? '初审通过，已转入教务终审' : '审核结论已提交')
    reviewDialogOpen.value = false
    await loadApplications()
  } finally {
    saving.value = false
  }
}

const generateResults = async () => {
  const ids = selectedApproved.value.map((item) => item.scholarshipId)
  await ElMessageBox.confirm(`将选中的 ${ids.length} 项申请加入正式资助名单，确认继续？`, '生成资助名单', { confirmButtonText: '确认生成', cancelButtonText: '取消', type: 'warning' })
  await generateScholarshipResults(ids)
  ElMessage.success('资助名单已生成')
  activeTeacherTab.value = 'results'
  await resetAndLoad()
}

watch(() => currentUser.value, (value) => {
  if (value) resetAndLoad()
}, { immediate: true })
</script>

<style scoped>
.scholarship-workspace { width:min(1180px,calc(100% - 56px)); margin:0 auto; padding:32px 0 48px; }
.workspace-header { display:flex; align-items:flex-end; justify-content:space-between; gap:24px; margin-bottom:24px; }
.workspace-header h1 { margin:2px 0 4px; font-size:26px; font-weight:650; letter-spacing:0; }
.workspace-header p { margin:0; color:var(--color-text-secondary); }
.workspace-header .eyebrow { color:var(--color-brand-600); font-size:12px; font-weight:700; }
.summary-strip { display:grid; grid-template-columns:1fr 64px 1fr 64px 1fr; align-items:center; margin-bottom:20px; padding:18px 24px; background:#fff; border:1px solid var(--color-border-light); border-radius:8px; }
.summary-strip div { display:grid; grid-template-columns:34px 1fr; column-gap:10px; align-items:center; }
.summary-strip div > span { display:inline-flex; grid-row:1/3; width:30px; height:30px; align-items:center; justify-content:center; color:#fff; background:var(--color-brand-600); border-radius:50%; font-weight:700; }
.summary-strip strong { font-weight:600; }
.summary-strip small { color:var(--color-text-tertiary); }
.summary-strip i { height:1px; background:var(--color-border); }
.data-panel { min-height:360px; padding:0 22px 20px; }
.panel-toolbar { display:flex; min-height:72px; align-items:center; justify-content:space-between; gap:20px; }
.panel-toolbar > div:first-child { display:flex; align-items:baseline; gap:10px; }
.panel-toolbar h2 { margin:0; font-size:17px; font-weight:600; }
.panel-toolbar span { color:var(--color-text-tertiary); font-size:13px; }
.filter-actions { display:flex; align-items:center; gap:8px; }
.filter-actions .el-select { width:150px; }
.application-no { color:var(--color-text-secondary); font-family:ui-monospace,SFMono-Regular,Consolas,monospace; font-size:13px; }
.cell-subtitle { display:block; margin-top:2px; color:var(--color-text-tertiary); }
.row-actions { display:flex; align-items:center; white-space:nowrap; }
.desktop-table :deep(.el-table__row) { cursor:pointer; }
.desktop-table :deep(th.el-table__cell) { color:var(--color-text-secondary); background:#f8fafb; font-weight:600; }
.el-pagination { justify-content:flex-end; margin-top:20px; }
.teacher-panel { padding-top:0; }
.review-tabs :deep(.el-tabs__header) { margin:0; }
.review-tabs :deep(.el-tabs__nav-wrap::after) { height:1px; background:var(--color-border-light); }
.review-tabs :deep(.el-tabs__content) { display:none; }
.tab-label { display:inline-flex; align-items:center; gap:6px; }
.teacher-toolbar { min-height:68px; }
.desktop-empty { padding:44px 0; }
.full-width { width:100%; }
.review-target { display:flex; flex-direction:column; gap:4px; margin:-4px 0 20px; padding:12px 14px; background:var(--color-brand-50); border-left:3px solid var(--color-brand-600); }
.review-target span { color:var(--color-text-secondary); font-size:13px; }
.detail-content { padding:0 4px 24px; }
.detail-title { display:flex; align-items:flex-start; justify-content:space-between; gap:18px; padding-bottom:20px; border-bottom:1px solid var(--color-border-light); }
.detail-title h2 { margin:4px 0; font-size:20px; letter-spacing:0; }
.detail-title span,.detail-title small { color:var(--color-text-tertiary); }
.detail-content dl { display:grid; grid-template-columns:86px 1fr; gap:18px 12px; margin:24px 0; }
.detail-content dt { color:var(--color-text-tertiary); }
.detail-content dd { min-width:0; margin:0; color:var(--color-text-primary); }
.detail-content a { color:var(--color-brand-600); }
.reason-text { white-space:pre-wrap; }
.review-opinion { padding:10px 12px; background:#fff8e8; border-left:3px solid var(--color-warning); }
.drawer-actions { display:flex; justify-content:flex-end; gap:8px; padding-top:18px; border-top:1px solid var(--color-border-light); }
.mobile-records { display:none; }
@media(max-width:991px){.scholarship-workspace{width:calc(100% - 40px)}.summary-strip{grid-template-columns:1fr 32px 1fr 32px 1fr;padding:16px}.summary-strip small{display:none}}
@media(max-width:767px){
  .scholarship-workspace{width:calc(100% - 24px);padding:22px 0 36px}.workspace-header{align-items:flex-start;margin-bottom:18px}.workspace-header h1{font-size:21px}.workspace-header>div>p:last-child{font-size:13px}.workspace-header .el-button{flex:0 0 auto}.summary-strip{grid-template-columns:1fr 16px 1fr 16px 1fr;padding:12px 10px}.summary-strip div{display:flex;flex-direction:column;gap:3px;text-align:center}.summary-strip div>span{width:26px;height:26px;font-size:12px}.summary-strip strong{font-size:12px}.data-panel{padding:0;background:transparent;border:0}.panel-toolbar{min-height:60px}.filter-actions .el-select{width:128px}.desktop-table,.desktop-empty{display:none}.mobile-records{display:flex;min-height:160px;flex-direction:column;gap:8px}.mobile-record{display:flex;width:100%;flex-direction:column;gap:4px;padding:14px;color:var(--color-text-secondary);text-align:left;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.record-top{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;color:var(--color-text-primary)}.record-top strong{min-width:0;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.record-top .el-tag{flex:0 0 auto}.mobile-record small{color:var(--color-text-tertiary)}.el-pagination{justify-content:center}.teacher-panel .panel-toolbar{padding-top:6px}.review-tabs :deep(.el-tabs__nav){display:flex;width:100%}.review-tabs :deep(.el-tabs__item){min-width:0;flex:1;justify-content:center;padding:0 4px}.tab-label{gap:4px;font-size:13px}.detail-content dl{grid-template-columns:72px 1fr}.drawer-actions{flex-wrap:wrap}}
</style>
