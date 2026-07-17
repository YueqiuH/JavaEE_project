<template>
  <div class="d-page">
    <div class="d-crumb d-rise" style="--rise: 0">
      <router-link to="/home">首页</router-link>
      <el-icon><ArrowRight /></el-icon>
      <router-link :to="{ path: '/services', query: { domain: 'base' } }">基础数据</router-link>
      <el-icon><ArrowRight /></el-icon>
      <span>AI 智能报表</span>
    </div>

    <header class="d-head d-rise" style="--rise: 1">
      <div>
        <span class="d-head-module">基础数据 · D6</span>
        <h1>AI 自然语言报表助手</h1>
        <p class="d-head-desc">一句话描述需求，自动查询校园数据并生成分析与图表</p>
      </div>
    </header>

    <!-- 提问区 -->
    <section class="d-panel ask-panel d-rise" style="--rise: 2">
      <div class="ask-box" :class="{ 'is-busy': loading }">
        <el-input
          v-model="question"
          type="textarea"
          :rows="2"
          maxlength="500"
          placeholder="例如：统计今年各院系新生的报到率，并按报到率从高到低排列"
          @keydown.enter.exact.prevent="submit"
        />
        <div class="ask-foot">
          <span class="ask-count d-num">{{ question.length }} / 500</span>
          <el-button type="primary" :icon="Promotion" :loading="loading" :disabled="!question.trim()" @click="submit()">
            {{ loading ? '分析中' : '生成报表' }}
          </el-button>
        </div>
      </div>
      <div class="example-chips">
        <span>试试</span>
        <button v-for="example in examples" :key="example" type="button" :disabled="loading" @click="submit(example)">
          {{ example }}
        </button>
      </div>
    </section>

    <!-- 结果区 -->
    <section v-if="report" class="d-panel d-panel-pad result-panel d-rise">
      <div class="result-heading">
        <h2>{{ report.title }}</h2>
        <el-tag v-if="report.demoMode" type="warning" effect="plain" size="small">演示模式</el-tag>
      </div>

      <div class="analysis-quote">
        <p>{{ report.analysis }}</p>
        <cite>AI 分析 · 基于 {{ report.rows.length }} 条查询结果</cite>
      </div>

      <el-empty v-if="!report.rows.length" description="查询结果为空，试着换一种问法" :image-size="56" />
      <template v-else>
        <EChart v-if="report.chartType !== 'table' && chartOption" :option="chartOption" height="360px" />
        <el-table :data="report.rows" size="small" max-height="360" class="result-table">
          <el-table-column
            v-for="col in report.columns" :key="col"
            :prop="col" :label="col" min-width="120" show-overflow-tooltip
          />
        </el-table>
      </template>

      <el-collapse class="sql-collapse">
        <el-collapse-item title="查看生成的 SQL" name="sql">
          <pre class="sql-text">{{ report.sql }}</pre>
        </el-collapse-item>
      </el-collapse>
      <p class="ai-disclaimer">AI 生成内容仅供参考，请结合实际业务数据判断</p>
    </section>

    <!-- 空状态 -->
    <section v-else-if="!loading" class="d-panel empty-panel d-rise" style="--rise: 3">
      <span class="empty-mark"><el-icon><DataLine /></el-icon></span>
      <h3>输入问题，开始探索校园数据</h3>
      <p>支持招生报到率、学生结构、师资分布、选课情况等统计口径，可以要求对比、排序与占比。</p>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ArrowRight, DataLine, Promotion } from '@element-plus/icons-vue'
import { generateAiReport } from '@/api/base.js'
import EChart from './components/EChart.vue'
import './base-d.css'

const examples = [
  '统计今年各院系新生的报到率',
  '对比近三年全校计划招生与实际报到人数',
  '各专业在读学生人数排名前十',
  '统计学生生源地分布',
]

const question = ref('')
const loading = ref(false)
const report = ref(null)

const submit = async (preset) => {
  const text = (typeof preset === 'string' ? preset : question.value).trim()
  if (!text || loading.value) return
  question.value = text
  loading.value = true
  try {
    const res = await generateAiReport({ question: text })
    report.value = res.data
  } finally {
    loading.value = false
  }
}

// 首列作维度，其余数值列作系列（色板由 EChart 主题提供）
const chartOption = computed(() => {
  const r = report.value
  if (!r || !r.rows.length || r.columns.length < 2) return null
  const [dimension, ...metrics] = r.columns
  const categories = r.rows.map((row) => String(row[dimension]))

  if (r.chartType === 'pie') {
    return {
      tooltip: { trigger: 'item', formatter: '{b}：{c}（{d}%）' },
      legend: { type: 'scroll', bottom: 0 },
      series: [{
        type: 'pie', radius: ['42%', '66%'], center: ['50%', '44%'],
        data: r.rows.map((row) => ({ name: String(row[dimension]), value: row[metrics[0]] })),
      }],
    }
  }
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: metrics, top: 0 },
    grid: { left: 8, right: 12, top: 34, bottom: 6, containLabel: true },
    xAxis: { type: 'category', data: categories, axisLabel: { interval: 0, rotate: categories.length > 6 ? 24 : 0 } },
    yAxis: { type: 'value' },
    series: metrics.map((metric) => ({
      name: metric,
      type: r.chartType === 'line' ? 'line' : 'bar',
      data: r.rows.map((row) => row[metric]),
    })),
  }
})
</script>

<style scoped>
.ask-panel { margin-bottom: 18px; padding: 18px 20px; }
.ask-box {
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.ask-box:focus-within { border-color: var(--d-accent); box-shadow: 0 0 0 3px rgba(138, 77, 143, 0.09); }
.ask-box.is-busy { border-color: var(--d-accent-line); }
.ask-box :deep(.el-textarea__inner) { border: 0; box-shadow: none; padding: 12px 14px; }
.ask-foot { display: flex; align-items: center; justify-content: space-between; padding: 6px 8px 8px 14px; }
.ask-count { color: var(--color-text-tertiary); font-size: 11px; }
.example-chips { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin-top: 12px; }
.example-chips > span { color: var(--color-text-tertiary); font-size: 12px; }
.example-chips button {
  padding: 4px 12px;
  color: var(--color-text-secondary);
  font-size: 12px;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.example-chips button:hover:not(:disabled) { color: var(--d-accent); background: var(--d-accent-soft); border-color: var(--d-accent-line); }
.example-chips button:disabled { opacity: 0.5; cursor: default; }

.result-heading { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.result-heading h2 { margin: 0; font-size: 17px; font-weight: 600; }
.analysis-quote {
  margin-bottom: 18px;
  padding: 13px 16px;
  background: var(--d-accent-soft);
  border-left: 3px solid var(--d-accent);
  border-radius: 0 8px 8px 0;
}
.analysis-quote p { margin: 0; line-height: 1.85; font-size: 13.5px; }
.analysis-quote cite { display: block; margin-top: 7px; color: var(--d-accent); font-size: 11px; font-style: normal; letter-spacing: 0.04em; }
.result-table { margin-top: 16px; }
.sql-collapse { margin-top: 16px; }
.sql-text {
  margin: 0;
  padding: 12px 14px;
  overflow-x: auto;
  background: #f6f8fa;
  border-radius: 6px;
  font-family: var(--d-num-font);
  font-size: 12px;
  line-height: 1.65;
  white-space: pre-wrap;
}
.ai-disclaimer { margin: 12px 0 0; color: var(--color-text-tertiary); font-size: 11px; text-align: center; }

.empty-panel { display: flex; flex-direction: column; align-items: center; padding: 58px 20px; text-align: center; }
.empty-mark {
  display: inline-flex;
  width: 52px; height: 52px;
  align-items: center; justify-content: center;
  margin-bottom: 14px;
  color: var(--d-accent);
  font-size: 26px;
  background: var(--d-accent-soft);
  border-radius: 14px;
}
.empty-panel h3 { margin: 0 0 6px; font-size: 16px; font-weight: 600; }
.empty-panel p { margin: 0; max-width: 440px; color: var(--color-text-secondary); font-size: 13px; line-height: 1.7; }
</style>
