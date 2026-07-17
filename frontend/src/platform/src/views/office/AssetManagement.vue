<template>
  <section class="office-page">
    <header class="office-page__header">
      <div><h1>固定资产管理与申领</h1><p>资产台账、领用申请与部门审批。</p></div>
      <div class="office-page__actions">
        <el-input-number v-if="canRead" v-model="deptId" :min="1" placeholder="部门ID" />
        <el-button v-if="canRead" :loading="loading" @click="load">查询</el-button>
        <el-button v-if="canApply" type="primary" @click="openDialog('apply')">发起申请</el-button>
        <el-button v-if="canManage" @click="openDialog('save')">新增资产</el-button>
      </div>
    </header>

    <el-result v-if="!canRead" class="office-page__empty" icon="warning" title="无资产访问权限" />
    <el-card v-else class="office-page__section" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe empty-text="暂无资产数据">
        <el-table-column prop="assetName" label="资产名称" min-width="150" />
        <el-table-column prop="assetType" label="类型" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="deptId" label="部门" width="90" />
        <el-table-column prop="applyUserId" label="申请人" width="100" />
        <el-table-column label="审批" width="110"><template #default="scope"><el-tag :type="approvalTypes[scope.row.approveStatus]">{{ approvalTexts[scope.row.approveStatus] }}</el-tag></template></el-table-column>
        <el-table-column v-if="canManage" label="管理" min-width="210"><template #default="scope"><el-button v-if="scope.row.approveStatus===0" link type="success" @click="approve(scope.row,1)">通过</el-button><el-button v-if="scope.row.approveStatus===0" link type="danger" @click="approve(scope.row,0)">拒绝</el-button><el-button link type="danger" @click="remove(scope.row)">删除</el-button></template></el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="dialogMode==='apply'?'资产申领':'新增资产'" width="min(520px,92vw)">
      <el-form label-position="top">
        <el-form-item label="资产名称"><el-input v-model="form.assetName" /></el-form-item>
        <el-form-item label="资产类型"><el-select v-model="form.assetType"><el-option v-for="value in ['设备','办公用品','其他']" :key="value" :value="value" /></el-select></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="form.quantity" :min="1" /></el-form-item>
        <el-form-item label="所属部门 ID"><el-input-number v-model="form.deptId" :min="1" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="submit">提交</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assetAPI } from '@/api/office.js'
import { useOfficeAccess } from '@/composables/useOfficeAccess.js'

const { hasPermission } = useOfficeAccess()
const canRead = computed(() => hasPermission('asset:read'))
const canApply = computed(() => hasPermission('asset:apply'))
const canManage = computed(() => hasPermission('asset:manage'))
const deptId = ref()
const rows = ref([])
const loading = ref(false)
const visible = ref(false)
const dialogMode = ref('apply')
const form = reactive({ assetName: '', assetType: '设备', quantity: 1, deptId: 1 })
const approvalTexts = ['待审批', '已通过', '已拒绝']
const approvalTypes = ['warning', 'success', 'danger']

const load = async () => { if (!canRead.value) return; loading.value=true; try { rows.value=(await assetAPI.list(deptId.value)).data||[] } finally { loading.value=false } }
const openDialog = (mode) => { dialogMode.value=mode; Object.assign(form,{assetName:'',assetType:'设备',quantity:1,deptId:deptId.value||1}); visible.value=true }
const submit = async () => { if(dialogMode.value==='apply') await assetAPI.apply({...form}); else await assetAPI.save({...form}); ElMessage.success(dialogMode.value==='apply'?'申请已提交':'资产已新增'); visible.value=false; await load() }
const approve = async (row, approved) => { await assetAPI.approve(row.assetId,approved); ElMessage.success('审批完成'); await load() }
const remove = async (row) => { await ElMessageBox.confirm('确认删除该资产记录？','删除确认'); await assetAPI.remove(row.assetId); ElMessage.success('已删除'); await load() }
onMounted(load)
</script>

<style scoped>@import '@/assets/office-workspace.css';</style>
