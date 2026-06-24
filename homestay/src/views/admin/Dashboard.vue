<template>
  <div class="admin-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <h1>管理员控制台</h1>
      <p class="subtitle">平台数据总览与管理</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: #FFE8E8;">
            <el-icon :size="24" color="#FF385C"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: #E8F4FF;">
            <el-icon :size="24" color="#409EFF"><House /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalHouses }}</div>
            <div class="stat-label">房源总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: #E8FFE8;">
            <el-icon :size="24" color="#67C23A"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalOrders }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: #FFF4E8;">
            <el-icon :size="24" color="#F59E0B"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.totalRevenue?.toLocaleString() || 0 }}</div>
            <div class="stat-label">交易总额</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 平台数据增长趋势 -->
      <el-card class="chart-card full-width" shadow="never">
        <div class="chart-header">
          <h2>平台数据增长趋势</h2>
          <div class="chart-controls">
            <el-date-picker
              v-model="growthDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadGrowthChart"
            />
            <el-button size="small" @click="resetGrowthDate">重置</el-button>
          </div>
        </div>
        <div ref="growthChart" class="chart-container"></div>
      </el-card>

      <!-- 房源状态分布饼图 -->
      <el-card class="chart-card" shadow="never">
        <h2>房源状态分布</h2>
        <div ref="houseStatusChart" class="chart-container"></div>
      </el-card>

      <!-- 订单数量趋势 -->
      <el-card class="chart-card" shadow="never">
        <div class="chart-header">
          <h2>订单数量趋势</h2>
          <div class="chart-controls">
            <el-date-picker
              v-model="orderDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadOrderChart"
            />
            <el-button size="small" @click="resetOrderDate">重置</el-button>
          </div>
        </div>
        <div ref="monthlyOrdersChart" class="chart-container"></div>
      </el-card>

      <!-- 平台收益趋势 -->
      <el-card class="chart-card full-width" shadow="never">
        <div class="chart-header">
          <h2>平台收益趋势</h2>
          <div class="chart-controls">
            <el-date-picker
              v-model="revenueDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadRevenueChart"
            />
            <el-button size="small" @click="resetRevenueDate">重置</el-button>
          </div>
        </div>
        <div ref="revenueChart" class="chart-container"></div>
      </el-card>
    </div>

    <!-- 快捷操作 -->
    <el-card class="quick-actions" shadow="never">
      <h2>快捷操作</h2>
      <div class="actions-grid">
        <div class="action-item" @click="router.push('/admin/users')">
          <div class="action-icon">
            <el-icon :size="32"><UserFilled /></el-icon>
          </div>
          <div class="action-text">
            <h3>用户管理</h3>
            <p>管理平台用户</p>
          </div>
        </div>

        <div class="action-item" @click="router.push('/admin/houses')">
          <div class="action-icon">
            <el-icon :size="32"><House /></el-icon>
          </div>
          <div class="action-text">
            <h3>房源管理</h3>
            <p>审核和管理房源信息</p>
          </div>
        </div>

        <div class="action-item" @click="router.push('/admin/hosts')">
          <div class="action-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="action-text">
            <h3>房东管理</h3>
            <p>查看和管理房东账号</p>
          </div>
        </div>

        <div class="action-item" @click="router.push('/admin/orders')">
          <div class="action-icon">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="action-text">
            <h3>订单管理</h3>
            <p>查看所有订单记录</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, House, User, Document, Money } from '@element-plus/icons-vue'
import request from '../../utils/request'
import * as echarts from 'echarts'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const router = useRouter()

const stats = ref({
  totalUsers: 0,
  totalHouses: 0,
  totalOrders: 0,
  totalRevenue: 0
})

// 各图表独立的日期范围
const growthDateRange = ref(null)
const orderDateRange = ref(null)
const revenueDateRange = ref(null)

const houseStatusChart = ref(null)
const monthlyOrdersChart = ref(null)
const revenueChart = ref(null)
const growthChart = ref(null)

let houseStatusChartInstance = null
let monthlyOrdersChartInstance = null
let revenueChartInstance = null
let growthChartInstance = null

// 获取默认日期范围（最近30天）
const getDefaultDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 29)
  return [
    start.toISOString().split('T')[0],
    end.toISOString().split('T')[0]
  ]
}

onMounted(() => {
  // 初始化默认日期范围
  growthDateRange.value = getDefaultDateRange()
  orderDateRange.value = getDefaultDateRange()
  revenueDateRange.value = getDefaultDateRange()
  
  loadStats()
  loadAllCharts()
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (houseStatusChartInstance) houseStatusChartInstance.dispose()
  if (monthlyOrdersChartInstance) monthlyOrdersChartInstance.dispose()
  if (revenueChartInstance) revenueChartInstance.dispose()
  if (growthChartInstance) growthChartInstance.dispose()
  
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
  houseStatusChartInstance?.resize()
  monthlyOrdersChartInstance?.resize()
  revenueChartInstance?.resize()
  growthChartInstance?.resize()
}

const loadStats = async () => {
  try {
    const res = await request.get('/api/admin/stats')
    stats.value = res
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadAllCharts = () => {
  loadGrowthChart()
  loadHouseStatusChart()
  loadOrderChart()
  loadRevenueChart()
}

const loadGrowthChart = async () => {
  try {
    const params = {}
    if (growthDateRange.value && growthDateRange.value.length === 2) {
      params.startDate = growthDateRange.value[0]
      params.endDate = growthDateRange.value[1]
    }
    
    const res = await request.get('/api/admin/stats/growth', { params })
    initGrowthChart(res)
  } catch (error) {
    console.error('加载增长趋势失败:', error)
  }
}

const loadHouseStatusChart = async () => {
  try {
    const res = await request.get('/api/admin/stats/house-status')
    initHouseStatusChart(res)
  } catch (error) {
    console.error('加载房源状态失败:', error)
  }
}

const loadOrderChart = async () => {
  try {
    const params = {}
    if (orderDateRange.value && orderDateRange.value.length === 2) {
      params.startDate = orderDateRange.value[0]
      params.endDate = orderDateRange.value[1]
    }
    
    const res = await request.get('/api/admin/stats/orders', { params })
    initMonthlyOrdersChart(res)
  } catch (error) {
    console.error('加载订单趋势失败:', error)
  }
}

const loadRevenueChart = async () => {
  try {
    const params = {}
    if (revenueDateRange.value && revenueDateRange.value.length === 2) {
      params.startDate = revenueDateRange.value[0]
      params.endDate = revenueDateRange.value[1]
    }
    
    const res = await request.get('/api/admin/stats/revenue', { params })
    initRevenueChart(res)
  } catch (error) {
    console.error('加载收益趋势失败:', error)
  }
}

const resetGrowthDate = () => {
  growthDateRange.value = getDefaultDateRange()
  loadGrowthChart()
}

const resetOrderDate = () => {
  orderDateRange.value = getDefaultDateRange()
  loadOrderChart()
}

const resetRevenueDate = () => {
  revenueDateRange.value = getDefaultDateRange()
  loadRevenueChart()
}

const initGrowthChart = (data) => {
  if (!growthChart.value) return
  
  if (!growthChartInstance) {
    growthChartInstance = echarts.init(growthChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['用户数', '房源数', '房东数'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.dates || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '用户数',
        type: 'bar',
        data: data.users || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#9C27B0' },
            { offset: 1, color: '#CE93D8' }
          ]),
          borderRadius: [5, 5, 0, 0]
        }
      },
      {
        name: '房源数',
        type: 'bar',
        data: data.houses || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#79bbff' }
          ]),
          borderRadius: [5, 5, 0, 0]
        }
      },
      {
        name: '房东数',
        type: 'bar',
        data: data.hosts || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FF385C' },
            { offset: 1, color: '#FF8FA3' }
          ]),
          borderRadius: [5, 5, 0, 0]
        }
      }
    ]
  }
  
  growthChartInstance.setOption(option)
}

const initHouseStatusChart = (data) => {
  if (!houseStatusChart.value) return
  
  if (!houseStatusChartInstance) {
    houseStatusChartInstance = echarts.init(houseStatusChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        name: '房源状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: data.pending || 0, name: '待审核', itemStyle: { color: '#F59E0B' } },
          { value: data.active || 0, name: '已上线', itemStyle: { color: '#67C23A' } },
          { value: data.inactive || 0, name: '已下架', itemStyle: { color: '#909399' } }
        ]
      }
    ]
  }
  
  houseStatusChartInstance.setOption(option)
}

const initMonthlyOrdersChart = (data) => {
  if (!monthlyOrdersChart.value) return
  
  if (!monthlyOrdersChartInstance) {
    monthlyOrdersChartInstance = echarts.init(monthlyOrdersChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.dates || [],
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '订单数量',
        type: 'bar',
        barWidth: '60%',
        data: data.counts || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#79bbff' }
          ]),
          borderRadius: [5, 5, 0, 0]
        }
      }
    ]
  }
  
  monthlyOrdersChartInstance.setOption(option)
}

const initRevenueChart = (data) => {
  if (!revenueChart.value) return
  
  if (!revenueChartInstance) {
    revenueChartInstance = echarts.init(revenueChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>{a}: ¥{c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates || []
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '平台收益',
        type: 'line',
        smooth: true,
        data: data.revenues || [],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        },
        itemStyle: {
          color: '#67C23A'
        },
        lineStyle: {
          width: 3
        }
      }
    ]
  }
  
  revenueChartInstance.setOption(option)
}
</script>

<style scoped>
.admin-dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.welcome-section {
  margin-bottom: 32px;
}

.welcome-section h1 {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #222;
}

.subtitle {
  font-size: 16px;
  color: #717171;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #222;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #717171;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.chart-card {
  border-radius: 12px;
  padding: 20px;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.chart-header h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #222;
}

.chart-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.chart-card h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #222;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.quick-actions {
  margin-bottom: 32px;
  border-radius: 12px;
}

.quick-actions h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 24px 0;
  color: #222;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  border: 1px solid #EBEBEB;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-item:hover {
  border-color: #222;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.action-icon {
  width: 64px;
  height: 64px;
  background: #f7f7f7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  color: #222;
}

.action-text {
  text-align: center;
}

.action-text h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #222;
}

.action-text p {
  font-size: 14px;
  color: #717171;
  margin: 0;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid,
  .actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
