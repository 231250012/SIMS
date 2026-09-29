<template>
  <div class="student-list-container">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><User /></el-icon>
            学生管理
          </h1>
          <p class="page-subtitle">管理学生信息，查看学业表现</p>
        </div>
        <div class="header-actions"></div>
      </div>
    </div>

    <!-- Search and Filter Section -->
    <div class="search-section">
      <el-card class="search-card" shadow="never">
        <div class="search-header">
          <el-icon class="search-icon"><Search /></el-icon>
          <span class="search-title">筛选条件</span>
        </div>
        <el-form :model="searchForm" class="search-form">
          <el-row :gutter="24">
            <el-col :xs="24" :sm="12" :md="8" :lg="6">
              <el-form-item label="学生姓名">
                <el-input 
                  v-model="searchForm.name" 
                  placeholder="请输入学生姓名"
                  clearable
                  prefix-icon="User"
                  @clear="handleSearch"
                  @keyup.enter="handleSearch"
                  class="search-input"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="8" :lg="6">
              <el-form-item label="班级">
                <el-select 
                  v-model="searchForm.classId" 
                  placeholder="请选择班级"
                  clearable
                  @change="handleSearch"
                  class="search-select"
                >
                  <el-option label="全部班级" value="" />
                  <el-option 
                    v-for="cls in classes" 
                    :key="cls.id" 
                    :label="cls.name" 
                    :value="cls.id" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :md="8" :lg="12">
              <el-form-item class="search-actions">
                <el-button type="primary" @click="handleSearch" class="search-btn">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="handleReset" class="reset-btn">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </div>
    
    <!-- Statistics Cards -->
    <div class="stats-section">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card total-students">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ pagination.totalElements }}</div>
              <div class="stat-label">总学生数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card active-classes">
            <div class="stat-icon">
              <el-icon><School /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ classes.length }}</div>
              <div class="stat-label">活跃班级</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Student List Section -->
    <div class="list-section">
      <el-card class="list-card" shadow="never">
        <template #header>
          <div class="list-header">
            <div class="list-title">
              <el-icon><List /></el-icon>
              <span>学生列表</span>
              <el-tag type="info" class="count-tag">{{ pagination.totalElements }} 人</el-tag>
            </div>
          </div>
        </template>
        
        <!-- Card View -->
        <div class="card-view" v-loading="loading">
          <div v-if="students.length === 0" class="empty-state">
            <el-empty description="暂无学生数据" />
          </div>
          <el-row v-else :gutter="24">
            <el-col 
              v-for="student in students" 
              :key="student.id"
              :xs="24" :sm="12" :md="8" :lg="6"
              class="student-card-col"
            >
              <div class="student-card" @click="handleView(student.id)">
                <div class="card-header">
                  <el-avatar 
                    :size="60" 
                    :src="student.avatar" 
                    :alt="student.name"
                    class="card-avatar"
                  >
                    {{ student.name.charAt(0) }}
                  </el-avatar>
                  <div class="card-actions">
                    <el-dropdown trigger="click">
                      <el-button size="small" circle>
                        <el-icon><MoreFilled /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item @click.stop="handleView(student.id)">
                            <el-icon><View /></el-icon>查看详情
                          </el-dropdown-item>
                          <el-dropdown-item @click.stop="handleEdit(student.id)">
                            <el-icon><Edit /></el-icon>
                            {{ canManageStudentProfile ? '编辑信息' : '修改成绩' }}
                          </el-dropdown-item>
                          <el-dropdown-item
                            v-if="canManageStudentProfile"
                            @click.stop="handleDelete(student)"
                            divided
                          >
                            <el-icon><Delete /></el-icon>删除学生
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>
                <div class="card-content">
                  <h3 class="card-name">{{ student.name }}</h3>
                  <div class="card-info">
                    <div class="info-item">
                      <el-icon><User /></el-icon>
                      <span>{{ student.age }}岁</span>
                    </div>
                    <div class="info-item">
                      <el-icon><School /></el-icon>
                      <span>{{ student.className || '未分配' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <!-- Pagination -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[12, 24, 48, 96]"
            :total="pagination.totalElements"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="custom-pagination"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, View, Edit, Delete, User, Search, Refresh, 
  School, List, Clock, MoreFilled 
} from '@element-plus/icons-vue'
import { studentApi, type Student, type PageRequest } from '@/api/student'
import { classApi, type ClassInfo } from '@/api/class'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { canManageStudentProfile } = useAuth()

// Reactive data
const students = ref<Student[]>([])
const classes = ref<ClassInfo[]>([])
const loading = ref(false)

// Search form
const searchForm = reactive({
  name: '',
  classId: '' as string | number
})

// Pagination
const pagination = reactive({
  page: 1,
  size: 20,
  totalElements: 0,
  totalPages: 0
})

// Computed properties

// Methods
const loadStudents = async () => {
  try {
    loading.value = true
    const params: PageRequest = {
      page: pagination.page - 1, // Backend uses 0-based pagination
      size: pagination.size,
      name: searchForm.name || undefined,
      classId: searchForm.classId === '' ? undefined : Number(searchForm.classId)
    }
    
    const response = await studentApi.getStudents(params)
    students.value = response.content
    pagination.totalElements = response.totalElements
    pagination.totalPages = response.totalPages
  } catch (error) {
    console.error('Failed to load students:', error)
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

const loadClasses = async () => {
  try {
    classes.value = await classApi.getClasses()
  } catch (error) {
    console.error('Failed to load classes:', error)
    ElMessage.error('加载班级列表失败')
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadStudents()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.classId = ''
  pagination.page = 1
  loadStudents()
}


const handleView = (id: number) => {
  router.push(`/students/${id}`)
}

const handleEdit = (id: number) => {
  router.push(`/students/${id}/edit`)
}

const handleDelete = async (student: Student) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 "${student.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await studentApi.deleteStudent(student.id)
    ElMessage.success('删除成功')
    loadStudents()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete student:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadStudents()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadStudents()
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// Lifecycle
onMounted(() => {
  loadClasses()
  loadStudents()
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}
.student-list-container {
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

.create-btn {
  height: 48px;
  padding: 0 24px;
  font-size: 16px;
  border-radius: 4px;
  background: #5b9bd5;
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

/* Search Section */
.search-section {
  margin: 0 20px 20px 20px;
  box-sizing: border-box;
}

.search-card {
  border: none;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.search-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.search-icon {
  font-size: 20px;
  color: #5b9bd5;
}

.search-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.search-form {
  margin: 0;
}

.search-input, .search-select {
  border-radius: 8px;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
}

.search-btn {
  background: #5b9bd5;
  border: none;
  border-radius: 8px;
  margin-right: 12px;
}

.reset-btn {
  border-radius: 8px;
}

/* Statistics Section */
.stats-section {
  margin: 0 20px 20px 20px;
  box-sizing: border-box;
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

.total-students .stat-icon {
  background: #5b9bd5;
}

.active-classes .stat-icon {
  background: #5b9bd5;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  margin-top: 4px;
}

/* List Section */
.list-section {
  margin: 0 20px 20px 20px;
  box-sizing: border-box;
}

.list-card {
  border: none;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  min-height: calc(100vh - 400px);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.count-tag {
  margin-left: 8px;
}

.list-actions {
  display: flex;
  gap: 12px;
}

/* Card View */
.card-view {
  min-height: 400px;
}

.student-card-col {
  margin-bottom: 24px;
}

.student-card {
  background: white;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
}

.student-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.card-avatar {
  border: 3px solid #e2e8f0;
}

.card-actions {
  opacity: 0;
  transition: opacity 0.3s ease;
}

.student-card:hover .card-actions {
  opacity: 1;
}

.card-content {
  text-align: center;
}

.card-name {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.card-info {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #718096;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.custom-pagination {
  background: white;
  padding: 16px 24px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* Empty State */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

/* Responsive Design */
@media (max-width: 768px) {
  .student-list-container {
    padding: 0;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    padding: 12px;
    margin: 0 2px;
  }
  
  .search-section {
    margin: 0 2px 8px 2px;
  }
  
  .stats-section {
    margin: 0 2px 8px 2px;
  }
  
  .list-section {
    margin: 0 2px 2px 2px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .stats-section .el-col {
    margin-bottom: 16px;
  }
  
  .search-actions {
    justify-content: flex-start;
  }
  
  .card-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .student-card-col {
    margin-bottom: 16px;
  }
}
</style>