<template>
  <section class="office-page">
    <header class="office-page__header">
      <div><h1>学杂费交纳与流水查询</h1><p>学生可查询并支付本人账单，教师可查看全校学生缴费情况。</p></div>
      <div class="office-page__actions">
        <el-button :loading="loading" @click="load">刷新</el-button>
        <el-button v-if="canManage" type="primary" @click="dialogVisible=true">导入账单</el-button>
      </div>
    </header>

    <el-result v-if="!canRead && !canOverview && !canManage" class="office-page__empty" icon="warning" title="无缴费访问权限" sub-title="当前账号没有账单查询、缴费概览或账单管理权限" />
    <el-alert v-else-if="canManage && !canRead && !canOverview" class="office-page__section" type="info" :closable="false" title="当前账号可导入账单，但无权查看学生缴费情况" />
    <template v-if="canRead && !canOverview">
      <el-card class="office-page__section" shadow="never">
        <template #header><strong>个人费用账单</strong></template>
        <el-table :data="fees" v-loading="loading" stripe empty-text="当前没有待缴或历史账单">
          <el-table-column prop="feeType" label="费用类型" min-width="110" />
          <el-table-column prop="semester" label="学期" min-width="130" />
          <el-table-column prop="amount" label="金额" min-width="110"><template #default="scope">¥ {{ scope.row.amount }}</template></el-table-column>
          <el-table-column prop="dueDate" label="截止日期" min-width="120" />
          <el-table-column label="状态" width="100"><template #default="scope"><el-tag :type="scope.row.status===1?'success':'warning'">{{ scope.row.status===1?'已支付':'未支付' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="110"><template #default="scope"><el-button type="primary" link :disabled="!canPay||scope.row.status===1" @click="pay(scope.row)">立即支付</el-button></template></el-table-column>
        </el-table>
      </el-card>

      <el-card class="office-page__section card-balance" shadow="never" v-loading="loading">
        <template #header><strong>一卡通余额</strong></template>
        <el-statistic :value="Number(cardBalance)" :precision="2">
          <template #prefix>¥</template>
        </el-statistic>
        <p>余额按本人一卡通充值和勤工俭学工资减去消费流水实时计算。</p>
      </el-card>

      <el-card class="office-page__section" shadow="never">
        <template #header><strong>一卡通最近五笔明细</strong></template>
        <el-table :data="payments" stripe empty-text="暂无充值或消费记录">
          <el-table-column prop="paymentTime" label="时间" min-width="170" />
          <el-table-column prop="paymentType" label="类型" min-width="110" />
          <el-table-column prop="description" label="说明" min-width="180" />
          <el-table-column prop="amount" label="金额" min-width="100" />
        </el-table>
      </el-card>
    </template>

    <el-card v-if="canOverview" class="office-page__section" shadow="never" v-loading="loading">
      <template #header>
        <div class="overview-header">
          <strong>学生缴费情况</strong>
          <el-tag type="info">共 {{ overview.total }} 名学生</el-tag>
        </div>
      </template>
      <div class="overview-filters">
        <el-input v-model="overviewQuery.keyword" clearable placeholder="按学号或姓名查询" @keyup.enter="searchOverview" />
        <el-select v-model="overviewQuery.status" clearable placeholder="全部缴费状态" @change="searchOverview">
          <el-option v-for="value in ['欠费', '已缴清', '未出账']" :key="value" :value="value" />
        </el-select>
        <el-button type="primary" @click="searchOverview">查询</el-button>
      </div>
      <el-table :data="overview.records" stripe empty-text="没有符合条件的学生">
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="studentName" label="姓名" min-width="120" />
        <el-table-column prop="billCount" label="账单数" width="90" />
        <el-table-column label="应缴金额" min-width="120"><template #default="scope">¥ {{ scope.row.payableAmount }}</template></el-table-column>
        <el-table-column label="已缴金额" min-width="120"><template #default="scope">¥ {{ scope.row.paidAmount }}</template></el-table-column>
        <el-table-column label="欠费金额" min-width="120"><template #default="scope"><strong class="unpaid-amount">¥ {{ scope.row.unpaidAmount }}</strong></template></el-table-column>
        <el-table-column label="缴费状态" width="100">
          <template #default="scope"><el-tag :type="overviewStatusType(scope.row.paymentStatus)">{{ scope.row.paymentStatus }}</el-tag></template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="overviewQuery.page"
        v-model:page-size="overviewQuery.size"
        class="overview-pagination"
        layout="total, prev, pager, next"
        :total="overview.total"
        @current-change="load"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="财务人员导入账单" width="min(500px,92vw)">
      <el-form label-position="top">
        <el-form-item label="学生用户 ID"><el-input-number v-model="form.studentId" :min="1" /></el-form-item>
        <el-form-item label="费用类型"><el-select v-model="form.feeType"><el-option v-for="value in ['学费','报考费','住宿费']" :key="value" :value="value" /></el-select></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item label="学期"><el-input v-model="form.semester" /></el-form-item>
        <el-form-item label="截止日期"><el-date-picker v-model="form.dueDate" value-format="YYYY-MM-DD" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="importFee">导入</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { feeAPI } from '@/api/office.js'
import { useOfficeAccess } from '@/composables/useOfficeAccess.js'

const { userId, hasPermission } = useOfficeAccess()
const canRead = computed(() => hasPermission('fee:self:read'))
const canPay = computed(() => hasPermission('fee:self:pay'))
const canManage = computed(() => hasPermission('fee:manage'))
const canOverview = computed(() => hasPermission('fee:overview:read'))
const fees = ref([])
const payments = ref([])
const cardBalance = ref('0.00')
const overview = reactive({ records: [], total: 0 })
const overviewQuery = reactive({ keyword: '', status: '', page: 1, size: 20 })
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ studentId: 1, feeType: '学费', amount: 0.01, semester: '2025-2026-1', dueDate: '' })

const load = async () => {
  if (!canRead.value && !canOverview.value) return
  loading.value = true
  try {
    const tasks = []
    if (canRead.value && !canOverview.value) {
      tasks.push(Promise.all([feeAPI.getMyFees(), feeAPI.getCardBalance(), feeAPI.getRecentPayments()])
        .then(([feeResult, balanceResult, paymentResult]) => {
          fees.value = feeResult.data || []
          cardBalance.value = balanceResult.data?.balance ?? '0.00'
          payments.value = paymentResult.data || []
        }))
    }
    if (canOverview.value) {
      tasks.push(feeAPI.getStudentOverview({ ...overviewQuery }).then(result => {
        overview.records = result.data?.records || []
        overview.total = Number(result.data?.total || 0)
      }))
    }
    await Promise.all(tasks)
  } finally { loading.value = false }
}
const searchOverview = () => {
  overviewQuery.page = 1
  load()
}
const overviewStatusType = status => ({ 欠费: 'danger', 已缴清: 'success', 未出账: 'info' }[status] || 'info')
const pay = async (row) => {
  await ElMessageBox.confirm(`确认支付 ¥${row.amount}？`, '支付确认')
  await feeAPI.pay(row.feeId)
  ElMessage.success('支付成功')
  await load()
}
const importFee = async () => {
  await feeAPI.importFees([{ ...form }])
  ElMessage.success('账单导入成功')
  dialogVisible.value = false
  if (canOverview.value || form.studentId === userId.value) await load()
}
onMounted(load)
</script>

<style scoped>
@import '@/assets/office-workspace.css';

.card-balance p {
  margin: 10px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.overview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.overview-filters {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 180px auto;
  gap: 10px;
  margin-bottom: 16px;
}

.overview-pagination {
  justify-content: flex-end;
  margin-top: 16px;
}

.unpaid-amount { color: var(--el-color-danger); }

@media (max-width: 640px) {
  .overview-filters { grid-template-columns: 1fr; }
}
</style>
