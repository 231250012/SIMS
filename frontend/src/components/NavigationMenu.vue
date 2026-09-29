<template>
  <el-menu
    :default-active="activeMenu"
    class="navigation-menu"
    :class="{ 'is-horizontal': mode === 'horizontal' }"
    :mode="mode"
    router
    :ellipsis="false"
    :background-color="menuTheme.backgroundColor"
    :text-color="menuTheme.textColor"
    :active-text-color="menuTheme.activeTextColor"
    :collapse="mode === 'vertical' && isCollapsed"
    :collapse-transition="false"
  >
    <template v-for="item in navigationItems" :key="item.path">
      <el-menu-item
        v-if="!item.children || item.children.length === 0"
        :index="item.path"
        @click="handleMenuClick(item)"
      >
        <el-icon v-if="mode === 'vertical'">
          <component :is="getIconComponent(item.icon)" />
        </el-icon>
        <template #title>
          <span>{{ item.title }}</span>
          <el-badge
            v-if="item.badge"
            :value="item.badge.value"
            :type="item.badge.type"
            class="menu-badge"
          />
        </template>
      </el-menu-item>

      <el-sub-menu v-else :index="item.path">
        <template #title>
          <el-icon v-if="mode === 'vertical'">
            <component :is="getIconComponent(item.icon)" />
          </el-icon>
          <span>{{ item.title }}</span>
          <el-badge
            v-if="item.badge"
            :value="item.badge.value"
            :type="item.badge.type"
            class="menu-badge"
          />
        </template>

        <el-menu-item
          v-for="child in item.children"
          :key="child.path"
          :index="child.path"
          @click="handleMenuClick(child)"
        >
          <span>{{ child.title }}</span>
        </el-menu-item>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  House, User, DataLine, Odometer, School, TrendCharts,
  Management, Document, UserFilled, Setting, Menu as MenuIcon,
  Fold, Expand, Bell, Message, Monitor
} from '@element-plus/icons-vue'
import { useAuth } from '../composables/useAuth'
import { RoleRouter, type UserRole } from '../utils/roleRouter'
import { notificationApi } from '../api/notification'

interface MenuBadge {
  value: string | number
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
}

interface NavigationItem {
  title: string
  path: string
  icon: string
  roles: string[]
  children?: NavigationItem[]
  badge?: MenuBadge
  hidden?: boolean
  disabled?: boolean
}

interface MenuTheme {
  backgroundColor: string
  textColor: string
  activeTextColor: string
}

const props = withDefaults(defineProps<{
  isCollapsed?: boolean
  theme?: 'light' | 'dark'
  mode?: 'vertical' | 'horizontal'
}>(), {
  isCollapsed: false,
  theme: 'light',
  mode: 'vertical'
})

const route = useRoute()
const router = useRouter()
const { user } = useAuth()

const unreadCount = ref(0)
let unreadPollTimer: ReturnType<typeof setInterval> | null = null

async function refreshUnreadCount() {
  if (user.value?.role !== 'STUDENT') {
    unreadCount.value = 0
    return
  }
  try {
    unreadCount.value = (await notificationApi.unreadCount()).count
  } catch {
    // ignore
  }
}

function onUnreadEvent(e: CustomEvent<number>) {
  if (typeof e.detail === 'number') unreadCount.value = e.detail
}

onMounted(() => {
  refreshUnreadCount()
  if (user.value?.role === 'STUDENT') {
    unreadPollTimer = setInterval(refreshUnreadCount, 30000)
  }
  window.addEventListener('notifications:unread', onUnreadEvent as EventListener)
})

onUnmounted(() => {
  if (unreadPollTimer) clearInterval(unreadPollTimer)
  window.removeEventListener('notifications:unread', onUnreadEvent as EventListener)
})

const iconComponents = {
  House, User, DataLine, Odometer, School, TrendCharts,
  Management, Document, UserFilled, Setting, MenuIcon,
  Fold, Expand, Bell, Message, Monitor
}

const getIconComponent = (iconName: string) =>
  iconComponents[iconName as keyof typeof iconComponents] || House

const menuTheme = computed<MenuTheme>(() => ({
  backgroundColor: 'transparent',
  textColor: '#4a5568',
  activeTextColor: '#0066ff'
}))

const activeMenu = computed(() => route.path)

const navigationItems = computed(() => {
  if (!user.value?.role) return []
  const userRole = user.value.role as UserRole
  return enhanceNavigationItems(RoleRouter.getNavigationItems(userRole), unreadCount.value)
})

function enhanceNavigationItems(items: any[], unread: number): NavigationItem[] {
  return items.map(item => {
    const enhancedItem: NavigationItem = {
      ...item,
      children: [],
      badge: undefined,
      hidden: false,
      disabled: false
    }
    if (item.path === '/student/notifications' && unread > 0) {
      enhancedItem.badge = {
        value: unread > 99 ? '99+' : unread,
        type: 'danger'
      }
    }
    return enhancedItem
  }).filter(item => !item.hidden)
}

function handleMenuClick(item: NavigationItem) {
  if (item.disabled) return
  if (item.path.startsWith('http')) {
    window.open(item.path, '_blank')
    return
  }
  if (item.path !== route.path) router.push(item.path)
}
</script>

<style scoped>
.navigation-menu {
  border: none !important;
  background: transparent !important;
  width: 100%;
}

.navigation-menu.is-horizontal {
  display: flex;
  justify-content: center;
  height: 44px;
}

.menu-badge {
  margin-left: 6px;
}

:deep(.el-menu--horizontal) {
  border-bottom: none !important;
}

:deep(.el-menu--horizontal > .el-menu-item),
:deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title) {
  height: 40px !important;
  line-height: 40px !important;
  margin: 0 2px;
  padding: 0 14px !important;
  border-radius: 9999px !important;
  border-bottom: none !important;
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-menu--horizontal > .el-menu-item:hover),
:deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title:hover) {
  background: #eff6ff !important;
  color: #0066ff !important;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active) {
  background: #eff6ff !important;
  color: #0066ff !important;
  font-weight: 700;
  border-bottom: none !important;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active::after),
:deep(.el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title::after) {
  display: none !important;
}

/* vertical fallback */
:deep(.el-menu-item) {
  border-radius: 12px;
  margin: 4px 8px;
}

:deep(.el-menu-item.is-active) {
  background: #eff6ff !important;
  color: #0066ff !important;
}

@media (max-width: 900px) {
  .navigation-menu.is-horizontal {
    justify-content: flex-start;
  }

  :deep(.el-menu--horizontal > .el-menu-item) {
    padding: 0 12px !important;
    font-size: 13px;
  }
}
</style>
