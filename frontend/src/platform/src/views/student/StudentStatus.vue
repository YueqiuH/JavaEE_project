<template>
  <section class="status-workspace">
    <header class="workspace-header">
      <div>
        <p class="eyebrow">学生事务</p>
        <h1>{{ isReviewer ? '学籍异动审核' : '学籍信息与异动' }}</h1>
        <p>{{ isReviewer ? (isCounselor ? '审核本人所带学生的学籍异动申请' : '完成学籍异动的教务终审') : '维护个人联系信息并办理学籍异动' }}</p>
      </div>
      <el-button v-if="isStudent && activeStudentTab === 'applications'" type="primary" :icon="DocumentAdd" @click="openCreateDialog">新建申请</el-button>
    </header>

    <el-result v-if="roleResolved && !isStudent && !isReviewer" icon="warning" title="当前账号无学籍业务权限" />

    <template v-else-if="isStudent">
      <el-tabs v-model="activeStudentTab" class="workspace-tabs" @tab-change="handleStudentTabChange">
        <el-tab-pane name="profile">
          <template #label><span class="tab-label"><el-icon><User /></el-icon>个人信息</span></template>
        </el-tab-pane>
        <el-tab-pane name="applications">
          <template #label><span class="tab-label"><el-icon><Tickets /></el-icon>异动申请</span></template>
        </el-tab-pane>
      </el-tabs>

      <div v-if="activeStudentTab === 'profile'" v-loading="profileLoading" class="surface-panel profile-panel">
        <div class="panel-heading">
          <div><h2>基础信息</h2><span>核心身份信息仅供查看</span></div>
          <el-tag type="info" effect="plain">只读</el-tag>
        </div>
        <dl class="core-profile">
          <div><dt>姓名</dt><dd>{{ profile.studentName || '—' }}</dd></div>
          <div><dt>学号</dt><dd>{{ profile.studentNo || '—' }}</dd></div>
          <div><dt>年级</dt><dd>{{ profile.gradeName || '未设置' }}</dd></div>
          <div><dt>出生日期</dt><dd>{{ profile.studentBirth || '未设置' }}</dd></div>
        </dl>

        <div class="panel-heading contact-heading">
          <div><h2>联系信息</h2><span>更新常用联系方式与紧急联系人</span></div>
        </div>
        <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-position="top" class="profile-form">
          <el-form-item label="常住地址" prop="currentAddress"><el-input v-model="profileForm.currentAddress" maxlength="128" /></el-form-item>
          <el-form-item label="手机号码" prop="phone"><el-input v-model="profileForm.phone" maxlength="11" /></el-form-item>
          <el-form-item label="电子邮箱" prop="email"><el-input v-model="profileForm.email" maxlength="128" /></el-form-item>
          <el-form-item label="紧急联系人" prop="emergencyContact"><el-input v-model="profileForm.emergencyContact" maxlength="32" /></el-form-item>
          <el-form-item label="紧急联系电话" prop="emergencyPhone"><el-input v-model="profileForm.emergencyPhone" maxlength="20" /></el-form-item>
          <div class="profile-submit"><el-button type="primary" :icon="Check" :loading="saving" @click="saveProfile">保存修改</el-button></div>
        </el-form>
      </div>

      <div v-else class="surface-panel data-panel">
        <div class="panel-toolbar">
          <div><h2>我的异动申请</h2><span>共 {{ total }} 项</span></div>
          <div class="filter-actions">
            <el-select v-model="studentStatus" aria-label="申请状态" placeholder="全部状态" clearable @change="resetAndLoad">
              <el-option v-for="item in statusFilterOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-tooltip content="刷新列表" placement="top"><el-button :icon="Refresh" circle aria-label="刷新列表" @click="loadApplications" /></el-tooltip>
          </div>
        </div>
        <ApplicationTable v-if="loading || applications.length" :applications="applications" :loading="loading" :teacher="false" @detail="openDetail" @edit="openEditDialog" @submit="submitApplication" @withdraw="withdrawApplication" />
        <MobileRecords :applications="applications" :loading="loading" @detail="openDetail" />
        <el-empty v-if="!loading && applications.length === 0" class="desktop-empty" description="暂无异动申请" :image-size="88" />
        <el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadApplications" />
      </div>
    </template>

    <template v-else-if="isReviewer">
      <div class="surface-panel data-panel teacher-panel">
        <el-tabs v-model="reviewStage" class="workspace-tabs review-tabs" @tab-change="handleReviewStageChange">
          <el-tab-pane v-if="isAcademic" name="ALL"><template #label><span class="tab-label"><el-icon><Tickets /></el-icon>全部记录</span></template></el-tab-pane>
          <el-tab-pane v-if="isCounselor" name="COUNSELOR"><template #label><span class="tab-label"><el-icon><UserFilled /></el-icon>辅导员初审</span></template></el-tab-pane>
          <el-tab-pane v-if="isAcademic" name="ACADEMIC"><template #label><span class="tab-label"><el-icon><OfficeBuilding /></el-icon>教务终审</span></template></el-tab-pane>
        </el-tabs>
        <div class="panel-toolbar teacher-toolbar">
          <div><h2>{{ reviewTitle }}</h2><span>共 {{ total }} 项</span></div>
          <div class="filter-actions">
            <el-select v-if="reviewStage === 'ALL'" v-model="teacherStatus" aria-label="异动记录状态" placeholder="全部状态" clearable @change="resetAndLoad">
              <el-option v-for="item in teacherStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-tooltip content="刷新列表" placement="top"><el-button :icon="Refresh" circle aria-label="刷新列表" @click="loadApplications" /></el-tooltip>
          </div>
        </div>
        <ApplicationTable v-if="loading || applications.length" :applications="applications" :loading="loading" teacher @detail="openDetail" @review="openReviewDialog" />
        <MobileRecords :applications="applications" :loading="loading" teacher @detail="openDetail" />
        <el-empty v-if="!loading && applications.length === 0" class="desktop-empty" :description="reviewEmptyText" :image-size="88" />
        <el-pagination v-if="total > pageSize" v-model:current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadApplications" />
      </div>
    </template>

    <el-dialog v-model="applicationDialogOpen" :title="editingApplication ? '修改异动申请' : '新建异动申请'" width="min(560px, calc(100vw - 32px))" destroy-on-close>
      <el-form ref="applicationFormRef" :model="applicationForm" :rules="applicationRules" label-position="top">
        <el-form-item label="异动类型" prop="changeType">
          <el-select v-model="applicationForm.changeType" class="full-width" placeholder="请选择异动类型" @change="handleChangeType">
            <el-option v-for="item in changeTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="applicationForm.changeType === 'MAJOR_CHANGE'" label="目标专业" prop="newMajorId">
          <el-select v-model="applicationForm.newMajorId" class="full-width" placeholder="请选择目标专业" filterable>
            <el-option v-for="major in majors" :key="major.majorId" :label="`${major.majorName}（${major.majorCode || '无代码'}）`" :value="major.majorId" />
          </el-select>
        </el-form-item>
        <el-form-item label="期望生效日期" prop="desiredEffectiveDate">
          <el-date-picker v-model="applicationForm.desiredEffectiveDate" type="date" value-format="YYYY-MM-DD" :disabled-date="disablePastDate" class="full-width" />
        </el-form-item>
        <el-form-item label="申请原因" prop="reason">
          <el-input v-model="applicationForm.reason" type="textarea" :rows="6" maxlength="512" show-word-limit placeholder="请说明申请原因和相关情况" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="applicationDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveApplication">保存草稿</el-button></template>
    </el-dialog>

    <el-dialog v-model="reviewDialogOpen" :title="reviewStage === 'COUNSELOR' ? '辅导员初审' : '教务复审'" width="min(520px, calc(100vw - 32px))" destroy-on-close>
      <div v-if="reviewingApplication" class="review-target"><strong>{{ reviewingApplication.studentName }} · {{ changeTypeLabel(reviewingApplication.changeType) }}</strong><span>{{ reviewingApplication.applicationNo }}</span></div>
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-position="top">
        <el-form-item label="审核结论" prop="decision"><el-radio-group v-model="reviewForm.decision"><el-radio-button value="APPROVE">通过</el-radio-button><el-radio-button value="REJECT">拒绝</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="审核意见" prop="opinion"><el-input v-model="reviewForm.opinion" type="textarea" :rows="4" maxlength="256" show-word-limit placeholder="拒绝时必须填写具体原因" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="reviewDialogOpen = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveReview">确认提交</el-button></template>
    </el-dialog>

    <el-drawer v-model="detailOpen" title="异动申请详情" size="min(540px, 100vw)" destroy-on-close>
      <div v-if="detail" class="detail-content">
        <div class="detail-title"><div><span>{{ changeTypeLabel(detail.changeType) }}</span><h2>{{ detail.studentName || '我的申请' }}</h2><small>{{ detail.applicationNo }}</small></div><el-tag :type="statusMeta(detail.status).type">{{ statusMeta(detail.status).label }}</el-tag></div>
        <el-steps :active="stepActive(detail.status)" finish-status="success" align-center class="review-steps"><el-step title="提交" /><el-step title="初审" /><el-step title="复审" /><el-step title="办结" /></el-steps>
        <dl>
          <template v-if="isReviewer"><dt>申请学生</dt><dd>{{ detail.studentName }}（{{ detail.studentNo }}）</dd></template>
          <dt>异动类型</dt><dd>{{ changeTypeLabel(detail.changeType) }}</dd>
          <template v-if="detail.newMajorName"><dt>目标专业</dt><dd>{{ detail.newMajorName }}</dd></template>
          <dt>生效日期</dt><dd>{{ detail.desiredEffectiveDate }}</dd>
          <dt>申请原因</dt><dd class="reason-text">{{ detail.reason }}</dd>
          <template v-if="detail.counselorReviewedAt"><dt>辅导员初审</dt><dd>{{ detail.counselorOpinion || '通过' }}<small>{{ detail.counselorName }} · {{ formatDate(detail.counselorReviewedAt) }}</small></dd></template>
          <template v-if="detail.academicReviewedAt"><dt>教务复审</dt><dd>{{ detail.academicOpinion || '通过' }}<small>{{ detail.academicReviewerName }} · {{ formatDate(detail.academicReviewedAt) }}</small></dd></template>
        </dl>
        <div class="drawer-actions">
          <el-button v-if="isStudent && detail.status === 'DRAFT'" :icon="EditPen" @click="openEditDialog(detail); detailOpen = false">修改申请</el-button>
          <el-button v-if="isStudent && detail.status === 'DRAFT'" type="primary" :icon="Promotion" @click="submitApplication(detail)">提交申请</el-button>
          <el-button v-if="isReviewer && isCurrentReviewStage(detail)" type="primary" :icon="Stamp" @click="openReviewDialog(detail); detailOpen = false">开始审核</el-button>
        </div>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, defineComponent, h, inject, reactive, ref, watch } from 'vue'
import { Check, Close, DocumentAdd, EditPen, OfficeBuilding, Promotion, Refresh, Stamp, Tickets, User, UserFilled, View } from '@element-plus/icons-vue'
import { ElButton, ElMessage, ElMessageBox, ElTable, ElTableColumn, ElTag } from 'element-plus'
import {
  createStatusChange, getStatusChange, getStudentProfile, listMyStatusChanges, listStatusChangeMajors,
  listStatusChangeReviews, reviewStatusChange, submitStatusChange, updateStatusChange,
  updateStudentProfile, withdrawStatusChange,
} from '@/api/student.js'

const changeTypes = [
  { value: 'SUSPENSION', label: '休学' }, { value: 'RESUMPTION', label: '复学' },
  { value: 'MAJOR_CHANGE', label: '转专业' }, { value: 'WITHDRAWAL', label: '退学' },
]
const statusMap = {
  DRAFT: { label: '草稿', type: 'info' }, COUNSELOR_REVIEW: { label: '辅导员初审', type: 'warning' },
  ACADEMIC_REVIEW: { label: '教务复审', type: 'warning' }, APPROVED: { label: '已通过', type: 'success' },
  COUNSELOR_REJECTED: { label: '初审未通过', type: 'danger' }, ACADEMIC_REJECTED: { label: '复审未通过', type: 'danger' },
  WITHDRAWN: { label: '已撤回', type: 'info' },
}
const statusFilterOptions = Object.entries(statusMap).map(([value, meta]) => ({ value, label: meta.label }))
const statusMeta = (status) => statusMap[status] || { label: status || '未知', type: 'info' }
const changeTypeLabel = (type) => changeTypes.find((item) => item.value === type)?.label || type
const formatDate = (value) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'

const ApplicationTable = defineComponent({
  props: { applications: Array, loading: Boolean, teacher: Boolean },
  emits: ['detail', 'edit', 'submit', 'withdraw', 'review'],
  setup(props, { emit }) {
    return () => h(ElTable, { data: props.applications, class: 'desktop-table', rowKey: 'changeId', onRowClick: (row) => emit('detail', row), loading: props.loading }, {
      default: () => [
        props.teacher ? h(ElTableColumn, { label: '学生', minWidth: 150 }, { default: ({ row }) => h('div', [h('strong', row.studentName), h('small', { class: 'cell-subtitle' }, String(row.studentNo))]) }) : h(ElTableColumn, { label: '申请编号', minWidth: 180 }, { default: ({ row }) => h('span', { class: 'application-no' }, row.applicationNo) }),
        h(ElTableColumn, { label: '异动事项', minWidth: 180 }, { default: ({ row }) => h('div', [h('strong', changeTypeLabel(row.changeType)), row.newMajorName ? h('small', { class: 'cell-subtitle' }, `目标：${row.newMajorName}`) : null]) }),
        h(ElTableColumn, { label: '期望生效', minWidth: 120, prop: 'desiredEffectiveDate' }),
        h(ElTableColumn, { label: '状态', width: 130 }, { default: ({ row }) => h(ElTag, { type: statusMeta(row.status).type }, () => statusMeta(row.status).label) }),
        h(ElTableColumn, { label: '操作', width: props.teacher ? 150 : 225, fixed: 'right' }, { default: ({ row }) => h('div', { class: 'row-actions', onClick: (event) => event.stopPropagation() }, [
          h(ElButton, { link: true, icon: View, onClick: () => emit('detail', row) }, () => '查看'),
          !props.teacher && row.status === 'DRAFT' ? h(ElButton, { link: true, type: 'primary', icon: EditPen, onClick: () => emit('edit', row) }, () => '修改') : null,
          !props.teacher && row.status === 'DRAFT' ? h(ElButton, { link: true, type: 'primary', icon: Promotion, onClick: () => emit('submit', row) }, () => '提交') : null,
          !props.teacher && ['DRAFT', 'COUNSELOR_REVIEW'].includes(row.status) ? h(ElButton, { link: true, type: 'danger', icon: Close, onClick: () => emit('withdraw', row) }, () => '撤回') : null,
          props.teacher && isCurrentReviewStage(row) ? h(ElButton, { link: true, type: 'primary', icon: Stamp, onClick: () => emit('review', row) }, () => '审核') : null,
        ]) }),
      ],
    })
  },
})

const MobileRecords = defineComponent({
  props: { applications: Array, loading: Boolean, teacher: Boolean }, emits: ['detail'],
  setup(props, { emit }) { return () => h('div', { class: 'mobile-records' }, props.applications.map((row) => h('button', { type: 'button', class: 'mobile-record', onClick: () => emit('detail', row) }, [h('span', { class: 'record-top' }, [h('strong', props.teacher ? `${row.studentName} · ${changeTypeLabel(row.changeType)}` : changeTypeLabel(row.changeType)), h(ElTag, { type: statusMeta(row.status).type, size: 'small' }, () => statusMeta(row.status).label)]), h('span', row.newMajorName || `生效日期 ${row.desiredEffectiveDate}`), h('small', row.applicationNo)]))) },
})

const currentUser = inject('currentUser', ref(null))
const roles = computed(() => new Set(currentUser.value?.roles || []))
const roleResolved = computed(() => Boolean(currentUser.value))
const isStudent = computed(() => roles.value.has('STUDENT'))
const isCounselor = computed(() => roles.value.has('COUNSELOR'))
const isAcademic = computed(() => roles.value.has('ADMIN'))
const isReviewer = computed(() => isCounselor.value || isAcademic.value)
const activeStudentTab = ref('profile')
const reviewStage = ref('ACADEMIC')
const applications = ref([]), majors = ref([]), loading = ref(false), profileLoading = ref(false), saving = ref(false)
const page = ref(1), total = ref(0), studentStatus = ref(''), teacherStatus = ref(''), pageSize = 10
const teacherStatusOptions = statusFilterOptions.filter((item) => item.value !== 'DRAFT')
const reviewTitle = computed(() => ({ ALL: '全部异动记录', COUNSELOR: '待初审申请', ACADEMIC: '待复审申请' }[reviewStage.value]))
const reviewEmptyText = computed(() => ({ ALL: '暂无已提交的异动记录', COUNSELOR: '暂无待初审申请', ACADEMIC: '暂无待复审申请' }[reviewStage.value]))
const profile = reactive({})
const profileFormRef = ref(), profileForm = reactive({ currentAddress: '', phone: '', email: '', emergencyContact: '', emergencyPhone: '' })
const profileRules = { phone: [{ pattern: /^$|^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }], email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }], emergencyPhone: [{ pattern: /^$|^[0-9+\-]{6,20}$/, message: '请输入正确的联系电话', trigger: 'blur' }] }
const applicationDialogOpen = ref(false), applicationFormRef = ref(), editingApplication = ref(null)
const applicationForm = reactive({ changeType: '', newMajorId: null, desiredEffectiveDate: '', reason: '' })
const applicationRules = computed(() => ({ changeType: [{ required: true, message: '请选择异动类型', trigger: 'change' }], newMajorId: applicationForm.changeType === 'MAJOR_CHANGE' ? [{ required: true, message: '请选择目标专业', trigger: 'change' }] : [], desiredEffectiveDate: [{ required: true, message: '请选择期望生效日期', trigger: 'change' }], reason: [{ required: true, message: '请填写申请原因', trigger: 'blur' }] }))
const reviewDialogOpen = ref(false), reviewFormRef = ref(), reviewingApplication = ref(null), reviewForm = reactive({ decision: 'APPROVE', opinion: '' })
const reviewRules = computed(() => ({ decision: [{ required: true, message: '请选择审核结论', trigger: 'change' }], opinion: reviewForm.decision === 'REJECT' ? [{ required: true, message: '拒绝时必须填写审核意见', trigger: 'blur' }] : [] }))
const detailOpen = ref(false), detail = ref(null)

const loadProfile = async () => { profileLoading.value = true; try { const result = await getStudentProfile(); Object.assign(profile, result.data); Object.assign(profileForm, { currentAddress: result.data.currentAddress || '', phone: result.data.phone || '', email: result.data.email || '', emergencyContact: result.data.emergencyContact || '', emergencyPhone: result.data.emergencyPhone || '' }) } finally { profileLoading.value = false } }
const saveProfile = async () => { await profileFormRef.value.validate(); saving.value = true; try { const result = await updateStudentProfile(profileForm); Object.assign(profile, result.data); ElMessage.success('联系信息已更新') } finally { saving.value = false } }
const loadApplications = async () => { if (!isStudent.value && !isReviewer.value) return; loading.value = true; try { const result = isStudent.value ? await listMyStatusChanges({ page: page.value, size: pageSize, status: studentStatus.value || undefined }) : await listStatusChangeReviews({ stage: reviewStage.value, status: reviewStage.value === 'ALL' ? teacherStatus.value || undefined : undefined, page: page.value, size: pageSize }); applications.value = result.data.records; total.value = result.data.total } finally { loading.value = false } }
const resetAndLoad = () => { page.value = 1; return loadApplications() }
const handleReviewStageChange = () => { teacherStatus.value = ''; return resetAndLoad() }
const handleStudentTabChange = () => activeStudentTab.value === 'profile' ? loadProfile() : resetAndLoad()
const openCreateDialog = () => { editingApplication.value = null; Object.assign(applicationForm, { changeType: '', newMajorId: null, desiredEffectiveDate: '', reason: '' }); applicationDialogOpen.value = true }
const openEditDialog = (row) => { editingApplication.value = row; Object.assign(applicationForm, { changeType: row.changeType, newMajorId: row.newMajorId || null, desiredEffectiveDate: row.desiredEffectiveDate, reason: row.reason }); applicationDialogOpen.value = true }
const handleChangeType = (type) => { if (type !== 'MAJOR_CHANGE') applicationForm.newMajorId = null }
const disablePastDate = (date) => date.getTime() < new Date().setHours(0, 0, 0, 0)
const saveApplication = async () => { await applicationFormRef.value.validate(); saving.value = true; try { const payload = { ...applicationForm, newMajorId: applicationForm.newMajorId || null }; if (editingApplication.value) await updateStatusChange(editingApplication.value.changeId, payload); else await createStatusChange(payload); ElMessage.success(editingApplication.value ? '申请已更新' : '草稿已保存'); applicationDialogOpen.value = false; await loadApplications() } finally { saving.value = false } }
const submitApplication = async (row) => { await ElMessageBox.confirm('提交后将进入辅导员初审，确认提交？', '提交申请', { confirmButtonText: '确认提交', cancelButtonText: '取消', type: 'warning' }); await submitStatusChange(row.changeId); ElMessage.success('申请已提交'); detailOpen.value = false; await loadApplications() }
const withdrawApplication = async (row) => { await ElMessageBox.confirm('确认撤回该学籍异动申请？', '撤回申请', { confirmButtonText: '确认撤回', cancelButtonText: '取消', type: 'warning' }); await withdrawStatusChange(row.changeId); ElMessage.success('申请已撤回'); await loadApplications() }
const openDetail = async (row) => { const result = await getStatusChange(row.changeId); detail.value = result.data; detailOpen.value = true }
const openReviewDialog = (row) => { reviewingApplication.value = row; Object.assign(reviewForm, { decision: 'APPROVE', opinion: '' }); reviewDialogOpen.value = true }
const saveReview = async () => { await reviewFormRef.value.validate(); saving.value = true; try { await reviewStatusChange(reviewingApplication.value.changeId, { stage: reviewStage.value, decision: reviewForm.decision, opinion: reviewForm.opinion || null }); ElMessage.success(reviewStage.value === 'COUNSELOR' && reviewForm.decision === 'APPROVE' ? '初审通过，已转入教务复审' : '审核结论已提交'); reviewDialogOpen.value = false; await loadApplications() } finally { saving.value = false } }
const isCurrentReviewStage = (row) => (reviewStage.value === 'COUNSELOR' && row.status === 'COUNSELOR_REVIEW') || (reviewStage.value === 'ACADEMIC' && row.status === 'ACADEMIC_REVIEW')
const stepActive = (status) => ({ DRAFT: 0, COUNSELOR_REVIEW: 1, ACADEMIC_REVIEW: 2, APPROVED: 4, COUNSELOR_REJECTED: 1, ACADEMIC_REJECTED: 2, WITHDRAWN: 0 }[status] ?? 0)

watch(() => currentUser.value, async (value) => { if (!value) return; if (isStudent.value) { await Promise.all([loadProfile(), listStatusChangeMajors().then((result) => { majors.value = result.data })]) } else if (isReviewer.value) { reviewStage.value = isCounselor.value ? 'COUNSELOR' : 'ACADEMIC'; await loadApplications() } }, { immediate: true })
</script>

<style scoped>
.status-workspace{width:min(1180px,calc(100% - 56px));margin:0 auto;padding:32px 0 48px}.workspace-header{display:flex;align-items:flex-end;justify-content:space-between;gap:24px;margin-bottom:18px}.workspace-header h1{margin:2px 0 4px;font-size:26px;font-weight:650;letter-spacing:0}.workspace-header p{margin:0;color:var(--color-text-secondary)}.workspace-header .eyebrow{color:var(--color-brand-600);font-size:12px;font-weight:700}.workspace-tabs{margin-bottom:18px}.workspace-tabs :deep(.el-tabs__header){margin:0}.workspace-tabs :deep(.el-tabs__content){display:none}.tab-label{display:inline-flex;align-items:center;gap:6px}.profile-panel{padding:0 24px 24px}.panel-heading{display:flex;min-height:68px;align-items:center;justify-content:space-between;border-bottom:1px solid var(--color-border-light)}.panel-heading>div{display:flex;align-items:baseline;gap:10px}.panel-heading h2,.panel-toolbar h2{margin:0;font-size:17px;font-weight:600}.panel-heading span,.panel-toolbar span{color:var(--color-text-tertiary);font-size:13px}.core-profile{display:grid;grid-template-columns:repeat(4,1fr);margin:0;padding:24px 0}.core-profile div{padding:0 20px;border-right:1px solid var(--color-border-light)}.core-profile div:first-child{padding-left:0}.core-profile div:last-child{border-right:0}.core-profile dt{color:var(--color-text-tertiary);font-size:12px}.core-profile dd{margin:5px 0 0;font-size:16px;font-weight:600}.contact-heading{border-top:1px solid var(--color-border-light)}.profile-form{display:grid;grid-template-columns:2fr 1fr 1.2fr;gap:0 18px;padding-top:20px}.profile-submit{display:flex;align-items:flex-end;justify-content:flex-end;padding-bottom:18px}.data-panel{min-height:360px;padding:0 22px 20px}.panel-toolbar{display:flex;min-height:68px;align-items:center;justify-content:space-between;gap:20px}.panel-toolbar>div:first-child{display:flex;align-items:baseline;gap:10px}.filter-actions{display:flex;align-items:center;gap:8px}.filter-actions .el-select{width:150px}.teacher-panel{padding-top:0}.teacher-panel .review-tabs{margin:0}.teacher-toolbar{min-height:68px}.application-no{color:var(--color-text-secondary);font-family:ui-monospace,SFMono-Regular,Consolas,monospace;font-size:13px}.cell-subtitle{display:block;margin-top:2px;color:var(--color-text-tertiary)}.row-actions{display:flex;align-items:center;white-space:nowrap}.desktop-table :deep(.el-table__row){cursor:pointer}.desktop-table :deep(th.el-table__cell){color:var(--color-text-secondary);background:#f8fafb;font-weight:600}.desktop-empty{padding:44px 0}.el-pagination{justify-content:flex-end;margin-top:20px}.full-width{width:100%!important}.review-target{display:flex;flex-direction:column;gap:4px;margin:-4px 0 20px;padding:12px 14px;background:var(--color-brand-50);border-left:3px solid var(--color-brand-600)}.review-target span{color:var(--color-text-secondary);font-size:13px}.detail-title{display:flex;align-items:flex-start;justify-content:space-between;gap:16px;padding-bottom:20px;border-bottom:1px solid var(--color-border-light)}.detail-title h2{margin:4px 0;font-size:20px;letter-spacing:0}.detail-title span,.detail-title small{color:var(--color-text-tertiary)}.review-steps{margin:28px 0}.detail-content dl{display:grid;grid-template-columns:90px 1fr;gap:18px 12px;margin:24px 0}.detail-content dt{color:var(--color-text-tertiary)}.detail-content dd{min-width:0;margin:0}.detail-content dd small{display:block;margin-top:4px;color:var(--color-text-tertiary)}.reason-text{white-space:pre-wrap}.drawer-actions{display:flex;justify-content:flex-end;gap:8px;padding-top:18px;border-top:1px solid var(--color-border-light)}.mobile-records{display:none}
@media(max-width:991px){.status-workspace{width:calc(100% - 40px)}.core-profile{grid-template-columns:repeat(2,1fr);row-gap:20px}.core-profile div:nth-child(2){border-right:0}.profile-form{grid-template-columns:1fr 1fr}}
@media(max-width:767px){.status-workspace{width:calc(100% - 24px);padding:22px 0 36px}.workspace-header{align-items:flex-start}.workspace-header h1{font-size:21px}.workspace-header>div>p:last-child{font-size:13px}.workspace-header .el-button{flex:0 0 auto}.profile-panel{padding:0 14px 18px}.panel-heading>div{display:block}.panel-heading span{display:block}.core-profile{grid-template-columns:1fr 1fr;padding:18px 0}.core-profile div{padding:0 12px}.core-profile div:nth-child(3){padding-left:0}.profile-form{display:block}.data-panel{padding:0;background:transparent;border:0}.panel-toolbar{min-height:60px}.filter-actions .el-select{width:128px}.desktop-table,.desktop-empty{display:none}.mobile-records{display:flex;min-height:160px;flex-direction:column;gap:8px}.mobile-records :deep(.mobile-record){display:flex;width:100%;flex-direction:column;gap:4px;padding:14px;color:var(--color-text-secondary);text-align:left;background:#fff;border:1px solid var(--color-border-light);border-radius:6px}.mobile-records :deep(.record-top){display:flex;align-items:flex-start;justify-content:space-between;gap:10px;color:var(--color-text-primary)}.mobile-records :deep(.record-top strong){min-width:0;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.mobile-records :deep(.record-top .el-tag){flex:0 0 auto}.mobile-records :deep(.mobile-record small){color:var(--color-text-tertiary)}.review-tabs :deep(.el-tabs__nav){display:flex;width:100%}.review-tabs :deep(.el-tabs__item){min-width:0;flex:1;justify-content:center;padding:0 6px}.el-pagination{justify-content:center}.detail-content dl{grid-template-columns:76px 1fr}.drawer-actions{flex-wrap:wrap}}
</style>
