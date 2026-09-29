<template>
  <div class="student-detail-container">
    <!-- Navigation Header -->
    <div class="nav-header">
      <el-button @click="goBack" class="back-button" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <div class="breadcrumb">
        <span class="breadcrumb-item">学生管理</span>
        <el-icon class="breadcrumb-separator"><ArrowRight /></el-icon>
        <span class="breadcrumb-current">学生详情</span>
      </div>
    </div>
    
    <div v-loading="loading" class="content-wrapper">
      <!-- Student Profile Header -->
      <div class="profile-header">
        <div class="profile-banner">
          <div class="banner-bg"></div>
          <div class="profile-content">
            <div class="avatar-section">
              <div class="avatar-wrapper">
                <el-avatar 
                  :size="120" 
                  class="profile-avatar"
                >
                  {{ student?.name?.charAt(0) }}
                </el-avatar>
              </div>
              <div class="profile-info">
                <h1 class="student-name">{{ student?.name }}</h1>
                <div class="student-meta">
                  <el-tag type="info" class="id-tag">ID: {{ student?.id }}</el-tag>
                  <span class="age-info">{{ student?.age }}岁</span>
                  <el-tag v-if="student?.className" type="primary" class="class-tag">
                    <el-icon><School /></el-icon>
                    {{ student.className }}
                  </el-tag>
                  <span v-else class="no-class">未分配班级</span>
                </div>
                <div class="join-date">
                  <el-icon><Calendar /></el-icon>
                  加入时间：{{ formatDate(student?.createTime) }}
                </div>
              </div>
            </div>
            <div class="action-section">
              <el-button type="primary" size="large" @click="handleEdit" class="edit-btn">
                <el-icon><Edit /></el-icon>
                {{ canManageStudentProfile ? '编辑信息' : '修改成绩' }}
              </el-button>
              <el-button
                v-if="canManageStudentProfile"
                size="large"
                type="danger"
                plain
                @click="handleDelete"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="main-content">
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><User /></el-icon>
                <span>基本信息</span>
              </div>
            </div>
          </template>
          
          <div class="info-content">
            <div class="info-grid">
              <div class="info-item">
                <div class="info-label">
                  <el-icon><CreditCard /></el-icon>
                  学生ID
                </div>
                <div class="info-value">{{ student?.id }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><User /></el-icon>
                  姓名
                </div>
                <div class="info-value">{{ student?.name }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><Calendar /></el-icon>
                  年龄
                </div>
                <div class="info-value">{{ student?.age }}岁</div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><School /></el-icon>
                  班级
                </div>
                <div class="info-value">
                  {{ student?.className || '未分配' }}
                </div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><Clock /></el-icon>
                  创建时间
                </div>
                <div class="info-value">{{ formatDate(student?.createTime) }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="info-card grades-card" shadow="never" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon class="header-icon"><TrendCharts /></el-icon>
                <span>科目成绩</span>
              </div>
            </div>
          </template>
          <el-table v-loading="gradesLoading" :data="grades" stripe empty-text="暂无科目成绩">
            <el-table-column prop="subject" label="科目" min-width="100" />
            <el-table-column prop="score" label="成绩" width="100">
              <template #default="{ row }">{{ Number(row.score).toFixed(1) }}</template>
            </el-table-column>
            <el-table-column prop="maxScore" label="满分" width="100">
              <template #default="{ row }">{{ Number(row.maxScore).toFixed(0) }}</template>
            </el-table-column>
            <el-table-column prop="examDate" label="考试日期" min-width="120" />
            <el-table-column prop="semester" label="学期" min-width="120" />
            <el-table-column prop="gradeLevel" label="等级" width="90" />
          </el-table>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, ArrowRight, Edit, Delete, School, Calendar, 
  User, CreditCard, Clock, TrendCharts
} from '@element-plus/icons-vue'
import { studentApi, type Student } from '@/api/student'
import { gradeApi, type GradeRecord } from '@/api/grade'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { canManageStudentProfile } = useAuth()

const student = ref<Student | null>(null)
const loading = ref(false)
const gradesLoading = ref(false)
const grades = ref<GradeRecord[]>([])

const studentId = computed(() => Number(route.params.id))

const loadStudent = async () => {
  try {
    loading.value = true
    student.value = await studentApi.getStudent(studentId.value)
    await loadGrades()
  } catch (error) {
    console.error('Failed to load student:', error)
    ElMessage.error('加载学生信息失败')
    router.push('/students')
  } finally {
    loading.value = false
  }
}

const loadGrades = async () => {
  try {
    gradesLoading.value = true
    grades.value = await gradeApi.getStudentGradeRecords(studentId.value)
  } catch (error) {
    console.error('Failed to load grades:', error)
    grades.value = []
  } finally {
    gradesLoading.value = false
  }
}

const goBack = () => {
  router.push('/students')
}

const handleEdit = () => {
  router.push(`/students/${studentId.value}/edit`)
}

const handleDelete = async () => {
  if (!student.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 "${student.value.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await studentApi.deleteStudent(studentId.value)
    ElMessage.success('删除成功')
    router.push('/students')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete student:', error)
      ElMessage.error('删除失败')
    }
  }
}

const formatDate = (dateString?: string) => {
  if (!dateString) return '--'
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadStudent()
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}
.student-detail-container {
  padding: 0;
  background: #f0f5fa;
  min-height: 100%;
  height: 100%;
  width: 100%;
  margin: 0;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* Navigation Header */
.nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 20px 20px 20px;
  padding-top: 16px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 20px;
  border-radius: 4px;
  background: white;
  border: 1px solid #e2e8f0;
  color: #4a5568;
  transition: all 0.3s ease;
}

.back-button:hover {
  background: #5b9bd5;
  color: white;
  border-color: #5b9bd5;
  transform: translateX(-4px);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-size: 14px;
}

.breadcrumb-item {
  color: #a0aec0;
}

.breadcrumb-separator {
  font-size: 12px;
}

.breadcrumb-current {
  color: #2d3748;
  font-weight: 600;
}

/* Content Wrapper */
.content-wrapper {
  position: relative;
}

/* Profile Header */
.profile-header {
  margin: 0 20px 24px 20px;
}

.profile-banner {
  position: relative;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.banner-bg {
  height: 120px;
  background: #5b9bd5;
  position: relative;
}

.banner-bg::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.15"/><circle cx="10" cy="60" r="0.5" fill="white" opacity="0.15"/><circle cx="90" cy="40" r="0.5" fill="white" opacity="0.15"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
}

.profile-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 0 32px 32px;
  margin-top: -60px;
  position: relative;
  z-index: 1;
}

.avatar-section {
  display: flex;
  gap: 20px;
  align-items: flex-end;
}

.avatar-wrapper {
  position: relative;
}

.profile-avatar {
  border: 4px solid white;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  background: #5b9bd5;
  color: white;
  font-size: 48px;
  font-weight: 700;
}

.avatar-overlay {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

.avatar-upload-btn {
  width: 36px;
  height: 36px;
  background: white;
  color: #5b9bd5;
  border: 2px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-upload-btn:hover {
  background: #5b9bd5;
  color: white;
}

.profile-info {
  flex: 1;
}

.student-name {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
}

.student-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.id-tag {
  font-family: 'Courier New', monospace;
  font-weight: 600;
}

.age-info {
  color: #718096;
  font-weight: 500;
}

.class-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.no-class {
  color: #a0aec0;
  font-style: italic;
}

.join-date {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #718096;
  font-size: 14px;
}

.action-section {
  display: flex;
  gap: 12px;
}

.edit-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 4px;
  background: #5b9bd5;
  border: none;
  font-weight: 600;
}

.more-btn {
  width: 44px;
  height: 44px;
  border-radius: 4px;
  background: white;
  border: 1px solid #e2e8f0;
}

/* Main Content */
.main-content {
  gap: 24px;
  margin: 0 20px;
  box-sizing: border-box;
}

/* Card Styles */
.activities-card,
.info-card,
.actions-card {
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

/* Activities Content */
.activities-content {
  padding: 20px 0;
}

.no-activities {
  padding: 40px 20px;
  text-align: center;
}

/* Info Content */
.info-content {
  padding: 20px 0;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-weight: 500;
}

.info-value {
  font-weight: 600;
  color: #2d3748;
}

/* Actions Content */
.actions-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px 0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  height: 48px;
  padding: 0 20px;
  border-radius: 4px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #4a5568;
  font-weight: 500;
  transition: all 0.3s ease;
  justify-content: flex-start;
}

.action-item:hover {
  background: #5b9bd5;
  color: white;
  border-color: #5b9bd5;
  transform: translateX(4px);
}

.action-item.danger:hover {
  background: #f56565;
  border-color: #f56565;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .main-content {
    margin: 0 2px;
  }
}

@media (max-width: 768px) {
  .student-detail-container {
    padding: 0;
  }
  
  .nav-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    margin: 0 2px 8px 2px;
    padding-top: 4px;
  }
  
  .profile-header {
    margin: 0 2px 12px 2px;
  }
  
  .main-content {
    margin: 0 2px;
    max-width: calc(100% - 4px);
  }
  
  .profile-content {
    flex-direction: column;
    gap: 20px;
    align-items: center;
    text-align: center;
  }
  
  .avatar-section {
    flex-direction: column;
    align-items: center;
  }
  
  .student-name {
    font-size: 24px;
  }
  
  .student-meta {
    justify-content: center;
  }
  
  .action-section {
    width: 100%;
    justify-content: center;
  }
}
</style>