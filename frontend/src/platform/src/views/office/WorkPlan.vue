<template>
  <section class="office-page">
    <header class="office-page__header">
      <div><h1>教职工工作计划与协同</h1><p>维护本人周月计划，负责人可统一查看和点评。</p></div>
      <div class="office-page__actions">
        <el-button v-if="canSelf" @click="loadMine">我的计划</el-button>
        <el-button v-if="canManage" @click="loadAll">全部计划</el-button>
        <el-button v-if="canSelf" type="primary" @click="openCreate">新建计划</el-button>
      </div>
    </header>

    <el-result v-if="!canSelf" class="office-page__empty" icon="warning" title="无工作计划访问权限" />
    <template v-else>
      <div class="office-page__grid">
        <el-card v-for="row in rows" :key="row.planId" class="plan-card" shadow="hover">
          <template #header><div class="card-title"><strong>{{ row.planType }}</strong><el-tag :type="row.status===2?'success':'primary'">{{ row.status===2?'已完成':'进行中' }}</el-tag></div></template>
          <p class="plan-content">{{ row.content }}</p>
          <p class="office-page__muted">负责人：{{ row.userId }} · {{ row.startDate || '未设置' }} 至 {{ row.endDate || '未设置' }}</p>
          <el-alert v-if="row.supervisorComment" :title="`上级点评：${row.supervisorComment}`" type="info" :closable="false" />
          <div class="plan-actions"><el-button v-if="isMine(row)" link type="primary" @click="edit(row)">编辑</el-button><el-button v-if="canManage" link @click="comment(row)">点评</el-button><el-button v-if="isMine(row)||canManage" link type="danger" @click="remove(row)">删除</el-button></div>
        </el-card>
      </div>
      <el-empty v-if="!rows.length" description="暂无工作计划" />
    </template>

    <el-dialog v-model="visible" title="工作计划" width="min(560px,92vw)">
      <el-form label-position="top">
        <el-form-item label="计划类型"><el-radio-group v-model="form.planType"><el-radio-button value="周计划">周计划</el-radio-button><el-radio-button value="月计划">月计划</el-radio-button></el-radio-group></el-form-item>
        <el-form-item label="计划内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="起止日期"><el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option label="进行中" :value="1" /><el-option label="已完成" :value="2" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
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
const visible = ref(false)
const dateRange = ref([])
const form = reactive({ planId: null, planType: '周计划', content: '', status: 1 })
const isMine = (row) => row.userId === userId.value
const loadMine = async () => { rows.value=(await workPlanAPI.getMine()).data||[] }
const loadAll = async () => { rows.value=(await workPlanAPI.list()).data||[] }
const openCreate = () => { Object.assign(form,{planId:null,planType:'周计划',content:'',status:1}); dateRange.value=[]; visible.value=true }
const edit = (row) => { Object.assign(form,{planId:row.planId,planType:row.planType,content:row.content,status:row.status}); dateRange.value=[row.startDate,row.endDate].filter(Boolean); visible.value=true }
const save = async () => { await workPlanAPI.save({...form,startDate:dateRange.value?.[0],endDate:dateRange.value?.[1]}); ElMessage.success('计划已保存'); visible.value=false; await loadMine() }
const comment = async (row) => { const {value}=await ElMessageBox.prompt('请输入负责人点评','计划点评',{inputValue:row.supervisorComment||''}); await workPlanAPI.comment(row.planId,value); ElMessage.success('点评成功'); await loadAll() }
const remove = async (row) => { await ElMessageBox.confirm('确认删除该计划？','删除确认'); await workPlanAPI.remove(row.planId); ElMessage.success('已删除'); if(canManage.value&&!isMine(row)) await loadAll(); else await loadMine() }
onMounted(() => { if(canSelf.value) loadMine() })
</script>

<style scoped>
@import '@/assets/office-workspace.css';
.card-title{display:flex;align-items:center;justify-content:space-between}.plan-content{min-height:52px;line-height:1.65;white-space:pre-wrap}.plan-actions{display:flex;justify-content:flex-end;margin-top:14px}
</style>
