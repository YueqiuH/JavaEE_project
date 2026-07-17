<template>
  <section class="office-page">
    <header class="office-page__header">
      <div><h1>教职工工作计划与协同</h1><p>维护本人周月计划，拥有管理权限的负责人可统一查看、点评并指派任务。</p></div>
      <div class="office-page__actions">
        <el-button v-if="canSelf" @click="loadMine">我的计划</el-button>
        <el-button v-if="canManage" @click="loadAll">全部计划</el-button>
        <el-button v-if="canManage" type="success" @click="openAssign">指派任务</el-button>
        <el-button v-if="canSelf" type="primary" @click="openCreate">新建计划</el-button>
      </div>
    </header>

    <el-result v-if="!canSelf&&!canManage" class="office-page__empty" icon="warning" title="无工作计划访问权限" />
    <template v-else>
      <div class="office-page__grid">
        <el-card v-for="row in rows" :key="row.planId" class="plan-card" shadow="hover">
          <template #header><div class="card-title"><strong>{{ row.planType }}</strong><div><el-tag v-if="isAssigned(row)" type="warning" effect="plain">领导指派</el-tag><el-tag :type="row.status===2?'success':'primary'">{{ row.status===2?'已完成':'进行中' }}</el-tag></div></div></template>
          <p class="plan-content">{{ row.content }}</p>
          <p class="office-page__muted">执行人：{{ assigneeName(row.userId) }} · {{ row.startDate || '未设置' }} 至 {{ row.endDate || '未设置' }}</p>
          <el-alert v-if="row.supervisorComment" :title="`上级点评：${row.supervisorComment}`" type="info" :closable="false" />
          <div class="plan-actions"><el-button v-if="isMine(row)" link type="primary" @click="edit(row)">{{ isAssigned(row) ? '更新状态' : '编辑' }}</el-button><el-button v-if="canManage" link @click="comment(row)">点评</el-button><el-button v-if="isMine(row)||canManage" link type="danger" @click="remove(row)">删除</el-button></div>
        </el-card>
      </div>
      <el-empty v-if="!rows.length" description="暂无工作计划" />
    </template>

    <el-dialog v-model="visible" :title="form.planType==='指派任务'?'更新任务状态':'工作计划'" width="min(560px,92vw)">
      <el-form label-position="top">
        <el-form-item label="计划类型"><el-radio-group v-model="form.planType" :disabled="form.planType==='指派任务'"><el-radio-button value="周计划">周计划</el-radio-button><el-radio-button value="月计划">月计划</el-radio-button><el-radio-button v-if="form.planType==='指派任务'" value="指派任务">指派任务</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="计划内容"><el-input v-model="form.content" type="textarea" :rows="5" :disabled="form.planType==='指派任务'" /></el-form-item>
        <el-form-item label="起止日期"><el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" :disabled="form.planType==='指派任务'" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option label="进行中" :value="1" /><el-option label="已完成" :value="2" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="指派工作任务" width="min(560px,92vw)">
      <el-form label-position="top">
        <el-form-item label="任务接收人" required><el-select v-model="assignForm.assigneeId" style="width:100%" placeholder="请选择教职工"><el-option v-for="item in assignees" :key="item.userId" :value="item.userId" :label="`${item.username}（${userTypeName(item.userType)}）`" /></el-select></el-form-item>
        <el-form-item label="任务内容" required><el-input v-model="assignForm.content" type="textarea" :rows="5" placeholder="请输入需要完成的工作任务" /></el-form-item>
        <el-form-item label="起止日期"><el-date-picker v-model="assignDateRange" type="daterange" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="assignVisible=false">取消</el-button><el-button type="primary" @click="assignTask">确认指派</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { workPlanAPI } from '@/api/office.js'
import { useOfficeAccess } from '@/composables/useOfficeAccess.js'

const { userId, hasPermission } = useOfficeAccess()
const canSelf = computed(() => hasPermission('work-plan:self'))
const canManage = computed(() => hasPermission('work-plan:manage'))
const rows = ref([])
const assignees = ref([])
const visible = ref(false)
const assignVisible = ref(false)
const dateRange = ref([])
const assignDateRange = ref([])
const form = reactive({ planId: null, planType: '周计划', content: '', status: 1 })
const assignForm = reactive({ assigneeId: null, content: '' })
const isMine = (row) => row.userId === userId.value
const isAssigned = (row) => row.planType === '指派任务'
const userTypeName = (type) => ({ 2: '教师', 3: '教职工' }[type] || '用户')
const assigneeName = (id) => { if (id === userId.value) return '本人'; const item = assignees.value.find(value => value.userId === id); return item ? item.username : `用户 ${id}` }
const loadMine = async () => { rows.value=(await workPlanAPI.getMine()).data||[] }
const loadAll = async () => { rows.value=(await workPlanAPI.list()).data||[] }
const loadAssignees = async () => { assignees.value=(await workPlanAPI.assignees()).data||[] }
const openCreate = () => { Object.assign(form,{planId:null,planType:'周计划',content:'',status:1}); dateRange.value=[]; visible.value=true }
const edit = (row) => { Object.assign(form,{planId:row.planId,planType:row.planType,content:row.content,status:row.status}); dateRange.value=[row.startDate,row.endDate].filter(Boolean); visible.value=true }
const save = async () => { await workPlanAPI.save({...form,startDate:dateRange.value?.[0],endDate:dateRange.value?.[1]}); ElMessage.success('计划已保存'); visible.value=false; await loadMine() }
const openAssign = async () => { if (!assignees.value.length) await loadAssignees(); if (!assignees.value.length) return ElMessage.warning('当前没有可接收任务的教职工'); Object.assign(assignForm,{assigneeId:assignees.value[0].userId,content:''}); assignDateRange.value=[]; assignVisible.value=true }
const assignTask = async () => { if (!assignForm.assigneeId || !assignForm.content.trim()) return ElMessage.warning('请选择接收人并填写任务内容'); await workPlanAPI.assign({...assignForm,startDate:assignDateRange.value?.[0],endDate:assignDateRange.value?.[1]}); ElMessage.success('任务已指派'); assignVisible.value=false; await loadAll() }
const comment = async (row) => { const {value}=await ElMessageBox.prompt('请输入负责人点评','计划点评',{inputValue:row.supervisorComment||''}); await workPlanAPI.comment(row.planId,value); ElMessage.success('点评成功'); await loadAll() }
const remove = async (row) => { await ElMessageBox.confirm('确认删除该计划？','删除确认'); await workPlanAPI.remove(row.planId); ElMessage.success('已删除'); if(canManage.value&&!isMine(row)) await loadAll(); else await loadMine() }
onMounted(() => { if(canSelf.value) loadMine(); else if(canManage.value) loadAll(); if(canManage.value) loadAssignees() })
</script>

<style scoped>
@import '@/assets/office-workspace.css';
.card-title{display:flex;align-items:center;justify-content:space-between}.card-title>div{display:flex;gap:8px}.plan-content{min-height:52px;line-height:1.65;white-space:pre-wrap}.plan-actions{display:flex;justify-content:flex-end;margin-top:14px}
</style>
