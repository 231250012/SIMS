import apiClient from './index'

export interface TeacherClassBrief {
  id: number
  name: string
  grade?: string
}

export interface TeacherAdminInfo {
  id: number
  username: string
  isHomeroomTeacher: boolean
  createTime?: string
  managedClasses: TeacherClassBrief[]
}

export interface AssignTeacherClassRequest {
  classId: number
  isHomeroomTeacher?: boolean
}

export const teacherAdminApi = {
  list(): Promise<TeacherAdminInfo[]> {
    return apiClient.get('/admin/teachers').then(res => res.data)
  },

  assignClass(teacherId: number, data: AssignTeacherClassRequest): Promise<TeacherAdminInfo> {
    return apiClient.put(`/admin/teachers/${teacherId}/class`, data).then(res => res.data)
  },

  setHomeroom(teacherId: number, isHomeroomTeacher: boolean): Promise<TeacherAdminInfo> {
    return apiClient.put(`/admin/teachers/${teacherId}/homeroom`, { isHomeroomTeacher }).then(res => res.data)
  },

  delete(teacherId: number): Promise<void> {
    return apiClient.delete(`/admin/teachers/${teacherId}`).then(res => res.data)
  }
}
