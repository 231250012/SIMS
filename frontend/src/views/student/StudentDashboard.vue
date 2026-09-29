<template>
  <div class="student-dashboard">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">欢迎回来，{{ user?.username }} 同学</h1>
          <p class="page-subtitle">查看您的学习进度和成绩信息</p>
        </div>
        <el-button type="primary" @click="refreshData" :loading="loading">刷新数据</el-button>
      </div>
    </div>

    <div class="stats-section" v-loading="loading">
      <div class="stat-card">
        <div class="stat-label">平均成绩</div>
        <div class="stat-number">{{ averageScore }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">成绩记录</div>
        <div class="stat-number">{{ recentGrades.length }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">所属班级</div>
        <div class="stat-number class-name">{{ studentInfo?.className || '未分配' }}</div>
      </div>
    </div>

    <div class="main-content">
      <el-card shadow="never" class="info-card">
        <template #header>个人信息</template>
        <div class="info-row"><span>姓名</span><span>{{ studentInfo?.name || '-' }}</span></div>
        <div class="info-row"><span>年龄</span><span>{{ studentInfo?.age ?? '-' }}</span></div>
        <div class="info-row"><span>班级</span><span>{{ studentInfo?.className || '未分配' }}</span></div>
        <div class="info-row"><span>学号</span><span>{{ studentInfo?.studentId || '-' }}</span></div>
      </el-card>

      <QuickActions />

      <el-card shadow="never" class="info-card">
        <template #header>最近成绩</template>
        <el-table :data="recentGrades" size="small" empty-text="暂无成绩">
          <el-table-column prop="subject" label="科目" />
          <el-table-column prop="score" label="成绩" width="80" />
          <el-table-column prop="examDate" label="日期" min-width="110" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { studentApi, type Student } from '@/api/student'
import { gradeApi, type GradeRecord } from '@/api/grade'
import QuickActions from '@/components/student/QuickActions.vue'

const { user } = useAuth()
const loading = ref(false)
const studentInfo = ref<Student | null>(null)
const recentGrades = ref<GradeRecord[]>([])

const averageScore = computed(() => {
  if (recentGrades.value.length === 0) return '0.0'
  const total = recentGrades.value.reduce((sum, g) => sum + Number(g.score || 0), 0)
  return (total / recentGrades.value.length).toFixed(1)
})

const loadStudentData = async () => {
  loading.value = true
  try {
    const [profile, grades] = await Promise.all([
      studentApi.getMyProfile(),
      gradeApi.getMyGrades().catch(() => studentApi.getMyGrades().catch(() => []))
    ])
    studentInfo.value = profile
    recentGrades.value = (grades || []).slice(0, 10)
  } catch (error) {
    console.error('Failed to load student data:', error)
    ElMessage.error('加载学生数据失败')
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  await loadStudentData()
  ElMessage.success('数据已刷新')
}

onMounted(() => {
  loadStudentData()
})
</script>

<style scoped>
.student-dashboard {
  padding: 16px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  margin: 0 0 4px;
  font-size: 20px;
  color: #3a6d99;
}

.page-subtitle {
  margin: 0;
  color: #6a8094;
  font-size: 14px;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border: 1px solid #d0e0ee;
  border-radius: 4px;
  padding: 16px;
}

.stat-label {
  font-size: 13px;
  color: #6a8094;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 22px;
  font-weight: 600;
  color: #2c3e50;
}

.stat-number.class-name {
  font-size: 16px;
}

.main-content {
  display: grid;
  gap: 16px;
}

.info-card {
  border: 1px solid #d0e0ee;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eef3f8;
  color: #2c3e50;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row span:first-child {
  color: #6a8094;
}
</style>
