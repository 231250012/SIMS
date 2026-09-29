<template>
  <div class="notifications-page">
    <div class="page-header">
      <div>
        <h2>消息中心</h2>
        <p class="subtitle">成绩变更等站内通知</p>
      </div>
      <div class="header-actions">
        <el-button @click="loadData" :loading="loading">刷新</el-button>
        <el-button type="primary" :disabled="unreadCount === 0" @click="handleMarkAll">
          全部已读
        </el-button>
      </div>
    </div>

    <div class="toolbar">
      <el-radio-group v-model="filter" @change="onFilterChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="unread">未读（{{ unreadCount }}）</el-radio-button>
      </el-radio-group>
    </div>

    <el-card shadow="never" v-loading="loading">
      <div v-if="items.length === 0" class="empty">
        暂无{{ filter === 'unread' ? '未读' : '' }}通知
      </div>
      <div
        v-for="item in items"
        :key="item.id"
        class="notice-item"
        :class="{ unread: !item.isRead }"
        @click="openItem(item)"
      >
        <div class="notice-main">
          <div class="notice-title">
            <span v-if="!item.isRead" class="dot" />
            {{ item.title }}
          </div>
          <div class="notice-content">{{ item.content }}</div>
          <div class="notice-meta">{{ formatTime(item.createTime) }}</div>
        </div>
        <el-button type="danger" link @click.stop="handleDelete(item)">删除</el-button>
      </div>
    </el-card>

    <div class="pager" v-if="totalPages > 1">
      <el-pagination
        background
        layout="prev, pager, next"
        :page-size="pageSize"
        :current-page="page + 1"
        :total="total"
        @current-change="onPageChange"
      />
    </div>

    <el-drawer v-model="drawerVisible" title="通知详情" size="420px">
      <template v-if="current">
        <h3>{{ current.title }}</h3>
        <p class="drawer-time">{{ formatTime(current.createTime) }}</p>
        <p class="drawer-content">{{ current.content }}</p>
        <el-descriptions v-if="payload" :column="1" border size="small" style="margin-top: 16px">
          <el-descriptions-item v-if="payload.teacherName" label="教师">
            {{ payload.teacherName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="current.relatedSubject" label="科目">
            {{ current.relatedSubject }}
          </el-descriptions-item>
          <el-descriptions-item v-if="payload.oldScore != null" label="原成绩">
            {{ payload.oldScore }}
          </el-descriptions-item>
          <el-descriptions-item v-if="payload.newScore != null" label="新成绩">
            {{ payload.newScore }}
          </el-descriptions-item>
          <el-descriptions-item v-if="payload.maxScore != null" label="满分">
            {{ payload.maxScore }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { notificationApi, type NotificationItem } from '@/api/notification'

const loading = ref(false)
const items = ref<NotificationItem[]>([])
const page = ref(0)
const pageSize = 20
const total = ref(0)
const totalPages = ref(0)
const unreadCount = ref(0)
const filter = ref<'all' | 'unread'>('all')
const drawerVisible = ref(false)
const current = ref<NotificationItem | null>(null)
const payload = ref<Record<string, any> | null>(null)

let pollTimer: number | undefined

const loadUnread = async () => {
  try {
    const res = await notificationApi.unreadCount()
    unreadCount.value = res.count || 0
    window.dispatchEvent(new CustomEvent('notifications:unread', { detail: unreadCount.value }))
  } catch {
    unreadCount.value = 0
  }
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await notificationApi.list({
      page: page.value,
      size: pageSize,
      unreadOnly: filter.value === 'unread' ? true : undefined
    })
    items.value = res.content || []
    total.value = res.totalElements || 0
    totalPages.value = res.totalPages || 0
    await loadUnread()
  } catch (e) {
    console.error(e)
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

const onFilterChange = () => {
  page.value = 0
  loadData()
}

const onPageChange = (p: number) => {
  page.value = p - 1
  loadData()
}

const parsePayload = (item: NotificationItem) => {
  if (!item.payload) return null
  try {
    return JSON.parse(item.payload)
  } catch {
    return null
  }
}

const openItem = async (item: NotificationItem) => {
  current.value = item
  payload.value = parsePayload(item)
  drawerVisible.value = true
  if (!item.isRead) {
    try {
      await notificationApi.markRead(item.id)
      item.isRead = true
      await loadUnread()
    } catch (e) {
      console.error(e)
    }
  }
}

const handleMarkAll = async () => {
  try {
    await notificationApi.markAllRead()
    ElMessage.success('已全部标为已读')
    await loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (item: NotificationItem) => {
  try {
    await ElMessageBox.confirm('确定删除这条通知吗？', '确认', { type: 'warning' })
    await notificationApi.remove(item.id)
    ElMessage.success('已删除')
    if (current.value?.id === item.id) {
      drawerVisible.value = false
      current.value = null
    }
    await loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const formatTime = (t?: string) => {
  if (!t) return '--'
  return new Date(t).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
  pollTimer = window.setInterval(loadUnread, 30000)
})

onUnmounted(() => {
  if (pollTimer) window.clearInterval(pollTimer)
})
</script>

<style scoped>
.notifications-page {
  padding: 0 0 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 16px;
}

.page-header h2 {
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text-primary);
}

.subtitle {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 15px;
  line-height: 1.6;
}

.toolbar {
  margin-bottom: 16px;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 12px;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  border-radius: var(--radius-sm);
}

.notice-item:hover {
  background: var(--color-bg-soft);
}

.notice-item.unread {
  background: var(--color-primary-soft);
}

.notice-title {
  font-weight: 600;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-accent);
}

.notice-content {
  margin-top: 6px;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.notice-meta {
  margin-top: 6px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.empty {
  text-align: center;
  color: var(--color-text-muted);
  padding: 48px 0;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.drawer-time {
  color: var(--color-text-muted);
  font-size: 12px;
}

.drawer-content {
  color: var(--color-text-primary);
  line-height: 1.6;
}
</style>
