import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type AuthResponse, type UserInfo, type RegisterRequest } from '../api/auth'
import type { UserRole } from '../utils/roleRouter'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<UserInfo | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => (user.value?.role as UserRole) === 'ADMIN')
  const isTeacher = computed(() => (user.value?.role as UserRole) === 'TEACHER')
  const isStudent = computed(() => (user.value?.role as UserRole) === 'STUDENT')
  const isHomeroomTeacher = computed(() =>
    isTeacher.value && !!user.value?.isHomeroomTeacher
  )
  /** 可改学生资料：管理员或班主任 */
  const canManageStudentProfile = computed(() => isAdmin.value || isHomeroomTeacher.value)

  // Actions
  const login = async (username: string, password: string): Promise<void> => {
    try {
      const response: AuthResponse = await authApi.login({ username, password })
      
      // Store token and user info
      token.value = response.token
      const userInfo = {
        id: response.userId,
        username: response.username,
        role: response.role,
        isHomeroomTeacher: !!response.isHomeroomTeacher
      }
      user.value = userInfo
      
      // Persist to localStorage
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(userInfo))
    } catch (error) {
      throw error
    }
  }

  const register = async (registerData: RegisterRequest): Promise<void> => {
    try {
      const response: AuthResponse = await authApi.register(registerData)
      
      // Store token and user info
      token.value = response.token
      const userInfo = {
        id: response.userId,
        username: response.username,
        role: response.role,
        isHomeroomTeacher: !!response.isHomeroomTeacher
      }
      user.value = userInfo
      
      // Persist to localStorage
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(userInfo))
    } catch (error) {
      throw error
    }
  }

  const logout = (): void => {
    // Clear state
    token.value = null
    user.value = null
    
    // Clear localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const loadUserFromStorage = (): void => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser && storedUser !== 'undefined' && storedUser !== 'null') {
      token.value = storedToken
      try {
        const parsedUser = JSON.parse(storedUser)
        // 验证用户数据的完整性
        if (parsedUser && parsedUser.id && parsedUser.username && parsedUser.role) {
          user.value = parsedUser
        } else {
          console.warn('Invalid user data structure, clearing storage')
          logout()
        }
      } catch (error) {
        console.error('Failed to parse stored user data:', error)
        logout()
      }
    } else if (storedToken || storedUser) {
      // 如果只有部分数据或数据无效，清除所有存储
      console.warn('Incomplete or invalid stored data, clearing storage')
      logout()
    }
  }

  const fetchCurrentUser = async (): Promise<void> => {
    try {
      const userInfo = await authApi.getCurrentUser()
      user.value = userInfo
      localStorage.setItem('user', JSON.stringify(userInfo))
    } catch (error) {
      console.error('Failed to fetch current user:', error)
      logout()
      throw error
    }
  }

  /** JWT 使用 Base64URL，且常省略 `=` 填充，不能直接 atob */
  const decodeJwtPayload = (tokenValue: string): Record<string, unknown> | null => {
    const parts = tokenValue.split('.')
    if (parts.length !== 3 || !parts[1]) return null

    let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const pad = base64.length % 4
    if (pad === 2) base64 += '=='
    else if (pad === 3) base64 += '='
    else if (pad === 1) return null

    return JSON.parse(atob(base64))
  }

  const isTokenValid = (): boolean => {
    if (!token.value) return false
    
    try {
      const payload = decodeJwtPayload(token.value)
      if (!payload) return false

      const currentTime = Math.floor(Date.now() / 1000)
      if (typeof payload.exp === 'number' && payload.exp < currentTime) {
        logout()
        return false
      }
      
      return true
    } catch (error) {
      console.error('Token validation error:', error)
      logout()
      return false
    }
  }

  const refreshUserData = async (): Promise<void> => {
    if (!isAuthenticated.value) return
    
    try {
      await fetchCurrentUser()
    } catch (error) {
      console.error('Failed to refresh user data:', error)
      // Don't logout here as fetchCurrentUser already handles it
    }
  }

  // Initialize store
  loadUserFromStorage()

  return {
    // State
    token,
    user,
    // Getters
    isAuthenticated,
    isAdmin,
    isTeacher,
    isStudent,
    isHomeroomTeacher,
    canManageStudentProfile,
    // Actions
    login,
    register,
    logout,
    loadUserFromStorage,
    fetchCurrentUser,
    isTokenValid,
    refreshUserData
  }
})