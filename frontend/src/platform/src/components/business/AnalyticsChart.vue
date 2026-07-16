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
  mode: { type: String, default: 'line' },
})

const chartRef = ref(null)
let chart = null
let resizeObserver = null

const buildOption = () => {
  const categories = ['1月', '2月', '3月', '4月', '5月', '6月', '7月']
  const values = [1180, 1260, 1215, 1380, 1460, 1520, 1648]
  if (props.mode === 'bar') {
    return {
      color: ['#08678f', '#e9a824'],
      tooltip: { trigger: 'axis' },
      legend: { right: 8, top: 0, data: ['计划人数', '报到人数'] },
      grid: { left: 44, right: 22, top: 48, bottom: 34 },
      xAxis: { type: 'category', data: ['工学', '理学', '管理学', '文学', '艺术学'], axisTick: { show: false }, axisLine: { lineStyle: { color: '#dfe4ea' } } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#edf0f3' } } },
      series: [
        { name: '计划人数', type: 'bar', barMaxWidth: 28, data: [820, 420, 360, 280, 160], itemStyle: { borderRadius: [3, 3, 0, 0] } },
        { name: '报到人数', type: 'bar', barMaxWidth: 28, data: [786, 405, 348, 269, 151], itemStyle: { borderRadius: [3, 3, 0, 0] } },
      ],
    }
  }
  return {
    color: ['#08678f'],
    tooltip: { trigger: 'axis', valueFormatter: (value) => `${value} 人` },
    grid: { left: 48, right: 22, top: 34, bottom: 34 },
    xAxis: { type: 'category', boundaryGap: false, data: categories, axisTick: { show: false }, axisLine: { lineStyle: { color: '#dfe4ea' } } },
    yAxis: { type: 'value', min: 1000, splitLine: { lineStyle: { color: '#edf0f3' } } },
    series: [{ type: 'line', smooth: true, symbolSize: 7, data: values, lineStyle: { width: 3 }, areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(8,103,143,.24)' }, { offset: 1, color: 'rgba(8,103,143,.02)' }] } } }],
  }
}

const render = () => chart?.setOption(buildOption(), true)

onMounted(() => {
  chart = init(chartRef.value)
  render()
  resizeObserver = new ResizeObserver(() => chart?.resize())
  resizeObserver.observe(chartRef.value)
})

watch(() => props.mode, render)
onBeforeUnmount(() => { resizeObserver?.disconnect(); chart?.dispose() })
</script>

<style scoped>
.analytics-chart { width:100%; height:320px; }
@media(max-width:767px){.analytics-chart{height:280px}}
</style>
