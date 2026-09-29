<template>
  <div class="auth-page">
    <div class="auth-panel">
      <h1 class="auth-title">注册账户</h1>
      <p class="auth-desc">学生信息管理系统</p>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="auth-form"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username" label="用户名">
          <el-input
            v-model="registerForm.username"
            placeholder="3-50 个字符"
            :prefix-icon="User"
            clearable
            size="large"
          />
        </el-form-item>

        <el-form-item prop="role" label="用户角色">
          <el-select
            v-model="registerForm.role"
            placeholder="请选择角色"
            size="large"
            style="width: 100%"
            @change="handleRoleChange"
          >
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item prop="password" label="密码">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="至少 6 个字符"
            :prefix-icon="Lock"
            show-password
            clearable
            size="large"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword" label="确认密码">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
            size="large"
          />
        </el-form-item>

        <template v-if="registerForm.role === 'STUDENT'">
          <el-form-item prop="studentName" label="真实姓名">
            <el-input
              v-model="registerForm.studentName"
              placeholder="请输入真实姓名"
              :prefix-icon="User"
              clearable
              size="large"
            />
          </el-form-item>

          <el-form-item prop="age" label="年龄">
            <el-input-number
              v-model="registerForm.age"
              :min="16"
              :max="100"
              size="large"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item prop="classId" label="所属班级">
            <el-select
              v-model="registerForm.classId"
              placeholder="请选择班级"
              size="large"
              :loading="classesLoading"
              style="width: 100%"
            >
              <el-option
                v-for="cls in classes"
                :key="cls.id"
                :label="formatClassLabel(cls)"
                :value="cls.id"
              />
            </el-select>
          </el-form-item>
        </template>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="full-btn"
            :loading="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-links">
        <span>已有账户？</span>
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuth } from '../composables/useAuth'
import { authApi } from '../api/auth'
import { RoleRouter, type UserRole } from '../utils/roleRouter'

const router = useRouter()
const { register } = useAuth()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const classesLoading = ref(false)
const classes = ref<Array<{ id: number; name: string; grade?: string; teacherName?: string }>>([])

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'STUDENT' as 'ADMIN' | 'TEACHER' | 'STUDENT',
  studentName: '',
  age: undefined as number | undefined,
  classId: undefined as number | undefined
})

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ],
  studentName: [
    {
      required: true,
      trigger: 'blur',
      validator: (_rule: any, value: any, callback: any) => {
        if (registerForm.role === 'STUDENT' && !value) {
          callback(new Error('学生用户必须填写真实姓名'))
        } else {
          callback()
        }
      }
    }
  ],
  age: [
    {
      required: true,
      trigger: 'blur',
      validator: (_rule: any, value: any, callback: any) => {
        if (registerForm.role === 'STUDENT' && !value) {
          callback(new Error('学生用户必须填写年龄'))
        } else if (value && (value < 16 || value > 100)) {
          callback(new Error('年龄必须在16-100之间'))
        } else {
          callback()
        }
      }
    }
  ],
  classId: [
    {
      required: true,
      trigger: 'change',
      validator: (_rule: any, value: any, callback: any) => {
        if (registerForm.role === 'STUDENT' && !value) {
          callback(new Error('请选择所属班级'))
        } else {
          callback()
        }
      }
    }
  ]
}

const formatClassLabel = (cls: { name: string; grade?: string; teacherName?: string }) => {
  const parts = [cls.name]
  if (cls.grade) parts.push(cls.grade)
  if (cls.teacherName) parts.push(`班主任：${cls.teacherName}`)
  return parts.join(' · ')
}

const handleRoleChange = () => {
  if (registerForm.role !== 'STUDENT') {
    registerForm.studentName = ''
    registerForm.age = undefined
    registerForm.classId = undefined
    return
  }
  if (classes.value.length === 0) {
    loadClasses()
  }
}

const loadClasses = async () => {
  try {
    classesLoading.value = true
    classes.value = await authApi.getRegisterClasses()
  } catch (error) {
    console.error('Failed to load classes:', error)
    classes.value = []
  } finally {
    classesLoading.value = false
  }
}

onMounted(() => {
  if (registerForm.role === 'STUDENT') {
    loadClasses()
  }
})

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    const registerData = {
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      role: registerForm.role,
      ...(registerForm.role === 'STUDENT' && {
        studentName: registerForm.studentName,
        age: registerForm.age,
        classId: registerForm.classId
      })
    }

    const success = await register(registerData)
    if (success) {
      router.push(RoleRouter.getDefaultRoute(registerData.role as UserRole))
    }
  } catch (error: any) {
    console.error('Registration failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 32px 16px;
  background: transparent;
  overflow-y: auto;
}

.auth-panel {
  width: 100%;
  max-width: 440px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: 32px 28px;
  margin: auto 0;
  box-shadow: var(--shadow-shell);
}

.auth-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-text-primary);
  text-align: center;
}

.auth-desc {
  margin: 0 0 24px;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 15px;
  line-height: 1.6;
}

.full-btn {
  width: 100%;
  height: 44px;
}

.auth-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  color: var(--color-text-muted);
  font-size: 14px;
}

.auth-links a {
  color: var(--color-text-link);
  font-weight: 600;
}
</style>
