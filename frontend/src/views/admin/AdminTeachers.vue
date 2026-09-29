<template>
  <div class="admin-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">教师管理</h1>
        <p class="page-sub">调整教师负责班级，或删除教师账号</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadTeachers">
        刷新列表
      </el-button>
    </div>

    <el-card shadow="never" class="table-card" v-loading="loading">
      <el-table :data="teachers" stripe empty-text="暂无教师">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="班主任" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isHomeroomTeacher ? 'success' : 'info'" size="small">
              {{ row.isHomeroomTeacher ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="负责班级" min-width="180">
          <template #default="{ row }">
            <span v-if="!row.managedClasses?.length" class="muted">未分配</span>
            <span v-else>
              {{ row.managedClasses.map((c: any) => c.name).join('、') }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAssign(row)">调整班级</el-button>
            <el-button
              link
              :type="row.isHomeroomTeacher ? 'warning' : 'success'"
              @click="toggleHomeroom(row)"
            >
              {{ row.isHomeroomTeacher ? '取消班主任' : '设为班主任' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="assignVisible" title="调整负责班级" width="440px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="教师">
          <el-input :model-value="current?.username" disabled />
        </el-form-item>
        <el-form-item label="目标班级" required>
          <el-select v-model="assignForm.classId" placeholder="选择班级" style="width: 100%">
            <el-option
              v-for="cls in classes"
              :key="cls.id"
              :label="cls.grade ? `${cls.name}（${cls.grade}）` : cls.name"
              :value="cls.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否班主任">
          <el-switch v-model="assignForm.isHomeroomTeacher" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherAdminApi, type TeacherAdminInfo } from '../../api/teacherAdmin'
import { classApi, type ClassInfo } from '../../api/class'

const loading = ref(false)
const saving = ref(false)
const teachers = ref<TeacherAdminInfo[]>([])
const classes = ref<ClassInfo[]>([])
const assignVisible = ref(false)
const current = ref<TeacherAdminInfo | null>(null)
const assignForm = reactive({
  classId: undefined as number | undefined,
  isHomeroomTeacher: true
})

async function loadTeachers() {
  loading.value = true
  try {
    teachers.value = await teacherAdminApi.list()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '加载教师列表失败')
  } finally {
    loading.value = false
  }
}

async function loadClasses() {
  try {
    classes.value = await classApi.getAllClasses()
  } catch {
    classes.value = []
  }
}

function openAssign(row: TeacherAdminInfo) {
  current.value = row
  assignForm.classId = row.managedClasses?.[0]?.id
  assignForm.isHomeroomTeacher = !!row.isHomeroomTeacher
  assignVisible.value = true
}

async function submitAssign() {
  if (!current.value || !assignForm.classId) {
    ElMessage.warning('请选择目标班级')
    return
  }
  saving.value = true
  try {
    await teacherAdminApi.assignClass(current.value.id, {
      classId: assignForm.classId,
      isHomeroomTeacher: assignForm.isHomeroomTeacher
    })
    ElMessage.success('班级已调整')
    assignVisible.value = false
    await loadTeachers()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '调整失败')
  } finally {
    saving.value = false
  }
}

async function toggleHomeroom(row: TeacherAdminInfo) {
  try {
    await teacherAdminApi.setHomeroom(row.id, !row.isHomeroomTeacher)
    ElMessage.success('已更新班主任状态')
    await loadTeachers()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '更新失败')
  }
}

async function handleDelete(row: TeacherAdminInfo) {
  try {
    await ElMessageBox.confirm(
      `确认删除教师「${row.username}」？其负责班级将解除绑定。`,
      '删除教师',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await teacherAdminApi.delete(row.id)
    ElMessage.success('教师已删除')
    await loadTeachers()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '删除失败')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadTeachers(), loadClasses()])
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

.muted {
  color: #9ca3af;
}
</style>
