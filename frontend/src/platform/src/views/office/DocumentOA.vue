<template>
  <section class="office-page">
    <PageBreadcrumb domain="office" title="公文流转 OA" />
    <header class="office-page__header">
      <div>
        <h1>官方公文流转 OA</h1>
        <p>审批流程由管理员按公文类型固定配置，学生可发起请假申请并按照快照逐级流转。</p>
      </div>
      <div class="office-page__actions">
        <el-button :loading="loading" @click="load">刷新</el-button>
        <el-button v-if="canSelf" type="primary" @click="openStart">{{ isStudent ? '发起请假' : '发起公文' }}</el-button>
      </div>
    </header>

    <el-alert v-if="canSelf" class="flow-tip" type="info" :closable="false" show-icon
      :title="isStudent ? '学生只能发起请假申请；审批人由管理员固定配置；退回后可修改并重新提交。' : '发起人不能更换固定审批人；当前步骤审批人可以处理本人发起的公文；退回重提沿用原流程快照。'" />
    <el-result v-if="!canSelf && !canApprove && !canManage" class="office-page__empty" icon="warning" title="无公文访问权限" />

    <el-card v-else class="office-page__section" shadow="never" v-loading="loading">
      <el-tabs v-model="tab">
        <el-tab-pane v-if="canSelf" label="我发起的" name="mine">
          <el-table :data="mine" empty-text="暂无发起的公文">
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column prop="docType" label="类型" width="110" />
            <el-table-column label="当前进度" min-width="180">
              <template #default="scope">
                <span v-if="scope.row.status === 0">第 {{ scope.row.currentStep }} 步 · {{ approverName(scope.row.currentApproverId) }}</span>
                <span v-else>第 {{ scope.row.approvalRound || 1 }} 轮已结束</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope"><el-tag :type="statusTypes[scope.row.status]">{{ statusTexts[scope.row.status] }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" min-width="260">
              <template #default="scope">
                <el-button link @click="showProgress(scope.row)">流程记录</el-button>
                <el-button v-if="scope.row.status === 3" link type="primary" @click="openResubmit(scope.row)">修改并重新提交</el-button>
                <el-button v-else link type="warning" :disabled="scope.row.status !== 0" @click="remind(scope.row)">催办</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane v-if="canApprove" :label="`待我审批 (${pending.length})`" name="pending">
          <el-table :data="pending" empty-text="当前没有待审批公文">
            <el-table-column prop="title" label="标题" min-width="170" />
            <el-table-column prop="docType" label="类型" width="110" />
            <el-table-column label="步骤" width="100"><template #default="scope">第 {{ scope.row.currentStep }} 步</template></el-table-column>
            <el-table-column prop="content" label="正文" min-width="220" show-overflow-tooltip />
            <el-table-column prop="initiatorId" label="发起人" width="90" />
            <el-table-column label="操作" min-width="260">
              <template #default="scope">
                <el-button link type="success" @click="approve(scope.row, '同意')">同意</el-button>
                <el-button link type="danger" @click="approve(scope.row, '拒绝')">拒绝</el-button>
                <el-button link type="warning" @click="approve(scope.row, '退回')">退回修改</el-button>
                <el-button link @click="showProgress(scope.row)">流程</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane v-if="canManage" label="审批配置" name="config">
          <div class="config-grid">
            <el-card shadow="never">
              <template #header><strong>审批资格（学生不会出现在候选列表）</strong></template>
              <el-table :data="candidates" max-height="430" empty-text="暂无非学生用户">
                <el-table-column prop="username" label="账号" min-width="110" />
                <el-table-column label="人员类型" width="90"><template #default="scope">{{ userTypeName(scope.row.userType) }}</template></el-table-column>
                <el-table-column label="显示名称" min-width="150">
                  <template #default="scope"><el-input v-model="scope.row.displayName" maxlength="32" /></template>
                </el-table-column>
                <el-table-column label="有审批资格" width="110">
                  <template #default="scope"><el-switch v-model="scope.row.qualified" @change="value => changeQualification(scope.row, value)" /></template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default="scope"><el-button v-if="scope.row.qualified" link type="primary" @click="saveQualificationName(scope.row)">保存</el-button></template>
                </el-table-column>
              </el-table>
            </el-card>

            <el-card shadow="never">
              <template #header>
                <div class="config-header"><strong>固定流程配置</strong><el-tag v-if="editingWorkflow">当前 v{{ editingWorkflow.version }}</el-tag></div>
              </template>
              <el-form label-position="top">
                <el-form-item label="公文类型">
                  <el-select v-model="workflowDocType" style="width:100%" @change="resetWorkflowEditor">
                    <el-option v-for="value in documentTypes" :key="value" :value="value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="流程名称"><el-input v-model="workflowForm.workflowName" maxlength="64" /></el-form-item>
                <el-form-item label="审批步骤（按顺序执行）">
                  <div class="step-editor">
                    <div v-for="(step, index) in workflowForm.steps" :key="index" class="step-row">
                      <span class="step-index">{{ index + 1 }}</span>
                      <el-input v-model="step.stepName" placeholder="步骤名称" maxlength="64" />
                      <el-select v-model="step.approverId" placeholder="固定审批人" filterable>
                        <el-option v-for="item in qualifiedApprovers" :key="item.userId" :value="item.userId" :label="`${item.displayName}（${item.username}）`" />
                      </el-select>
                      <el-button-group>
                        <el-button :disabled="index === 0" @click="moveStep(index, -1)">↑</el-button>
                        <el-button :disabled="index === workflowForm.steps.length - 1" @click="moveStep(index, 1)">↓</el-button>
                        <el-button type="danger" :disabled="workflowForm.steps.length === 1" @click="removeStep(index)">删</el-button>
                      </el-button-group>
                    </div>
                  </div>
                </el-form-item>
              </el-form>
              <div class="config-actions">
                <el-button @click="addStep">添加步骤</el-button>
                <el-button type="primary" @click="saveWorkflow">保存并启用新版本</el-button>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="visible" :title="editingDocId ? '修改并重新提交公文' : (isStudent ? '发起请假申请' : '发起公文')" width="min(680px, 92vw)">
      <el-form label-position="top">
        <el-form-item label="标题" required><el-input v-model="form.title" maxlength="128" show-word-limit /></el-form-item>
        <el-form-item label="公文类型" required>
          <el-select v-model="form.docType" style="width:100%" :disabled="Boolean(editingDocId)">
            <el-option v-for="value in documentTypes" :key="value" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="正文" required><el-input v-model="form.content" type="textarea" :rows="7" /></el-form-item>
        <el-form-item label="管理员固定审批流程">
          <div v-if="formWorkflow" class="flow-preview">
            <strong>{{ formWorkflow.workflowName }}（v{{ formWorkflow.version }}）</strong>
            <ol><li v-for="step in formWorkflow.steps" :key="step.stepId">{{ step.stepName }}：{{ step.approverName }}（{{ step.approverUsername }}）</li></ol>
          </div>
          <el-alert v-else type="warning" :closable="false" title="该公文类型尚未配置审批流程，请联系管理员。" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :disabled="!editingDocId && !formWorkflow" @click="submit">
          {{ editingDocId ? '按原流程重新提交' : '提交审批' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="progressVisible" title="审批流程与记录" width="min(720px, 92vw)">
      <el-table :data="progressTasks" size="small" empty-text="暂无流程任务">
        <el-table-column prop="roundNo" label="轮次" width="70" />
        <el-table-column prop="stepOrder" label="步骤" width="70" />
        <el-table-column prop="stepName" label="名称" min-width="120" />
        <el-table-column label="审批人" min-width="150"><template #default="scope">{{ approverName(scope.row.approverId) }}</template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="scope">{{ taskStatusTexts[scope.row.status] }}</template></el-table-column>
      </el-table>
      <el-divider>审批意见</el-divider>
      <el-timeline>
        <el-timeline-item v-for="item in history" :key="item.approvalId" :timestamp="item.approvalTime">
          <strong>第 {{ item.roundNo || 1 }} 轮 · {{ item.stepName || '原单步审批' }} · {{ approverName(item.approverId) }}：{{ item.action }}</strong>
          <p>{{ item.opinion || '无审批意见' }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!history.length" description="暂无审批意见" />
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { documentAPI } from '@/api/office.js'
import { useOfficeAccess } from '@/composables/useOfficeAccess.js'
import PageBreadcrumb from '@/components/business/PageBreadcrumb.vue'

const { currentUser, hasPermission } = useOfficeAccess()
const canSelf = computed(() => hasPermission('document:self'))
const canApprove = computed(() => hasPermission('document:approve'))
const canManage = computed(() => hasPermission('document:manage'))
const isStudent = computed(() => currentUser.value?.roles?.includes('STUDENT'))
const tab = ref(canManage.value ? 'config' : (canSelf.value ? 'mine' : 'pending'))
const loading = ref(false)
const mine = ref([])
const pending = ref([])
const approvers = ref([])
const candidates = ref([])
const workflows = ref([])
const visible = ref(false)
const editingDocId = ref(null)
const progressVisible = ref(false)
const progressTasks = ref([])
const history = ref([])
const allDocumentTypes = ['公文会签', '请示报告', '请假申请']
const documentTypes = computed(() => isStudent.value ? ['请假申请'] : allDocumentTypes)
const form = reactive({ title: '', docType: '请示报告', content: '' })
const workflowDocType = ref('请示报告')
const workflowForm = reactive({ workflowName: '', steps: [{ stepName: '审批', approverId: null }] })
const statusTexts = ['审批中', '已通过', '已拒绝', '已退回']
const statusTypes = ['warning', 'success', 'danger', 'info']
const taskStatusTexts = ['等待', '待审批', '已同意', '已拒绝', '已退回', '已取消']

const qualifiedApprovers = computed(() => candidates.value.filter(item => item.qualified))
const editingWorkflow = computed(() => workflows.value.find(item => item.docType === workflowDocType.value) || null)
const formWorkflow = computed(() => workflows.value.find(item => item.docType === form.docType) || null)
const userTypeName = type => ({ 2: '教师', 3: '教职工', 4: '管理员' }[type] || '用户')
const approverName = id => {
  const item = [...approvers.value, ...candidates.value].find(value => value.userId === id)
  return item ? `${item.displayName}（${item.username}）` : (id ? `用户 ${id}` : '—')
}

const load = async () => {
  loading.value = true
  try {
    const tasks = []
    if (canSelf.value) {
      tasks.push(documentAPI.approvers().then(r => { approvers.value = r.data || [] }))
      tasks.push(documentAPI.workflows().then(r => { workflows.value = r.data || [] }))
      tasks.push(documentAPI.initiated().then(r => { mine.value = r.data || [] }))
    }
    if (canApprove.value) tasks.push(documentAPI.pending().then(r => { pending.value = r.data || [] }))
    if (canManage.value) tasks.push(documentAPI.approverCandidates().then(r => { candidates.value = (r.data || []).map(item => ({ ...item, qualified: !!item.qualified })) }))
    await Promise.all(tasks)
    if (canManage.value) resetWorkflowEditor()
  } finally {
    loading.value = false
  }
}

const resetWorkflowEditor = () => {
  const current = workflows.value.find(item => item.docType === workflowDocType.value)
  workflowForm.workflowName = current?.workflowName || `${workflowDocType.value}审批流程`
  workflowForm.steps = current?.steps?.length
    ? current.steps.map(step => ({ stepName: step.stepName, approverId: step.approverId }))
    : [{ stepName: '审批', approverId: qualifiedApprovers.value[0]?.userId ?? null }]
}
const addStep = () => {
  if (workflowForm.steps.length >= 10) return ElMessage.warning('最多配置10个审批步骤')
  workflowForm.steps.push({ stepName: `第${workflowForm.steps.length + 1}步审批`, approverId: qualifiedApprovers.value[0]?.userId ?? null })
}
const removeStep = index => workflowForm.steps.splice(index, 1)
const moveStep = (index, offset) => {
  const target = index + offset
  if (target < 0 || target >= workflowForm.steps.length) return
  const [item] = workflowForm.steps.splice(index, 1)
  workflowForm.steps.splice(target, 0, item)
}
const changeQualification = async (row, enabled) => {
  try {
    await documentAPI.updateApprover(row.userId, { displayName: row.displayName || row.username, enabled })
    ElMessage.success(enabled ? '已授予审批资格' : '已停用审批资格')
    await load()
  } catch (error) {
    row.qualified = !enabled
  }
}
const saveQualificationName = async row => {
  await documentAPI.updateApprover(row.userId, { displayName: row.displayName || row.username, enabled: true })
  ElMessage.success('审批人显示名称已保存')
  await load()
}
const saveWorkflow = async () => {
  if (!workflowForm.workflowName.trim() || workflowForm.steps.some(step => !step.stepName.trim() || !step.approverId)) {
    return ElMessage.warning('请填写流程名称，并为每一步指定审批人')
  }
  await documentAPI.saveWorkflow(workflowDocType.value, {
    workflowName: workflowForm.workflowName,
    steps: workflowForm.steps.map(step => ({ stepName: step.stepName, approverId: step.approverId })),
  })
  ElMessage.success('新流程版本已启用，已有公文不受影响')
  await load()
}

const openStart = () => {
  editingDocId.value = null
  form.title = ''
  form.docType = isStudent.value ? '请假申请' : '请示报告'
  form.content = ''
  visible.value = true
}
const openResubmit = row => {
  editingDocId.value = row.docId
  form.title = row.title
  form.docType = row.docType
  form.content = row.content
  visible.value = true
}
const submit = async () => {
  if (!form.title.trim() || !form.content.trim()) return ElMessage.warning('请完整填写标题和正文')
  const data = { title: form.title, docType: form.docType, content: form.content }
  if (editingDocId.value) await documentAPI.resubmit(editingDocId.value, data)
  else await documentAPI.start(data)
  ElMessage.success(editingDocId.value ? '公文已按原固定流程重新提交' : '公文已进入固定审批流程')
  visible.value = false
  await load()
}
const approve = async (row, action) => {
  const { value } = await ElMessageBox.prompt(`请输入“${action}”意见`, '审批意见', { inputValue: action })
  await documentAPI.approve(row.docId, { action, opinion: value })
  ElMessage.success(action === '同意' ? '已完成当前步骤' : (action === '退回' ? '已退回发起人修改' : '已拒绝公文'))
  await load()
}
const remind = async row => {
  await documentAPI.remind(row.docId)
  ElMessage.success('催办通知已发送给当前步骤审批人')
}
const showProgress = async row => {
  const [taskResult, historyResult] = await Promise.all([documentAPI.tasks(row.docId), documentAPI.history(row.docId)])
  progressTasks.value = taskResult.data || []
  history.value = historyResult.data || []
  progressVisible.value = true
}

onMounted(load)
</script>

<style scoped>
@import '@/assets/office-workspace.css';
.flow-tip { margin-bottom: 16px; }
.config-grid { display: grid; grid-template-columns: minmax(360px, .9fr) minmax(460px, 1.1fr); gap: 16px; }
.config-header, .config-actions { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.step-editor { display: grid; gap: 10px; width: 100%; }
.step-row { display: grid; grid-template-columns: 28px minmax(120px, .8fr) minmax(180px, 1fr) auto; gap: 8px; align-items: center; }
.step-index { width: 26px; height: 26px; display: grid; place-items: center; border-radius: 50%; background: var(--el-color-primary-light-9); color: var(--el-color-primary); }
.flow-preview { width: 100%; padding: 12px; border: 1px solid var(--el-border-color); border-radius: 8px; }
.flow-preview ol { margin: 10px 0; padding-left: 22px; line-height: 1.9; }
@media (max-width: 1000px) { .config-grid { grid-template-columns: 1fr; } }
@media (max-width: 720px) { .step-row { grid-template-columns: 28px 1fr; } .step-row :deep(.el-select), .step-row .el-button-group { grid-column: 2; } }
</style>
