<template>
  <div class="teacher-dashboard">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><Odometer /></el-icon>
            欢迎回来，{{ user?.username }} 老师
          </h1>
          <p class="page-subtitle">管理您的班级和学生信息</p>
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
      <!-- Error State -->
      <div v-if="error && !loading" class="error-state">
        <el-alert
          :title="error"
          type="error"
          show-icon
          :closable="false"
        >
          <template #default>
            <p>无法加载教师数据，请检查网络连接后重试。</p>
            <el-button type="primary" size="small" @click="retryLoad">
              <el-icon><Refresh /></el-icon>
              重新加载
            </el-button>
          </template>
        </el-alert>
      </div>

      <!-- Statistics Cards -->
      <el-row v-else :gutter="24">
        <el-col :xs="24" :sm="12" :md="8">
          <div class="stat-card my-classes" @click="navigateTo('/teacher/classes')">
            <div class="stat-icon">
              <el-icon><School /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ teacherStats.totalClasses }}</div>
              <div class="stat-label">我的班级</div>
              <div class="stat-trend">
                <el-icon><CaretTop /></el-icon>
                <span>负责管理</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <div class="stat-card my-students" @click="navigateTo('/students')">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ teacherStats.totalStudents }}</div>
              <div class="stat-label">我的学生</div>
              <div class="stat-trend">
                <el-icon><CaretTop /></el-icon>
                <span>教学对象</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <el-row :gutter="24">
        <!-- Left Column - Classes and Quick Actions -->
        <el-col :xs="24" :lg="16">
          <!-- My Classes -->
          <el-card class="classes-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><School /></el-icon>
                  <span>我的班级</span>
                </div>
                <el-button type="primary" size="small" @click="navigateTo('/teacher/classes')">
                  查看全部
                </el-button>
              </div>
            </template>
            
            <div class="classes-grid">
              <ClassCard
                v-for="classInfo in myClasses" 
                :key="classInfo.id" 
                :class-info="classInfo"
                :show-actions="true"
                @click="handleClassClick"
                @manage="handleClassManage"
                @view-students="handleViewStudents"
              />
              
              <!-- Empty state -->
              <div v-if="myClasses.length === 0 && !loading" class="empty-state">
                <el-icon class="empty-icon"><School /></el-icon>
                <p>暂无班级数据</p>
              </div>
            </div>
          </el-card>

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
              <div class="action-item" @click="navigateTo('/teacher/classes')">
                <div class="action-icon manage-classes">
                  <el-icon><School /></el-icon>
                </div>
                <div class="action-content">
                  <div class="action-title">班级管理</div>
                  <div class="action-desc">管理班级和学生信息</div>
                </div>
              </div>
              <div class="action-item" @click="navigateTo('/students')">
                <div class="action-icon manage-students">
                  <el-icon><User /></el-icon>
                </div>
                <div class="action-content">
                  <div class="action-title">学生管理</div>
                  <div class="action-desc">查看学生档案与科目成绩</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="8">
          <el-card class="top-students-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><Trophy /></el-icon>
                  <span>科目成绩排行榜</span>
                </div>
              </div>
            </template>

            <el-tabs v-model="rankingSubject" @tab-change="loadRankings">
              <el-tab-pane label="C语言" name="C语言" />
              <el-tab-pane label="英语四级" name="英语四级" />
            </el-tabs>

            <div v-loading="rankingLoading" class="ranking-list">
              <div
                v-for="item in rankings"
                :key="item.studentId"
                class="ranking-item"
                @click="navigateTo(`/students/${item.studentId}`)"
              >
                <span class="rank" :class="'rank-' + item.rank">{{ item.rank }}</span>
                <div class="ranking-info">
                  <div class="name">{{ item.studentName }}</div>
                  <div class="meta">{{ item.className || '未分班' }}</div>
                </div>
                <div class="score">{{ Number(item.score).toFixed(1) }}</div>
              </div>
              <div v-if="!rankingLoading && rankings.length === 0" class="empty-state">
                <el-icon class="empty-icon"><Trophy /></el-icon>
                <p>暂无 {{ rankingSubject }} 成绩</p>
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Odometer, Refresh, School, User, CaretTop,
  Operation, Trophy
} from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import { classApi, type TeacherDashboardData, type ClassInfo, type StudentInfo } from '@/api/class'
import { gradeApi, type RankingEntry } from '@/api/grade'
import ClassCard from '@/components/ClassCard.vue'

const router = useRouter()
const { user } = useAuth()

// Reactive data
const loading = ref(false)
const dashboardData = ref<TeacherDashboardData | null>(null)
const error = ref<string | null>(null)
const rankingSubject = ref('C语言')
const rankingLoading = ref(false)
const rankings = ref<RankingEntry[]>([])

// Computed properties for easier access
const teacherStats = computed(() => dashboardData.value?.teacherStats || {
  totalClasses: 0,
  totalStudents: 0
})

const myClasses = computed(() => dashboardData.value?.myClasses || [])

const loadRankings = async () => {
  try {
    rankingLoading.value = true
    const res = await gradeApi.getGradeRanking({
      subject: rankingSubject.value,
      scope: 'SCHOOL',
      limit: 10
    })
    rankings.value = res.rankings || []
  } catch (err) {
    console.error('Failed to load rankings:', err)
    rankings.value = []
  } finally {
    rankingLoading.value = false
  }
}

// Load teacher dashboard data
const loadTeacherData = async () => {
  try {
    loading.value = true
    error.value = null
    
    const data = await classApi.getTeacherDashboard()
    dashboardData.value = data
    await loadRankings()
    
  } catch (err) {
    console.error('Failed to load teacher data:', err)
    error.value = '加载教师数据失败'
    ElMessage.error('加载教师数据失败，请稍后重试')
    
    dashboardData.value = {
      teacherStats: {
        totalClasses: 0,
        totalStudents: 0
      },
      myClasses: [],
      topStudents: []
    }
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  await loadTeacherData()
  if (!error.value) {
    ElMessage.success('数据已刷新')
  }
}

const navigateTo = (path: string) => {
  router.push(path)
}

const handleClassClick = (classInfo: ClassInfo) => {
  navigateTo(`/teacher/classes/${classInfo.id}`)
}

const handleClassManage = (classInfo: ClassInfo) => {
  navigateTo(`/teacher/classes/${classInfo.id}`)
}

const handleViewStudents = (classInfo: ClassInfo) => {
  navigateTo(`/teacher/classes/${classInfo.id}`)
}

const handleStudentClick = (student: StudentInfo) => {
  navigateTo(`/students/${student.id}`)
}

// Retry function for error state
const retryLoad = () => {
  loadTeacherData()
}

// Lifecycle
onMounted(() => {
  loadTeacherData()
})
</script>

<style scoped>
/* 复用Dashboard的样式，并添加教师特定的样式 */
.teacher-dashboard {
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
}

/* Statistics Section */
.stats-section {
  margin: 0 20px 20px 20px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
  margin-bottom: 16px;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #5b9bd5;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.my-classes .stat-icon {
  background: #5b9bd5;
}

.my-students .stat-icon {
  background: #6ba3d6;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #10b981;
}

/* Main Content */
.main-content {
  margin: 0 20px;
  box-sizing: border-box;
}

/* Cards */
.classes-card,
.actions-card,
.top-students-card {
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

/* Classes Grid */
.classes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

/* Quick Actions */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.add-student {
  background: #5b9bd5;
}

.manage-classes {
  background: #6ba3d6;
}

.manage-students {
  background: #5b9bd5;
}

.export-data {
  background: #5b9bd5;
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 14px;
  color: #718096;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 200px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: #f7fafc;
  border-radius: 4px;
  cursor: pointer;
}

.ranking-item:hover {
  background: #e8f1f8;
}

.rank {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  background: #e2e8f0;
  color: #4a5568;
}

.rank-1 { background: #f6ad55; color: #fff; }
.rank-2 { background: #a0aec0; color: #fff; }
.rank-3 { background: #d69e2e; color: #fff; }

.ranking-info {
  flex: 1;
  min-width: 0;
}

.ranking-info .name {
  font-weight: 600;
  color: #2d3748;
}

.ranking-info .meta {
  font-size: 12px;
  color: #718096;
}

.ranking-item .score {
  font-weight: 700;
  color: #3a6d99;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #718096;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* Error State */
.error-state {
  margin-bottom: 20px;
}

.error-state .el-alert {
  border-radius: 4px;
}

.error-state .el-button {
  margin-top: 8px;
}

/* Responsive Design */
@media (max-width: 768px) {
  .teacher-dashboard {
    padding: 0;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    padding: 16px;
    margin: 0 4px;
  }
  
  .stats-section {
    margin: 0 4px 12px 4px;
  }
  
  .main-content {
    margin: 0 4px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .classes-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>