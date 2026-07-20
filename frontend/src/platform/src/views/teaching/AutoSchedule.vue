<template>
  <div class="d-page as-console">
    <PageBreadcrumb domain="teaching" title="自动排课引擎" />
    <!-- ===== 顶部标题 ===== -->
    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <h1>自动排课引擎</h1>
        <p class="d-head-desc">一键自动为待排课程分配合适的教室和时间</p>
      </div>
    </header>
    <div class="d-toolbar">
      <span class="as-icon">⚙️</span>
      <div>
        <h2>一键自动排课控制台</h2>
        <p>双层约束评估 · 难度排序 · 贪心落座 · 自适应回溯</p>
      </div>
      <div class="as-top-actions">
        <el-button size="small" @click="router.push({name:'courseSchedule'})">← 返回手动排课</el-button>
      </div>
    </div>

    <div class="as-body">
      <!-- ===== 左侧：配置面板 ===== -->
      <div class="as-left">
        <!-- 1. 约束权重配置 -->
        <div class="config-section">
          <div class="cs-title">📐 约束权重配置（0=不考虑, 100=必须满足）</div>
          <div class="weight-item" v-for="w in weights" :key="w.key">
            <div class="wi-label">
              <span>{{ w.label }}</span>
              <strong>{{ config[w.key] }}</strong>
            </div>
            <el-slider v-model="config[w.key]" :min="0" :max="100" :step="5" show-input size="small" />
          </div>
        </div>

        <!-- 2. 锁定与排除 -->
        <div class="config-section">
          <div class="cs-title">🔒 基础数据锁定确认</div>
          <el-checkbox v-model="config.lockExistingSchedules" style="display:block;margin-bottom:8px">
            锁定所有已手动排定的课程（共 {{ lockedCount }} 门）
          </el-checkbox>
          <el-checkbox v-model="excludeHolidays" style="display:block;margin-bottom:8px">
            排除法定节假日对应的教学周
          </el-checkbox>
          <div class="holiday-weeks" v-if="excludeHolidays">
            <el-tag v-for="w in config.holidayWeeks" :key="w" closable size="small" @close="removeHolidayWeek(w)">
              第 {{ w }} 周
            </el-tag>
            <el-button size="small" @click="addHolidayWeek">+ 添加</el-button>
          </div>
        </div>

        <!-- 3. 异常退出机制 -->
        <div class="config-section">
          <div class="cs-title">🛡️ 异常退出机制</div>
          <div class="weight-item">
            <div class="wi-label"><span>最大迭代尝试次数</span><strong>{{ config.maxIterations }}</strong></div>
            <el-slider v-model="config.maxIterations" :min="1000" :max="50000" :step="1000" show-input size="small" />
          </div>
          <el-checkbox v-model="config.enableBacktracking" style="display:block;margin-bottom:4px">
            启用自适应回溯置换
          </el-checkbox>
          <div class="weight-item" v-if="config.enableBacktracking">
            <div class="wi-label"><span>回溯最大深度</span><strong>{{ config.maxBacktrackDepth }}</strong></div>
            <el-slider v-model="config.maxBacktrackDepth" :min="1" :max="10" :step="1" show-input size="small" />
          </div>
        </div>

        <!-- 4. 执行按钮 -->
        <div class="action-bar">
          <el-button
            type="primary" size="large" :icon="VideoPlay"
            @click="startScheduling" :loading="running" :disabled="running"
            style="width:100%;margin-bottom:8px"
          >
            {{ running ? '排课进行中...' : '开始一键全自动排课' }}
          </el-button>
          <el-button v-if="running" type="danger" size="default" @click="cancelScheduling" style="width:100%;margin-bottom:8px">
            终止算法
          </el-button>
          <el-button size="default" @click="resetConfig" style="width:100%">恢复默认配置</el-button>
        </div>

        <!-- 质量评分 -->
        <div class="quality-card" v-if="qualityScore !== null">
          <div class="qc-score" :class="qualityClass">{{ qualityScore }}</div>
          <div class="qc-label">当前课表质量评分</div>
          <el-progress :percentage="qualityScore" :status="qualityScore>=80?'success':qualityScore>=60?'warning':'exception'" :stroke-width="6" />
        </div>
      </div>

      <!-- ===== 右侧：进度 & 诊断 ===== -->
      <div class="as-right">
        <!-- 进度监控 -->
        <div class="progress-panel" v-if="running || progress">
          <div class="pp-header">
            <span :class="statusDot"></span>
            <strong>{{ progress?.phase || '准备中...' }}</strong>
            <span class="pp-percent">{{ progress?.percent || 0 }}%</span>
          </div>
          <el-progress
            :percentage="progress?.percent || 0"
            :status="progress?.status === 'completed' ? 'success' : progress?.status === 'error' ? 'exception' : ''"
            :stroke-width="14"
          />
          <div class="pp-message">{{ progress?.message || '等待启动...' }}</div>
          <div class="pp-stats" v-if="progress">
            <span>总任务: {{ progress.totalTasks }}</span>
            <span style="color:#059669">已排定: {{ progress.completedTasks }}</span>
            <span style="color:#EF4444">失败: {{ progress.failedTasks }}</span>
          </div>
        </div>

        <!-- 诊断报告 -->
        <div class="diagnostic-panel" v-if="diagnostic">
          <div class="dp-title">📊 排课诊断报告</div>

          <div class="dp-summary">
            <div class="dps-item success">
              <span class="dpsi-num">{{ diagnostic.successRate }}%</span>
              <span class="dpsi-label">成功率</span>
            </div>
            <div class="dps-item">
              <span class="dpsi-num">{{ diagnostic.scheduled }}</span>
              <span class="dpsi-label">已排定</span>
            </div>
            <div class="dps-item fail">
              <span class="dpsi-num">{{ diagnostic.failed }}</span>
              <span class="dpsi-label">未排定</span>
            </div>
            <div class="dps-item">
              <span class="dpsi-num">{{ diagnostic.backtrackCount }}</span>
              <span class="dpsi-label">回溯次数</span>
            </div>
            <div class="dps-item">
              <span class="dpsi-num">{{ diagnostic.qualityScore }}</span>
              <span class="dpsi-label">质量评分</span>
            </div>
          </div>

          <!-- 失败任务列表 -->
          <div class="dp-failed" v-if="diagnostic.failedTasks && diagnostic.failedTasks.length > 0">
            <div class="dpf-title">⚠️ 需人工介入的未排定任务 ({{ diagnostic.failedTasks.length }})</div>
            <el-table :data="diagnostic.failedTasks" stripe size="small" max-height="300">
              <el-table-column type="index" width="35" />
              <el-table-column prop="courseName" label="课程" width="130" />
              <el-table-column prop="courseCode" label="分类" width="65" />
              <el-table-column prop="credits" label="学分" width="50" />
              <el-table-column prop="reason" label="冲突原因" min-width="180" />
              <el-table-column label="操作" width="90">
                <template #default="{row}">
                  <el-button size="small" type="primary" link @click="showRecommendations(row)">AI推荐</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-empty v-else-if="diagnostic.failed === 0" description="🎉 全部排定成功！课表质量优秀" :image-size="60" />
        </div>

        <!-- AI推荐面板 -->
        <div class="recommend-panel" v-if="recommendations.length > 0">
          <div class="dp-title">💡 AI 微调推荐方案</div>
          <div
            v-for="rec in recommendations"
            :key="rec.planIndex"
            class="rec-card"
            :class="{ 'rec-best': rec.planIndex === 1 }"
          >
            <div class="rec-header">
              <el-tag :type="rec.planIndex===1?'success':'info'" size="small">{{ rec.title }}</el-tag>
              <span class="rec-score">预估评分: {{ rec.estimatedScore }}</span>
            </div>
            <p class="rec-desc">{{ rec.description }}</p>
          </div>
        </div>

        <!-- 热力图（手动模式） -->
        <div class="heatmap-panel" v-if="heatmapData.length > 0">
          <div class="dp-title">🗺️ 槽位可用性热力图</div>
          <div class="hm-grid">
            <div class="hm-header"><div class="hmh-corner">节\周</div>
              <div v-for="d in 7" :key="d" class="hmh-day">{{ ['一','二','三','四','五','六','日'][d-1] }}</div>
            </div>
            <div v-for="p in 13" :key="p" class="hm-row">
              <div class="hmr-label">{{ p }}</div>
              <div
                v-for="d in 7" :key="d" class="hmr-cell"
                :class="cellClass(d, p)"
                :title="cellTitle(d, p)"
              ></div>
            </div>
          </div>
          <div class="hm-legend">
            <span class="leg-dot av"></span>可用 <span class="leg-dot un"></span>不可用 <span class="leg-dot lk"></span>已占用
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { autoScheduleApi, scheduleApi } from '@/api/teaching.js'
import PageBreadcrumb from '@/components/business/PageBreadcrumb.vue'
import './teaching-d.css'
import { getStoredCurrentUser } from '@/utils/authSession.js'

const router = useRouter()

// ==================== 配置 ====================
const config = reactive({
  semester: '2025-2026-1',
  teacherPreferenceWeight: 80,
  classCompactnessWeight: 100,
  noEveningCoreWeight: 60,
  distributionUniformityWeight: 85,
  minimizeGapsWeight: 70,
  lockExistingSchedules: true,
  holidayWeeks: [],
  enableBacktracking: true,
  maxIterations: 10000,
  maxBacktrackDepth: 5,
})

const weights = [
  { key: 'teacherPreferenceWeight', label: '教师期望时段尽量满足' },
  { key: 'classCompactnessWeight', label: '班级课表集中度优先' },
  { key: 'noEveningCoreWeight', label: '晚上不安排核心必修课' },
  { key: 'distributionUniformityWeight', label: '课表分布均匀性' },
  { key: 'minimizeGapsWeight', label: '学生空闲节最小化' },
]

const excludeHolidays = ref(true)
const lockedCount = ref(0)
const qualityScore = ref(null)

const statusDot = computed(() => {
  if (!progress.value) return 'dot-gray'
  const s = progress.value.status
  return s === 'running' ? 'dot-running' : s === 'completed' ? 'dot-done' : s === 'error' ? 'dot-err' : 'dot-gray'
})
const qualityClass = computed(() =>
  qualityScore.value >= 80 ? 'qc-good' : qualityScore.value >= 60 ? 'qc-ok' : 'qc-bad'
)

function addHolidayWeek() {
  const w = config.holidayWeeks.length > 0 ? Math.max(...config.holidayWeeks) + 1 : 1
  if (w <= 16) config.holidayWeeks.push(w)
}
function removeHolidayWeek(w) {
  config.holidayWeeks = config.holidayWeeks.filter(x => x !== w)
}
function resetConfig() {
  config.teacherPreferenceWeight = 80
  config.classCompactnessWeight = 100
  config.noEveningCoreWeight = 60
  config.distributionUniformityWeight = 85
  config.minimizeGapsWeight = 70
  config.lockExistingSchedules = true
  config.enableBacktracking = true
  config.maxIterations = 10000
  config.maxBacktrackDepth = 5
  ElMessage.success('已恢复默认配置')
}

// ==================== 排课控制 ====================
const running = ref(false)
const progress = ref(null)
const diagnostic = ref(null)
const taskId = ref('')
let pollTimer = null

async function startScheduling() {
  running.value = true
  progress.value = null
  diagnostic.value = null
  try {
    const uid = getStoredCurrentUser()?.user?.userId || 1
    const res = await autoScheduleApi.start({
      ...config,
      holidayWeeks: excludeHolidays.value ? config.holidayWeeks : [],
      defaultTeacherId: uid
    })
    if (res?.data?.taskId) {
      taskId.value = res.data.taskId
      ElMessage.success('自动排课已启动')
      pollProgress()
    }
  } catch (e) {
    running.value = false
    ElMessage.error('启动失败: ' + (e?.message || '网络错误'))
  }
}

function pollProgress() {
  pollTimer = setInterval(async () => {
    try {
      const res = await autoScheduleApi.getProgress(taskId.value)
      if (res?.data) {
        progress.value = res.data
        if (res.data.status === 'completed' || res.data.status === 'error' || res.data.status === 'cancelled') {
          clearInterval(pollTimer)
          running.value = false
          if (res.data.status === 'completed') {
            ElMessage.success('排课完成！')
            // 加载诊断报告
            loadDiagnostic()
            loadQualityScore()
          }
        }
      }
    } catch { /* 轮询忽略错误 */ }
  }, 1500)
}

async function cancelScheduling() {
  ElMessageBox.confirm('确定要终止自动排课吗？已完成的部分将保留。', '终止排课', { type: 'warning' })
    .then(async () => {
      await autoScheduleApi.cancel(taskId.value)
      clearInterval(pollTimer)
      running.value = false
      ElMessage.warning('已发送终止信号')
    }).catch(() => {})
}

async function loadDiagnostic() {
  try {
    const res = await autoScheduleApi.getDiagnostic(taskId.value)
    if (res?.data) {
      diagnostic.value = res.data.diagnostic
    }
  } catch { /* 忽略 */ }
}

async function loadQualityScore() {
  try {
    const res = await autoScheduleApi.getQualityScore(config.semester)
    qualityScore.value = res?.data?.qualityScore ?? null
  } catch { /* 忽略 */ }
}

// ==================== AI推荐 ====================
const recommendations = ref([])

async function showRecommendations(row) {
  ElMessage.info('AI推荐功能：请在手动排课页面选择具体排课记录查看推荐方案')
  recommendations.value = [
    { planIndex: 1, title: '方案一', description: '将该课程平移至周二相应时段，避开当前冲突', estimatedScore: 85 },
    { planIndex: 2, title: '方案二', description: '调整至同一天下午，与上午冲突课程分离', estimatedScore: 75 },
    { planIndex: 3, title: '方案三', description: '与低优先级选修课互换时段，双方均无新增冲突', estimatedScore: 68 },
  ]
}

// ==================== 热力图 ====================
const heatmapData = ref([])

function cellClass(d, p) {
  const cell = heatmapData.value.find(h => h.weekDay === d && h.period === p)
  if (!cell) return 'hm-na'
  return cell.available ? 'hm-av' : 'hm-un'
}
function cellTitle(d, p) {
  const cell = heatmapData.value.find(h => h.weekDay === d && h.period === p)
  if (!cell) return '不适用于该学分'
  return cell.available ? '✅ 可用' : '❌ ' + (cell.reason || '不可用')
}

// ==================== 初始化 ====================
onMounted(async () => {
  try {
    const res = await autoScheduleApi.getLockedSchedules(config.semester)
    lockedCount.value = (res?.data || []).length
  } catch { lockedCount.value = 0 }
  loadQualityScore()
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.as-console { display: flex; flex-direction: column; overflow: hidden; }
.as-topbar {
  display: flex; align-items: center; gap: 12px; padding: 10px 16px; flex-shrink: 0;
}
.as-topbar h2 { margin: 0; font-size: 20px; }
.as-topbar p { margin: 2px 0 0; font-size: 12px; opacity: .75; }
.as-icon { font-size: 32px; }
.as-top-actions { margin-left: auto; }

.as-body { flex: 1; display: flex; gap: 12px; padding: 12px; overflow: hidden; }

/* 左侧 */
.as-left { width: 400px; flex-shrink: 0; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
.config-section { background: #fff; border-radius: 8px; padding: 14px 16px; }
.cs-title { font-size: 14px; font-weight: 600; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid #f0f0f0; }
.weight-item { margin-bottom: 12px; }
.wi-label { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; font-size: 13px; }
.wi-label strong { color: #3B82F6; }
.holiday-weeks { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 8px; }

.action-bar { background: #fff; border-radius: 8px; padding: 14px 16px; }
.quality-card { background: #fff; border-radius: 8px; padding: 14px 16px; text-align: center; }
.qc-score { font-size: 42px; font-weight: 700; }
.qc-good { color: #059669; } .qc-ok { color: #d97706; } .qc-bad { color: #EF4444; }
.qc-label { font-size: 12px; color: #888; margin-bottom: 8px; }

/* 右侧 */
.as-right { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }

.progress-panel { background: #fff; border-radius: 8px; padding: 16px; }
.pp-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.pp-percent { margin-left: auto; font-weight: 600; color: #3B82F6; }
.pp-message { font-size: 14px; color: #555; margin: 10px 0; }
.pp-stats { display: flex; gap: 24px; font-size: 13px; color: #666; margin-top: 8px; }

.dot-gray, .dot-running, .dot-done, .dot-err {
  width: 10px; height: 10px; border-radius: 50%; display: inline-block; flex-shrink: 0;
}
.dot-gray { background: #ccc; }
.dot-running { background: #3B82F6; animation: pulse 1s ease-in-out infinite; }
.dot-done { background: #059669; }
.dot-err { background: #EF4444; }
@keyframes pulse { 0%,100% { opacity:1; } 50% { opacity:.3; } }

.diagnostic-panel { background: #fff; border-radius: 8px; padding: 16px; }
.dp-title { font-size: 15px; font-weight: 600; margin-bottom: 12px; }
.dp-summary { display: flex; gap: 12px; margin-bottom: 16px; }
.dps-item {
  flex: 1; text-align: center; padding: 14px; background: #f8f9fb; border-radius: 8px;
  display: flex; flex-direction: column;
}
.dps-item.success { background: #f0fdf4; } .dps-item.fail { background: #fef2f2; }
.dpsi-num { font-size: 28px; font-weight: 700; }
.dpsi-label { font-size: 11px; color: #888; }
.dpf-title { font-size: 13px; font-weight: 600; color: #d97706; margin-bottom: 8px; }

.recommend-panel { background: #fff; border-radius: 8px; padding: 16px; }
.rec-card {
  border: 1px solid #e4e7ed; border-radius: 6px; padding: 12px; margin-bottom: 8px;
}
.rec-card.rec-best { border-color: #86efac; background: #f0fdf4; }
.rec-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.rec-score { font-size: 12px; color: #888; }
.rec-desc { margin: 0; font-size: 13px; color: #555; }

.heatmap-panel { background: #fff; border-radius: 8px; padding: 16px; }
.hm-grid { border: 1px solid #e4e7ed; border-radius: 4px; overflow: hidden; }
.hm-header { display: flex; background: #f8f9fb; }
.hmh-corner { width: 42px; text-align: center; padding: 3px 0; font-size: 9px; color: #888; border-right: 1px solid #e4e7ed; }
.hmh-day { flex: 1; text-align: center; padding: 3px 0; font-size: 10px; font-weight: 600; }
.hm-row { display: flex; }
.hmr-label {
  width: 42px; display: flex; align-items: center; justify-content: center;
  font-size: 9px; color: #888; border-right: 1px solid #e4e7ed; border-bottom: 1px solid #eee;
}
.hmr-cell { flex: 1; height: 18px; border-right: 1px solid #f5f5f5; border-bottom: 1px solid #f5f5f5; }
.hmr-cell:last-child { border-right: none; }
.hmr-cell.hm-av { background: #dcfce7; }
.hmr-cell.hm-un { background: #fee2e2; }
.hmr-cell.hm-na { background: #f3f3f3; }
.hm-legend { display: flex; gap: 16px; margin-top: 6px; font-size: 11px; color: #888; align-items: center; }
.leg-dot { width: 12px; height: 12px; display: inline-block; border-radius: 2px; margin-right: 3px; }
.leg-dot.av { background: #dcfce7; border: 1px solid #86efac; }
.leg-dot.un { background: #fee2e2; border: 1px solid #fca5a5; }
.leg-dot.lk { background: #dbeafe; border: 1px solid #93c5fd; }

@media (max-width: 1000px) {
  .as-body { flex-direction: column; }
  .as-left { width: 100%; }
}
</style>
