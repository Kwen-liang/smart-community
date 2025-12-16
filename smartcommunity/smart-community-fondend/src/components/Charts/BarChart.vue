<template>
  <div ref="chartRef" :style="{ width: width, height: height }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

// 接收父组件传递的 props
const props = defineProps({
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '300px'
  },
  // 图表数据
  chartData: {
    type: Object,
    required: true,
    default: () => ({
      categories: [], // x轴分类
      data: []        // y轴数据
    })
  },
  title: {
    type: String,
    default: ''
  }
})

const chartRef = ref(null)
let myChart = null

const initChart = () => {
  if (!chartRef.value) return

  // 初始化 ECharts 实例
  myChart = echarts.init(chartRef.value)

  // 设置配置项
  const option = {
    title: {
      text: props.title,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: props.chartData.categories,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        barWidth: '60%',
        data: props.chartData.data,
        itemStyle: {
          color: '#409EFF' // Element Plus 主题色
        }
      }
    ]
  }

  myChart.setOption(option)
}

// 监听数据变化，自动刷新图表
watch(() => props.chartData, () => {
  if (myChart) {
    myChart.setOption({
      xAxis: { data: props.chartData.categories },
      series: [{ data: props.chartData.data }]
    })
  }
}, { deep: true })

// 窗口大小改变时自适应
const handleResize = () => {
  myChart && myChart.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (myChart) {
    myChart.dispose()
  }
})
</script>

<style scoped>

</style>
