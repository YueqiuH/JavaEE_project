<template>
  <section class="office-page">
    <header class="office-page__header">
      <div><h1>官方公文流转 OA</h1><p>以当前身份发起、审批、退回与催办公文。</p></div>
      <div class="office-page__actions"><el-button @click="load">刷新</el-button><el-button v-if="canSelf" type="primary" @click="visible=true">发起公文</el-button></div>
    </header>

    <el-result v-if="!canSelf&&!canApprove" class="office-page__empty" icon="warning" title="无公文访问权限" />
    <el-card v-else class="office-page__section" shadow="never">
      <el-tabs v-model="tab">
        <el-tab-pane v-if="canSelf" label="我发起的" name="mine">
          <el-table :data="mine" empty-text="暂无发起的公文">
            <el-table-column prop="title" label="标题" min-width="180" /><el-table-column prop="docType" label="类型" width="90" /><el-table-column prop="currentApproverId" label="当前审批人" width="110" />
            <el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="statusTypes[scope.row.status]">{{ statusTexts[scope.row.status] }}</el-tag></template></el-table-column>
            <el-table-column label="操作" min-width="180"><template #default="scope"><el-button link @click="showHistory(scope.row)">审批记录</el-button><el-button link type="warning" :disabled="scope.row.status!==0" @click="remind(scope.row)">催办</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane v-if="canApprove" :label="`待我审批 (${pending.length})`" name="pending">
          <el-table :data="pending" empty-text="当前没有待审批公文">
            <el-table-column prop="title" label="标题" min-width="170" /><el-table-column prop="docType" label="类型" width="90" /><el-table-column prop="content" label="正文" min-width="220" show-overflow-tooltip /><el-table-column prop="initiatorId" label="发起人" width="90" />
            <el-table-column label="操作" min-width="230"><template #default="scope"><el-button link type="success" @click="approve(scope.row,'同意')">同意</el-button><el-button link type="danger" @click="approve(scope.row,'拒绝')">拒绝</el-button><el-button link type="warning" @click="approve(scope.row,'退回')">退回</el-button><el-button link @click="showHistory(scope.row)">记录</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="visible" title="发起公文" width="min(640px,92vw)">
      <el-form label-position="top"><el-form-item label="标题"><el-input v-model="form.title" /></el-form-item><el-form-item label="公文类型"><el-select v-model="form.docType"><el-option v-for="value in ['会签','请示','请假','报告']" :key="value" :value="value" /></el-select></el-form-item><el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="7" /></el-form-item><el-form-item label="审批链"><el-input v-model="chain" placeholder="按顺序填写用户 ID，如：2,3,4" /></el-form-item></el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="start">提交审批</el-button></template>
    </el-dialog>
    <el-dialog v-model="historyVisible" title="审批记录"><el-timeline><el-timeline-item v-for="item in history" :key="item.approvalId" :timestamp="item.approvalTime"><strong>{{ item.approverId }}：{{ item.action }}</strong><p>{{ item.opinion||'无审批意见' }}</p></el-timeline-item></el-timeline><el-empty v-if="!history.length" description="暂无审批记录" /></el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { documentAPI } from '@/api/office.js'
import { useOfficeAccess } from '@/composables/useOfficeAccess.js'

const { hasPermission } = useOfficeAccess()
const canSelf = computed(() => hasPermission('document:self'))
const canApprove = computed(() => hasPermission('document:approve'))
const tab = ref(canSelf.value ? 'mine' : 'pending')
const mine = ref([])
const pending = ref([])
const visible = ref(false)
const historyVisible = ref(false)
const history = ref([])
const chain = ref('2')
const form = reactive({ title: '', docType: '请示', content: '' })
const statusTexts = ['审批中','已通过','已拒绝','已退回']
const statusTypes = ['warning','success','danger','info']
const load = async () => { const tasks=[]; if(canSelf.value)tasks.push(documentAPI.initiated().then(r=>{mine.value=r.data||[]})); if(canApprove.value)tasks.push(documentAPI.pending().then(r=>{pending.value=r.data||[]})); await Promise.all(tasks) }
const start = async () => { const ids=chain.value.split(',').map(v=>Number(v.trim())).filter(Number.isInteger); await documentAPI.start({...form,approvalChain:JSON.stringify(ids)}); ElMessage.success('公文已进入审批流程'); visible.value=false; await load() }
const approve = async (row, action) => { const {value}=await ElMessageBox.prompt(`请输入“${action}”意见`,'审批意见',{inputValue:action}); await documentAPI.approve(row.docId,{action,opinion:value}); ElMessage.success('审批完成'); await load() }
const remind = async (row) => { await documentAPI.remind(row.docId); ElMessage.success('催办通知已发送') }
const showHistory = async (row) => { history.value=(await documentAPI.history(row.docId)).data||[]; historyVisible.value=true }
onMounted(load)
</script>

<style scoped>@import '@/assets/office-workspace.css';</style>
