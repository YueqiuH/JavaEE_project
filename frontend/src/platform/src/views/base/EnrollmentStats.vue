<template>
  <div class="d-page">
    <div class="d-crumb d-rise" style="--rise: 0">
      <router-link to="/home">首页</router-link>
      <el-icon><ArrowRight /></el-icon>
      <router-link :to="{ path: '/services', query: { domain: 'base' } }">基础数据</router-link>
      <el-icon><ArrowRight /></el-icon>
      <span>招生统计</span>
    </div>

    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <span class="d-head-module">基础数据 · D2</span>
        <h1>招生与迎新数据统计</h1>
        <p class="d-head-desc">年度招生计划、报到率与生源地分布实时汇总</p>
      </div>
      <div class="d-head-side">
        <el-select v-model="statsYear" class="year-select" @change="loadStats">
          <el-option v-for="y in yearOptions" :key="y" :label="`${y} 年度`" :value="y" />
        </el-select>
      </div>
    </header>

    <!-- 汇总卡片 -->
    <section v-loading="statsLoading" class="d-stats">
      <div class="d-stat d-rise" style="--rise: 2">
        <small>计划招生</small>
        <strong>{{ fmt(stats.planTotal) }}<span class="unit">人</span></strong>
        <em>{{ statsYear }} 年度全校合计</em>
      </div>
      <div class="d-stat d-rise" style="--rise: 3">
        <small>实际报到</small>
        <strong>{{ fmt(stats.actualTotal) }}<span class="unit">人</span></strong>
        <em>迎新期间实时更新</em>
      </div>
      <div class="d-stat d-rise" style="--rise: 4">
        <small>全校报到率</small>
        <strong>{{ stats.reportRate != null ? stats.reportRate : '—' }}<span class="unit">%</span></strong>
        <em>实际报到 ÷ 计划招生</em>
      </div>
      <div class="d-stat d-rise" style="--rise: 5">
        <small>生源省份</small>
        <strong>{{ stats.originDistribution?.length ?? '—' }}<span class="unit">个</span></strong>
        <em>{{ statsYear }} 级新生覆盖范围</em>
      </div>
    </section>

    <!-- 图表 -->
    <section class="chart-grid">
      <article class="d-panel d-panel-pad d-rise" style="--rise: 6">
        <div class="d-chart-head">
          <h2>各院系计划与报到对比</h2>
          <p>{{ statsYear }} 年度 · 深色为实际报到</p>
        </div>
        <EChart :option="deptChartOption" height="330px" />
      </article>
      <article class="d-panel d-panel-pad d-rise" style="--rise: 7">
        <div class="d-chart-head">
          <h2>历年招生趋势</h2>
          <p>柱为人数 · 折线为报到率</p>
        </div>
        <EChart :option="trendChartOption" height="330px" />
      </article>
      <article class="d-panel d-panel-pad d-rise" style="--rise: 8">
        <div class="d-chart-head">
          <h2>新生生源地分布</h2>
          <p>{{ statsYear }} 级</p>
        </div>
        <EChart :option="originChartOption" height="330px" />
      </article>

      <!-- 招生计划管理 -->
      <article class="d-panel d-rise" style="--rise: 9">
        <div class="d-toolbar">
          <h2>招生计划管理</h2>
          <span class="d-toolbar-note">迎新期间可随时更新报到人数</span>
          <span class="d-spacer"></span>
          <el-select v-model="planQuery.year" clearable placeholder="全部年度" class="plan-filter" @change="loadPlans(1)">
            <el-option v-for="y in yearOptions" :key="y" :label="`${y} 年度`" :value="y" />
          </el-select>
          <el-button v-if="canWrite" type="primary" :icon="Plus" @click="openPlanForm()">录入计划</el-button>
        </div>
        <el-table v-loading="planLoading" :data="planRows" size="small">
          <el-table-column prop="year" label="年度" width="76">
            <template #default="scope"><span class="d-num">{{ scope.row.year }}</span></template>
          </el-table-column>
          <el-table-column prop="deptName" label="院系" min-width="150" show-overflow-tooltip />
          <el-table-column prop="majorName" label="专业" min-width="130" show-overflow-tooltip />
          <el-table-column label="计划" width="76" align="right">
            <template #default="scope"><span class="d-num">{{ scope.row.planCount }}</span></template>
          </el-table-column>
          <el-table-column label="报到" width="76" align="right">
            <template #default="scope"><span class="d-num">{{ scope.row.actualCount }}</span></template>
          </el-table-column>
          <el-table-column label="报到率" width="110" align="right">
            <template #default="scope">
              <span class="d-num rate" :class="rateClass(scope.row.reportRate)">
                {{ scope.row.reportRate != null ? `${scope.row.reportRate}%` : '—' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column v-if="canWrite" label="操作" width="118" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="openPlanForm(scope.row)">更新</el-button>
              <el-button link type="danger" @click="removePlan(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="d-foot">
          <span>共 {{ planTotal }} 条计划</span>
          <el-pagination
            background layout="prev, pager, next" :total="planTotal"
            :page-size="planQuery.size" :current-page="planQuery.page" @current-change="loadPlans"
          />
        </div>
      </article>
    </section>

    <!-- 计划表单 -->
    <el-dialog v-model="planFormVisible" :title="planForm.enrollmentId ? '更新招生计划' : '录入招生计划'" width="min(520px, calc(100vw - 32px))">
      <el-form ref="planFormRef" :model="planForm" :rules="planRules" label-position="top">
        <el-form-item label="院系">
          <el-select v-model="planForm.deptId" placeholder="请选择院系" style="width: 100%" @change="onPlanDeptChange">
            <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" prop="majorId">
          <el-select v-model="planForm.majorId" placeholder="请先选择院系" style="width: 100%">
            <el-option v-for="m in majorOptions" :key="m.majorId" :label="m.majorName" :value="m.majorId" />
          </el-select>
        </el-form-item>
        <el-form-item label="年度" prop="year">
          <el-select v-model="planForm.year" placeholder="请选择年度" style="width: 100%">
            <el-option v-for="y in yearOptions" :key="y" :label="`${y} 年度`" :value="y" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划招生人数" prop="planCount">
          <el-input-number v-model="planForm.planCount" :min="0" :max="10000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实际报到人数">
          <el-input-number v-model="planForm.actualCount" :min="0" :max="10000" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight, Plus } from '@element-plus/icons-vue'
import {
  addEnrollment, delEnrollment, getEnrollmentStats,
  listDepartmentPage, listEnrollmentPage, listMajorPage, updateEnrollment,
} from '@/api/base.js'
import { getStoredCurrentUser } from '@/utils/authSession.js'
import EChart from './components/EChart.vue'
import './base-d.css'

const canWrite = computed(() => (getStoredCurrentUser()?.permissions || []).includes('base:write'))

const CURRENT_YEAR = new Date().getFullYear()
const yearOptions = [CURRENT_YEAR + 1, CURRENT_YEAR, CURRENT_YEAR - 1, CURRENT_YEAR - 2, CURRENT_YEAR - 3]
const statsYear = ref(CURRENT_YEAR)

const fmt = (n) => (n == null ? '—' : Number(n).toLocaleString())
const rateClass = (rate) => (rate == null ? '' : rate >= 90 ? 'rate-good' : rate >= 60 ? '' : 'rate-low')

// ===== 统计 =====
const statsLoading = ref(false)
const stats = ref({})

const loadStats = async () => {
  statsLoading.value = true
  try {
    const res = await getEnrollmentStats({ year: statsYear.value })
    stats.value = res.data
  } finally {
    statsLoading.value = false
  }
}

// 同色系深浅表达同一指标的两个状态：计划(浅紫) / 实际(深紫)
const PLAN_COLOR = '#cdb0d0'
const ACTUAL_COLOR = '#8a4d8f'
const RATE_COLOR = '#c77800'

const deptChartOption = computed(() => {
  const rows = stats.value.byDept || []
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['计划招生', '实际报到'], top: 0 },
    grid: { left: 8, right: 12, top: 34, bottom: 6, containLabel: true },
    xAxis: { type: 'category', data: rows.map((r) => r.label), axisLabel: { interval: 0, rotate: 24 } },
    yAxis: { type: 'value' },
    series: [
      { name: '计划招生', type: 'bar', data: rows.map((r) => r.planCount), itemStyle: { color: PLAN_COLOR }, barGap: '-46%', z: 1 },
      { name: '实际报到', type: 'bar', data: rows.map((r) => r.actualCount), itemStyle: { color: ACTUAL_COLOR }, z: 2 },
    ],
  }
})

const trendChartOption = computed(() => {
  const rows = stats.value.trend || []
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['计划招生', '实际报到', '报到率'], top: 0 },
    grid: { left: 8, right: 12, top: 34, bottom: 6, containLabel: true },
    xAxis: { type: 'category', data: rows.map((r) => r.label) },
    yAxis: [
      { type: 'value' },
      { type: 'value', max: 100, splitLine: { show: false }, axisLabel: { formatter: '{value}%' } },
    ],
    series: [
      { name: '计划招生', type: 'bar', data: rows.map((r) => r.planCount), itemStyle: { color: PLAN_COLOR }, barGap: '-46%', z: 1 },
      { name: '实际报到', type: 'bar', data: rows.map((r) => r.actualCount), itemStyle: { color: ACTUAL_COLOR }, z: 2 },
      { name: '报到率', type: 'line', yAxisIndex: 1, data: rows.map((r) => r.reportRate), itemStyle: { color: RATE_COLOR }, lineStyle: { color: RATE_COLOR } },
    ],
  }
})

const originChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}：{c} 人（{d}%）' },
  legend: { type: 'scroll', orient: 'vertical', right: 0, top: 'middle' },
  series: [{
    type: 'pie', radius: ['42%', '68%'], center: ['38%', '50%'],
    data: (stats.value.originDistribution || []).map((r) => ({ name: r.name, value: r.value })),
    label: { formatter: '{b} {c}', fontSize: 11 },
  }],
}))

// ===== 计划管理 =====
const planLoading = ref(false)
const planRows = ref([])
const planTotal = ref(0)
const planQuery = reactive({ page: 1, size: 10, year: null })

const loadPlans = async (page) => {
  if (page) planQuery.page = page
  planLoading.value = true
  try {
    const res = await listEnrollmentPage({ ...planQuery })
    planRows.value = res.data.records
    planTotal.value = Number(res.data.total)
  } finally {
    planLoading.value = false
  }
}

const saving = ref(false)
const deptOptions = ref([])
const majorOptions = ref([])
const planFormVisible = ref(false)
const planFormRef = ref(null)
const planForm = reactive({ enrollmentId: null, deptId: null, majorId: null, year: CURRENT_YEAR, planCount: 0, actualCount: 0 })
const planRules = {
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  year: [{ required: true, message: '请选择年度', trigger: 'change' }],
  planCount: [{ required: true, message: '请输入计划招生人数', trigger: 'blur' }],
}

const loadDeptOptions = async () => {
  const res = await listDepartmentPage({ page: 1, size: 200 })
  deptOptions.value = res.data.records
}

const loadMajorOptions = async (deptId) => {
  const res = await listMajorPage({ page: 1, size: 200, deptId })
  majorOptions.value = res.data.records
}

const onPlanDeptChange = async () => {
  planForm.majorId = null
  await loadMajorOptions(planForm.deptId)
}

const openPlanForm = async (row) => {
  Object.assign(planForm, row
    ? { enrollmentId: row.enrollmentId, deptId: row.deptId, majorId: row.majorId, year: row.year, planCount: row.planCount, actualCount: row.actualCount }
    : { enrollmentId: null, deptId: null, majorId: null, year: statsYear.value, planCount: 0, actualCount: 0 })
  await loadMajorOptions(planForm.deptId ?? undefined)
  planFormVisible.value = true
}

const savePlan = async () => {
  await planFormRef.value.validate()
  saving.value = true
  try {
    if (planForm.enrollmentId) {
      await updateEnrollment(planForm.enrollmentId, planForm)
    } else {
      await addEnrollment(planForm)
    }
    ElMessage.success('保存成功')
    planFormVisible.value = false
    await Promise.all([loadPlans(), loadStats()])
  } finally {
    saving.value = false
  }
}

const removePlan = async (row) => {
  await ElMessageBox.confirm(`确定删除 ${row.year} 年度「${row.majorName}」的招生计划吗？`, '删除确认', { type: 'warning' })
  await delEnrollment(row.enrollmentId)
  ElMessage.success('删除成功')
  await Promise.all([loadPlans(), loadStats()])
}

onMounted(() => {
  loadStats()
  loadPlans()
  loadDeptOptions()
})
</script>

<style scoped>
.year-select { width: 128px; }
.plan-filter { width: 118px; }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.rate.rate-good { color: var(--color-success, #16865b); }
.rate.rate-low { color: var(--color-danger, #c2413b); }
@media (max-width: 1199px) { .chart-grid { grid-template-columns: 1fr; } }
</style>
