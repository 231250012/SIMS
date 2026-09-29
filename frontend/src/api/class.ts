import apiClient from './index'

export interface ClassInfo {
  id: number
  name: string
  teacherId: number
  teacherName?: string
  studentCount?: number
  averageScore?: number
  grade?: string
  description?: string
  maxStudents?: number
  students?: StudentInfo[]
}

export interface CreateClassRequest {
  name: string
  grade?: string
  description?: string
  maxStudents?: number
  teacherId?: number
}

export interface StudentInfo {
  id: number
  name: string
  age: number
  score: number
  avatar?: string
  className?: string
}

export interface ClassStatistics {
  totalClasses: number
  totalStudents: number
  topStudent?: StudentInfo
}

export interface TeacherDashboardData {
  teacherStats: ClassStatistics
  myClasses: ClassInfo[]
  topStudents: StudentInfo[]
}

export const classApi = {
  // Get all classes
  getAllClasses(): Promise<ClassInfo[]> {
    return apiClient.get('/classes').then(res => res.data)
  },

  // Alias for getAllClasses (for compatibility)
  getClasses(): Promise<ClassInfo[]> {
    return this.getAllClasses()
  },

  createClass(data: CreateClassRequest): Promise<ClassInfo> {
    return apiClient.post('/classes', data).then(res => res.data)
  },

  // Get class details with students
  getClassDetails(classId: number): Promise<ClassInfo> {
    return apiClient.get(`/classes/${classId}`).then(res => res.data)
  },

  // Get class students
  getClassStudents(classId: number): Promise<StudentInfo[]> {
    return apiClient.get(`/classes/${classId}/students`).then(res => res.data)
  },

  // Get teacher dashboard data
  getTeacherDashboard(): Promise<TeacherDashboardData> {
    return apiClient.get('/classes/teacher/dashboard').then(res => {
      const data = res.data
      return {
        teacherStats: {
          totalClasses: data.totalClasses,
          totalStudents: data.totalStudents,
          topStudent: data.topStudents?.[0]
        },
        myClasses: (data.myClasses || []).map((cls: any) => ({
          ...cls,
          grade: cls.grade || '其他'
        })),
        topStudents: data.topStudents || []
      }
    }).catch(() => {
      return Promise.all([
        this.getAllClasses()
      ]).then(([classes]) => {
        const totalStudents = classes.reduce((sum, cls) => sum + (cls.studentCount || 0), 0)

        return {
          teacherStats: {
            totalClasses: classes.length,
            totalStudents
          },
          myClasses: classes.map(cls => ({
            ...cls,
            grade: cls.grade || '其他'
          })),
          topStudents: []
        }
      })
    })
  }
}