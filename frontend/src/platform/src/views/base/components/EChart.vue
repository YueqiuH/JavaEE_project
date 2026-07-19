<template>
  <div ref="el" class="echart" :style="{ height }" />
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

/**
 * 基础数据板块统一图表主题：
 * 分类色板以域紫为首色，坐标轴去线留网格，tooltip/图例风格统一。
 */
const THEME_NAME = 'campus-base'
// 模块只会执行一次，此处注册主题即可（重复注册也无副作用）
echarts.registerTheme(THEME_NAME, {
    color: ['#8a4d8f', '#3973b7', '#16865b', '#c77800', '#c2413b', '#5f6b7a', '#b5779e', '#4f9fc4'],
    textStyle: { color: '#3d434c' },
    categoryAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#8a919c', fontSize: 11 },
      splitLine: { show: false },
    },
    valueAxis: {
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#8a919c', fontSize: 11 },
      splitLine: { lineStyle: { color: '#eef0f3' } },
      nameTextStyle: { color: '#8a919c', fontSize: 11 },
    },
    legend: { textStyle: { color: '#5f6b7a', fontSize: 12 }, itemWidth: 14, itemHeight: 9 },
    tooltip: {
      backgroundColor: '#ffffff',
      borderColor: '#e6e8eb',
      borderWidth: 1,
      textStyle: { color: '#3d434c', fontSize: 12 },
      extraCssText: 'box-shadow: 0 4px 14px rgba(31,35,41,.1); border-radius: 6px; padding: 8px 12px;',
    },
    bar: { itemStyle: { borderRadius: [3, 3, 0, 0] }, barMaxWidth: 38 },
    line: { symbolSize: 5, lineStyle: { width: 2 } },
    pie: { itemStyle: { borderColor: '#fff', borderWidth: 2 }, label: { color: '#5f6b7a' } },
})

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '320px' },
})
const emit = defineEmits(['chart-click'])

const el = ref(null)
let chart = null

const render = () => {
  if (chart && props.option) chart.setOption(props.option, true)
}
const resize = () => chart?.resize()

onMounted(() => {
  chart = echarts.init(el.value, THEME_NAME)
  chart.on('click', (params) => emit('chart-click', params))
  render()
  window.addEventListener('resize', resize)
})

watch(() => props.option, render, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.echart { width: 100%; }
</style>
