<template>
  <div ref="chartRef" :style="{ width: width, height: height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  width: { type: String, default: '100%' },
  height: { type: String, default: '350px' },
  title: { type: String, default: '' },
  // 数据格式: { xData: [], yData: [], data: [[xIndex, yIndex, value], ...] }
  chartData: {
    type: Object,
    required: true,
    default: () => ({ xData: [], yData: [], data: [] })
  }
})

const chartRef = ref(null)
let myChart = null

const initChart = () => {
  if (!chartRef.value) return
  myChart = echarts.init(chartRef.value)

  const option = {
    title: {
      text: props.title,
      left: 'center',
      textStyle: { fontSize: 14, color: '#666' }
    },
    tooltip: {
      position: 'top',
      formatter: (params) => {
        return `区域: ${props.chartData.xData[params.value[0]]}-${props.chartData.yData[params.value[1]]}<br />事件密度: ${params.value[2]}`
      }
    },
    grid: {
      top: '15%',
      bottom: '10%',
      left: '8%',
      right: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: props.chartData.xData,
      splitArea: { show: true },
      name: '网格横轴',
      nameLocation: 'middle',
      nameGap: 25
    },
    yAxis: {
      type: 'category',
      data: props.chartData.yData,
      splitArea: { show: true },
      name: '网格纵轴',
      nameLocation: 'middle',
      nameGap: 30
    },
    visualMap: {
      min: 0,
      max: 10,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '0%',
      inRange: {
        color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695'] // 经典的蓝深色热力图配色
        // 或者暖色调: ['#f6efa6', '#d88273', '#bf444c']
      }
    },
    series: [{
      name: '事件密度',
      type: 'heatmap',
      data: props.chartData.data,
      label: { show: false },
      itemStyle: {
        emphasis: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  myChart.setOption(option)
}

watch(() => props.chartData, () => {
  if (myChart) {
    myChart.setOption({
      xAxis: { data: props.chartData.xData },
      yAxis: { data: props.chartData.yData },
      series: [{ data: props.chartData.data }]
    })
  }
}, { deep: true })

const handleResize = () => myChart && myChart.resize()

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (myChart) myChart.dispose()
})
</script>
