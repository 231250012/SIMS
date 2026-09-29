<template>
  <div class="dashboard-container">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><Odometer /></el-icon>
            {{ getDashboardTitle() }}
          </h1>
          <p class="page-subtitle">{{ getDashboardSubtitle() }}</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-section" v-loading="loading">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="stat-card total-students">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ systemOverview?.totalStudents || 0 }}</div>
              <div class="stat-label">总学生数</div>
              <div class="stat-trend positive">
                <el-icon><CaretTop /></el-icon>
                <span>实时数据</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="stat-card total-classes">
            <div class="stat-icon">
              <el-icon><School /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ systemOverview?.totalClasses || 0 }}</div>
              <div class="stat-label">总班级数</div>
              <div class="stat-trend positive">
                <el-icon><CaretTop /></el-icon>
                <span>教学单位</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card total-api-calls">
            <div class="stat-icon">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ formatNumber(systemOverview?.totalApiCalls || 0) }}</div>
              <div class="stat-label">API调用总数</div>
              <div class="stat-trend positive">
                <el-icon><CaretTop /></el-icon>
                <span>系统活跃度</span>
              </div>
            </div>
          </div>
        </el-col>

      </el-row>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <el-row :gutter="24">
        <!-- Main Column -->
        <el-col :xs="24">
          <!-- Quick Actions -->
          <el-card class="actions-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><Operation /></el-icon>
                  <span>快速操作</span>
                </div>
              </div>
            </template>
            
            <div class="quick-actions">
              <template v-if="isAdmin">
                <div class="action-item" @click="navigateTo('/students')">
                  <div class="action-icon manage-students">
                    <el-icon><User /></el-icon>
                  </div>
                  <div class="action-content">
                    <div class="action-title">学生管理</div>
                    <div class="action-desc">查看和管理学生信息</div>
                  </div>
                </div>
                <div class="action-item" @click="navigateTo('/admin/teachers')">
                  <div class="action-icon manage-students">
                    <el-icon><UserFilled /></el-icon>
                  </div>
                  <div class="action-content">
                    <div class="action-title">教师管理</div>
                    <div class="action-desc">调整班级与删除教师</div>
                  </div>
                </div>
                <div class="action-item" @click="navigateTo('/admin/classes')">
                  <div class="action-icon view-stats">
                    <el-icon><School /></el-icon>
                  </div>
                  <div class="action-content">
                    <div class="action-title">班级管理</div>
                    <div class="action-desc">新建班级与班级概况</div>
                  </div>
                </div>
                <div class="action-item" @click="navigateTo('/statistics')">
                  <div class="action-icon view-stats">
                    <el-icon><TrendCharts /></el-icon>
                  </div>
                  <div class="action-content">
                    <div class="action-title">统计分析</div>
                    <div class="action-desc">查看详细统计报告</div>
                  </div>
                </div>
              </template>

              <template v-else-if="isTeacher">
                <div class="action-item" @click="navigateTo('/students')">
                  <div class="action-icon manage-students">
                    <el-icon><User /></el-icon>
                  </div>
                  <div class="action-content">
                    <div class="action-title">学生管理</div>
                    <div class="action-desc">查看和管理学生信息</div>
                  </div>
                </div>
              </template>
              
              <!-- 学生可以看到的操作 -->
              <template v-if="isStudent">
                <div class="action-item" @click="navigateTo('/profile')">
                  <div class="action-icon manage-students">
                    <el-icon><User /></el-icon>
                  </div>
                  <div class="action-content">
                    <div class="action-title">个人信息</div>
                    <div class="action-desc">查看和编辑个人资料</div>
                  </div>
                </div>
                <div class="action-item" @click="navigateTo('/student/grades')">
                  <div class="action-icon view-stats">
                    <el-icon><TrendCharts /></el-icon>
                  </div>
                  <div class="action-content">
                    <div class="action-title">我的成绩</div>
                    <div class="action-desc">查看个人学习成绩</div>
                  </div>
                </div>
              </template>
            </div>
          </el-card>

          <!-- Top Students -->
          <el-card class="top-students-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><Trophy /></el-icon>
                  <span>优秀学生</span>
                </div>
              </div>
            </template>
            
            <div class="student-list">
              <div v-for="student in topStudents" :key="student.id" class="student-item">
                <div class="student-rank">{{ student.rank }}</div>
                <el-avatar :size="40" class="student-avatar">
                  {{ student.name.charAt(0) }}
                </el-avatar>
                <div class="student-info">
                  <div class="student-name">{{ student.name }}</div>
                  <div class="student-score">{{ student.score }}分</div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- API Statistics (Admin Only) -->
          <!-- API统计功能已移除 -->
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Odometer, Refresh, User, UserFilled, School, TrendCharts, CaretTop,
  Operation, Plus, Trophy, DataAnalysis
} from '@element-plus/icons-vue'
import { statisticsApi, type SystemOverviewResponse } from '@/api/statistics'
import { studentApi, type StudentRankResponse } from '@/api/student'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { user, isAdmin, isTeacher, isStudent } = useAuth()

// Reactive data
const systemOverview = ref<SystemOverviewResponse | null>(null)
const loading = ref(false)
const topStudents = ref<StudentRankResponse[]>([])
// Methods
const formatNumber = (num: number): string => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
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

const loadTopStudents = async () => {
  try {
    const result = await studentApi.getScoreRanking(5)
    topStudents.value = Array.isArray(result) ? result : []
  } catch (error) {
    console.error('Failed to load top students:', error)
    ElMessage.error('加载优秀学生失败')
  }
}

const refreshData = async () => {
  await Promise.all([
    loadSystemOverview(),
    loadTopStudents()
  ])
  ElMessage.success('数据已刷新')
}

const navigateTo = (path: string) => {
  router.push(path)
}


const getDashboardTitle = () => {
  if (isAdmin.value) {
    return `欢迎回来，${user.value?.username} 管理员`
  } else if (isTeacher.value) {
    return `欢迎回来，${user.value?.username} 老师`
  } else if (isStudent.value) {
    return `欢迎回来，${user.value?.username} 同学`
  }
  return '系统仪表板'
}

const getDashboardSubtitle = () => {
  if (isAdmin.value) {
    return '管理系统运行状态和关键指标'
  } else if (isTeacher.value) {
    return '管理您的班级和学生信息'
  } else if (isStudent.value) {
    return '查看您的学习进度和成绩信息'
  }
  return '实时监控系统运行状态和关键指标'
}

// Lifecycle
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}
.dashboard-container {
  padding: 4px 0 24px;
  background: transparent;
  min-height: 100%;
  width: 100%;
  margin: 0;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* Page Header */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
  padding: 22px 24px;
  margin: 0;
  border: 1px solid rgba(229, 231, 235, 0.85);
  border-radius: 24px;
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.08);
}

.title-section {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 6px 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #0a1128;
}

.title-icon {
  font-size: 26px;
  color: #0066ff;
}

.page-subtitle {
  margin: 0;
  color: #4a5568;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 16px;
}

/* Statistics Section */
.stats-section {
  margin: 0 0 24px 0;
}

.stat-card {
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  padding: 22px;
  border-radius: 24px;
  border: 1px solid rgba(229, 231, 235, 0.85);
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  margin-bottom: 16px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: #bfdbfe;
  background: #fff;
  box-shadow: 0 12px 32px rgba(0, 102, 255, 0.12);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  background: linear-gradient(180deg, #3b82f6, #0066ff);
  box-shadow: 0 8px 16px rgba(0, 102, 255, 0.25);
}

.total-students .stat-icon,
.total-classes .stat-icon,
.total-api-calls .stat-icon {
  background: linear-gradient(180deg, #3b82f6, #0066ff);
}



.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 26px;
  font-weight: 700;
  color: #0a1128;
  line-height: 1;
  margin-bottom: 6px;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 14px;
  color: #4a5568;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
}

.stat-trend.positive {
  color: #0ea5e9;
}

/* Main Content */
.main-content {
  margin: 0;
  box-sizing: border-box;
}

/* Cards */
.actions-card,
.top-students-card {
  border: none !important;
  margin-bottom: 20px;
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
  font-size: 17px;
  font-weight: 700;
  color: #0a1128;
}

.header-icon {
  font-size: 18px;
  color: #0066ff;
}

/* Quick Actions */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: #f8fbff;
  border: 1px solid rgba(229, 231, 235, 0.9);
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-item:hover {
  background: #eff6ff;
  border-color: #93c5fd;
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
  background: linear-gradient(180deg, #3b82f6, #0066ff);
}

.add-student,
.manage-students,
.view-stats,
.export-data {
  background: linear-gradient(180deg, #3b82f6, #0066ff);
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 15px;
  font-weight: 700;
  color: #0a1128;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #6b7280;
}





/* Top Students */
.student-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.student-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #f8fbff;
  border-radius: 16px;
  border: 1px solid rgba(229, 231, 235, 0.8);
}

.student-rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #0066ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.student-avatar {
  border: 2px solid #dbeafe;
}

.student-info {
  flex: 1;
}

.student-name {
  font-weight: 600;
  color: #0a1128;
  margin-bottom: 2px;
}

.student-score {
  font-size: 12px;
  color: #6b7280;
}

/* Responsive Design */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 0;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }
  
  .stats-section {
    margin: 0 0 12px 0;
  }
  
  .main-content {
    margin: 0;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>