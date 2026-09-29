<template>
  <div class="student-form-container">
    <!-- Navigation Header -->
    <div class="nav-header">
      <el-button @click="goBack" class="back-button" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="breadcrumb">
        <span class="breadcrumb-item">学生管理</span>
        <el-icon class="breadcrumb-separator"><ArrowRight /></el-icon>
        <span class="breadcrumb-current">{{ isEdit ? (canManageStudentProfile ? '编辑学生' : '修改成绩') : '添加学生' }}</span>
      </div>
    </div>

    <!-- Form Header -->
    <div class="form-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="form-title">
            <el-icon class="title-icon">
              <component :is="isEdit ? 'Edit' : 'Plus'" />
            </el-icon>
            {{ formPageTitle }}
          </h1>
          <p class="form-subtitle">
            {{ formPageSubtitle }}
          </p>
        </div>
        <div class="progress-indicator" v-if="!isEdit">
          <div class="step active">
            <div class="step-number">1</div>
            <div class="step-label">基本信息</div>
          </div>
          <div class="step-line"></div>
          <div class="step">
            <div class="step-number">2</div>
            <div class="step-label">完成创建</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Form Content -->
    <div class="form-content" v-loading="loading">
      <el-row :gutter="24">
        <!-- Main Form -->
        <el-col :xs="24" :lg="16">
          <el-card class="form-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><User /></el-icon>
                  <span>学生信息</span>
                </div>
                <el-tag v-if="isEdit" type="warning">编辑模式</el-tag>
              </div>
            </template>

            <el-form 
              ref="formRef"
              :model="form" 
              :rules="formRules"
              label-width="120px"
              label-position="top"
              class="student-form"
              @submit.prevent="handleSubmit"
            >
              <!-- Basic Information Section -->
              <div class="form-section" v-if="canManageStudentProfile">
                <div class="section-title">
                  <el-icon><CreditCard /></el-icon>
                  <span>基本信息</span>
                </div>
                
                <el-row :gutter="24">
                  <el-col :xs="24" :md="12">
                    <el-form-item label="学生姓名" prop="name">
                      <el-input 
                        v-model="form.name" 
                        placeholder="请输入学生姓名"
                        maxlength="50"
                        show-word-limit
                        size="large"
                        prefix-icon="User"
                        class="form-input"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :md="12">
                    <el-form-item label="年龄" prop="age">
                      <el-input-number 
                        v-model="form.age" 
                        :min="1" 
                        :max="100"
                        placeholder="请输入年龄"
                        size="large"
                        class="form-input"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item label="所属班级" prop="classId">
                  <el-select 
                    v-model="form.classId" 
                    placeholder="请选择班级（可清空表示暂不分配）"
                    clearable
                    size="large"
                    class="form-input"
                    style="width: 100%"
                  >
                    <el-option 
                      v-for="cls in classes" 
                      :key="cls.id" 
                      :label="cls.name" 
                      :value="cls.id"
                    >
                      <div class="option-item">
                        <el-icon><School /></el-icon>
                        <span>{{ cls.name }}</span>
                        <el-tag size="small" type="info">{{ (cls as any).studentCount || 0 }}人</el-tag>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
              </div>

              <!-- Academic Information Section (Edit Mode) -->
              <div v-if="isEdit" class="form-section">
                <div class="section-title">
                  <el-icon><TrendCharts /></el-icon>
                  <span>科目成绩</span>
                </div>

                <el-row :gutter="24">
                  <el-col :xs="24" :md="12">
                    <el-form-item label="C语言（满分100）" prop="cLangScore">
                      <el-input-number
                        v-model="form.cLangScore"
                        :min="0"
                        :max="100"
                        :precision="1"
                        placeholder="未录入"
                        size="large"
                        controls-position="right"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :md="12">
                    <el-form-item label="英语四级（满分710）" prop="cet4Score">
                      <el-input-number
                        v-model="form.cet4Score"
                        :min="0"
                        :max="710"
                        :precision="1"
                        placeholder="未录入"
                        size="large"
                        controls-position="right"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                <p class="grade-hint">两门科目各仅记录一次正式考试成绩；保存时将同步更新排行榜。</p>
              </div>
            </el-form>
          </el-card>
        </el-col>

        <!-- Sidebar -->
        <el-col :xs="24" :lg="8">
          <!-- Preview Card -->
          <el-card class="preview-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><View /></el-icon>
                  <span>信息预览</span>
                </div>
              </div>
            </template>
            
            <div class="preview-content">
              <div class="preview-avatar">
                <el-avatar :size="80" class="preview-student-avatar">
                  {{ form.name?.charAt(0) || '?' }}
                </el-avatar>
              </div>
              <div class="preview-info">
                <h3 class="preview-name">{{ form.name || '未填写姓名' }}</h3>
                <div class="preview-details">
                  <div class="detail-item">
                    <span class="detail-label">年龄：</span>
                    <span class="detail-value">{{ form.age }}岁</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">班级：</span>
                    <span class="detail-value">
                      {{ getClassName(form.classId) || '未分配' }}
                    </span>
                  </div>
                  <template v-if="isEdit">
                    <div class="detail-item">
                      <span class="detail-label">C语言：</span>
                      <span class="detail-value">{{ formatSubjectScore(form.cLangScore, 100) }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">英语四级：</span>
                      <span class="detail-value">{{ formatSubjectScore(form.cet4Score, 710) }}</span>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </el-card>

          <!-- Actions Card -->
          <el-card class="actions-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon class="header-icon"><Operation /></el-icon>
                  <span>操作</span>
                </div>
              </div>
            </template>
            
            <div class="actions-content">
              <el-button 
                type="primary" 
                size="large"
                @click="handleSubmit"
                :loading="submitting"
                class="action-button primary"
              >
                <el-icon><Check /></el-icon>
                {{ isEdit ? '保存修改' : '创建学生' }}
              </el-button>
              
              <el-button 
                size="large"
                @click="handleCancel"
                class="action-button"
              >
                <el-icon><Close /></el-icon>
                取消操作
              </el-button>
              
              <el-button 
                v-if="isEdit" 
                type="info" 
                size="large"
                @click="handleReset"
                class="action-button"
              >
                <el-icon><Refresh /></el-icon>
                重置表单
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft, ArrowRight, User, CreditCard, TrendCharts,
  School, Operation, Check, Close, Refresh, Edit, Plus, View
} from '@element-plus/icons-vue'
import { studentApi, type Student, type CreateStudentRequest, type UpdateStudentRequest } from '@/api/student'
import { classApi, type ClassInfo } from '@/api/class'
import { gradeApi } from '@/api/grade'
import { DEFAULT_EXAM_TYPE, getSubjectMaxScore } from '@/constants/subjects'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { canManageStudentProfile } = useAuth()

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const classes = ref<ClassInfo[]>([])
const originalStudent = ref<Student | null>(null)
const originalScores = ref<{ cLang?: number; cet4?: number }>({})

const isEdit = computed(() => route.name === 'StudentEdit')
const studentId = computed(() => (isEdit.value ? Number(route.params.id) : null))
const formPageTitle = computed(() => {
  if (!isEdit.value) return '添加新学生'
  return canManageStudentProfile.value ? '编辑学生信息' : '修改学生成绩'
})
const formPageSubtitle = computed(() => {
  if (!isEdit.value) return '填写学生的基本信息，创建学生档案'
  return canManageStudentProfile.value
    ? '修改学生的基本信息和学业数据'
    : '非班主任仅可修改成绩，学生资料不可更改'
})

const form = reactive({
  name: '',
  age: 18,
  classId: undefined as number | undefined,
  cLangScore: undefined as number | undefined,
  cet4Score: undefined as number | undefined
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '年龄必须在 1 到 100 之间', trigger: 'blur' }
  ],
  cLangScore: [
    { type: 'number', min: 0, max: 100, message: 'C语言成绩须在 0-100', trigger: 'blur' }
  ],
  cet4Score: [
    { type: 'number', min: 0, max: 710, message: '英语四级成绩须在 0-710', trigger: 'blur' }
  ]
}

const formatSubjectScore = (score: number | undefined, max: number) => {
  if (score === undefined || score === null || Number.isNaN(Number(score))) return '未录入'
  return `${Number(score).toFixed(1)} / ${max}`
}

const loadClasses = async () => {
  try {
    classes.value = await classApi.getClasses()
  } catch (error) {
    console.error('Failed to load classes:', error)
    ElMessage.error('加载班级列表失败')
  }
}

const loadSubjectGrades = async (id: number) => {
  try {
    const grades = await gradeApi.getStudentGradeRecords(id)
    const cLang = grades.find(g => g.subject === 'C语言')
    const cet4 = grades.find(g => g.subject === '英语四级')
    form.cLangScore = cLang ? Number(cLang.score) : undefined
    form.cet4Score = cet4 ? Number(cet4.score) : undefined
    originalScores.value = { cLang: form.cLangScore, cet4: form.cet4Score }
  } catch (error) {
    console.error('Failed to load subject grades:', error)
    form.cLangScore = undefined
    form.cet4Score = undefined
    originalScores.value = {}
  }
}

const loadStudent = async () => {
  if (!isEdit.value || !studentId.value) return
  try {
    loading.value = true
    const student = await studentApi.getStudent(studentId.value)
    originalStudent.value = student
    form.name = student.name
    form.age = student.age
    form.classId = student.classId ?? undefined
    await loadSubjectGrades(studentId.value)
  } catch (error) {
    console.error('Failed to load student:', error)
    ElMessage.error('加载学生信息失败')
    router.push('/students')
  } finally {
    loading.value = false
  }
}

const getClassName = (classId?: number | null) => {
  if (!classId) return null
  return classes.value.find((c: ClassInfo) => c.id === classId)?.name || null
}

const saveSubjectGrade = async (subject: string, score: number | undefined) => {
  if (score === undefined || score === null || !studentId.value) return
  const today = new Date().toISOString().split('T')[0]
  await gradeApi.createGradeRecord({
    studentId: studentId.value,
    subject,
    score,
    maxScore: getSubjectMaxScore(subject),
    examType: DEFAULT_EXAM_TYPE,
    examDate: today
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    if (canManageStudentProfile.value) {
      const valid = await formRef.value.validate()
      if (!valid) return
    }
    submitting.value = true
    if (isEdit.value && studentId.value) {
      if (canManageStudentProfile.value) {
        await studentApi.updateStudent(studentId.value, {
          name: form.name,
          age: form.age,
          classId: form.classId
        })
      }
      if (form.cLangScore !== undefined && form.cLangScore !== originalScores.value.cLang) {
        await saveSubjectGrade('C语言', form.cLangScore)
      }
      if (form.cet4Score !== undefined && form.cet4Score !== originalScores.value.cet4) {
        await saveSubjectGrade('英语四级', form.cet4Score)
      }
      ElMessage.success(canManageStudentProfile.value ? '学生信息更新成功' : '成绩更新成功')
    } else {
      if (!canManageStudentProfile.value) {
        ElMessage.error('非班主任无权创建学生')
        return
      }
      const newStudent = await studentApi.createStudent({
        name: form.name,
        age: form.age,
        classId: form.classId
      })
      ElMessage.success('学生创建成功')
      router.push(`/students/${newStudent.id}`)
      return
    }
    router.push(`/students/${studentId.value}`)
  } catch (error: any) {
    console.error('Failed to save student:', error)
    ElMessage.error(error?.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

const handleCancel = async () => {
  const hasChanges = isEdit.value
    ? (form.name !== originalStudent.value?.name ||
      form.age !== originalStudent.value?.age ||
      form.classId !== originalStudent.value?.classId ||
      form.cLangScore !== originalScores.value.cLang ||
      form.cet4Score !== originalScores.value.cet4)
    : !!(form.name || form.age !== 18 || form.classId)
  if (hasChanges) {
    try {
      await ElMessageBox.confirm('表单内容已修改，确定要离开吗？', '确认离开', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
  }
  goBack()
}

const handleReset = () => {
  if (!originalStudent.value) return
  form.name = originalStudent.value.name
  form.age = originalStudent.value.age
  form.classId = originalStudent.value.classId ?? undefined
  form.cLangScore = originalScores.value.cLang
  form.cet4Score = originalScores.value.cet4
  ElMessage.success('表单已重置')
}

const goBack = () => {
  if (isEdit.value && studentId.value) {
    router.push(`/students/${studentId.value}`)
  } else {
    router.push('/students')
  }
}

onMounted(() => {
  if (!isEdit.value && !canManageStudentProfile.value) {
    ElMessage.warning('非班主任无权创建学生')
    router.replace('/students')
    return
  }
  loadClasses()
  if (isEdit.value) loadStudent()
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}
.student-form-container {
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

/* Form Header */
.form-header {
  margin: 0 20px 24px 20px;
}

.header-content {
  background: white;
  padding: 24px 32px;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.title-section {
  flex: 1;
}

.form-title {
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

.form-subtitle {
  margin: 0;
  color: #718096;
  font-size: 16px;
}

.progress-indicator {
  display: flex;
  align-items: center;
  gap: 16px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e2e8f0;
  color: #718096;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  transition: all 0.3s ease;
}

.step.active .step-number {
  background: #5b9bd5;
  color: white;
}

.step-label {
  font-size: 12px;
  color: #718096;
  font-weight: 500;
}

.step.active .step-label {
  color: #2d3748;
  font-weight: 600;
}

.step-line {
  width: 40px;
  height: 2px;
  background: #e2e8f0;
  margin-top: -20px;
}

/* Form Content */
.form-content {
  gap: 24px;
  margin: 0 20px;
  box-sizing: border-box;
}

/* Cards */
.form-card,
.preview-card,
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

/* Form Sections */
.form-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f1f5f9;
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.grade-hint {
  margin: 0;
  font-size: 12px;
  color: #718096;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

/* Avatar Upload */
.avatar-upload-container {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.avatar-preview-area {
  flex-shrink: 0;
}

.avatar-uploader .upload-area {
  position: relative;
  width: 120px;
  height: 120px;
  border: 3px dashed #d1d5db;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
}

.avatar-uploader .upload-area:hover {
  border-color: #5b9bd5;
  background: #f8faff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #9ca3af;
}

.upload-icon {
  font-size: 32px;
}

.upload-text {
  font-size: 14px;
  font-weight: 500;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(102, 126, 234, 0.8);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-uploader .upload-area:hover .upload-overlay {
  opacity: 1;
}

.avatar-preview {
  border: 3px solid #e2e8f0;
}

.avatar-info {
  flex: 1;
}

.upload-tips h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.upload-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #718096;
}

.upload-tips li {
  margin-bottom: 4px;
}

.avatar-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

/* Form Inputs */
.form-input {
  border-radius: 4px;
}

.form-input :deep(.el-input__wrapper) {
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.form-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.form-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* Option Items */
.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.option-item .el-tag {
  margin-left: auto;
}

/* Score Input */
.score-input-container {
  display: flex;
  gap: 20px;
  align-items: center;
}

.score-input {
  flex-shrink: 0;
  width: 200px;
}

.score-indicator {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-level {
  font-size: 14px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  text-align: center;
  width: fit-content;
}

.score-level.excellent {
  background: #f0f9ff;
  color: #0369a1;
}

.score-level.good {
  background: #eff6ff;
  color: #2563eb;
}

.score-level.average {
  background: #fffbeb;
  color: #d97706;
}

.score-level.poor {
  background: #fef2f2;
  color: #dc2626;
}

.score-progress {
  width: 200px;
}

/* Preview Card */
.preview-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
}

.preview-avatar {
  position: relative;
}

.preview-student-avatar {
  border: 3px solid #e2e8f0;
  background: #5b9bd5;
  color: white;
  font-size: 32px;
  font-weight: 700;
}

.preview-info {
  text-align: center;
  width: 100%;
}

.preview-name {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
}

.preview-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  color: #718096;
  font-weight: 500;
}

.detail-value {
  color: #2d3748;
  font-weight: 600;
}

/* Actions Card */
.actions-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 0;
}

.action-button {
  width: 100%;
  height: 48px;
  border-radius: 4px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.action-button.primary {
  background: #5b9bd5;
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.action-button.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.action-button:not(.primary) {
  background: white;
  border: 1px solid #e2e8f0;
  color: #4a5568;
}

.action-button:not(.primary):hover {
  background: #f8fafc;
  border-color: #cbd5e0;
}

/* Avatar Preview Dialog */
.avatar-preview-dialog {
  display: flex;
  justify-content: center;
  padding: 20px;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .avatar-upload-container {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .score-input-container {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .score-indicator {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .student-form-container {
    padding: 0;
  }
  
  .nav-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    margin: 0 2px 8px 2px;
    padding-top: 4px;
  }
  
  .form-header {
    margin: 0 2px 12px 2px;
  }
  
  .form-content {
    margin: 0 2px;
    max-width: calc(100% - 4px);
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    padding: 20px;
  }
  
  .form-title {
    font-size: 24px;
  }
  
  .progress-indicator {
    align-self: center;
  }
  
  .avatar-upload-container {
    gap: 20px;
  }
  
  .preview-content {
    padding: 16px 0;
  }
}
</style>