<template>
  <el-breadcrumb class="breadcrumb-container" separator="/">
    <el-breadcrumb-item 
      v-for="(item, index) in breadcrumbItems" 
      :key="index"
      :to="item.path"
    >
      <el-icon v-if="item.icon" class="breadcrumb-icon">
        <component :is="getIconComponent(item.icon)" />
      </el-icon>
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { 
  House, User, DataLine, Odometer, School, TrendCharts, 
  Management, Document, UserFilled, Setting
} from '@element-plus/icons-vue'
import { useAuth } from '../composables/useAuth'
import type { UserRole } from '../utils/roleRouter'

interface BreadcrumbItem {
  title: string
  path?: string
  icon?: string
}

const route = useRoute()
const { user } = useAuth()

// 图标组件映射
const iconComponents = {
  House,
  User,
  DataLine,
  Odometer,
  School,
  TrendCharts,
  Management,
  Document,
  UserFilled,
  Setting
}

const getIconComponent = (iconName: string) => {
  return iconComponents[iconName as keyof typeof iconComponents] || House
}

// 路由到面包屑的映射
const routeBreadcrumbMap: Record<string, BreadcrumbItem[]> = {
  // 管理员路由
  '/dashboard': [
    { title: '首页', path: '/dashboard', icon: 'House' },
    { title: '系统仪表板', icon: 'Odometer' }
  ],
  '/students': [
    { title: '首页', path: '/dashboard', icon: 'House' },
    { title: '学生管理', icon: 'User' }
  ],
  '/statistics': [
    { title: '首页', path: '/dashboard', icon: 'House' },
    { title: '统计分析', icon: 'TrendCharts' }
  ],
  
  // 教师路由
  '/teacher/dashboard': [
    { title: '首页', path: '/teacher/dashboard', icon: 'House' },
    { title: '教师仪表板', icon: 'Odometer' }
  ],
  '/teacher/classes': [
    { title: '首页', path: '/teacher/dashboard', icon: 'House' },
    { title: '班级管理', icon: 'School' }
  ],
  
  // 学生路由
  '/student/dashboard': [
    { title: '首页', path: '/student/dashboard', icon: 'House' },
    { title: '学生仪表板', icon: 'Odometer' }
  ],
  '/student/grades': [
    { title: '首页', path: '/student/dashboard', icon: 'House' },
    { title: '我的成绩', icon: 'TrendCharts' }
  ],
  '/student/classmates': [
    { title: '首页', path: '/student/dashboard', icon: 'House' },
    { title: '班级同学', icon: 'School' }
  ],
  
  // 通用路由
  '/profile': [
    { title: '首页', icon: 'House' },
    { title: '个人资料', icon: 'Setting' }
  ]
}

// 动态生成面包屑
const breadcrumbItems = computed(() => {
  const currentPath = route.path
  const userRole = user.value?.role as UserRole
  
  // 处理动态路由（如 /students/:id）
  let matchedPath = currentPath
  
  // 检查是否为动态路由
  if (currentPath.includes('/students/') && currentPath !== '/students/new') {
    if (currentPath.endsWith('/edit')) {
      matchedPath = '/students/:id/edit'
    } else {
      matchedPath = '/students/:id'
    }
  } else if (currentPath.includes('/teacher/classes/') && currentPath !== '/teacher/classes') {
    matchedPath = '/teacher/classes/:id'
  }
  
  // 获取基础面包屑
  let breadcrumbs = routeBreadcrumbMap[matchedPath] || []
  
  // 如果没有找到匹配的路由，生成默认面包屑
  if (breadcrumbs.length === 0) {
    breadcrumbs = generateDefaultBreadcrumb(currentPath, userRole)
  }
  
  // 处理动态路由的特殊情况
  if (matchedPath === '/students/:id') {
    const studentId = route.params.id
    breadcrumbs = [
      ...breadcrumbs.slice(0, -1),
      { title: `学生详情 #${studentId}`, icon: 'UserFilled' }
    ]
  } else if (matchedPath === '/students/:id/edit') {
    const studentId = route.params.id
    breadcrumbs = [
      { title: '首页', path: getHomeRoute(userRole), icon: 'House' },
      { title: '学生管理', path: '/students', icon: 'User' },
      { title: `学生详情`, path: `/students/${studentId}`, icon: 'UserFilled' },
      { title: '编辑学生', icon: 'Setting' }
    ]
  } else if (matchedPath === '/teacher/classes/:id') {
    const classId = route.params.id
    breadcrumbs = [
      ...breadcrumbs.slice(0, -1),
      { title: `班级详情 #${classId}`, icon: 'School' }
    ]
  }
  
  return breadcrumbs
})

// 生成默认面包屑
function generateDefaultBreadcrumb(path: string, userRole: UserRole): BreadcrumbItem[] {
  const homeRoute = getHomeRoute(userRole)
  const segments = path.split('/').filter(Boolean)
  
  const breadcrumbs: BreadcrumbItem[] = [
    { title: '首页', path: homeRoute, icon: 'House' }
  ]
  
  // 根据路径段生成面包屑
  let currentPath = ''
  segments.forEach((segment, index) => {
    currentPath += `/${segment}`
    
    if (index === segments.length - 1) {
      // 最后一个段不添加链接
      breadcrumbs.push({
        title: getSegmentTitle(segment),
        icon: getSegmentIcon(segment)
      })
    } else {
      breadcrumbs.push({
        title: getSegmentTitle(segment),
        path: currentPath,
        icon: getSegmentIcon(segment)
      })
    }
  })
  
  return breadcrumbs
}

// 获取角色对应的首页路由
function getHomeRoute(userRole: UserRole): string {
  switch (userRole) {
    case 'ADMIN':
      return '/dashboard'
    case 'TEACHER':
      return '/teacher/dashboard'
    case 'STUDENT':
      return '/student/dashboard'
    default:
      return '/dashboard'
  }
}

// 获取路径段的标题
function getSegmentTitle(segment: string): string {
  const titleMap: Record<string, string> = {
    'teacher': '教师',
    'student': '学生',
    'dashboard': '仪表板',
    'classes': '班级',
    'profile': '个人中心',
    'grades': '成绩',
    'classmates': '同学',
    'students': '学生管理',
    'statistics': '统计',
    'new': '新增',
    'edit': '编辑'
  }
  
  return titleMap[segment] || segment
}

// 获取路径段的图标
function getSegmentIcon(segment: string): string {
  const iconMap: Record<string, string> = {
    'teacher': 'User',
    'student': 'User',
    'dashboard': 'Odometer',
    'classes': 'School',
    'profile': 'User',
    'grades': 'TrendCharts',
    'classmates': 'School',
    'students': 'User',
    'statistics': 'TrendCharts',
    'new': 'UserFilled',
    'edit': 'Setting'
  }
  
  return iconMap[segment] || 'Document'
}
</script>

<style scoped>
.breadcrumb-container {
  padding: 12px 20px;
  background-color: var(--el-bg-color-page);
  border-bottom: 1px solid var(--el-border-color-light);
  font-size: 14px;
}

.breadcrumb-icon {
  margin-right: 4px;
  font-size: 14px;
}

:deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  color: var(--el-text-color-regular);
  font-weight: 400;
}

:deep(.el-breadcrumb__inner:hover) {
  color: var(--el-color-primary);
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--el-text-color-primary);
  font-weight: 500;
}
</style>