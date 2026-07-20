<template>
  <section class="office-page">
    <PageBreadcrumb domain="office" title="工作计划与勤工俭学" />
    <header class="office-page__header">
      <div><h1>工作计划与勤工俭学</h1><p>教师或教职工可向学生指派带工资的勤工俭学任务，学生提交完成后由原指派人确认发薪。</p></div>
      <div class="office-page__actions">
        <el-button v-if="canSelf" @click="loadMine">我的计划</el-button>
        <el-button v-if="canManage" @click="loadAll">全部计划</el-button>
        <el-button v-if="canAssignWorkStudy" type="success" @click="openAssign">指派勤工俭学</el-button>
        <el-button v-if="canSelf&&!isStudent" type="primary" @click="openCreate">新建计划</el-button>
      </div>
    </header>

    <el-result v-if="!canSelf&&!canManage" class="office-page__empty" icon="warning" title="无工作计划访问权限" />
    <template v-else>
      <div class="office-page__grid">
        <el-card v-for="row in rows" :key="row.planId" class="plan-card" shadow="hover">
          <template #header><div class="card-title"><strong>{{ row.planType }}</strong><div><el-tag v-if="isWorkStudy(row)" type="warning" effect="plain">勤工俭学</el-tag><el-tag v-else-if="isLegacyAssigned(row)" type="info" effect="plain">历史任务</el-tag><el-tag :type="statusType(row)">{{ statusText(row) }}</el-tag></div></div></template>
          <p class="plan-content">{{ row.content }}</p>
          <p class="office-page__muted">执行人：{{ assigneeName(row.userId) }} · {{ row.startDate || '未设置' }} 至 {{ row.endDate || '未设置' }}</p>
          <p v-if="isWorkStudy(row)" class="wage-line">工资：¥ {{ Number(row.wageAmount || 0).toFixed(2) }} · 指派人：{{ row.assignerId }} · {{ row.wagePaid ? '已计入学生余额' : '尚未发放' }}</p>
          <el-alert v-if="row.supervisorComment" :title="`上级点评：${row.supervisorComment}`" type="info" :closable="false" />
          <div class="plan-actions"><el-button v-if="isMine(row)&&!row.wagePaid" link type="primary" @click="edit(row)">{{ isWorkStudy(row) ? '提交进度' : '编辑' }}</el-button><el-button v-if="canManage" link @click="comment(row)">点评</el-button><el-button v-if="canSettle(row)" link type="success" @click="settle(row)">确认并发薪</el-button><el-button v-if="canDelete(row)" link type="danger" @click="remove(row)">删除</el-button></div>
        </el-card>
      </div>
      <el-empty v-if="!rows.length" description="暂无工作计划" />
    </template>

    <el-dialog v-model="visible" :title="['勤工俭学','指派任务'].includes(form.planType)?'提交任务进度':'工作计划'" width="min(560px,92vw)">
      <el-form label-position="top">
        <el-form-item label="计划类型"><el-radio-group v-model="form.planType" :disabled="['勤工俭学','指派任务'].includes(form.planType)"><el-radio-button value="周计划">周计划</el-radio-button><el-radio-button value="月计划">月计划</el-radio-button><el-radio-button v-if="form.planType==='勤工俭学'" value="勤工俭学">勤工俭学</el-radio-button><el-radio-button v-if="form.planType==='指派任务'" value="指派任务">历史指派任务</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="计划内容"><el-input v-model="form.content" type="textarea" :rows="5" :disabled="['勤工俭学','指派任务'].includes(form.planType)" /></el-form-item>
        <el-form-item label="起止日期"><el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" :disabled="['勤工俭学','指派任务'].includes(form.planType)" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option label="进行中" :value="1" /><el-option :label="form.planType==='勤工俭学'?'提交完成，等待确认':'已完成'" :value="2" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="指派勤工俭学任务" width="min(560px,92vw)">
      <el-form label-position="top">
        <el-form-item label="勤工俭学学生" required><el-select v-model="assignForm.assigneeId" style="width:100%" placeholder="请选择学生"><el-option v-for="item in assignees" :key="item.userId" :value="item.userId" :label="`${item.username}（${userTypeName(item.userType)}）`" /></el-select></el-form-item>
        <el-form-item label="任务内容" required><el-input v-model="assignForm.content" type="textarea" :rows="5" placeholder="请输入需要完成的工作任务" /></el-form-item>
        <el-form-item label="任务工资（元）" required><el-input-number v-model="assignForm.wageAmount" :min="0.01" :max="10000" :precision="2" style="width:100%" /></el-form-item>
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
import PageBreadcrumb from '@/components/business/PageBreadcrumb.vue'

const { currentUser, userId, hasPermission } = useOfficeAccess()
const canSelf = computed(() => hasPermission('work-plan:self'))
const canManage = computed(() => hasPermission('work-plan:manage'))
const isStudent = computed(() => currentUser.value?.roles?.includes('STUDENT'))
const canAssignWorkStudy = computed(() => canManage.value && currentUser.value?.roles?.some(role => ['TEACHER', 'STAFF'].includes(role)))
const rows = ref([])
const assignees = ref([])
const visible = ref(false)
const assignVisible = ref(false)
const dateRange = ref([])
const assignDateRange = ref([])
const form = reactive({ planId: null, planType: '周计划', content: '', status: 1 })
const assignForm = reactive({ assigneeId: null, content: '', wageAmount: 100 })
const isMine = (row) => row.userId === userId.value
const isWorkStudy = (row) => row.planType === '勤工俭学'
const isLegacyAssigned = (row) => row.planType === '指派任务'
const userTypeName = (type) => ({ 1: '学生', 2: '教师', 3: '教职工' }[type] || '用户')
const statusText = (row) => isWorkStudy(row) ? ({ 1: '进行中', 2: '待确认', 3: '已结算' }[row.status] || '未知') : (row.status === 2 ? '已完成' : '进行中')
const statusType = (row) => isWorkStudy(row) ? ({ 1: 'primary', 2: 'warning', 3: 'success' }[row.status] || 'info') : (row.status === 2 ? 'success' : 'primary')
const canSettle = (row) => canAssignWorkStudy.value && row.assignerId === userId.value && isWorkStudy(row) && row.status === 2 && !row.wagePaid
const canDelete = (row) => isWorkStudy(row) ? (canAssignWorkStudy.value && row.assignerId === userId.value && !row.wagePaid) : (isMine(row) || canManage.value)
const assigneeName = (id) => { if (id === userId.value) return '本人'; const item = assignees.value.find(value => value.userId === id); return item ? item.username : `用户 ${id}` }
const loadMine = async () => { try { rows.value=(await workPlanAPI.getMine()).data||[] } catch(e) { ElMessage.error('操作失败'); console.error(e) } }
const loadAll = async () => { try { rows.value=(await workPlanAPI.list()).data||[] } catch(e) { ElMessage.error('操作失败'); console.error(e) } }
const loadAssignees = async () => { assignees.value=(await workPlanAPI.assignees()).data||[] }
const openCreate = () => { Object.assign(form,{planId:null,planType:'周计划',content:'',status:1}); dateRange.value=[]; visible.value=true }
const edit = (row) => { Object.assign(form,{planId:row.planId,planType:row.planType,content:row.content,status:row.status}); dateRange.value=[row.startDate,row.endDate].filter(Boolean); visible.value=true }
const save = async () => { try { await workPlanAPI.save({...form,startDate:dateRange.value?.[0],endDate:dateRange.value?.[1]}); ElMessage.success('计划已保存'); visible.value=false; await loadMine() } catch(e) { ElMessage.error('操作失败'); console.error(e) } }
const openAssign = async () => { if (!assignees.value.length) await loadAssignees(); if (!assignees.value.length) return ElMessage.warning('当前没有可接收任务的学生'); Object.assign(assignForm,{assigneeId:assignees.value[0].userId,content:'',wageAmount:100}); assignDateRange.value=[]; assignVisible.value=true }
const assignTask = async () => { if (!assignForm.assigneeId || !assignForm.content.trim() || !assignForm.wageAmount) return ElMessage.warning('请选择学生并填写任务内容和工资'); try { await workPlanAPI.assign({...assignForm,startDate:assignDateRange.value?.[0],endDate:assignDateRange.value?.[1]}); ElMessage.success('勤工俭学任务已指派'); assignVisible.value=false; await loadAll() } catch(e) { ElMessage.error('操作失败'); console.error(e) } }
const settle = async (row) => { try { await ElMessageBox.confirm(`确认任务完成并向学生发放 ¥${Number(row.wageAmount).toFixed(2)}？工资将直接计入余额且不可撤销。`,'确认发薪'); await workPlanAPI.settle(row.planId); ElMessage.success('工资已计入学生余额并生成流水'); await loadAll() } catch(e) { if (e !== 'cancel' && e !== 'close') { ElMessage.error('操作失败'); console.error(e) } } }
const comment = async (row) => { const {value}=await ElMessageBox.prompt('请输入负责人点评','计划点评',{inputValue:row.supervisorComment||''}); await workPlanAPI.comment(row.planId,value); ElMessage.success('点评成功'); await loadAll() }
const remove = async (row) => { await ElMessageBox.confirm('确认删除该计划？','删除确认'); await workPlanAPI.remove(row.planId); ElMessage.success('已删除'); if(canManage.value&&!isMine(row)) await loadAll(); else await loadMine() }
onMounted(() => { if(canSelf.value) loadMine(); else if(canManage.value) loadAll(); if(canAssignWorkStudy.value) loadAssignees() })
</script>

<style scoped>
@import '@/assets/office-workspace.css';
.card-title{display:flex;align-items:center;justify-content:space-between}.card-title>div{display:flex;gap:8px}.plan-content{min-height:52px;line-height:1.65;white-space:pre-wrap}.wage-line{color:var(--el-color-success);font-weight:600}.plan-actions{display:flex;justify-content:flex-end;margin-top:14px}
</style>
