import apiClient from './index'

export interface Student {
  id: number
  name: string
  studentId?: string
  age: number
  gender?: string
  birthDate?: string
  phone?: string
  address?: string
  parentName?: string
  parentPhone?: string
  enrollmentDate?: string
  status?: string
  classId?: number
  className?: string
  score: number
  avatar?: string
  createTime: string
  updateTime?: string
  studentClass?: {
    id: number
    name: string
    teacher?: {
      id: number
      username: string
    }
  }
}

export interface CreateStudentRequest {
  name: string
  age: number
  classId?: number
}

export interface UpdateStudentRequest {
  name?: string
  age?: number
  gender?: string
  birthDate?: string
  phone?: string
  address?: string
  parentName?: string
  parentPhone?: string
  classId?: number
  score?: number
  avatar?: string
}

export interface PageRequest {
  page: number
  size: number
  name?: string
  classId?: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface StudentRankResponse {
  id: number
  name: string
  age: number
  className: string
  score: number
  rank: number
}

export interface GradeRecord {
  id: number
  subject: string
  score: number
  maxScore: number
  examType: string
  examDate: string
  semester: string
  academicYear?: string
  gradeLevel: string
  percentage: number
  remarks?: string
}


export const studentApi = {
  // Get students with pagination and filtering
  getStudents(params: PageRequest): Promise<PageResponse<Student>> {
    return apiClient.get('/students', { params }).then(res => res.data)
  },

  // Get student by ID
  getStudent(id: number): Promise<Student> {
    return apiClient.get(`/students/${id}`).then(res => res.data)
  },

  // Get current student profile (for logged-in student)
  getMyProfile(): Promise<Student> {
    return apiClient.get('/students/profile').then(res => res.data)
  },

  // Update current student profile
  updateMyProfile(student: UpdateStudentRequest): Promise<Student> {
    return apiClient.put('/students/profile', student).then(res => res.data)
  },

  // Get student grade history
  getMyGrades(): Promise<GradeRecord[]> {
    return apiClient.get('/students/grades').then(res => res.data)
  },

  // Get class ranking for specific class
  getClassScoreRanking(classId: number, limit: number = 10): Promise<StudentRankResponse[]> {
    return apiClient.get(`/students/ranking/class/${classId}`, { params: { limit } }).then(res => res.data.ranking)
  },

  // Create new student
  createStudent(student: CreateStudentRequest): Promise<Student> {
    return apiClient.post('/students', student).then(res => res.data)
  },

  // Update student
  updateStudent(id: number, student: UpdateStudentRequest): Promise<Student> {
    return apiClient.put(`/students/${id}`, student).then(res => res.data)
  },

  // Delete student
  deleteStudent(id: number): Promise<void> {
    return apiClient.delete(`/students/${id}`).then(res => res.data)
  },

  // Get score ranking
  getScoreRanking(limit: number = 10): Promise<StudentRankResponse[]> {
    return apiClient.get('/students/ranking', { params: { limit } }).then(res => res.data.ranking)
  }
}