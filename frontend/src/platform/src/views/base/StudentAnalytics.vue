<template>
  <div class="d-page">
    <div class="d-crumb d-rise" style="--rise: 0">
      <router-link to="/home">首页</router-link>
      <el-icon><ArrowRight /></el-icon>
      <router-link :to="{ path: '/services', query: { domain: 'base' } }">基础数据</router-link>
      <el-icon><ArrowRight /></el-icon>
      <span>学生多维统计</span>
    </div>

    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <h1>学生特征多维统计</h1>
        <p class="d-head-desc">按院系、专业、班级逐级穿透，分析在校生结构与生源分布</p>
      </div>
      <div class="d-head-side">
        <el-select ref="yearSelectRef" v-model="filter.enrollYear" clearable placeholder="全部年级" class="year-select" @change="loadStats" @visible-change="onYearDropdownChange">
          <el-option v-for="y in yearOptions" :key="y" :label="`${y}级`" :value="y" />
        </el-select>
        <el-button :icon="RefreshLeft" @click="resetDrill">重置</el-button>
      </div>
    </header>

    <!-- 穿透路径 -->
    <section class="d-panel drill-bar d-rise" style="--rise: 2">
      <span class="drill-label">穿透路径</span>
      <el-button link :type="!filter.deptId ? 'primary' : ''" @click="resetDrill">全校</el-button>
      <template v-if="selectedDept">
        <el-icon class="drill-arrow"><ArrowRight /></el-icon>
        <el-button link :type="filter.deptId && !filter.majorId ? 'primary' : ''" @click="drillToDept(selectedDept)">
          {{ selectedDept.deptName }}
        </el-button>
      </template>
      <template v-if="selectedMajor">
        <el-icon class="drill-arrow"><ArrowRight /></el-icon>
        <el-button link type="primary">{{ selectedMajor.majorName }}</el-button>
      </template>
      <span class="d-spacer"></span>
      <span class="drill-total">在读 <strong class="d-num">{{ stats.total ?? 0 }}</strong> 人{{ filter.enrollYear ? ` · ${filter.enrollYear}级` : '' }}</span>
      <el-tooltip content="点击下方「分组统计」的柱条可逐级穿透：院系 → 专业 → 班级">
        <el-icon class="drill-tip"><InfoFilled /></el-icon>
      </el-tooltip>
    </section>

    <!-- 图表 -->
    <section v-loading="loading" class="chart-grid">
      <article class="d-panel d-panel-pad span-2 d-rise" style="--rise: 3">
        <div class="d-chart-head">
          <h2>{{ groupTitle }}</h2>
          <p>点击柱条继续下钻</p>
        </div>
        <EChart :option="groupChartOption" height="340px" @chart-click="onGroupChartClick" />
      </article>

      <article class="d-panel d-panel-pad d-rise" style="--rise: 4">
        <div class="d-chart-head"><h2>年级分布</h2></div>
        <EChart :option="yearChartOption" height="270px" />
      </article>
      <article class="d-panel d-panel-pad d-rise" style="--rise: 5">
        <div class="d-chart-head"><h2>性别比例</h2></div>
        <EChart :option="genderChartOption" height="270px" />
      </article>
      <article class="d-panel d-panel-pad d-rise" style="--rise: 6">
        <div class="d-chart-head"><h2>生源地分布</h2><p>Top 10</p></div>
        <EChart :option="originChartOption" height="270px" />
      </article>
      <article class="d-panel d-panel-pad d-rise" style="--rise: 7">
        <div class="d-chart-head"><h2>学籍状态</h2></div>
        <EChart :option="statusChartOption" height="270px" />
      </article>

      <article class="d-panel d-panel-pad span-2 d-rise" style="--rise: 8">
        <div class="d-chart-head">
          <h2>选课偏好</h2>
          <p>Top 10 · 数据来自教务选课（只读）</p>
        </div>
        <el-empty
          v-if="!(stats.coursePreference || []).length"
          description="选课数据尚未产生，待选课系统上线后自动呈现"
          :image-size="56"
        />
        <EChart v-else :option="courseChartOption" height="300px" />
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { ArrowRight, InfoFilled, RefreshLeft } from '@element-plus/icons-vue'
import { getStudentStats, listDepartmentPage, listMajorPage } from '@/api/base.js'
import EChart from './components/EChart.vue'
import './base-d.css'

const yearOptions = [2022, 2023, 2024, 2025]

const loading = ref(false)
const stats = ref({})
const filter = reactive({ deptId: null, majorId: null, enrollYear: null })
const selectedDept = ref(null)
const selectedMajor = ref(null)

// 名称 → 实体映射，用于图表点击穿透
const deptList = ref([])
const majorList = ref([])

const groupTitle = computed(() => {
  if (stats.value.groupBy === 'class') return `${selectedMajor.value?.majorName ?? ''} · 班级分组`
  if (stats.value.groupBy === 'major') return `${selectedDept.value?.deptName ?? ''} · 专业分组`
  return '各院系分组统计'
})

const loadStats = async () => {
  loading.value = true
  try {
    const res = await getStudentStats({ ...filter })
    stats.value = res.data || {}
  } catch {
    stats.value = {}
  } finally {
    loading.value = false
  }
}

const onGroupChartClick = async (params) => {
  if (stats.value.groupBy === 'dept') {
    const dept = deptList.value.find((d) => d.deptName === params.name)
    if (dept) await drillToDept(dept)
  } else if (stats.value.groupBy === 'major') {
    const major = majorList.value.find((m) => m.majorName === params.name)
    if (major) {
      selectedMajor.value = major
      filter.majorId = major.majorId
      await loadStats()
    }
  }
}

const drillToDept = async (dept) => {
  selectedDept.value = dept
  selectedMajor.value = null
  filter.deptId = dept.deptId
  filter.majorId = null
  try {
    const res = await listMajorPage({ page: 1, size: 200, deptId: dept.deptId })
    majorList.value = res.data.records
  } catch {
    majorList.value = []
  }
  await loadStats()
}

const resetDrill = async () => {
  selectedDept.value = null
  selectedMajor.value = null
  filter.deptId = null
  filter.majorId = null
  await loadStats()
}

// ===== 图表配置（色板与轴样式由 EChart 主题统一提供） =====
const ACCENT = '#8a4d8f'

const barOption = (rows, color, rotate = 24) => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 12, right: 16, top: 14, bottom: rotate ? 80 : 20, containLabel: true },
  xAxis: { type: 'category', data: (rows || []).map((r) => r.name), axisLabel: { interval: 0, rotate, fontSize: 11, formatter: (v) => v.length > 6 ? v.slice(0, 5) + '…' : v } },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: (rows || []).map((r) => r.value), ...(color ? { itemStyle: { color } } : {}) }],
})

const pieOption = (rows) => ({
  tooltip: { trigger: 'item', formatter: '{b}：{c} 人（{d}%）' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['42%', '64%'], center: ['50%', '44%'],
    data: (rows || []).map((r) => ({ name: r.name, value: r.value })),
  }],
})

const groupChartOption = computed(() => {
  const option = barOption(stats.value.byGroup, ACCENT)
  option.series[0].emphasis = { itemStyle: { color: '#6d3a72' } }
  return option
})
const yearChartOption = computed(() => barOption(stats.value.byEnrollYear, '#3973b7', 0))
const genderChartOption = computed(() => pieOption(stats.value.byGender))
const statusChartOption = computed(() => pieOption(stats.value.byStatus))
const originChartOption = computed(() => barOption((stats.value.byOrigin || []).slice(0, 10), '#16865b'))
const courseChartOption = computed(() => {
  const rows = [...(stats.value.coursePreference || [])].reverse()
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 20, right: 24, top: 8, bottom: 6, containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: rows.map((r) => r.name) },
    series: [{ type: 'bar', data: rows.map((r) => r.value), itemStyle: { color: '#c77800', borderRadius: [0, 3, 3, 0] }, barMaxWidth: 20 }],
  }
})

onMounted(async () => {
  try {
    const res = await listDepartmentPage({ page: 1, size: 200 })
    deptList.value = res.data.records
  } catch { /* interceptor shows error toast */ }
  await loadStats()
})

const yearSelectRef = ref(null)
let yearDropdownOpen = false
const onYearDropdownChange = (v) => { yearDropdownOpen = v }
const closeYearDropdown = () => { if (yearDropdownOpen && yearSelectRef.value) yearSelectRef.value.blur() }
onMounted(() => { window.addEventListener('scroll', closeYearDropdown, true) })
onUnmounted(() => { window.removeEventListener('scroll', closeYearDropdown, true) })
</script>

<style scoped>
.year-select { width: 124px; }
.drill-bar {
  display: flex; flex-wrap: wrap; align-items: center;
  gap: 4px; margin-bottom: 18px;
  padding: 12px 18px;
  background: #fafafa;
}
.drill-label { margin-right: 6px; color: var(--color-text-tertiary); font-size: 12px; font-weight: 500; }
.drill-arrow { color: #c4b5fd; font-size: 11px; }
.drill-total { color: var(--color-text-secondary); font-size: 13px; margin-left: auto; }
.drill-total strong { color: var(--d-accent); font-size: 15px; font-weight: 700; }
.drill-tip { margin-left: 8px; color: var(--color-text-tertiary); cursor: help; }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.span-2 { grid-column: span 2; }
@media (max-width: 1199px) { .chart-grid { grid-template-columns: 1fr; } .span-2 { grid-column: span 1; } }
</style>
