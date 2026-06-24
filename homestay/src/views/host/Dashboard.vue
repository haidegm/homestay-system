<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <h1>欢迎回来，{{ username }}</h1>
      <p class="subtitle">管理您的房源，查看预订和收入</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: #FFE8E8;">
            <el-icon :size="24" color="#FF385C"><House /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalHouses }}</div>
            <div class="stat-label">房源总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: #E8F4FF;">
            <el-icon :size="24" color="#409EFF"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalOrders }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: #E8FFE8;">
            <el-icon :size="24" color="#67C23A"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.totalIncome }}</div>
            <div class="stat-label">总收入</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon" style="background: #FFF4E8;">
            <el-icon :size="24" color="#E6A23C"><Star /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.avgRating }}</div>
            <div class="stat-label">平均评分</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 快捷操作 -->
    <el-card class="quick-actions" shadow="never">
      <h2>快捷操作</h2>
      <div class="actions-grid">
        <div class="action-item" @click="router.push('/host/house/add')">
          <div class="action-icon">
            <el-icon :size="32"><Plus /></el-icon>
          </div>
          <div class="action-text">
            <h3>发布新房源</h3>
            <p>添加新的出租房源</p>
          </div>
        </div>

        <div class="action-item" @click="router.push('/host/house/list')">
          <div class="action-icon">
            <el-icon :size="32"><Edit /></el-icon>
          </div>
          <div class="action-text">
            <h3>管理房源</h3>
            <p>编辑房源信息和价格</p>
          </div>
        </div>

        <div class="action-item" @click="router.push('/host/order')">
          <div class="action-icon">
            <el-icon :size="32"><Calendar /></el-icon>
          </div>
          <div class="action-text">
            <h3>查看预订</h3>
            <p>管理即将到来的预订</p>
          </div>
        </div>

        <div class="action-item" @click="router.push('/host/income')">
          <div class="action-icon">
            <el-icon :size="32"><TrendCharts /></el-icon>
          </div>
          <div class="action-text">
            <h3>收入报告</h3>
            <p>查看收入统计和趋势</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 数据可视化 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="chart-card" shadow="never">
            <h2>收入趋势</h2>
            <div ref="incomeChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card" shadow="never">
            <h2>订单状态分布</h2>
            <div ref="orderStatusChartRef" class="chart-container-small"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 最近订单 -->
    <el-card class="recent-orders" shadow="never">
      <div class="card-header">
        <h2>最近订单</h2>
        <el-button text @click="router.push('/host/order')">
          查看全部
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <el-table 
        :data="recentOrders" 
        style="width: 100%" 
        v-loading="loading"
        @row-click="handleRowClick"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }">
            <span class="order-no-link">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="houseTitle" label="房源" min-width="200">
          <template #default="{ row }">
            <div class="house-title-cell">
              <el-icon><House /></el-icon>
              <span>{{ row.houseTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="guestName" label="房客" width="120">
          <template #default="{ row }">
            <div class="guest-cell">
              <el-icon><User /></el-icon>
              <span>{{ row.guestName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="入住时间" width="220">
          <template #default="{ row }">
            <div class="date-range">
              <span>{{ row.checkInDate }}</span>
              <el-icon><Right /></el-icon>
              <span>{{ row.checkOutDate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.totalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty 
        v-if="!loading && recentOrders.length === 0" 
        description="暂无订单"
        :image-size="100"
      >
        <el-button type="primary" @click="router.push('/host/house/add')">
          发布房源
        </el-button>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { 
  House, Document, Money, Star, Plus, Edit, 
  Calendar, TrendCharts, ArrowRight, Right, User
} from '@element-plus/icons-vue'
import request from '../../utils/request'
import * as echarts from 'echarts'

const router = useRouter()
const username = ref('')
const loading = ref(false)

const incomeChartRef = ref(null)
const orderStatusChartRef = ref(null)
let incomeChart = null
let orderStatusChart = null

const stats = ref({
  totalHouses: 0,
  totalOrders: 0,
  totalIncome: 0,
  avgRating: 0
})

const recentOrders = ref([])

onMounted(async () => {
  username.value = localStorage.getItem('username') || '房东'
  await loadDashboardData()
  await nextTick()
  initCharts()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  if (incomeChart) {
    incomeChart.dispose()
  }
  if (orderStatusChart) {
    orderStatusChart.dispose()
  }
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
  if (incomeChart) {
    incomeChart.resize()
  }
  if (orderStatusChart) {
    orderStatusChart.resize()
  }
}

const loadDashboardData = async () => {
  loading.value = true
  try {
    // 加载统计数据
    const statsRes = await request.get('/api/host/stats')
    console.log('统计数据:', statsRes)
    
    stats.value = {
      totalHouses: statsRes.totalHouses || 0,
      totalOrders: statsRes.totalOrders || 0,
      totalIncome: statsRes.totalIncome ? Math.round(statsRes.totalIncome) : 0,
      avgRating: statsRes.avgRating ? statsRes.avgRating.toFixed(1) : '0.0'
    }

    // 加载最近订单
    const ordersRes = await request.get('/api/host/orders/recent', {
      params: { limit: 5 }
    })
    console.log('最近订单:', ordersRes)
    
    recentOrders.value = (ordersRes || []).map(order => ({
      id: order.id,
      orderNo: order.orderNo,
      houseTitle: order.houseTitle || '未命名房源',
      guestName: order.guestName || '匿名用户',
      checkInDate: formatDate(order.checkInDate),
      checkOutDate: formatDate(order.checkOutDate),
      totalPrice: order.totalPrice,
      status: order.status
    }))

  } catch (error) {
    console.error('加载数据失败:', error)
    // 如果加载失败，显示默认值
    stats.value = {
      totalHouses: 0,
      totalOrders: 0,
      totalIncome: 0,
      avgRating: '0.0'
    }
    recentOrders.value = []
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  // 处理 LocalDate 格式 (YYYY-MM-DD)
  const dateString = String(dateStr)
  if (dateString.includes('T')) {
    // 如果是 ISO 格式，只取日期部分
    return dateString.split('T')[0]
  }
  // 如果已经是 YYYY-MM-DD 格式，直接返回
  return dateString.slice(0, 10)
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'CANCELLED': 'info',
    'COMPLETED': ''
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'CANCELLED': '已取消',
    'COMPLETED': '已完成'
  }
  return map[status] || status
}

const handleRowClick = () => {
  router.push('/host/order')
}

const tableRowClassName = () => {
  return 'clickable-row'
}

// 初始化图表
const initCharts = async () => {
  try {
    // 获取图表数据
    const chartData = await request.get('/api/host/chart-data')
    
    // 初始化收入趋势图
    if (incomeChartRef.value) {
      incomeChart = echarts.init(incomeChartRef.value)
      const incomeOption = {
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
          data: chartData.incomeData?.dates || ['1月', '2月', '3月', '4月', '5月', '6月'],
          axisLine: {
            lineStyle: {
              color: '#EBEBEB'
            }
          },
          axisLabel: {
            color: '#717171'
          }
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            color: '#717171',
            formatter: '¥{value}'
          },
          splitLine: {
            lineStyle: {
              color: '#EBEBEB',
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: '收入',
            type: 'line',
            smooth: true,
            data: chartData.incomeData?.values || [1200, 2300, 1800, 3200, 2800, 4100],
            itemStyle: {
              color: '#FF385C'
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(255, 56, 92, 0.3)' },
                { offset: 1, color: 'rgba(255, 56, 92, 0.05)' }
              ])
            }
          }
        ]
      }
      incomeChart.setOption(incomeOption)
    }

    // 初始化订单状态分布图
    if (orderStatusChartRef.value) {
      orderStatusChart = echarts.init(orderStatusChartRef.value)
      const statusOption = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: '10%',
          top: 'center',
          textStyle: {
            color: '#717171'
          }
        },
        series: [
          {
            name: '订单状态',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['35%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 14,
                fontWeight: 'bold'
              }
            },
            data: chartData.orderStatusData || [
              { value: stats.value.totalOrders * 0.1, name: '待确认', itemStyle: { color: '#E6A23C' } },
              { value: stats.value.totalOrders * 0.6, name: '已确认', itemStyle: { color: '#67C23A' } },
              { value: stats.value.totalOrders * 0.25, name: '已完成', itemStyle: { color: '#909399' } },
              { value: stats.value.totalOrders * 0.05, name: '已取消', itemStyle: { color: '#F56C6C' } }
            ]
          }
        ]
      }
      orderStatusChart.setOption(statusOption)
    }
  } catch (error) {
    console.error('加载图表数据失败:', error)
    // 使用模拟数据
    initChartsWithMockData()
  }
}

const initChartsWithMockData = () => {
  // 使用模拟数据初始化图表（如果API失败）
  if (incomeChartRef.value && !incomeChart) {
    incomeChart = echarts.init(incomeChartRef.value)
    incomeChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
      yAxis: { type: 'value' },
      series: [{ type: 'line', data: [1200, 2300, 1800, 3200, 2800, 4100], smooth: true }]
    })
  }
}
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

/* 欢迎区域 */
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

/* 统计卡片 */
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

/* 快捷操作 */
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

/* 最近订单 */
.recent-orders {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
  color: #222;
}

.price-text {
  font-weight: 600;
  color: #222;
}

/* 表格样式优化 */
.clickable-row {
  cursor: pointer;
  transition: background-color 0.2s;
}

.clickable-row:hover {
  background-color: #f7f7f7;
}

.order-no-link {
  color: #FF385C;
  font-weight: 500;
}

.house-title-cell,
.guest-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.house-title-cell .el-icon,
.guest-cell .el-icon {
  color: #717171;
  font-size: 14px;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #717171;
}

.date-range .el-icon {
  font-size: 12px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .stats-grid,
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

/* 图表区域 */
.charts-section {
  margin-bottom: 32px;
}

.chart-card {
  border-radius: 12px;
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

.chart-container-small {
  width: 100%;
  height: 350px;
}

</style>