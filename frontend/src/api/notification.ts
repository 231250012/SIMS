import api from './index'

export interface NotificationItem {
  id: number
  type: string
  title: string
  content: string
  relatedStudentId?: number
  relatedSubject?: string
  payload?: string
  isRead: boolean
  createTime: string
}

export interface NotificationPage {
  content: NotificationItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export const notificationApi = {
  list(params?: { page?: number; size?: number; unreadOnly?: boolean }) {
    return api.get<NotificationPage>('/notifications', { params }).then(res => res.data)
  },

  unreadCount() {
    return api.get<{ count: number }>('/notifications/unread-count').then(res => res.data)
  },

  markRead(id: number) {
    return api.put<NotificationItem>(`/notifications/${id}/read`).then(res => res.data)
  },

  markAllRead() {
    return api.put<{ updated: number; message: string }>('/notifications/read-all').then(res => res.data)
  },

  remove(id: number) {
    return api.delete<{ message: string }>(`/notifications/${id}`).then(res => res.data)
  }
}
