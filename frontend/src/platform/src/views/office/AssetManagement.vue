<template>
  <section class="office-page">
    <header class="office-page__header">
      <div><h1>固定资产申请与审批</h1><p>购买、添加入库、借用和损坏报废均由教师或教职工申请，管理员统一审批。</p></div>
      <div class="office-page__actions">
        <el-button v-if="activeTab==='inventory'&&canRead" :loading="loading" @click="load">刷新</el-button>
        <el-button v-if="canSubmit" type="primary" @click="openDialog('purchase')">申请购买</el-button>
        <el-button v-if="canSubmit" @click="openDialog('add')">申请添加资产</el-button>
      </div>
    </header>

    <el-result v-if="!canRead&&!canApply&&!canManage" class="office-page__empty" icon="warning" title="无资产访问权限" />
    <template v-else>
      <el-tabs v-model="activeTab" @tab-change="load">
        <el-tab-pane v-if="canRead" label="资产台账" name="inventory" />
        <el-tab-pane v-if="canSubmit" label="我的申请" name="mine" />
        <el-tab-pane v-if="isAdmin" label="审批中心" name="applications" />
      </el-tabs>

      <el-alert v-if="activeTab==='inventory'" class="inventory-tip" type="info" :closable="false" show-icon title="这里只显示管理员审批入库且仍有库存的资产；借用或报废审批通过后扣减可用数量。" />
      <el-card class="office-page__section" shadow="never">
        <el-table :data="rows" v-loading="loading" stripe :empty-text="emptyText">
          <el-table-column prop="assetName" label="资产名称" min-width="150" />
          <el-table-column prop="assetType" label="类型" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column v-if="activeTab!=='inventory'" label="申请类型" width="110"><template #default="scope"><el-tag :type="applicationTypeStyles[scope.row.applicationType]">{{ applicationTypeTexts[scope.row.applicationType] || '历史申请' }}</el-tag></template></el-table-column>
          <el-table-column v-if="activeTab==='applications'" prop="applyUserId" label="申请人" width="100" />
          <el-table-column v-if="activeTab==='inventory'" label="状态" width="100"><template #default="scope"><el-tag type="success">{{ statusTexts[scope.row.status] || '可用' }}</el-tag></template></el-table-column>
          <el-table-column v-else label="审批状态" width="110"><template #default="scope"><el-tag :type="approvalTypes[scope.row.approveStatus]">{{ approvalTexts[scope.row.approveStatus] || '未知' }}</el-tag></template></el-table-column>
          <el-table-column v-if="activeTab==='inventory'&&(canSubmit||isAdmin)" label="操作" min-width="240">
            <template #default="scope">
              <el-button v-if="canSubmit" link type="primary" @click="openAvailableApply(scope.row)">申请借用</el-button>
              <el-button v-if="canSubmit" link type="warning" @click="openScrapApply(scope.row)">损坏/报废</el-button>
              <el-button v-if="isAdmin" link type="danger" @click="remove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
          <el-table-column v-if="activeTab!=='inventory'" prop="approveRemark" label="审批意见" min-width="150" show-overflow-tooltip />
          <el-table-column v-if="activeTab!=='inventory'" prop="applicationReason" label="损坏/报废原因" min-width="180" show-overflow-tooltip />
          <el-table-column v-if="isAdmin&&activeTab==='applications'" label="管理员审批" min-width="160">
            <template #default="scope">
              <template v-if="scope.row.approveStatus===0">
                <el-button link type="success" :disabled="scope.row.applyUserId===userId" @click="approve(scope.row,1)">通过</el-button>
                <el-button link type="danger" :disabled="scope.row.applyUserId===userId" @click="approve(scope.row,0)">拒绝</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <el-dialog v-model="visible" :title="dialogTitle" width="min(520px,92vw)">
      <el-form label-position="top">
        <el-alert v-if="dialogMode==='purchase'" type="info" :closable="false" title="购买审批通过仅代表同意采购；资产到货后还需提交添加入库审批。" />
        <el-form-item label="资产名称" required><el-input v-model="form.assetName" :disabled="['borrow','scrap'].includes(dialogMode)" /></el-form-item>
        <el-form-item label="资产类型" required><el-select v-model="form.assetType" style="width:100%" :disabled="['borrow','scrap'].includes(dialogMode)"><el-option v-for="value in ['设备','办公用品','其他']" :key="value" :value="value" /></el-select></el-form-item>
        <el-form-item label="数量" required><el-input-number v-model="form.quantity" :min="1" :max="['borrow','scrap'].includes(dialogMode)?selectedInventory?.quantity:undefined" /></el-form-item>
        <el-form-item v-if="dialogMode==='scrap'" label="损坏情况和报废原因" required><el-input v-model="form.applicationReason" type="textarea" :rows="4" maxlength="512" show-word-limit /></el-form-item>
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

const { currentUser, userId, hasPermission } = useOfficeAccess()
const canRead = computed(() => hasPermission('asset:read'))
const canApply = computed(() => hasPermission('asset:apply'))
const canManage = computed(() => hasPermission('asset:manage'))
const canSubmit = computed(() => canApply.value && currentUser.value?.roles?.some(role => ['TEACHER', 'STAFF'].includes(role)))
const isAdmin = computed(() => canManage.value && currentUser.value?.roles?.includes('ADMIN'))
const activeTab = ref(canRead.value ? 'inventory' : (canSubmit.value ? 'mine' : 'applications'))
const rows = ref([])
const loading = ref(false)
const visible = ref(false)
const dialogMode = ref('purchase')
const selectedInventory = ref(null)
const form = reactive({ assetName: '', assetType: '设备', quantity: 1, applicationReason: '' })
const approvalTexts = ['待审批', '已通过', '已拒绝']
const approvalTypes = ['warning', 'success', 'danger']
const applicationTypeTexts = { PURCHASE: '购买申请', ADD: '添加审批', BORROW: '借用审批', SCRAP: '损坏/报废', LEGACY: '历史申请' }
const applicationTypeStyles = { PURCHASE: 'primary', ADD: 'success', BORROW: 'warning', SCRAP: 'danger', LEGACY: 'info' }
const statusTexts = { 1: '在库', 2: '已借出', 3: '已报废' }
const emptyText = computed(() => ({ inventory: '暂无可借资产', mine: '暂无个人申请', applications: '暂无待处理或历史审批' }[activeTab.value]))
const dialogTitle = computed(() => ({ purchase: '提交资产购买申请', add: '提交资产添加入库申请', borrow: '申请借用现有资产', scrap: '提交损坏/报废申请' }[dialogMode.value]))

const load = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'inventory') rows.value = (await assetAPI.inventory()).data || []
    else if (activeTab.value === 'mine') rows.value = (await assetAPI.myApplications()).data || []
    else rows.value = (await assetAPI.applications()).data || []
  } finally {
    loading.value = false
  }
}
const openDialog = (mode) => { dialogMode.value = mode; selectedInventory.value = null; Object.assign(form, { assetName: '', assetType: '设备', quantity: 1, applicationReason: '' }); visible.value = true }
const openAvailableApply = (row) => { dialogMode.value = 'borrow'; selectedInventory.value = row; Object.assign(form, { assetName: row.assetName, assetType: row.assetType, quantity: 1, applicationReason: '' }); visible.value = true }
const openScrapApply = (row) => { dialogMode.value = 'scrap'; selectedInventory.value = row; Object.assign(form, { assetName: row.assetName, assetType: row.assetType, quantity: 1, applicationReason: '' }); visible.value = true }
const submit = async () => {
  if (!form.assetName.trim() || !form.quantity) return ElMessage.warning('请填写资产名称和数量')
  if (dialogMode.value === 'scrap' && !form.applicationReason.trim()) return ElMessage.warning('请填写资产损坏情况和报废原因')
  if (dialogMode.value === 'borrow') {
    await assetAPI.applyAvailable(selectedInventory.value.assetId, form.quantity)
    activeTab.value = 'mine'
    ElMessage.success('借用申请已提交，等待管理员审批')
  } else if (dialogMode.value === 'scrap') {
    await assetAPI.scrap(selectedInventory.value.assetId, form.quantity, form.applicationReason.trim())
    activeTab.value = 'mine'
    ElMessage.success('损坏/报废申请已提交，等待管理员审批')
  } else if (dialogMode.value === 'purchase') {
    await assetAPI.apply({ ...form })
    activeTab.value = 'mine'
    ElMessage.success('购买申请已提交，等待管理员审批')
  } else {
    await assetAPI.add({ ...form })
    activeTab.value = 'mine'
    ElMessage.success('添加申请已提交，审批通过后资产进入台账')
  }
  visible.value = false
  await load()
}
const approve = async (row, approved) => {
  const result = await ElMessageBox.prompt(approved ? '可填写审批意见（选填）' : '请填写拒绝原因', approved ? '确认通过' : '确认拒绝', {
    inputType: 'textarea', inputPattern: approved ? undefined : /\S+/, inputErrorMessage: '拒绝申请时必须填写审批意见',
    confirmButtonText: approved ? '通过' : '拒绝', cancelButtonText: '取消',
  })
  await assetAPI.approve(row.assetId, approved, result.value?.trim() || undefined)
  ElMessage.success(approved ? '申请已通过' : '申请已拒绝')
  await load()
}
const remove = async (row) => { await ElMessageBox.confirm('确认删除该记录？', '删除确认'); await assetAPI.remove(row.assetId); ElMessage.success('已删除'); await load() }
onMounted(load)
</script>

<style scoped>
@import '@/assets/office-workspace.css';
.inventory-tip{margin-bottom:16px}
</style>
