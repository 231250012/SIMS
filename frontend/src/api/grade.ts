import api from './index'

export interface GradeRecord {
  id: number
  studentId: number
  studentName: string
  className: string
  subject: string
  score: number
  maxScore: number
  examType: string
  examDate: string
  semester: string
  academicYear: string
  teacherId?: number
  teacherName?: string
  remarks?: string
  createTime: string
  percentage: number
  gradeLevel: string
}

export interface GradeRecordRequest {
  studentId: number
  subject: string
  score: number
  maxScore?: number
  examType?: string
  examDate: string
  semester?: string
  academicYear?: string
  teacherId?: number
  remarks?: string
}

export interface BatchGradeUpdateRequest {
  gradeRecords: GradeRecordRequest[]
}

export interface GradeTrendPoint {
  examDate: string
  score: number
  maxScore: number
  examType: string
  percentage: number
}

export interface GradeTrendResponse {
  studentId: number
  studentName: string
  subject: string
  trendPoints: GradeTrendPoint[]
  averageScore: number
  highestScore: number
  lowestScore: number
  trend: string
}

export interface RankingEntry {
  rank: number
  studentId: number
  studentName: string
  className: string
  score: number
  maxScore: number
  percentage: number
  gradeLevel: string
}

export interface GradeRankingResponse {
  subject: string
  scope: string
  classId?: number
  className?: string
  rankings: RankingEntry[]
  averageScore: number
  highestScore: number
  lowestScore: number
  totalStudents: number
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export const gradeApi = {
  // 获取成绩记录列表
  getGradeRecords: (params: {
    page?: number
    size?: number
    studentId?: number
    classId?: number
    subject?: string
    examType?: string
    semester?: string
    academicYear?: string
    startDate?: string
    endDate?: string
  }) => {
    return api.get<PageResponse<GradeRecord>>('/grades', { params }).then(res => res.data)
  },

  // 创建成绩记录
  createGradeRecord: (data: GradeRecordRequest) => {
    return api.post<GradeRecord>('/grades', data).then(res => res.data)
  },

  // 批量更新成绩记录
  batchUpdateGrades: (data: BatchGradeUpdateRequest) => {
    return api.post<{
      message: string
      total: number
      success: number
      failure: number
      successRecords: GradeRecord[]
      errors: string[]
    }>('/grades/batch', data).then(res => res.data)
  },

  // 更新成绩记录
  updateGradeRecord: (id: number, data: Partial<GradeRecordRequest>) => {
    return api.put<GradeRecord>(`/grades/${id}`, data).then(res => res.data)
  },

  // 删除成绩记录
  deleteGradeRecord: (id: number) => {
    return api.delete<{ message: string; gradeRecordId: string }>(`/grades/${id}`).then(res => res.data)
  },

  // 获取学生成绩记录
  getStudentGradeRecords: (studentId: number) => {
    return api.get<GradeRecord[]>(`/grades/student/${studentId}`).then(res => res.data)
  },

  // 获取学生最新成绩
  getStudentLatestGrades: (studentId: number) => {
    return api.get<GradeRecord[]>(`/grades/student/${studentId}/latest`).then(res => res.data)
  },

  // 获取学生成绩趋势分析
  getGradeTrend: (studentId: number, subject: string) => {
    return api.get<GradeTrendResponse>(`/grades/trend/${studentId}`, {
      params: { subject }
    }).then(res => res.data)
  },

  // 获取成绩排行榜
  getGradeRanking: (params: {
    subject: string
    scope?: string
    classId?: number
    limit?: number
  }) => {
    return api.get<GradeRankingResponse>('/grades/ranking', { params }).then(res => res.data)
  },

  // 获取班级成绩排行榜
  getClassGradeRanking: (classId: number, subject: string, limit?: number) => {
    return api.get<GradeRankingResponse>(`/grades/ranking/class/${classId}`, {
      params: { subject, limit }
    }).then(res => res.data)
  },

  // 获取当前学生的成绩记录
  getMyGrades: () => {
    return api.get<GradeRecord[]>('/grades/my-grades').then(res => res.data)
  },

  // 获取当前学生的成绩趋势
  getMyGradeTrend: (subject: string) => {
    return api.get<GradeTrendResponse>('/grades/my-trend', {
      params: { subject }
    }).then(res => res.data)
  }
}