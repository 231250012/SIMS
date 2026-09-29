<template>
  <div class="student-grades">
    <div class="grades-header">
      <h2>我的成绩</h2>
      <el-button type="primary" @click="loadGrades" :loading="loading">刷新</el-button>
    </div>

    <div v-loading="loading" class="grades-content">
      <div class="grades-overview">
        <div class="overview-card">
          <div class="card-content">
            <h3>平均分</h3>
            <p class="score">{{ averageScore }}</p>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-content">
            <h3>总科目数</h3>
            <p class="count">{{ totalSubjects }} 科</p>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-content">
            <h3>记录数</h3>
            <p class="count">{{ grades.length }} 条</p>
          </div>
        </div>
      </div>

      <el-table :data="grades" stripe empty-text="暂无成绩记录">
        <el-table-column prop="subject" label="科目" min-width="100" />
        <el-table-column prop="score" label="成绩" width="90" />
        <el-table-column prop="maxScore" label="满分" width="90" />
        <el-table-column prop="examDate" label="考试日期" min-width="120" />
        <el-table-column prop="semester" label="学期" min-width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { gradeApi, type GradeRecord } from '@/api/grade'

const loading = ref(false)
const grades = ref<GradeRecord[]>([])

const averageScore = computed(() => {
  if (grades.value.length === 0) return '0.0'
  const total = grades.value.reduce((sum, g) => sum + Number(g.score || 0), 0)
  return (total / grades.value.length).toFixed(1)
})

const totalSubjects = computed(() => {
  return new Set(grades.value.map(g => g.subject)).size
})

const loadGrades = async () => {
  loading.value = true
  try {
    grades.value = await gradeApi.getMyGrades()
  } catch (error) {
    console.error('加载成绩失败:', error)
    grades.value = []
    ElMessage.error('加载成绩失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadGrades()
})
</script>

<style scoped>
.student-grades {
  padding: 16px;
}

.grades-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.grades-header h2 {
  margin: 0;
  font-size: 20px;
  color: #3a6d99;
}

.grades-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.overview-card {
  background: #fff;
  border: 1px solid #d0e0ee;
  border-radius: 4px;
  padding: 16px;
}

.card-content h3 {
  margin: 0 0 8px;
  font-size: 13px;
  color: #6a8094;
  font-weight: 400;
}

.score,
.count {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #2c3e50;
}
</style>
