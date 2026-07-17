<template>
  <section class="office-page">
    <header class="office-page__header">
      <div><h1>固定资产管理与申领</h1><p>所有用户查看可用资产，教职工查看本人申请，资产负责人统一审批全部申请。</p></div>
      <div class="office-page__actions">
        <el-input-number v-if="activeTab==='inventory'&&canRead" v-model="deptId" :min="1" placeholder="部门ID" />
        <el-button v-if="activeTab==='inventory'&&canRead" :loading="loading" @click="load">查询</el-button>
        <el-button v-if="canApply" type="primary" @click="openDialog('apply')">发起申请</el-button>
        <el-button v-if="canManage" @click="openDialog('save')">新增资产</el-button>
      </div>
    </header>

    <el-result v-if="!canRead&&!canApply&&!canManage" class="office-page__empty" icon="warning" title="无资产访问权限" />
    <template v-else>
      <el-tabs v-model="activeTab" @tab-change="load">
        <el-tab-pane v-if="canRead" label="资产台账" name="inventory" />
        <el-tab-pane v-if="canApply" label="我的申请" name="mine" />
        <el-tab-pane v-if="canManage" label="全部申请" name="applications" />
      </el-tabs>

      <el-alert v-if="activeTab==='inventory'" class="inventory-tip" type="info" :closable="false" show-icon title="这里只显示审批通过、仍在库且数量大于 0 的资产；已经全部领用的资产不会显示。" />
      <el-card class="office-page__section" shadow="never">
        <el-table :data="rows" v-loading="loading" stripe :empty-text="emptyText">
          <el-table-column prop="assetName" label="资产名称" min-width="150" />
          <el-table-column prop="assetType" label="类型" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="deptId" label="部门" width="90" />
          <el-table-column v-if="activeTab==='applications'" prop="applyUserId" label="申请人" width="100" />
          <el-table-column v-if="activeTab==='inventory'" label="状态" width="100"><template #default="scope"><el-tag type="success">{{ statusTexts[scope.row.status] || '可用' }}</el-tag></template></el-table-column>
          <el-table-column v-else label="审批状态" width="110"><template #default="scope"><el-tag :type="approvalTypes[scope.row.approveStatus]">{{ approvalTexts[scope.row.approveStatus] || '未知' }}</el-tag></template></el-table-column>
          <el-table-column v-if="canManage&&activeTab!=='mine'" label="管理" min-width="210">
            <template #default="scope">
              <template v-if="activeTab==='applications'&&scope.row.approveStatus===0">
                <el-button link type="success" :disabled="scope.row.applyUserId===userId" @click="approve(scope.row,1)">通过</el-button>
                <el-button link type="danger" :disabled="scope.row.applyUserId===userId" @click="approve(scope.row,0)">拒绝</el-button>
              </template>
              <el-button link type="danger" @click="remove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <el-dialog v-model="visible" :title="dialogMode==='apply'?'资产购置或领用申请':'新增资产'" width="min(520px,92vw)">
      <el-form label-position="top">
        <el-form-item label="资产名称" required><el-input v-model="form.assetName" /></el-form-item>
        <el-form-item label="资产类型" required><el-select v-model="form.assetType" style="width:100%"><el-option v-for="value in ['设备','办公用品','其他']" :key="value" :value="value" /></el-select></el-form-item>
        <el-form-item label="数量" required><el-input-number v-model="form.quantity" :min="1" /></el-form-item>
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

const { userId, hasPermission } = useOfficeAccess()
const canRead = computed(() => hasPermission('asset:read'))
const canApply = computed(() => hasPermission('asset:apply'))
const canManage = computed(() => hasPermission('asset:manage'))
const activeTab = ref(canRead.value ? 'inventory' : (canApply.value ? 'mine' : 'applications'))
const deptId = ref()
const rows = ref([])
const loading = ref(false)
const visible = ref(false)
const dialogMode = ref('apply')
const form = reactive({ assetName: '', assetType: '设备', quantity: 1, deptId: 1 })
const approvalTexts = ['待审批', '已通过', '已拒绝']
const approvalTypes = ['warning', 'success', 'danger']
const statusTexts = { 1: '在库', 2: '已领用', 3: '已报废' }
const emptyText = computed(() => ({ inventory: '暂无可用资产', mine: '暂无个人申请', applications: '暂无资产申请' }[activeTab.value]))

const load = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'inventory') rows.value = (await assetAPI.inventory(deptId.value)).data || []
    else if (activeTab.value === 'mine') rows.value = (await assetAPI.myApplications()).data || []
    else rows.value = (await assetAPI.applications()).data || []
  } finally {
    loading.value = false
  }
}
const openDialog = (mode) => { dialogMode.value = mode; Object.assign(form, { assetName: '', assetType: '设备', quantity: 1, deptId: deptId.value || 1 }); visible.value = true }
const submit = async () => {
  if (!form.assetName.trim() || !form.quantity) return ElMessage.warning('请填写资产名称和数量')
  if (dialogMode.value === 'apply') {
    await assetAPI.apply({ ...form })
    activeTab.value = 'mine'
    ElMessage.success('申请已提交，可在“我的申请”查看进度')
  } else {
    await assetAPI.save({ ...form })
    activeTab.value = 'inventory'
    ElMessage.success('资产已新增')
  }
  visible.value = false
  await load()
}
const approve = async (row, approved) => { await assetAPI.approve(row.assetId, approved); ElMessage.success(approved ? '申请已通过' : '申请已拒绝'); await load() }
const remove = async (row) => { await ElMessageBox.confirm('确认删除该记录？', '删除确认'); await assetAPI.remove(row.assetId); ElMessage.success('已删除'); await load() }
onMounted(load)
</script>

<style scoped>
@import '@/assets/office-workspace.css';
.inventory-tip{margin-bottom:16px}
</style>
