<template>
  <div class="layout-container">
    <div v-if="isAuthPage" class="auth-layout">
      <router-view />
    </div>

    <div v-else class="app-shell">
      <!-- 柔和蓝青氛围底 -->
      <div class="app-bg" aria-hidden="true">
        <div class="blob blob-a" />
        <div class="blob blob-b" />
        <div class="blob blob-c" />
      </div>

      <header class="top-nav">
        <div class="brand" @click="goHome">
          <span class="brand-mark">S</span>
          <span class="brand-name">SIMS</span>
        </div>

        <nav v-if="isAuthenticated" class="top-nav-menu">
          <NavigationMenu mode="horizontal" />
        </nav>

        <div class="nav-right">
          <span v-if="user?.role" class="role-pill">{{ getRoleName(user.role) }}</span>

          <el-dropdown v-if="isAuthenticated" @command="handleCommand" trigger="click">
            <button type="button" class="user-chip">
              <el-avatar :size="30" class="user-avatar">
                {{ user?.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span v-if="!isMobile" class="username">{{ user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="app-main">
        <div class="page-surface">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { useAuth } from '../composables/useAuth'
import { RoleRouter, type UserRole } from '../utils/roleRouter'
import { ElMessage } from 'element-plus'
import NavigationMenu from './NavigationMenu.vue'

const router = useRouter()
const route = useRoute()
const { isAuthenticated, user, logout } = useAuth()

const isMobile = ref(false)

const isAuthPage = computed(() => ['/login', '/register'].includes(route.path))

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 900
}

const getRoleName = (role: string) => RoleRouter.getRoleName(role as UserRole)

const goHome = () => {
  if (!user.value?.role) return
  router.push(RoleRouter.getDefaultRoute(user.value.role as UserRole))
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      logout()
      ElMessage.success('已退出登录')
      break
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  width: 100%;
  background: #ffffff;
}

.auth-layout {
  width: 100%;
  min-height: 100vh;
  background: #ffffff;
}

.app-shell {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.app-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 48%, #eef6ff 100%);
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.55;
}

.blob-a {
  width: 480px;
  height: 480px;
  left: -140px;
  bottom: -180px;
  background: radial-gradient(circle, #67e8f9 0%, #38bdf8 45%, transparent 70%);
}

.blob-b {
  width: 520px;
  height: 520px;
  right: -160px;
  bottom: -200px;
  background: radial-gradient(circle, #60a5fa 0%, #2563eb 50%, transparent 70%);
}

.blob-c {
  width: 320px;
  height: 320px;
  right: 30%;
  top: -80px;
  background: radial-gradient(circle, #dbeafe 0%, transparent 70%);
  opacity: 0.45;
}

.top-nav {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 14px 28px;
  margin: 12px 16px 0;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(229, 231, 235, 0.85);
  border-radius: 9999px;
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.08);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-mark {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: #0a1128;
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 14px;
}

.brand-name {
  font-weight: 700;
  font-size: 17px;
  letter-spacing: -0.02em;
  color: #0a1128;
}

.top-nav-menu {
  flex: 1;
  min-width: 0;
  display: flex;
  justify-content: center;
  overflow: hidden;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.role-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  color: #1d4ed8;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #e5e7eb;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 9999px;
  padding: 4px 12px 4px 4px;
  cursor: pointer;
  font-family: inherit;
  color: #0a1128;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.user-chip:hover {
  border-color: #93c5fd;
  box-shadow: 0 4px 14px rgba(0, 102, 255, 0.12);
}

.user-avatar {
  background: #0a1128;
  color: #fff;
  font-weight: 600;
}

.username {
  font-size: 13px;
  font-weight: 600;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-main {
  position: relative;
  z-index: 1;
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 20px 20px 32px;
}

.page-surface {
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 120px);
}

:deep(.el-dropdown-menu) {
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 12px 32px rgba(10, 17, 40, 0.1);
  padding: 6px;
}

:deep(.el-dropdown-menu__item) {
  border-radius: 10px;
  margin: 2px 0;
  gap: 8px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: #eff6ff;
  color: #0066ff;
}

@media (max-width: 900px) {
  .top-nav {
    margin: 8px;
    padding: 10px 12px;
    border-radius: 22px;
    flex-wrap: wrap;
  }

  .top-nav-menu {
    order: 3;
    width: 100%;
    justify-content: flex-start;
    overflow-x: auto;
  }

  .brand-name {
    display: none;
  }

  .role-pill {
    display: none;
  }

  .app-main {
    padding: 12px 12px 24px;
  }
}
</style>
