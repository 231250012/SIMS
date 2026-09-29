<template>
  <div class="teacher-class-detail">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="breadcrumb-section">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="navigateBack">班级管理</el-breadcrumb-item>
            <el-breadcrumb-item>{{ classInfo?.name || '班级详情' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><School /></el-icon>
            {{ classInfo?.name || '加载中...' }}
          </h1>
          <p class="page-subtitle">班级详细信息和学生管理</p>
        </div>
        <div class="header-actions">
          <el-button @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <el-alert
        :title="error"
        type="error"
        show-icon
        :closable="false"
      >
        <template #default>
          <p>无法加载班级详情，请检查网络连接后重试。</p>
          <el-button type="primary" size="small" @click="retryLoad">
            <el-icon><Refresh /></el-icon>
            重新加载
          </el-button>
        </template>
      </el-alert>
    </div>

    <!-- Main Content -->
    <div v-else class="main-content">
      <!-- Class Information Card -->
      <div class="class-info-section">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><InfoFilled /></el-icon>
                <span>班级信息</span>
              </div>
            </div>
          </template>
          
          <div class="class-info-content">
            <el-row :gutter="24">
              <el-col :xs="24" :sm="12" :md="8">
                <div class="info-item">
                  <div class="info-label">班级名称</div>
                  <div class="info-value">{{ classInfo?.name }}</div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <div class="info-item">
                  <div class="info-label">年级</div>
                  <div class="info-value">
                    <el-tag :type="getGradeTagType(classInfo?.grade)">
                      {{ classInfo?.grade || '其他' }}
                    </el-tag>
                  </div>
                </div>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <div class="info-item">
                  <div class="info-label">任课教师</div>
                  <div class="info-value">{{ classInfo?.teacherName || '未分配' }}</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </div>

      <div class="statistics-section">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="8">
            <div class="stat-card student-count">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ classStatistics.totalStudents }}</div>
                <div class="stat-label">学生总数</div>
                <div class="stat-trend">
                  <span>在校学生</span>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="16">
            <el-card shadow="never" class="class-ranking-card">
              <template #header>
                <div class="card-header">
                  <div class="header-title">
                    <el-icon><Trophy /></el-icon>
                    <span>本班科目排行榜</span>
                  </div>
                </div>
              </template>
              <el-tabs v-model="classRankingSubject" @tab-change="loadClassRankings">
                <el-tab-pane label="C语言" name="C语言" />
                <el-tab-pane label="英语四级" name="英语四级" />
              </el-tabs>
              <el-table :data="classRankings" size="small" empty-text="暂无成绩">
                <el-table-column prop="rank" label="名次" width="60" />
                <el-table-column prop="studentName" label="姓名" />
                <el-table-column prop="score" label="成绩" width="90">
                  <template #default="{ row }">{{ Number(row.score).toFixed(1) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- Students Management Section -->
      <div class="students-section">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><User /></el-icon>
                <span>学生列表 ({{ filteredStudents.length }})</span>
              </div>
              <div class="header-actions">
                <el-input
                  v-model="studentSearchQuery"
                  placeholder="搜索学生..."
                  clearable
                  style="width: 200px; margin-right: 12px;"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select
                  v-model="studentSortBy"
                  placeholder="排序方式"
                  style="width: 120px;"
                >
                  <el-option label="按姓名" value="name" />
                  <el-option label="按成绩" value="score" />
                  <el-option label="按年龄" value="age" />
                </el-select>
              </div>
            </div>
          </template>

          <!-- Empty State -->
          <div v-if="filteredStudents.length === 0" class="empty-state">
            <el-icon class="empty-icon"><User /></el-icon>
            <p v-if="studentSearchQuery">没有找到符合条件的学生</p>
            <p v-else>该班级暂无学生（学生通过注册选择本班后自动加入）</p>
          </div>

          <div v-else class="students-table">
            <el-table
              :data="filteredStudents"
              style="width: 100%"
              @row-click="viewStudentDetail"
            >
              <el-table-column type="index" label="#" width="50" />
              <el-table-column prop="name" label="姓名" min-width="100">
                <template #default="{ row }">
                  <div class="student-name-cell">
                    <el-avatar :size="32" :src="row.avatar">
                      {{ row.name.charAt(0) }}
                    </el-avatar>
                    <span class="student-name">{{ row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="age" label="年龄" width="80" align="center" />
              <el-table-column prop="score" label="成绩" width="100" align="center">
                <template #default="{ row }">
                  <span class="score-value" :class="getScoreClass(row.score)">
                    {{ row.score.toFixed(1) }}
                  </span>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="320" align="center">
                <template #default="{ row }">
                  <div class="action-buttons">
                    <el-button type="text" size="small" @click.stop="viewStudentDetail(row)">
                      查看详情
                    </el-button>
                    <el-button type="text" size="small" @click.stop="editStudent(row)">
                      编辑
                    </el-button>
                    <el-button type="primary" size="small" @click.stop="manageGrades(row)">
                      成绩管理
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 成绩管理对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      :title="gradeDialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="grade-management">
        <!-- 学生信息 -->
        <el-card class="student-info-card" shadow="never">
          <div class="student-info">
            <el-avatar :size="60" :src="selectedStudent?.avatar">
              {{ selectedStudent?.name?.charAt(0) }}
            </el-avatar>
            <div class="student-details">
              <h3>{{ selectedStudent?.name }}</h3>
              <p>班级：{{ classInfo?.name }}</p>
              <p>已录入科目：{{ studentGrades.length }} 门</p>
            </div>
          </div>
        </el-card>

        <!-- 成绩录入表单 -->
        <el-card class="grade-form-card" shadow="never">
          <template #header>
            <span>{{ isEditingGrade ? '修改成绩' : '录入成绩' }}</span>
          </template>
          
          <el-form
            ref="gradeFormRef"
            :model="gradeForm"
            :rules="gradeRules"
            label-width="100px"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="科目" prop="subject">
                  <el-select
                    v-model="gradeForm.subject"
                    placeholder="请选择科目"
                    style="width: 100%"
                    @change="onSubjectChange"
                  >
                    <el-option
                      v-for="s in SUBJECT_OPTIONS"
                      :key="s.name"
                      :label="s.label"
                      :value="s.name"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="考试">
                  <el-input model-value="正式考试（每科仅一次）" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="成绩" prop="score">
                  <el-input-number
                    v-model="gradeForm.score"
                    :min="0"
                    :max="gradeForm.maxScore"
                    :precision="1"
                    placeholder="请输入成绩"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="满分" prop="maxScore">
                  <el-input-number
                    v-model="gradeForm.maxScore"
                    :min="1"
                    :max="200"
                    placeholder="请输入满分"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="考试日期" prop="examDate">
                  <el-date-picker
                    v-model="gradeForm.examDate"
                    type="date"
                    placeholder="请选择考试日期"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="学期" prop="semester">
                  <el-input
                    v-model="gradeForm.semester"
                    placeholder="如：2024春季"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="备注" prop="remarks">
              <el-input
                v-model="gradeForm.remarks"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息（可选）"
              />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 历史成绩列表 -->
        <el-card class="grade-history-card" shadow="never">
          <template #header>
            <span>历史成绩</span>
          </template>
          
          <el-table
            :data="studentGrades"
            style="width: 100%"
            max-height="300"
          >
            <el-table-column prop="subject" label="科目" width="80" />
            <el-table-column prop="examType" label="考试" width="90" />
            <el-table-column prop="score" label="成绩" width="80" align="center">
              <template #default="{ row }">
                <span :class="getScoreClass(row.score)">
                  {{ row.score }}/{{ row.maxScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="examDate" label="考试日期" width="120" />
            <el-table-column prop="semester" label="学期" width="100" />
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="editGrade(row)">
                  编辑
                </el-button>
                <el-button type="text" size="small" @click="deleteGrade(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="gradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveGrade" :loading="gradeSaving">
            {{ isEditingGrade ? '更新成绩' : '保存成绩' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  School, Refresh, InfoFilled, Edit,
  User, Trophy, Search
} from '@element-plus/icons-vue'
import { classApi, type ClassInfo, type StudentInfo } from '@/api/class'
import { gradeApi, type GradeRecord, type GradeRecordRequest, type RankingEntry } from '@/api/grade'
import { SUBJECT_OPTIONS, DEFAULT_EXAM_TYPE, getSubjectMaxScore } from '@/constants/subjects'

const route = useRoute()
const router = useRouter()

// Get class ID from route params
const classId = computed(() => parseInt(route.params.id as string))

// Reactive data
const loading = ref(false)
const error = ref<string | null>(null)
const classInfo = ref<ClassInfo | null>(null)
const students = ref<StudentInfo[]>([])
const studentSearchQuery = ref('')
const studentSortBy = ref('name')

// Grade management data
const gradeDialogVisible = ref(false)
const selectedStudent = ref<StudentInfo | null>(null)
const studentGrades = ref<GradeRecord[]>([])
const isEditingGrade = ref(false)
const gradeSaving = ref(false)
const gradeFormRef = ref<FormInstance>()

// Grade form data
const gradeForm = ref<GradeRecordRequest & { id?: number }>({
  studentId: 0,
  subject: '',
  score: 0,
  maxScore: 100,
  examType: DEFAULT_EXAM_TYPE,
  examDate: '',
  semester: '',
  academicYear: '',
  remarks: ''
})

// Form validation rules
const gradeRules: FormRules = {
  subject: [
    { required: true, message: '请选择科目', trigger: 'change' }
  ],
  score: [
    { required: true, message: '请输入成绩', trigger: 'blur' },
    { type: 'number', min: 0, message: '成绩不能为负数', trigger: 'blur' }
  ],
  maxScore: [
    { required: true, message: '请输入满分', trigger: 'blur' },
    { type: 'number', min: 1, message: '满分必须大于0', trigger: 'blur' }
  ],
  examDate: [
    { required: true, message: '请选择考试日期', trigger: 'change' }
  ]
}

const onSubjectChange = (subject: string) => {
  gradeForm.value.maxScore = getSubjectMaxScore(subject)
  gradeForm.value.examType = DEFAULT_EXAM_TYPE
  if (gradeForm.value.score > gradeForm.value.maxScore) {
    gradeForm.value.score = gradeForm.value.maxScore
  }
}

// Computed properties
const filteredStudents = computed(() => {
  let result = [...students.value]

  // Apply search filter
  if (studentSearchQuery.value) {
    const query = studentSearchQuery.value.toLowerCase()
    result = result.filter(student => 
      student.name.toLowerCase().includes(query)
    )
  }

  // Apply sorting
  result.sort((a, b) => {
    switch (studentSortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'score':
        return b.score - a.score
      case 'age':
        return a.age - b.age
      default:
        return 0
    }
  })

  return result
})

const classStatistics = computed(() => ({
  totalStudents: students.value.length
}))

const classRankingSubject = ref('C语言')
const classRankings = ref<RankingEntry[]>([])

const loadClassRankings = async () => {
  try {
    const res = await gradeApi.getClassGradeRanking(classId.value, classRankingSubject.value, 20)
    classRankings.value = res.rankings || []
  } catch (err) {
    console.error('Failed to load class rankings:', err)
    classRankings.value = []
  }
}

const gradeDialogTitle = computed(() => {
  return `${selectedStudent.value?.name || ''} - 成绩管理`
})

// Grade management methods
const manageGrades = async (student: StudentInfo) => {
  selectedStudent.value = student
  gradeDialogVisible.value = true
  isEditingGrade.value = false
  
  // Reset form
  resetGradeForm()
  gradeForm.value.studentId = student.id
  
  // Load student grades
  await loadStudentGrades(student.id)
}

const loadStudentGrades = async (studentId: number) => {
  try {
    const response = await gradeApi.getStudentGradeRecords(studentId)
    studentGrades.value = response
  } catch (err) {
    console.error('Failed to load student grades:', err)
    ElMessage.error('加载学生成绩失败')
    studentGrades.value = []
  }
}

const resetGradeForm = () => {
  gradeForm.value = {
    studentId: selectedStudent.value?.id || 0,
    subject: '',
    score: 0,
    maxScore: 100,
    examType: DEFAULT_EXAM_TYPE,
    examDate: '',
    semester: '',
    academicYear: '',
    remarks: ''
  }
  
  // Clear form validation
  if (gradeFormRef.value) {
    gradeFormRef.value.clearValidate()
  }
}

const saveGrade = async () => {
  if (!gradeFormRef.value) return
  
  try {
    // Validate form
    await gradeFormRef.value.validate()
    
    gradeSaving.value = true
    
    // Prepare request data
    const requestData: GradeRecordRequest = {
      ...gradeForm.value,
      examType: DEFAULT_EXAM_TYPE,
      maxScore: getSubjectMaxScore(gradeForm.value.subject),
      examDate: typeof gradeForm.value.examDate === 'string' 
        ? gradeForm.value.examDate 
        : (gradeForm.value.examDate as Date).toISOString().split('T')[0]
    }
    
    // Remove id from request data as it's not part of the request interface
    const { id, ...cleanRequestData } = requestData as any
    
    if (isEditingGrade.value && gradeForm.value.id) {
      // Update existing grade
      await gradeApi.updateGradeRecord(gradeForm.value.id, cleanRequestData)
      ElMessage.success('成绩更新成功')
    } else {
      // Create new grade
      await gradeApi.createGradeRecord(cleanRequestData)
      ElMessage.success('成绩录入成功')
    }
    
    await loadStudentGrades(selectedStudent.value!.id)
    await loadClassRankings()
    
    resetGradeForm()
    isEditingGrade.value = false
    
  } catch (err: any) {
    console.error('Failed to save grade:', err)
    if (err.response?.data?.message) {
      ElMessage.error(err.response.data.message)
    } else {
      ElMessage.error(isEditingGrade.value ? '成绩更新失败' : '成绩录入失败')
    }
  } finally {
    gradeSaving.value = false
  }
}

const editGrade = (grade: GradeRecord) => {
  isEditingGrade.value = true
  
  // Populate form with grade data
  gradeForm.value = {
    id: grade.id,
    studentId: grade.studentId,
    subject: grade.subject,
    score: Number(grade.score),
    maxScore: Number(grade.maxScore),
    examType: grade.examType,
    examDate: grade.examDate,
    semester: grade.semester || '',
    academicYear: grade.academicYear || '',
    remarks: grade.remarks || ''
  }
}

const deleteGrade = async (grade: GradeRecord) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${grade.subject}" 的成绩记录吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await gradeApi.deleteGradeRecord(grade.id)
    ElMessage.success('成绩删除成功')
    
    await loadStudentGrades(selectedStudent.value!.id)
    await loadClassRankings()
    
  } catch (err: any) {
    if (err !== 'cancel') {
      console.error('Failed to delete grade:', err)
      ElMessage.error('成绩删除失败')
    }
  }
}

// Methods
const loadClassDetail = async () => {
  try {
    loading.value = true
    error.value = null
    
    const [classData, studentsData] = await Promise.all([
      classApi.getClassDetails(classId.value),
      classApi.getClassStudents(classId.value)
    ])
    
    classInfo.value = {
      ...classData,
      grade: classData.name.includes('高三') ? '高三' : 
             classData.name.includes('高二') ? '高二' : 
             classData.name.includes('高一') ? '高一' : '其他'
    }
    students.value = studentsData
    
  } catch (err) {
    console.error('Failed to load class detail:', err)
    error.value = '加载班级详情失败'
    ElMessage.error('加载班级详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const refreshData = async () => {
  await loadClassDetail()
  if (!error.value) {
    ElMessage.success('数据已刷新')
  }
}

const retryLoad = () => {
  loadClassDetail()
}

const navigateBack = () => {
  router.push('/teacher/classes')
}

const getGradeTagType = (grade?: string) => {
  switch (grade) {
    case '高三': return 'danger'
    case '高二': return 'warning'
    case '高一': return 'success'
    default: return 'info'
  }
}

const getScoreClass = (score: number) => {
  if (score >= 90) return 'excellent'
  if (score >= 80) return 'good'
  if (score >= 70) return 'average'
  return 'poor'
}


const viewStudentDetail = (student: StudentInfo) => {
  router.push(`/students/${student.id}`)
}

const editStudent = (student: StudentInfo) => {
  router.push(`/students/${student.id}/edit`)
}

// Lifecycle
onMounted(() => {
  loadClassDetail()
  loadClassRankings()
})
</script>

<style scoped>
.teacher-class-detail {
  padding: 0;
  background: #f5f7fa;
  min-height: 100vh;
}

/* Page Header */
.page-header {
  margin-bottom: 20px;
}

.header-content {
  background: white;
  padding: 24px 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.breadcrumb-section {
  margin-bottom: 16px;
}

.breadcrumb-section .el-breadcrumb-item {
  cursor: pointer;
}

.title-section {
  margin-bottom: 16px;
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
  flex-wrap: wrap;
}

/* Loading and Error States */
.loading-container,
.error-container {
  margin: 0 20px;
  padding: 20px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* Main Content */
.main-content {
  margin: 0 20px;
}

/* Class Info Section */
.class-info-section {
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
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.class-info-content {
  padding: 16px 0;
}

.info-item {
  margin-bottom: 16px;
}

.info-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 4px;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

/* Statistics Section */
.statistics-section {
  margin-bottom: 20px;
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

.student-count .stat-icon {
  background: #5b9bd5;
}

.average-score .stat-icon {
  background: #5b9bd5;
}

.top-score .stat-icon {
  background: #6ba3d6;
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
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #a0aec0;
}

/* Students Section */
.students-section {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.students-table {
  margin-top: 16px;
}

.student-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.student-name {
  font-weight: 600;
  color: #2d3748;
}

.score-value {
  font-weight: 600;
}

.score-value.excellent {
  color: #10b981;
}

.score-value.good {
  color: #3b82f6;
}

.score-value.average {
  color: #f59e0b;
}

.score-value.poor {
  color: #ef4444;
}

/* Empty State */
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
  .teacher-class-detail {
    padding: 0;
  }
  
  .header-content {
    padding: 16px;
  }
  
  .header-actions {
    justify-content: flex-start;
  }
  
  .main-content {
    margin: 0 4px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .header-actions .el-input {
    width: 150px !important;
  }
  
  .header-actions .el-select {
    width: 100px !important;
  }
}

/* Grade Management Dialog Styles */
.grade-management {
  max-height: 70vh;
  overflow-y: auto;
}

.student-info-card {
  margin-bottom: 20px;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.student-details h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.student-details p {
  margin: 4px 0;
  color: #718096;
  font-size: 14px;
}

.grade-form-card,
.grade-history-card {
  margin-bottom: 20px;
}

.grade-form-card .el-card__header {
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.grade-history-card .el-card__header {
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Action buttons styling */
.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
}
</style>