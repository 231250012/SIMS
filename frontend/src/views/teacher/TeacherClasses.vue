<template>
  <div class="teacher-classes">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><School /></el-icon>
            班级管理
          </h1>
          <p class="page-subtitle">管理您负责的班级和学生信息</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- Filter and Search Section -->
    <div class="filter-section">
      <el-card shadow="never">
        <div class="filter-content">
          <div class="search-box">
            <el-input
              v-model="searchQuery"
              placeholder="搜索班级名称..."
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="filter-controls">
            <el-select
              v-model="selectedGrade"
              placeholder="选择年级"
              clearable
              @change="handleGradeFilter"
            >
              <el-option label="全部年级" value="" />
              <el-option label="高一" value="高一" />
              <el-option label="高二" value="高二" />
              <el-option label="高三" value="高三" />
            </el-select>
            <el-select
              v-model="sortBy"
              placeholder="排序方式"
              @change="handleSort"
            >
              <el-option label="按班级名称" value="name" />
              <el-option label="按学生数量" value="studentCount" />
            </el-select>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Statistics Overview -->
    <div class="stats-overview" v-loading="loading">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <div class="stat-card total-classes">
            <div class="stat-icon">
              <el-icon><School /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ classStatistics.totalClasses }}</div>
              <div class="stat-label">管理班级</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card total-students">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ classStatistics.totalStudents }}</div>
              <div class="stat-label">总学生数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Classes Grid -->
    <div class="classes-section">
      <el-card shadow="never">
        <template #header>
          <div class="section-header">
            <div class="header-title">
              <el-icon><School /></el-icon>
              <span>班级列表 ({{ filteredClasses.length }})</span>
            </div>
            <div class="view-toggle">
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button label="grid">
                  <el-icon><Grid /></el-icon>
                </el-radio-button>
                <el-radio-button label="list">
                  <el-icon><List /></el-icon>
                </el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>

        <!-- Loading State -->
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="error-state">
          <el-alert
            :title="error"
            type="error"
            show-icon
            :closable="false"
          >
            <template #default>
              <p>无法加载班级数据，请检查网络连接后重试。</p>
              <el-button type="primary" size="small" @click="retryLoad">
                <el-icon><Refresh /></el-icon>
                重新加载
              </el-button>
            </template>
          </el-alert>
        </div>

        <!-- Empty State -->
        <div v-else-if="filteredClasses.length === 0" class="empty-state">
          <el-icon class="empty-icon"><School /></el-icon>
          <p v-if="searchQuery || selectedGrade">没有找到符合条件的班级</p>
          <p v-else>暂无班级数据</p>
        </div>

        <!-- Grid View -->
        <div v-else-if="viewMode === 'grid'" class="classes-grid">
          <div
            v-for="classInfo in filteredClasses"
            :key="classInfo.id"
            class="class-card-wrapper"
          >
            <ClassCard
              :class-info="classInfo"
              :show-actions="true"
              @click="viewClassDetail"
              @manage="manageClass"
              @view-students="viewClassStudents"
            />
          </div>
        </div>

        <!-- List View -->
        <div v-else class="classes-list">
          <el-table
            :data="filteredClasses"
            style="width: 100%"
            @row-click="viewClassDetail"
          >
            <el-table-column prop="name" label="班级名称" min-width="120">
              <template #default="{ row }">
                <div class="class-name-cell">
                  <span class="class-name">{{ row.name }}</span>
                  <el-tag size="small" :type="getGradeTagType(row.grade)">
                    {{ row.grade || '其他' }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="studentCount" label="学生数量" width="100" align="center">
              <template #default="{ row }">
                <span class="student-count">{{ row.studentCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="teacherName" label="任课教师" width="120" />
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <el-button type="text" size="small" @click.stop="viewClassDetail(row)">
                  查看详情
                </el-button>
                <el-button type="text" size="small" @click.stop="manageClass(row)">
                  管理班级
                </el-button>
                <el-button type="text" size="small" @click.stop="viewClassStudents(row)">
                  学生列表
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  School, Refresh, Plus, Search, User, TrendCharts,
  Grid, List
} from '@element-plus/icons-vue'
import { classApi, type ClassInfo } from '@/api/class'
import ClassCard from '@/components/ClassCard.vue'

const router = useRouter()

// Reactive data
const loading = ref(false)
const error = ref<string | null>(null)
const classes = ref<ClassInfo[]>([])
const searchQuery = ref('')
const selectedGrade = ref('')
const sortBy = ref('name')
const viewMode = ref<'grid' | 'list'>('grid')

// Computed properties
const filteredClasses = computed(() => {
  let result = [...classes.value]

  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(cls => 
      cls.name.toLowerCase().includes(query)
    )
  }

  // Apply grade filter
  if (selectedGrade.value) {
    result = result.filter(cls => cls.grade === selectedGrade.value)
  }

  // Apply sorting
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'studentCount':
        return (b.studentCount || 0) - (a.studentCount || 0)
      default:
        return 0
    }
  })

  return result
})

const classStatistics = computed(() => {
  const totalClasses = classes.value.length
  const totalStudents = classes.value.reduce((sum, cls) => sum + (cls.studentCount || 0), 0)

  return {
    totalClasses,
    totalStudents
  }
})

// Methods
const loadClasses = async () => {
  try {
    loading.value = true
    error.value = null
    
    const data = await classApi.getAllClasses()
    classes.value = data.map(cls => ({
      ...cls,
      grade: cls.name.includes('高三') ? '高三' : 
             cls.name.includes('高二') ? '高二' : 
             cls.name.includes('高一') ? '高一' : '其他'
    }))
    
  } catch (err) {
    console.error('Failed to load classes:', err)
    error.value = '加载班级数据失败'
    ElMessage.error('加载班级数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  await loadClasses()
  if (!error.value) {
    ElMessage.success('数据已刷新')
  }
}

const retryLoad = () => {
  loadClasses()
}

const handleSearch = () => {
  // Search is handled by computed property
}

const handleGradeFilter = () => {
  // Filter is handled by computed property
}

const handleSort = () => {
  // Sort is handled by computed property
}

const formatScore = (score?: number) => {
  return score ? score.toFixed(1) : '0.0'
}

const getGradeTagType = (grade?: string) => {
  switch (grade) {
    case '高三': return 'danger'
    case '高二': return 'warning'
    case '高一': return 'success'
    default: return 'info'
  }
}

const viewClassDetail = (classInfo: ClassInfo) => {
  router.push(`/teacher/classes/${classInfo.id}`)
}

const manageClass = (classInfo: ClassInfo) => {
  router.push(`/teacher/classes/${classInfo.id}`)
}

const viewClassStudents = (classInfo: ClassInfo) => {
  router.push(`/teacher/classes/${classInfo.id}`)
}


// Lifecycle
onMounted(() => {
  loadClasses()
})
</script>

<style scoped>
.teacher-classes {
  padding: 0;
  background: #f5f7fa;
  min-height: 100vh;
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
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
}

.title-icon {
  font-size: 32px;
  color: #5b9bd5;
}

.page-subtitle {
  margin: 0;
  color: #718096;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* Filter Section */
.filter-section {
  margin: 0 20px 20px 20px;
}

.filter-content {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 200px;
}

.filter-controls {
  display: flex;
  gap: 12px;
}

.filter-controls .el-select {
  width: 120px;
}

/* Statistics Overview */
.stats-overview {
  margin: 0 20px 20px 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.total-classes .stat-icon {
  background: #5b9bd5;
}

.total-students .stat-icon {
  background: #6ba3d6;
}

.average-score .stat-icon {
  background: #5b9bd5;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #718096;
}

/* Classes Section */
.classes-section {
  margin: 0 20px 20px 20px;
}

.section-header {
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

.view-toggle {
  display: flex;
  align-items: center;
}

/* Grid View */
.classes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.class-card-wrapper {
  transition: all 0.3s ease;
}

/* List View */
.classes-list {
  margin-top: 16px;
}

.class-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.class-name {
  font-weight: 600;
  color: #2d3748;
}

.student-count,
.average-score {
  font-weight: 600;
  color: #2d3748;
}

/* States */
.loading-state {
  padding: 40px;
}

.error-state {
  padding: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #718096;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

/* Responsive Design */
@media (max-width: 768px) {
  .teacher-classes {
    padding: 0;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    padding: 16px;
  }
  
  .filter-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-controls {
    justify-content: space-between;
  }
  
  .filter-controls .el-select {
    flex: 1;
  }
  
  .classes-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .stats-overview {
    margin: 0 4px 12px 4px;
  }
  
  .filter-section,
  .classes-section {
    margin: 0 4px 12px 4px;
  }
}
</style>