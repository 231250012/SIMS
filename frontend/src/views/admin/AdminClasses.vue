<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">班级管理</h1>
        <p class="page-sub">创建新班级，查看现有班级概况</p>
      </div>
      <el-button type="primary" @click="dialogVisible = true">新建班级</el-button>
    </div>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <el-table :data="classes" stripe empty-text="暂无班级">
        <el-table-column prop="name" label="班级名称" min-width="140" />
        <el-table-column prop="grade" label="年级" width="120">
          <template #default="{ row }">{{ row.grade || '—' }}</template>
        </el-table-column>
        <el-table-column prop="teacherName" label="负责教师" min-width="120">
          <template #default="{ row }">{{ row.teacherName || '未分配' }}</template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学生数" width="100">
          <template #default="{ row }">{{ row.studentCount ?? 0 }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建班级" width="460px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：计算机1班" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="例如：2024级" maxlength="20" />
        </el-form-item>
        <el-form-item label="班级描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="form.maxStudents" :min="1" :max="500" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责教师（可选）">
          <el-select v-model="form.teacherId" clearable placeholder="稍后可通过教师管理分配" style="width: 100%">
            <el-option
              v-for="t in teachers"
              :key="t.id"
              :label="t.username"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { classApi, type ClassInfo } from '../../api/class'
import { teacherAdminApi, type TeacherAdminInfo } from '../../api/teacherAdmin'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const classes = ref<ClassInfo[]>([])
const teachers = ref<TeacherAdminInfo[]>([])
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  grade: '',
  description: '',
  maxStudents: 50 as number | undefined,
  teacherId: undefined as number | undefined
})

const rules: FormRules = {
  name: [{ required: true, message: '请填写班级名称', trigger: 'blur' }]
}

async function loadClasses() {
  loading.value = true
  try {
    classes.value = await classApi.getAllClasses()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '加载班级失败')
  } finally {
    loading.value = false
  }
}

async function loadTeachers() {
  try {
    teachers.value = await teacherAdminApi.list()
  } catch {
    teachers.value = []
  }
}

async function submitCreate() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await classApi.createClass({
        name: form.name.trim(),
        grade: form.grade?.trim() || undefined,
        description: form.description || undefined,
        maxStudents: form.maxStudents,
        teacherId: form.teacherId
      })
      ElMessage.success('班级已创建')
      dialogVisible.value = false
      form.name = ''
      form.grade = ''
      form.description = ''
      form.maxStudents = 50
      form.teacherId = undefined
      await loadClasses()
    } catch (e: any) {
      ElMessage.error(e?.response?.data?.message || '创建失败')
    } finally {
      saving.value = false
    }
  })
}

onMounted(async () => {
  await Promise.all([loadClasses(), loadTeachers()])
})
</script>

<style scoped>
.admin-page {
  padding: 8px 4px 32px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #0a1128;
}

.page-sub {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.table-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
}
</style>
