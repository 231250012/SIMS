import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { RoleRouter, type UserRole } from '../utils/roleRouter'

export function useAuth() {
  const router = useRouter()
  const userStore = useUserStore()

  // Computed properties for easy access
  const isAuthenticated = computed(() => userStore.isAuthenticated)
  const user = computed(() => userStore.user)
  const isAdmin = computed(() => userStore.isAdmin)
  const isTeacher = computed(() => userStore.isTeacher)
  const isStudent = computed(() => userStore.isStudent)
  const isHomeroomTeacher = computed(() => userStore.isHomeroomTeacher)
  const canManageStudentProfile = computed(() => userStore.canManageStudentProfile)

  // Login function with error handling and role-based redirect
  const login = async (
    username: string,
    password: string,
    options?: { silent?: boolean }
  ) => {
    try {
      await userStore.login(username, password)
      if (!options?.silent) {
        ElMessage.success('登录成功')
      }
      
      // Get role-specific default route
      const defaultRoute = RoleRouter.getDefaultRoute(userStore.user!.role as UserRole)
      
      // Navigate to role-specific dashboard
      router.push(defaultRoute)
      
      return true
    } catch (error: any) {
      if (!options?.silent) {
        const errorMessage = error?.response?.data?.message || error?.message || '登录失败'
        ElMessage.error(errorMessage)
      }
      return false
    }
  }

  // Register function with error handling
  const register = async (registerData: any, options?: { silent?: boolean }) => {
    try {
      await userStore.register(registerData)
      if (!options?.silent) {
        ElMessage.success('注册成功')
      }
      return true
    } catch (error: any) {
      if (!options?.silent) {
        const errorMessage = error?.response?.data?.message || error?.message || '注册失败'
        ElMessage.error(errorMessage)
      }
      return false
    }
  }

  // Logout function with navigation
  const logout = async () => {
    try {
      // Call logout API if available
      await userStore.logout()
    } catch (error) {
      // Even if API call fails, still logout locally
      console.error('Logout API call failed:', error)
    } finally {
      // Always clear local state and redirect
      userStore.logout()
      router.push('/login')
    }
  }

  // Check if user has specific role
  const hasRole = (role: string): boolean => {
    return user.value?.role === role
  }

  // Check if user has any of the specified roles
  const hasAnyRole = (roles: string[]): boolean => {
    return roles.includes(user.value?.role || '')
  }

  // Require authentication - redirect to login if not authenticated
  const requireAuth = () => {
    if (!isAuthenticated.value) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return false
    }
    return true
  }

  // Require specific role - show error if insufficient permissions
  const requireRole = (role: string) => {
    if (!requireAuth()) return false
    
    if (!hasRole(role)) {
      ElMessage.error('权限不足')
      return false
    }
    return true
  }

  // Require any of the specified roles
  const requireAnyRole = (roles: string[]) => {
    if (!requireAuth()) return false
    
    if (!hasAnyRole(roles)) {
      ElMessage.error('权限不足')
      return false
    }
    return true
  }

  return {
    // State
    isAuthenticated,
    user,
    isAdmin,
    isTeacher,
    isStudent,
    isHomeroomTeacher,
    canManageStudentProfile,
    
    // Actions
    login,
    register,
    logout,
    
    // Utilities
    hasRole,
    hasAnyRole,
    requireAuth,
    requireRole,
    requireAnyRole
  }
}