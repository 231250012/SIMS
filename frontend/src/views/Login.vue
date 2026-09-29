<template>
  <div class="login-page">
    <div class="bg-layer" aria-hidden="true">
      <div class="blob blob-a" />
      <div class="blob blob-b" />
      <div class="blob blob-c" />
      <div class="blob blob-d" />
      <div class="grid-fade" />
    </div>

    <header class="top-nav">
      <div class="brand">
        <span class="brand-mark">S</span>
        <span class="brand-name">SIMS</span>
      </div>
      <nav class="nav-links">
        <a href="#features">产品能力</a>
        <a href="#auth-card">成绩中心</a>
        <a href="#auth-card">消息通知</a>
      </nav>
      <div class="nav-actions">
        <button type="button" class="btn-ghost" @click="switchMode('login')">登录</button>
        <button type="button" class="btn-dark" @click="switchMode('register')">注册账号</button>
      </div>
    </header>

    <main class="hero">
      <div class="hero-copy">
        <button type="button" class="announce-pill" @click="scrollToCard">
          <span class="pill-dot" />
          面向师生的一站式学业工作台
          <span class="pill-chevron">›</span>
        </button>

        <h1 class="hero-title">
          从成绩录入<br />
          到消息触达。<br />
          <em>全在一处。</em>
        </h1>

        <p class="hero-sub">
          SIMS 帮助学生、教师与管理员协同管理班级、成绩与站内通知——
          录入即同步，排行实时可见，重要变更不再错过。
        </p>

        <ul id="features" class="feature-list">
          <li>
            <strong>双科成绩</strong>
            <span>C 语言与英语四级一站管理</span>
          </li>
          <li>
            <strong>实时排行</strong>
            <span>Redis 驱动的班级科目榜</span>
          </li>
          <li>
            <strong>变更通知</strong>
            <span>Kafka 异步投递站内消息</span>
          </li>
        </ul>
      </div>

      <section id="auth-card" class="login-shell">
        <div class="login-card" :class="{ 'is-register': authMode === 'register' }">
          <Transition name="mode-fade" mode="out-in">
            <!-- 登录表单 -->
            <div v-if="authMode === 'login'" key="login" class="mode-block">
              <div class="card-eyebrow">欢迎回来</div>
              <h2 class="card-title">登录你的工作区</h2>
              <p class="card-desc">使用账号进入对应角色的仪表盘</p>

              <el-form
                ref="loginFormRef"
                :model="loginForm"
                :rules="loginRules"
                class="login-form"
                @submit.prevent="handleLogin"
              >
                <el-form-item prop="username">
                  <el-input
                    v-model="loginForm.username"
                    placeholder="用户名"
                    :prefix-icon="User"
                    clearable
                    size="large"
                  />
                </el-form-item>

                <el-form-item prop="password">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="密码"
                    :prefix-icon="Lock"
                    show-password
                    clearable
                    size="large"
                    @keyup.enter="handleLogin"
                  />
                </el-form-item>

                <button type="button" class="btn-cta" :disabled="loading" @click="handleLogin">
                  <span v-if="!loading">进入系统</span>
                  <span v-else>正在验证…</span>
                  <span class="cta-arrow">→</span>
                </button>
              </el-form>

              <p class="card-micro">安全会话 · JWT + Redis · 角色自动跳转</p>

              <div class="demo-strip">
                <span class="demo-label">快速体验</span>
                <div class="demo-chips">
                  <button type="button" class="chip" @click="quickLogin('admin', 'admin123')">管理员</button>
                  <button type="button" class="chip" @click="quickLogin('teacher1', 'teacher123')">教师</button>
                  <button type="button" class="chip" @click="quickLogin('student1', 'student123')">学生</button>
                </div>
              </div>

              <p class="card-footer">
                还没有账户？
                <a href="#" @click.prevent="switchMode('register')">创建新账户</a>
              </p>
            </div>

            <!-- 注册表单：同一卡片位置，仅字段不同 -->
            <div v-else key="register" class="mode-block">
              <div class="card-eyebrow">加入 SIMS</div>
              <h2 class="card-title">创建你的工作区</h2>
              <p class="card-desc">填写信息后即可按角色进入系统</p>

              <el-form
                ref="registerFormRef"
                :model="registerForm"
                :rules="registerRules"
                class="login-form register-form"
                @submit.prevent="handleRegister"
              >
                <el-form-item prop="username">
                  <el-input
                    v-model="registerForm.username"
                    placeholder="用户名（3–50 字符）"
                    :prefix-icon="User"
                    clearable
                    size="large"
                  />
                </el-form-item>

                <el-form-item prop="role">
                  <el-select
                    v-model="registerForm.role"
                    placeholder="选择角色"
                    size="large"
                    style="width: 100%"
                    @change="handleRoleChange"
                  >
                    <el-option label="学生" value="STUDENT" />
                    <el-option label="教师" value="TEACHER" />
                  </el-select>
                </el-form-item>

                <el-form-item prop="password">
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="密码（至少 6 位）"
                    :prefix-icon="Lock"
                    show-password
                    clearable
                    size="large"
                  />
                </el-form-item>

                <el-form-item prop="confirmPassword">
                  <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="确认密码"
                    :prefix-icon="Lock"
                    show-password
                    clearable
                    size="large"
                  />
                </el-form-item>

                <template v-if="registerForm.role === 'STUDENT'">
                  <el-form-item prop="studentName">
                    <el-input
                      v-model="registerForm.studentName"
                      placeholder="真实姓名"
                      :prefix-icon="User"
                      clearable
                      size="large"
                    />
                  </el-form-item>

                  <el-form-item prop="age">
                    <el-input-number
                      v-model="registerForm.age"
                      :min="16"
                      :max="100"
                      size="large"
                      controls-position="right"
                      style="width: 100%"
                      placeholder="年龄"
                    />
                  </el-form-item>

                  <el-form-item prop="classId">
                    <el-select
                      v-model="registerForm.classId"
                      placeholder="所属班级"
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

                <template v-else-if="registerForm.role === 'TEACHER'">
                  <el-form-item prop="classId">
                    <el-select
                      v-model="registerForm.classId"
                      placeholder="选择管理班级"
                      size="large"
                      :loading="classesLoading"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="cls in classes"
                        :key="cls.id"
                        :label="formatTeacherClassLabel(cls)"
                        :value="cls.id"
                      />
                    </el-select>
                    <p class="field-tip">
                      若所选班级已有负责教师，注册后将由你接手管理该班。
                    </p>
                  </el-form-item>

                  <el-form-item label="是否班主任">
                    <div class="homeroom-row">
                      <el-switch v-model="registerForm.isHomeroomTeacher" />
                      <span class="homeroom-hint">
                        {{ registerForm.isHomeroomTeacher
                          ? '班主任可管理学生资料与成绩'
                          : '非班主任仅可修改学生成绩' }}
                      </span>
                    </div>
                  </el-form-item>
                </template>

                <button type="button" class="btn-cta" :disabled="loading" @click="handleRegister">
                  <span v-if="!loading">完成注册</span>
                  <span v-else>提交中…</span>
                  <span class="cta-arrow">→</span>
                </button>
              </el-form>

              <p class="card-micro">注册后将自动登录并跳转至角色首页</p>

              <p class="card-footer">
                已有账户？
                <a href="#" @click.prevent="switchMode('login')">返回登录</a>
              </p>
            </div>
          </Transition>
        </div>

        <p class="play-hint" aria-hidden="true">
          <svg class="scribble" viewBox="0 0 48 36" fill="none">
            <path
              d="M4 8c12 2 20 18 28 20M32 28c4-2 8-6 12-10"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linecap="round"
            />
          </svg>
          {{ authMode === 'login' ? '点个演示账号试试 ;-)' : '教师注册需选择管理班级 ;-)' }}
        </p>
      </section>
    </main>

    <Transition name="toast-pop">
      <div v-if="toast.visible" class="soft-toast" :class="toast.type" role="status">
        <div class="toast-icon">{{ toast.type === 'ok' ? '✓' : toast.type === 'err' ? '!' : '✧' }}</div>
        <div class="toast-body">
          <div class="toast-title">{{ toast.title }}</div>
          <div class="toast-msg">{{ toast.message }}</div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuth } from '../composables/useAuth'
import { authApi } from '../api/auth'
import { RoleRouter, type UserRole } from '../utils/roleRouter'

const router = useRouter()
const route = useRoute()
const { login, register } = useAuth()

type AuthMode = 'login' | 'register'
const authMode = ref<AuthMode>('login')

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const classesLoading = ref(false)
const classes = ref<Array<{ id: number; name: string; grade?: string; teacherName?: string }>>([])

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'STUDENT' as 'TEACHER' | 'STUDENT',
  studentName: '',
  age: undefined as number | undefined,
  classId: undefined as number | undefined,
  isHomeroomTeacher: true
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请填写用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度 3–50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请填写密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') callback(new Error('请再次输入密码'))
  else if (value !== registerForm.password) callback(new Error('两次输入密码不一致'))
  else callback()
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
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  role: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
  studentName: [
    {
      required: true,
      trigger: 'blur',
      validator: (_rule: any, value: any, callback: any) => {
        if (registerForm.role === 'STUDENT' && !value) callback(new Error('学生用户必须填写真实姓名'))
        else callback()
      }
    }
  ],
  age: [
    {
      required: true,
      trigger: 'blur',
      validator: (_rule: any, value: any, callback: any) => {
        if (registerForm.role === 'STUDENT' && !value) callback(new Error('学生用户必须填写年龄'))
        else if (value && (value < 16 || value > 100)) callback(new Error('年龄必须在16-100之间'))
        else callback()
      }
    }
  ],
  classId: [
    {
      required: true,
      trigger: 'change',
      validator: (_rule: any, value: any, callback: any) => {
        if ((registerForm.role === 'STUDENT' || registerForm.role === 'TEACHER') && !value) {
          callback(new Error(registerForm.role === 'TEACHER' ? '请选择管理班级' : '请选择所属班级'))
        } else callback()
      }
    }
  ]
}

const toast = reactive({
  visible: false,
  type: 'ok' as 'ok' | 'err' | 'info',
  title: '',
  message: ''
})
let toastTimer: ReturnType<typeof setTimeout> | undefined

function showToast(type: 'ok' | 'err' | 'info', title: string, message: string) {
  toast.type = type
  toast.title = title
  toast.message = message
  toast.visible = true
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.visible = false
  }, 2800)
}

function syncModeFromRoute() {
  authMode.value = route.path === '/register' || route.query.mode === 'register' ? 'register' : 'login'
}

function switchMode(mode: AuthMode) {
  authMode.value = mode
  const path = mode === 'register' ? '/register' : '/login'
  if (route.path !== path) {
    router.replace(path)
  }
  scrollToCard()
}

const scrollToCard = () => {
  document.getElementById('auth-card')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const formatClassLabel = (cls: { name: string; grade?: string; teacherName?: string }) => {
  const parts = [cls.name]
  if (cls.grade) parts.push(cls.grade)
  if (cls.teacherName) parts.push(`班主任：${cls.teacherName}`)
  return parts.join(' · ')
}

const formatTeacherClassLabel = (cls: { name: string; grade?: string; teacherName?: string }) => {
  const parts = [cls.name]
  if (cls.grade) parts.push(cls.grade)
  if (cls.teacherName) parts.push(`已有教师：${cls.teacherName}`)
  else parts.push('可注册')
  return parts.join(' · ')
}

const loadClasses = async () => {
  try {
    classesLoading.value = true
    classes.value = await authApi.getRegisterClasses()
  } catch {
    classes.value = []
  } finally {
    classesLoading.value = false
  }
}

const handleRoleChange = () => {
  registerForm.studentName = ''
  registerForm.age = undefined
  registerForm.classId = undefined
  if (registerForm.role === 'TEACHER') {
    registerForm.isHomeroomTeacher = true
  }
  if (registerForm.role === 'STUDENT' || registerForm.role === 'TEACHER') {
    loadClasses()
  }
}

watch(authMode, (mode) => {
  if (
    mode === 'register' &&
    (registerForm.role === 'STUDENT' || registerForm.role === 'TEACHER') &&
    classes.value.length === 0
  ) {
    loadClasses()
  }
})

onMounted(() => {
  syncModeFromRoute()
  if (
    authMode.value === 'register' &&
    (registerForm.role === 'STUDENT' || registerForm.role === 'TEACHER')
  ) {
    loadClasses()
  }
})

onUnmounted(() => {
  if (toastTimer) clearTimeout(toastTimer)
})

watch(() => route.path, syncModeFromRoute)

const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
    loading.value = true
    showToast('info', '正在登录', '正在核对身份，马上带你进入工作台…')
    const ok = await login(loginForm.username, loginForm.password, { silent: true })
    if (ok) showToast('ok', '欢迎回来', '身份已确认，正在进入你的仪表盘')
    else showToast('err', '没能登录', '用户名或密码好像不对，再试一次吧')
  } catch {
    showToast('err', '再看一眼表单', '请先把用户名和密码填写完整')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
    loading.value = true
    showToast('info', '正在创建账户', '稍等片刻，我们在为你准备工作区…')

    const registerData = {
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      role: registerForm.role,
      ...(registerForm.role === 'STUDENT' && {
        studentName: registerForm.studentName,
        age: registerForm.age,
        classId: registerForm.classId
      }),
      ...(registerForm.role === 'TEACHER' && {
        classId: registerForm.classId,
        isHomeroomTeacher: registerForm.isHomeroomTeacher
      })
    }

    const success = await register(registerData, { silent: true })
    if (success) {
      showToast('ok', '注册成功', '欢迎加入 SIMS，正在进入你的首页')
      router.push(RoleRouter.getDefaultRoute(registerData.role as UserRole))
    } else {
      showToast('err', '注册未成功', '可能是用户名已被占用，换一个试试')
    }
  } catch {
    showToast('err', '再检查一下', '请把必填项填写完整后再提交')
  } finally {
    loading.value = false
  }
}

const quickLogin = (username: string, password: string) => {
  loginForm.username = username
  loginForm.password = password
  let roleText = '学生'
  if (username === 'admin') roleText = '管理员'
  else if (username.startsWith('teacher')) roleText = '教师'
  showToast('info', `已选中${roleText}`, '账号已填入，点「进入系统」即可体验')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,500;9..144,700&display=swap');

.login-page {
  --billow-ink: #0a1128;
  --billow-blue: #0066ff;
  --billow-slate: #4a5568;
  --billow-muted: #6b7280;
  --billow-line: #e5e7eb;

  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  color: var(--billow-ink);
  background: #ffffff;
  font-family: 'Plus Jakarta Sans', 'PingFang SC', system-ui, sans-serif;
}

.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(72px);
  opacity: 0.72;
}

.blob-a {
  width: 520px;
  height: 520px;
  left: -120px;
  bottom: -160px;
  background: radial-gradient(circle, #67e8f9 0%, #38bdf8 40%, transparent 70%);
}

.blob-b {
  width: 560px;
  height: 560px;
  right: -140px;
  bottom: -180px;
  background: radial-gradient(circle, #60a5fa 0%, #2563eb 45%, transparent 70%);
}

.blob-c {
  width: 360px;
  height: 360px;
  left: 35%;
  bottom: -80px;
  background: radial-gradient(circle, #a5f3fc 0%, #7dd3fc 50%, transparent 70%);
  opacity: 0.55;
}

.blob-d {
  width: 280px;
  height: 280px;
  right: 22%;
  top: 12%;
  background: radial-gradient(circle, #dbeafe 0%, transparent 70%);
  opacity: 0.5;
  filter: blur(60px);
}

.grid-fade {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(10, 17, 40, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(10, 17, 40, 0.03) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 80% 60% at 50% 40%, black 20%, transparent 75%);
  opacity: 0.5;
}

.top-nav {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 20px 40px;
  max-width: 1120px;
  margin: 0 auto;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-mark {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: var(--billow-ink);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 14px;
}

.brand-name {
  font-weight: 700;
  font-size: 18px;
  letter-spacing: -0.02em;
}

.nav-links {
  display: flex;
  gap: 28px;
}

.nav-links a {
  color: var(--billow-slate);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
}

.nav-links a:hover {
  color: var(--billow-ink);
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-ghost,
.btn-dark {
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  border-radius: 9999px;
  padding: 10px 18px;
}

.btn-ghost {
  background: transparent;
  color: var(--billow-ink);
}

.btn-ghost:hover {
  background: rgba(10, 17, 40, 0.05);
}

.btn-dark {
  background: var(--billow-ink);
  color: #fff;
}

.btn-dark:hover {
  background: #15203f;
}

.hero {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 48px;
  align-items: start;
  max-width: 1120px;
  margin: 0 auto;
  padding: 48px 40px 80px;
}

.announce-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--billow-line);
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(8px);
  color: var(--billow-slate);
  font-size: 13px;
  font-weight: 500;
  padding: 8px 14px;
  border-radius: 9999px;
  cursor: pointer;
  margin-bottom: 28px;
  font-family: inherit;
}

.announce-pill:hover {
  border-color: #bfdbfe;
  color: var(--billow-ink);
}

.pill-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--billow-blue);
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.15);
}

.pill-chevron {
  color: var(--billow-muted);
}

.hero-title {
  margin: 0 0 20px;
  font-family: 'Fraunces', 'Times New Roman', serif;
  font-size: clamp(40px, 5vw, 56px);
  font-weight: 700;
  line-height: 1.12;
  letter-spacing: -0.03em;
  color: var(--billow-ink);
}

.hero-title em {
  font-style: italic;
  font-weight: 500;
}

.hero-sub {
  margin: 0 0 28px;
  max-width: 440px;
  font-size: 16px;
  line-height: 1.65;
  color: var(--billow-slate);
}

.feature-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 14px;
}

.feature-list li {
  display: grid;
  gap: 2px;
}

.feature-list strong {
  font-size: 14px;
  font-weight: 700;
  color: var(--billow-ink);
}

.feature-list span {
  font-size: 13px;
  color: var(--billow-muted);
  line-height: 1.5;
}

.login-shell {
  position: relative;
}

.login-card {
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 28px;
  padding: 32px 28px 28px;
  box-shadow:
    0 4px 6px rgba(10, 17, 40, 0.02),
    0 24px 48px rgba(37, 99, 235, 0.12);
  transition: min-height 0.25s ease;
}

.login-card.is-register {
  max-height: min(78vh, 720px);
  overflow-y: auto;
}

.mode-block {
  width: 100%;
}

.mode-fade-enter-active,
.mode-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.mode-fade-enter-from,
.mode-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.card-eyebrow {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--billow-blue);
  margin-bottom: 8px;
}

.card-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--billow-ink);
}

.card-desc {
  margin: 0 0 24px;
  font-size: 14px;
  color: var(--billow-muted);
  line-height: 1.5;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.login-form :deep(.el-input__wrapper),
.login-form :deep(.el-select__wrapper) {
  border-radius: 14px !important;
  box-shadow: 0 0 0 1px var(--billow-line) inset !important;
  background: rgba(255, 255, 255, 0.9);
  min-height: 44px;
}

.login-form :deep(.el-input__wrapper:hover),
.login-form :deep(.el-input__wrapper.is-focus),
.login-form :deep(.el-select__wrapper:hover),
.login-form :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px #93c5fd inset, 0 0 0 3px rgba(0, 102, 255, 0.12) !important;
}

.login-form :deep(.el-input-number) {
  width: 100%;
}

.login-form :deep(.el-input-number .el-input__wrapper) {
  padding-left: 12px;
}

.btn-cta {
  width: 100%;
  margin-top: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  padding: 14px 22px;
  border-radius: 9999px;
  background: linear-gradient(180deg, #3b82f6 0%, #0066ff 55%, #0052cc 100%);
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.35) inset,
    0 10px 28px rgba(0, 102, 255, 0.35);
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
}

.btn-cta:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.05);
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.4) inset,
    0 14px 32px rgba(0, 102, 255, 0.42);
}

.btn-cta:disabled {
  opacity: 0.75;
  cursor: wait;
}

.cta-arrow {
  font-size: 16px;
  transition: transform 0.2s ease;
}

.btn-cta:hover:not(:disabled) .cta-arrow {
  transform: translateX(3px);
}

.card-micro {
  margin: 14px 0 0;
  text-align: center;
  font-size: 12px;
  color: var(--billow-muted);
}

.demo-strip {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid rgba(229, 231, 235, 0.9);
}

.demo-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--billow-muted);
  margin-bottom: 10px;
}

.demo-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  border: 1px solid var(--billow-line);
  background: rgba(255, 255, 255, 0.7);
  color: var(--billow-slate);
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  padding: 8px 14px;
  border-radius: 9999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.chip:hover {
  border-color: #93c5fd;
  color: var(--billow-blue);
  background: #eff6ff;
}

.card-footer {
  margin: 18px 0 0;
  text-align: center;
  font-size: 14px;
  color: var(--billow-muted);
}

.card-footer a {
  color: var(--billow-blue);
  font-weight: 600;
  text-decoration: none;
}

.homeroom-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 40px;
}

.homeroom-hint {
  font-size: 13px;
  color: var(--billow-muted);
  line-height: 1.4;
}

.field-tip {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--billow-muted);
}

.play-hint {
  position: absolute;
  right: -8px;
  bottom: -42px;
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  font-family: 'Fraunces', serif;
  font-style: italic;
  font-size: 14px;
  color: var(--billow-slate);
  transform: rotate(-2deg);
}

.scribble {
  width: 36px;
  height: 28px;
  color: var(--billow-blue);
  transform: rotate(160deg);
}

.soft-toast {
  position: fixed;
  top: 28px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 50;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 280px;
  max-width: 420px;
  padding: 14px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(229, 231, 235, 0.9);
  box-shadow: 0 16px 40px rgba(10, 17, 40, 0.12);
}

.soft-toast.ok .toast-icon {
  background: #dcfce7;
  color: #15803d;
}

.soft-toast.err .toast-icon {
  background: #fee2e2;
  color: #b91c1c;
}

.soft-toast.info .toast-icon {
  background: #dbeafe;
  color: #1d4ed8;
}

.toast-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-weight: 700;
  flex-shrink: 0;
}

.toast-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--billow-ink);
  margin-bottom: 2px;
}

.toast-msg {
  font-size: 13px;
  color: var(--billow-slate);
  line-height: 1.45;
}

.toast-pop-enter-active,
.toast-pop-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}

.toast-pop-enter-from,
.toast-pop-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-12px);
}

@media (max-width: 900px) {
  .nav-links {
    display: none;
  }

  .hero {
    grid-template-columns: 1fr;
    padding: 24px 20px 96px;
    gap: 36px;
  }

  .top-nav {
    padding: 16px 20px;
  }

  .hero-title {
    font-size: 36px;
  }

  .play-hint {
    right: 8px;
    bottom: -36px;
  }
}
</style>
