<template>
  <div ref="chartRef" class="analytics-chart" role="img" :aria-label="title"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'

use([BarChart, LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const props = defineProps({
  title: { type: String, default: '数据趋势图' },
  /** 'line' | 'bar' */
  mode: { type: String, default: 'line' },
  /** x 轴类目标签，不传则使用演示数据 */
  categories: { type: Array, default: null },
  /**
   * 系列数据，每项 { name, type?, data }
   * line 模式默认取第一项 data；bar 模式取所有项
   */
  series: { type: Array, default: null },
  /** 图表高度 */
  height: { type: String, default: '320px' },
  /** y 轴最小值（line 模式） */
  yMin: { type: Number, default: null },
})

const chartRef = ref(null)
let chart = null
let resizeObserver = null

// ===== 演示默认数据（未传 props 时使用） =====
const DEFAULT_LINE_CATEGORIES = ['1月', '2月', '3月', '4月', '5月', '6月', '7月']
const DEFAULT_LINE_VALUES = [1180, 1260, 1215, 1380, 1460, 1520, 1648]
const DEFAULT_BAR_CATEGORIES = ['工学', '理学', '管理学', '文学', '艺术学']
const DEFAULT_BAR_SERIES = [
  { name: '计划人数', data: [820, 420, 360, 280, 160] },
  { name: '报到人数', data: [786, 405, 348, 269, 151] },
]

const CHART_COLORS = ['#08678f', '#e9a824', '#16865b', '#c77800', '#8a4d8f', '#c2413b']

const buildOption = () => {
  if (props.mode === 'bar') {
    const cats = props.categories?.length ? props.categories : DEFAULT_BAR_CATEGORIES
    const rawSeries = props.series?.length ? props.series : DEFAULT_BAR_SERIES
    const series = rawSeries.map((s, i) => ({
      name: s.name || `系列${i + 1}`,
      type: s.type || 'bar',
      barMaxWidth: 28,
      data: Array.isArray(s.data) ? s.data : [],
      itemStyle: { borderRadius: [3, 3, 0, 0] },
    }))
    return {
      color: CHART_COLORS,
      tooltip: { trigger: 'axis' },
      legend: { right: 8, top: 0, data: series.map((s) => s.name) },
      grid: { left: 44, right: 22, top: 48, bottom: 34 },
      xAxis: { type: 'category', data: cats, axisTick: { show: false }, axisLine: { lineStyle: { color: '#dfe4ea' } } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#edf0f3' } } },
      series,
    }
  }
  // line mode (default)
  const cats = props.categories?.length ? props.categories : DEFAULT_LINE_CATEGORIES
  const rawSeries = props.series?.length ? props.series : [{ data: DEFAULT_LINE_VALUES }]
  const yAxisConfig = { type: 'value', splitLine: { lineStyle: { color: '#edf0f3' } } }
  if (props.yMin != null) yAxisConfig.min = props.yMin
  return {
    color: CHART_COLORS,
    tooltip: { trigger: 'axis', valueFormatter: (value) => `${value} 人` },
    grid: { left: 48, right: 22, top: 34, bottom: 34 },
    xAxis: { type: 'category', boundaryGap: false, data: cats, axisTick: { show: false }, axisLine: { lineStyle: { color: '#dfe4ea' } } },
    yAxis: yAxisConfig,
    series: rawSeries.map((s) => ({
      name: s.name,
      type: s.type || 'line',
      smooth: true,
      symbolSize: 7,
      data: Array.isArray(s.data) ? s.data : [],
      lineStyle: { width: 3 },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(8,103,143,.24)' }, { offset: 1, color: 'rgba(8,103,143,.02)' }] } },
    })),
  }
}

const render = () => chart?.setOption(buildOption(), true)

onMounted(() => {
  chart = init(chartRef.value)
  render()
  resizeObserver = new ResizeObserver(() => chart?.resize())
  resizeObserver.observe(chartRef.value)
})

watch(() => [props.mode, props.categories, props.series, props.yMin], render, { deep: true })
onBeforeUnmount(() => { resizeObserver?.disconnect(); chart?.dispose() })
</script>

<style scoped>
.analytics-chart { width:100%; height:v-bind(height); }
@media(max-width:767px){.analytics-chart{height:280px}}
</style>
