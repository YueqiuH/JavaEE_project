<template>
  <section class="office-page">
    <header class="office-page__header">
      <div><h1>官方公文流转 OA</h1><p>三类公文采用固定单步流程，由发起人从两名指定负责人中选择一人审批。</p></div>
      <div class="office-page__actions"><el-button @click="load">刷新</el-button><el-button v-if="canSelf" type="primary" @click="openStart">发起公文</el-button></div>
    </header>

    <el-alert v-if="canSelf" class="flow-tip" type="info" :closable="false" show-icon title="审批人不能审批自己发起的公文；退回后由原发起人修改并重新提交。" />
    <el-result v-if="!canSelf&&!canApprove" class="office-page__empty" icon="warning" title="无公文访问权限" />
    <el-card v-else class="office-page__section" shadow="never">
      <el-tabs v-model="tab">
        <el-tab-pane v-if="canSelf" label="我发起的" name="mine">
          <el-table :data="mine" empty-text="暂无发起的公文">
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column prop="docType" label="类型" width="110" />
            <el-table-column label="审批人" min-width="150"><template #default="scope">{{ approverName(scope.row.currentApproverId, scope.row.approvalChain) }}</template></el-table-column>
            <el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="statusTypes[scope.row.status]">{{ statusTexts[scope.row.status] }}</el-tag></template></el-table-column>
            <el-table-column label="操作" min-width="250"><template #default="scope"><el-button link @click="showHistory(scope.row)">审批记录</el-button><el-button v-if="scope.row.status===3" link type="primary" @click="openResubmit(scope.row)">修改并重新提交</el-button><el-button v-else link type="warning" :disabled="scope.row.status!==0" @click="remind(scope.row)">催办</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane v-if="canApprove" :label="`待我审批 (${pending.length})`" name="pending">
          <el-table :data="pending" empty-text="当前没有待审批公文">
            <el-table-column prop="title" label="标题" min-width="170" />
            <el-table-column prop="docType" label="类型" width="110" />
            <el-table-column prop="content" label="正文" min-width="220" show-overflow-tooltip />
            <el-table-column prop="initiatorId" label="发起人" width="90" />
            <el-table-column label="操作" min-width="230"><template #default="scope"><el-button link type="success" @click="approve(scope.row,'同意')">同意</el-button><el-button link type="danger" @click="approve(scope.row,'拒绝')">拒绝</el-button><el-button link type="warning" @click="approve(scope.row,'退回')">退回修改</el-button><el-button link @click="showHistory(scope.row)">记录</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="visible" :title="editingDocId ? '修改并重新提交公文' : '发起公文'" width="min(640px,92vw)">
      <el-form label-position="top">
        <el-form-item label="标题" required><el-input v-model="form.title" maxlength="128" show-word-limit /></el-form-item>
        <el-form-item label="公文类型" required><el-select v-model="form.docType" style="width:100%"><el-option v-for="value in documentTypes" :key="value" :value="value" /></el-select></el-form-item>
        <el-form-item label="正文" required><el-input v-model="form.content" type="textarea" :rows="7" /></el-form-item>
        <el-form-item label="审批人（单步审批）" required>
          <el-select v-model="form.approverId" style="width:100%" placeholder="请选择一名负责人">
            <el-option v-for="item in approvers" :key="item.userId" :value="item.userId" :label="`${item.displayName}（${item.username}）`" :disabled="item.userId===userId" />
          </el-select>
        </el-form-item>
        <el-alert v-if="approvers.some(item=>item.userId===userId)" type="warning" :closable="false" title="您本人在审批人名单中，系统已禁止选择自己。" />
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="submit">{{ editingDocId ? '重新提交' : '提交审批' }}</el-button></template>
    </el-dialog>
    <el-dialog v-model="historyVisible" title="审批记录"><el-timeline><el-timeline-item v-for="item in history" :key="item.approvalId" :timestamp="item.approvalTime"><strong>{{ approverName(item.approverId) }}：{{ item.action }}</strong><p>{{ item.opinion||'无审批意见' }}</p></el-timeline-item></el-timeline><el-empty v-if="!history.length" description="暂无审批记录" /></el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { documentAPI } from '@/api/office.js'
import { useOfficeAccess } from '@/composables/useOfficeAccess.js'

const { userId, hasPermission } = useOfficeAccess()
const canSelf = computed(() => hasPermission('document:self'))
const canApprove = computed(() => hasPermission('document:approve'))
const tab = ref(canSelf.value ? 'mine' : 'pending')
const mine = ref([])
const pending = ref([])
const approvers = ref([])
const visible = ref(false)
const editingDocId = ref(null)
const historyVisible = ref(false)
const history = ref([])
const documentTypes = ['公文会签', '请示报告', '请假申请']
const form = reactive({ title: '', docType: '请示报告', content: '', approverId: null })
const statusTexts = ['审批中', '已通过', '已拒绝', '已退回']
const statusTypes = ['warning', 'success', 'danger', 'info']

const firstSelectableApprover = () => approvers.value.find(item => item.userId !== userId.value)?.userId ?? null
const resetForm = () => { form.title = ''; form.docType = '请示报告'; form.content = ''; form.approverId = firstSelectableApprover() }
const chainApproverId = (chain) => { try { return JSON.parse(chain || '[]')[0] ?? null } catch { return null } }
const approverName = (id, chain) => { const target = id ?? chainApproverId(chain); const item = approvers.value.find(value => value.userId === target); return item ? `${item.displayName}（${item.username}）` : (target ? `用户 ${target}` : '—') }

const load = async () => {
  const tasks = []
  if (canSelf.value) {
    tasks.push(documentAPI.approvers().then(r => { approvers.value = r.data || [] }))
    tasks.push(documentAPI.initiated().then(r => { mine.value = r.data || [] }))
  }
  if (canApprove.value) tasks.push(documentAPI.pending().then(r => { pending.value = r.data || [] }))
  await Promise.all(tasks)
}
const openStart = () => { editingDocId.value = null; resetForm(); visible.value = true }
const openResubmit = (row) => { editingDocId.value = row.docId; form.title = row.title; form.docType = documentTypes.includes(row.docType) ? row.docType : '请示报告'; form.content = row.content; const previous = chainApproverId(row.approvalChain); form.approverId = previous !== userId.value && approvers.value.some(item => item.userId === previous) ? previous : firstSelectableApprover(); visible.value = true }
const submit = async () => {
  if (!form.title.trim() || !form.content.trim() || !form.approverId) return ElMessage.warning('请完整填写标题、正文并选择审批人')
  const data = { title: form.title, docType: form.docType, content: form.content, approverId: form.approverId }
  if (editingDocId.value) await documentAPI.resubmit(editingDocId.value, data)
  else await documentAPI.start(data)
  ElMessage.success(editingDocId.value ? '公文已修改并重新提交' : '公文已进入单步审批流程')
  visible.value = false
  await load()
}
const approve = async (row, action) => { const { value } = await ElMessageBox.prompt(`请输入“${action}”意见`, '审批意见', { inputValue: action }); await documentAPI.approve(row.docId, { action, opinion: value }); ElMessage.success(action === '退回' ? '已退回发起人修改' : '审批完成'); await load() }
const remind = async (row) => { await documentAPI.remind(row.docId); ElMessage.success('催办通知已发送') }
const showHistory = async (row) => { history.value = (await documentAPI.history(row.docId)).data || []; historyVisible.value = true }
onMounted(load)
</script>

<style scoped>
@import '@/assets/office-workspace.css';
.flow-tip{margin-bottom:16px}
</style>
