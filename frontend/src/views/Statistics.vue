<template>
  <div class="statistics-container">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><TrendCharts /></el-icon>
            统计分析
          </h1>
          <p class="page-subtitle">深入了解系统数据和性能指标</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="refreshDashboard">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- Key Metrics -->
    <div class="metrics-section" v-loading="loading">
      <el-row :gutter="24">

        <el-col :xs="24" :sm="12" :md="6">
          <div class="metric-card">
            <div class="metric-icon response-time">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-number">{{ formatNumber(systemOverview?.totalStudents || 0) }}</div>
              <div class="metric-label">学生总数</div>
              <div class="metric-change positive">活跃用户</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="metric-card">
            <div class="metric-icon success-rate">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-number">{{ systemOverview?.averageScore?.toFixed(1) || '0.0' }}</div>
              <div class="metric-label">平均成绩</div>
              <div class="metric-change positive">学习成果</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="metric-card">
            <div class="metric-icon api-calls">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-number">{{ formatNumber(systemOverview?.totalApiCalls || 0) }}</div>
              <div class="metric-label">API调用总数</div>
              <div class="metric-change positive">系统活跃度</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <el-row :gutter="24">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><PieChart /></el-icon>
                  <span>成绩分布</span>
                </div>
                <el-button size="small" @click="refreshScoreStats">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            <div class="chart-container" v-loading="scoreStatsLoading">
              <v-chart 
                class="chart" 
                :option="scoreDistributionOption" 
                :loading="scoreStatsLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><DataAnalysis /></el-icon>
                  <span>API调用统计</span>
                </div>
                <el-button size="small" @click="refreshApiStats">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            <div class="chart-container" v-loading="apiStatsLoading">
              <v-chart 
                class="chart" 
                :option="apiStatsOption" 
                :loading="apiStatsLoading"
                autoresize
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="24">
        <el-col :span="24">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><Monitor /></el-icon>
                  <span>系统概览仪表板</span>
                </div>
                <div class="header-actions">
                  <el-button size="small" @click="refreshDashboard">
                    <el-icon><Refresh /></el-icon>
                    刷新
                  </el-button>
                </div>
              </div>
            </template>
            <div class="dashboard-container-inner" v-loading="dashboardLoading">
              <!-- System Status Grid -->
              <div class="dashboard-grid">
                <div class="dashboard-item">
                  <div class="dashboard-chart-container">
                    <h4>学生排行榜</h4>
                    <v-chart 
                      class="dashboard-chart" 
                      :option="studentRankingOption" 
                      :loading="rankingLoading"
                      autoresize
                    />
                  </div>
                </div>
                
                <div class="dashboard-item">
                  <div class="dashboard-chart-container">
                    <h4>教学数据概览</h4>
                    <v-chart 
                      class="dashboard-chart" 
                      :option="systemActivityOption" 
                      :loading="loading"
                      autoresize
                    />
                  </div>
                </div>
                
                <div class="dashboard-item">
                  <div class="dashboard-info">
                    <h4>系统信息</h4>
                    <div class="info-grid">
                      <div class="info-item">
                        <span class="info-label">最佳班级:</span>
                        <span class="info-value">{{ systemOverview?.topPerformingClass || '暂无数据' }}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">总用户数:</span>
                        <span class="info-value">{{ formatNumber(systemOverview?.totalUsers || 0) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">数据更新:</span>
                        <span class="info-value">{{ formatTime(lastUpdateTime) }}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">系统状态:</span>
                        <span class="info-value status-healthy">
                          <el-icon><CircleCheck /></el-icon>
                          正常运行
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart as EChartsP, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { 
  TrendCharts, DataAnalysis, Timer, CircleCheck, User,
  DataLine, PieChart, Monitor, Refresh, Warning
} from '@element-plus/icons-vue'
import { statisticsApi, type SystemOverviewResponse, type ScoreDistributionResponse, type ApiStatResponse } from '@/api/statistics'
import { studentApi, type StudentRankResponse } from '@/api/student'

// Register ECharts components
use([
  CanvasRenderer,
  BarChart,
  EChartsP,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
])

// Reactive data
const loading = ref(false)
const scoreStatsLoading = ref(false)
const apiStatsLoading = ref(false)
const dashboardLoading = ref(false)
const rankingLoading = ref(false)
const lastUpdateTime = ref(new Date())

// Data
const systemOverview = ref<SystemOverviewResponse | null>(null)
const studentRanking = ref<StudentRankResponse[]>([])
const scoreDistribution = ref<ScoreDistributionResponse[]>([])
const topApiStats = ref<ApiStatResponse[]>([])

// Chart options

const scoreDistributionOption = computed(() => {
  // Define colors for each score range
  const colorMap: { [key: string]: string } = {
    '优秀 (90-100)': '#10b981',
    '良好 (80-89)': '#3b82f6',
    '中等 (70-79)': '#f59e0b',
    '及格 (60-69)': '#ef4444',
    '不及格 (<60)': '#6b7280'
  }
  
  const distributionData = (scoreDistribution.value || []).map(item => ({
    name: item.name,
    value: item.count,
    itemStyle: { color: colorMap[item.name] || '#6b7280' }
  }))

  return {
    title: {
      text: '成绩分布',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: {
        fontSize: 12
      }
    },
    series: [{
      name: '成绩分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '18',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: distributionData
    }]
  }
})

const studentRankingOption = computed(() => ({
  title: {
    text: '学生排行榜 (Top 10)',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'bold'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: (params: any) => {
      const data = params[0]
      return `${data.name}<br/>成绩: ${data.value}`
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'value',
    axisLabel: {
      fontSize: 10
    }
  },
  yAxis: {
    type: 'category',
    data: (studentRanking.value || []).map(student => student.name),
    axisLabel: {
      fontSize: 10
    }
  },
  series: [{
    name: '成绩',
    type: 'bar',
    data: (studentRanking.value || []).map(student => student.score),
    itemStyle: {
      color: {
        type: 'linear',
        x: 1,
        y: 0,
        x2: 0,
        y2: 0,
        colorStops: [
          { offset: 0, color: '#43e97b' },
          { offset: 1, color: '#38f9d7' }
        ]
      }
    }
  }]
}))

const apiStatsOption = computed(() => ({
  title: {
    text: 'API调用统计 (Top 10)',
    left: 'center',
    textStyle: {
      fontSize: 16,
      fontWeight: 'bold'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: (params: any) => {
      const data = params[0]
      return `${data.name}<br/>调用次数: ${data.value}`
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'value',
    axisLabel: {
      fontSize: 10
    }
  },
  yAxis: {
    type: 'category',
    data: (topApiStats.value || []).map(api => {
      // 简化API端点显示
      const endpoint = api.endpoint
      if (endpoint.length > 25) {
        return endpoint.substring(0, 25) + '...'
      }
      return endpoint
    }),
    axisLabel: {
      fontSize: 10
    }
  },
  series: [{
    name: '调用次数',
    type: 'bar',
    data: (topApiStats.value || []).map(api => api.callCount),
    itemStyle: {
      color: {
        type: 'linear',
        x: 1,
        y: 0,
        x2: 0,
        y2: 0,
        colorStops: [
          { offset: 0, color: '#5b9bd5' },
          { offset: 1, color: '#4a84b8' }
        ]
      }
    }
  }]
}))
const systemActivityOption = computed(() => ({
  title: {
    text: '教学数据概览',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'bold'
    }
  },
  tooltip: {
    trigger: 'item'
  },
  series: [{
    name: '教学数据',
    type: 'pie',
    radius: '60%',
    data: [
      { value: systemOverview.value?.totalStudents || 0, name: '学生', itemStyle: { color: '#5b9bd5' } },
      { value: systemOverview.value?.totalClasses || 0, name: '班级', itemStyle: { color: '#6ba3d6' } },
      { value: systemOverview.value?.totalUsers || 0, name: '用户', itemStyle: { color: '#43e97b' } }
    ],
    emphasis: {
      itemStyle: {
        shadowBlur: 10,
        shadowOffsetX: 0,
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      }
    }
  }]
}))

const loadApiStats = async () => {
  try {
    apiStatsLoading.value = true
    const result = await statisticsApi.getTopApiStatistics(10)
    topApiStats.value = Array.isArray(result) ? result : []
  } catch (error) {
    console.error('Failed to load API stats:', error)
    topApiStats.value = []
    ElMessage.error('加载API统计失败')
  } finally {
    apiStatsLoading.value = false
  }
}

const refreshApiStats = async () => {
  await loadApiStats()
  ElMessage.success('API统计数据已刷新')
}



// Methods
const formatNumber = (num: number): string => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

const formatTime = (date: Date): string => {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const loadSystemOverview = async () => {
  try {
    loading.value = true
    systemOverview.value = await statisticsApi.getSystemOverview()
  } catch (error) {
    console.error('Failed to load system overview:', error)
    ElMessage.error('加载系统概览失败')
  } finally {
    loading.value = false
  }
}



const loadStudentRanking = async () => {
  try {
    rankingLoading.value = true
    const result = await studentApi.getScoreRanking(10)
    studentRanking.value = Array.isArray(result) ? result : []
  } catch (error) {
    console.error('Failed to load student ranking:', error)
    studentRanking.value = [] // 确保在错误情况下仍然是数组
    ElMessage.error('加载学生排行榜失败')
  } finally {
    rankingLoading.value = false
  }
}

const loadScoreDistribution = async () => {
  try {
    scoreStatsLoading.value = true
    const result = await statisticsApi.getScoreDistribution()
    scoreDistribution.value = Array.isArray(result) ? result : []
  } catch (error) {
    console.error('Failed to load score distribution:', error)
    scoreDistribution.value = [] // 确保在错误情况下仍然是数组
    ElMessage.error('加载成绩分布失败')
  } finally {
    scoreStatsLoading.value = false
  }
}

const loadData = async () => {
  await Promise.all([
    loadSystemOverview(),
    loadStudentRanking(),
    loadScoreDistribution(),
    loadApiStats()
  ])
  lastUpdateTime.value = new Date()
}



const refreshScoreStats = async () => {
  await loadScoreDistribution()
  ElMessage.success('成绩统计数据已刷新')
}

const refreshDashboard = async () => {
  dashboardLoading.value = true
  await loadData()
  dashboardLoading.value = false
  ElMessage.success('仪表板数据已刷新')
}

// Lifecycle
onMounted(() => {
  loadData()
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}
.statistics-container {
  padding: 0;
  background: #f0f5fa;
  min-height: 100%;
  height: 100%;
  width: 100%;
  margin: 0;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* Page Header */
.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: white;
  padding: 24px 32px;
  margin: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.title-section {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
}

.title-icon {
  font-size: 36px;
  color: #5b9bd5;
}

.page-subtitle {
  margin: 0;
  color: #718096;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

/* Metrics Section */
.metrics-section {
  margin: 0 20px 20px 20px;
}

.metric-card {
  background: white;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  margin-bottom: 16px;
  position: relative;
  overflow: hidden;
}

.metric-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.api-calls {
  background: #5b9bd5;
}

.response-time {
  background: #6ba3d6;
}

.success-rate {
  background: #5b9bd5;
}

.active-users {
  background: #5b9bd5;
}

.metric-card:nth-child(1)::before {
  background: #5b9bd5;
}

.metric-card:nth-child(2)::before {
  background: #6ba3d6;
}

.metric-card:nth-child(3)::before {
  background: #5b9bd5;
}

.metric-card:nth-child(4)::before {
  background: #5b9bd5;
}

.metric-content {
  flex: 1;
}

.metric-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 8px;
}

.metric-change {
  font-size: 12px;
  font-weight: 600;
}

.metric-change.positive {
  color: #10b981;
}

.metric-change.negative {
  color: #ef4444;
}

/* Charts Section */
.charts-section {
  margin: 0 20px;
  box-sizing: border-box;
}

.chart-card {
  border: none;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.header-icon {
  font-size: 20px;
  color: #5b9bd5;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.chart-container {
  min-height: 300px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #f7fafc;
  border-radius: 4px;
  text-align: center;
  padding: 40px;
}

.placeholder-icon {
  font-size: 48px;
  color: #cbd5e0;
  margin-bottom: 16px;
}

.placeholder-icon.large {
  font-size: 64px;
}

.chart-placeholder h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
}

.chart-placeholder p {
  margin: 0 0 20px 0;
  color: #718096;
  font-size: 14px;
}

.dashboard-container-inner {
  min-height: 400px;
}

.dashboard-placeholder {
  height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #f7fafc;
  border-radius: 4px;
  text-align: center;
  padding: 60px 40px;
}

.dashboard-placeholder h2 {
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
}

.dashboard-placeholder p {
  margin: 0 0 32px 0;
  color: #718096;
  font-size: 16px;
  max-width: 400px;
}

.placeholder-features {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
  flex-wrap: wrap;
  justify-content: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border-radius: 20px;
  font-size: 14px;
  color: #4a5568;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.feature-item .el-icon {
  color: #5b9bd5;
}

/* Chart Styles */
.chart {
  height: 300px;
  width: 100%;
}

/* Dashboard Styles */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
  padding: 20px;
}

.dashboard-item {
  background: #f8fafc;
  border-radius: 4px;
  padding: 16px;
  min-height: 200px;
}

.dashboard-chart-container h4,
.dashboard-info h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  text-align: center;
}

.dashboard-chart {
  height: 180px;
  width: 100%;
}

.dashboard-info {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.info-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.info-label {
  font-size: 14px;
  color: #718096;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #2d3748;
  font-weight: 600;
}

.status-healthy {
  color: #10b981 !important;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .statistics-container {
    padding: 0;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    padding: 16px;
    margin: 0 4px;
  }
  
  .metrics-section {
    margin: 0 4px 12px 4px;
  }
  
  .charts-section {
    margin: 0 4px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .placeholder-features {
    flex-direction: column;
    gap: 12px;
  }
  
  .dashboard-placeholder {
    padding: 40px 20px;
  }
  
  .dashboard-grid {
    padding: 12px;
    gap: 12px;
  }
  
  .dashboard-item {
    padding: 12px;
    min-height: 160px;
  }
  
  .dashboard-chart {
    height: 140px;
  }
  
  .chart {
    height: 250px;
  }
}
</style>