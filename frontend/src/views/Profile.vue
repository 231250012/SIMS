<template>
  <div class="profile-container">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><User /></el-icon>
            个人信息
          </h1>
          <p class="page-subtitle">查看和管理您的个人资料</p>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="main-content">
      <el-row :gutter="24">
        <el-col :span="24">
          <!-- 基本信息卡片 -->
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><User /></el-icon>
                  <span>基本信息</span>
                </div>
              </div>
            </template>
            
            <div class="user-info" v-loading="loading">
              <div class="info-item">
                <label>用户名：</label>
                <span>{{ user?.username || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>角色：</label>
                <el-tag :type="getRoleTagType(user?.role)">
                  {{ getRoleDisplayName(user?.role) }}
                </el-tag>
              </div>
              <div v-if="studentInfo" class="info-item">
                <label>真实姓名：</label>
                <span>{{ studentInfo.name || '未设置' }}</span>
              </div>
              <div v-if="studentInfo" class="info-item">
                <label>年龄：</label>
                <span>{{ studentInfo.age || '未设置' }}</span>
              </div>
              <div v-if="studentInfo" class="info-item">
                <label>班级：</label>
                <span>{{ studentInfo.className || '未分配' }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'

const { user, isStudent } = useAuth()

// Reactive data
const loading = ref(false)
const studentInfo = ref<any>(null)

// Methods
const getRoleTagType = (role?: string) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'TEACHER': return 'warning'
    case 'STUDENT': return 'success'
    default: return 'info'
  }
}

const getRoleDisplayName = (role?: string) => {
  switch (role) {
    case 'ADMIN': return '管理员'
    case 'TEACHER': return '教师'
    case 'STUDENT': return '学生'
    default: return '未知'
  }
}

const loadStudentInfo = async () => {
  if (!isStudent.value) return
  
  try {
    loading.value = true
    const { studentApi } = await import('@/api/student')
    studentInfo.value = await studentApi.getMyProfile()
  } catch (error) {
    console.error('Failed to load student info:', error)
    studentInfo.value = null
    ElMessage.error('加载学生信息失败')
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadStudentInfo()
})
</script>

<style scoped>
.profile-container {
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
  background: white;
  padding: 24px 32px;
  margin: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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

/* Main Content */
.main-content {
  margin: 0 20px;
  box-sizing: border-box;
}

/* Cards */
.info-card {
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

/* User Info */
.user-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 600;
  color: #2d3748;
  min-width: 100px;
  margin-right: 16px;
}

.info-item span {
  color: #4a5568;
}

/* Responsive Design */
@media (max-width: 768px) {
  .profile-container {
    padding: 0;
  }
  
  .header-content {
    padding: 16px;
    margin: 0 4px;
  }
  
  .main-content {
    margin: 0 4px;
  }
  
  .page-title {
    font-size: 24px;
  }
}
</style>