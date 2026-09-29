import apiClient from './index'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  role: 'ADMIN' | 'TEACHER' | 'STUDENT'
  studentName?: string
  age?: number
  classId?: number
  isHomeroomTeacher?: boolean
}

export interface AuthResponse {
  token: string
  type?: string
  userId: number
  username: string
  role: string
  isHomeroomTeacher?: boolean
  message?: string
}

export interface UserInfo {
  id: number
  username: string
  role: string
  isHomeroomTeacher?: boolean
}

export const authApi = {
  // User login
  login(credentials: LoginRequest): Promise<AuthResponse> {
    return apiClient.post('/auth/login', credentials).then(res => res.data)
  },

  // Get current user info
  getCurrentUser(): Promise<UserInfo> {
    return apiClient.get('/auth/me').then(res => res.data)
  },

  // User logout
  logout(): Promise<void> {
    return apiClient.post('/auth/logout').then(res => res.data)
  },

  // User registration
  register(data: RegisterRequest): Promise<AuthResponse> {
    return apiClient.post('/auth/register', data).then(res => res.data)
  },

  // Public class options for registration page
  getRegisterClasses(): Promise<Array<{ id: number; name: string; grade?: string; teacherName?: string }>> {
    return apiClient.get('/auth/classes').then(res => res.data)
  }
}