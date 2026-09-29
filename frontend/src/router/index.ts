import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { RoleRouter, type UserRole } from '../utils/roleRouter'

// Import components
const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Login.vue')
const Dashboard = () => import('../views/Dashboard.vue')
const StudentList = () => import('../views/student/StudentList.vue')
const StudentDetail = () => import('../views/student/StudentDetail.vue')
const StudentForm = () => import('../views/student/StudentForm.vue')
const Statistics = () => import('../views/Statistics.vue')
const AdminTeachers = () => import('../views/admin/AdminTeachers.vue')
const AdminClasses = () => import('../views/admin/AdminClasses.vue')

// Role-specific components
const TeacherDashboard = () => import('../views/teacher/TeacherDashboard.vue')
const TeacherClasses = () => import('../views/teacher/TeacherClasses.vue')
const TeacherClassDetail = () => import('../views/teacher/TeacherClassDetail.vue')
const StudentDashboard = () => import('../views/student/StudentDashboard.vue')
const StudentGrades = () => import('../views/student/StudentGrades.vue')
const StudentNotifications = () => import('../views/student/StudentNotifications.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: () => {
      // Will be handled by navigation guard
      return '/dashboard'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false }
  },
  
  // Admin routes
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/students',
    name: 'StudentList',
    component: StudentList,
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'] }
  },
  {
    path: '/students/:id',
    name: 'StudentDetail',
    component: StudentDetail,
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'] }
  },
  {
    path: '/students/:id/edit',
    name: 'StudentEdit',
    component: StudentForm,
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'] }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: Statistics,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/teachers',
    name: 'AdminTeachers',
    component: AdminTeachers,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/classes',
    name: 'AdminClasses',
    component: AdminClasses,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  
  // Teacher routes
  {
    path: '/teacher/dashboard',
    name: 'TeacherDashboard',
    component: TeacherDashboard,
    meta: { requiresAuth: true, roles: ['TEACHER'] }
  },
  {
    path: '/teacher/classes',
    name: 'TeacherClasses',
    component: TeacherClasses,
    meta: { requiresAuth: true, roles: ['TEACHER'] }
  },
  {
    path: '/teacher/classes/:id',
    name: 'TeacherClassDetail',
    component: TeacherClassDetail,
    meta: { requiresAuth: true, roles: ['TEACHER'] }
  },
  
  // Student routes
  {
    path: '/student/dashboard',
    name: 'StudentDashboard',
    component: StudentDashboard,
    meta: { requiresAuth: true, roles: ['STUDENT'] }
  },
  {
    path: '/student/grades',
    name: 'StudentGrades',
    component: StudentGrades,
    meta: { requiresAuth: true, roles: ['STUDENT'] }
  },
  {
    path: '/student/notifications',
    name: 'StudentNotifications',
    component: StudentNotifications,
    meta: { requiresAuth: true, roles: ['STUDENT'] }
  },
  
  // Common routes
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
  },
  
  // Catch-all route for 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: () => {
      // Redirect to appropriate dashboard based on user role
      return '/dashboard'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard for authentication and role-based routing
router.beforeEach(async (to, _from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false
  
  // Check if route is public (doesn't require authentication)
  if (RoleRouter.isPublicRoute(to.path) || !requiresAuth) {
    // If user is authenticated and trying to access login/register, redirect to dashboard
    if (token && (to.path === '/login' || to.path === '/register')) {
      try {
        const { useUserStore } = await import('../stores/user')
        const userStore = useUserStore()
        
        if (userStore.isTokenValid() && userStore.user) {
          const defaultRoute = RoleRouter.getDefaultRoute(userStore.user.role as UserRole)
          next(defaultRoute)
          return
        }
      } catch (error) {
        // If token validation fails, clear it and continue
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    }
    next()
    return
  }
  
  // Route requires authentication
  if (!token) {
    next('/login')
    return
  }
  
  // Validate token and check permissions for protected routes
  try {
    const { useUserStore } = await import('../stores/user')
    const userStore = useUserStore()
    
    // Validate token
    if (!userStore.isTokenValid()) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      next('/login')
      return
    }
    
    // Ensure user data is loaded
    if (!userStore.user) {
      await userStore.fetchCurrentUser()
    }
    
    const userRole = userStore.user?.role as UserRole
    
    // Handle root path and dashboard redirects
    if (to.path === '/' || (to.path === '/dashboard' && userRole !== 'ADMIN')) {
      const defaultRoute = RoleRouter.getDefaultRoute(userRole)
      next(defaultRoute)
      return
    }
    
    // Check if user has permission to access this route
    if (!RoleRouter.canAccessRoute(userRole, to.path)) {
      // Log unauthorized access attempt
      RoleRouter.logUnauthorizedAccess(userRole, to.path)
      
      // Get appropriate error message
      const errorMessage = RoleRouter.getPermissionErrorMessage(userRole, to.path)
      
      // Redirect to role-appropriate dashboard
      const defaultRoute = RoleRouter.getDefaultRoute(userRole)
      next(defaultRoute)
      
      // Show permission denied message
      const { ElMessage } = await import('element-plus')
      ElMessage.warning(errorMessage)
      return
    }
    
    // Additional role-specific route validation
    const requiredRoles = to.meta.roles as string[] | undefined
    if (requiredRoles && !requiredRoles.includes(userRole)) {
      console.warn(`User ${userRole} attempted to access route requiring roles: ${requiredRoles.join(', ')}`)
      
      const defaultRoute = RoleRouter.getDefaultRoute(userRole)
      next(defaultRoute)
      
      const { ElMessage } = await import('element-plus')
      ElMessage.error('您没有权限访问该页面')
      return
    }
    
    // All checks passed, allow navigation
    next()
  } catch (error) {
    console.error('Navigation guard error:', error)
    
    // Clear invalid session data
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    
    // Redirect to login
    next('/login')
    
    // Show error message
    const { ElMessage } = await import('element-plus')
    ElMessage.error('会话已过期，请重新登录')
  }
})

export default router