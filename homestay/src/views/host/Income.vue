<template>
  <div class="revenue-page">

    <h2 class="page-title">收入统计</h2>

    <!-- 时间筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="range" @change="loadData">
        <el-radio-button label="7">近7天</el-radio-button>
        <el-radio-button label="30">近30天</el-radio-button>
        <el-radio-button label="365">本年</el-radio-button>
      </el-radio-group>
    </div>

    <!-- KPI -->
    <div class="kpi">
      <div class="card">
        <div class="label">总收入</div>
        <div class="value">¥ {{ stats.totalIncome }}</div>
      </div>
      <div class="card">
        <div class="label">待结算</div>
        <div class="value">¥ {{ stats.pending }}</div>
      </div>
      <div class="card">
        <div class="label">已结算</div>
        <div class="value">¥ {{ stats.settled }}</div>
      </div>
      <div class="card">
        <div class="label">订单数</div>
        <div class="value">{{ stats.orders }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts">

      <div class="chart-card">
        <div class="chart-title">收入趋势</div>
        <div ref="lineChart" class="chart"></div>
      </div>

      <div class="chart-card">
        <div class="chart-title">收入来源占比</div>
        <div ref="pieChart" class="chart"></div>
      </div>

    </div>

    <!-- 经营指标 -->
    <div class="extra">
      <div class="extra-card">
        <div>入住率</div>
        <div class="big">{{ metrics.occupancyRate }}</div>
      </div>
      <div class="extra-card">
        <div>平均房价</div>
        <div class="big">{{ metrics.averagePrice }}</div>
      </div>
      <div class="extra-card">
        <div>取消率</div>
        <div class="big">{{ metrics.cancellationRate }}</div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../../utils/request'

const range = ref('30')

const stats = ref({
  totalIncome: 0,
  pending: 0,
  settled: 0,
  orders: 0
})

const metrics = ref({
  occupancyRate: '0%',
  averagePrice: '¥ 0',
  cancellationRate: '0%'
})

const lineChart = ref(null)
const pieChart = ref(null)

let lineInstance = null
let pieInstance = null

const loadData = async () => {
  try {
    const res = await request.get('/api/host/income', {
      params: { range: range.value }
    })

    if (res.success) {
      const data = res.data
      stats.value = {
        totalIncome: data.totalIncome,
        pending: data.pending,
        settled: data.settled,
        orders: data.orders
      }

      metrics.value = data.metrics

      // 处理收入趋势数据
      const dates = data.trend.map(item => item.date)
      const values = data.trend.map(item => item.income)

      renderLine(dates, values)
      renderPie(data.source)
    }
  } catch (error) {
    console.error('获取收入数据失败:', error)
  }
}

const renderLine = (dates, values) => {
  if (!lineChart.value) return
  
  if (lineInstance) {
    lineInstance.dispose()
  }
  
  lineInstance = echarts.init(lineChart.value)
  lineInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{
      data: values,
      type: 'line',
      smooth: true
    }]
  })
}

const renderPie = (sourceData) => {
  if (!pieChart.value) return
  
  if (pieInstance) {
    pieInstance.dispose()
  }
  
  pieInstance = echarts.init(pieChart.value)
  pieInstance.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: sourceData
    }]
  })
}

onMounted(() => {
  nextTick(() => {
    loadData()
  })
})
</script>

<style scoped>
.revenue-page {
  padding: 30px;
  background: #f7f7f7;
}

.filter-bar {
  margin-bottom: 20px;
}

.kpi {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.label {
  font-size: 14px;
  color: #888;
}

.value {
  font-size: 22px;
  font-weight: bold;
  margin-top: 8px;
}

.charts {
  display: flex;
  gap: 20px;
}

.chart-card {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.chart-title {
  margin-bottom: 10px;
  font-weight: 600;
}

.chart {
  height: 300px;
}

.extra {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}

.extra-card {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 14px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.big {
  font-size: 24px;
  font-weight: bold;
  margin-top: 8px;
}
</style>