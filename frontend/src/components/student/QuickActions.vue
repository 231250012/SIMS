<template>
  <el-card class="quick-actions-card" shadow="never">
    <template #header>
      <div class="card-header">
        <div class="header-title">
          <el-icon class="header-icon"><Operation /></el-icon>
          <span>快速操作</span>
        </div>
      </div>
    </template>
    
    <div class="actions-grid">
      <div class="action-item" @click="navigateTo('/student/grades')">
        <div class="action-icon view-grades">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="action-content">
          <div class="action-title">查看成绩</div>
          <div class="action-desc">查看详细成绩记录</div>
        </div>
        <div class="action-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <div class="action-item" @click="navigateTo('/profile')">
        <div class="action-icon edit-profile">
          <el-icon><User /></el-icon>
        </div>
        <div class="action-content">
          <div class="action-title">个人信息</div>
          <div class="action-desc">查看个人资料</div>
        </div>
        <div class="action-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Operation, TrendCharts, User, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.quick-actions-card {
  border: 1px solid #d0e0ee;
  border-radius: 4px;
  box-shadow: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.header-icon {
  font-size: 18px;
  color: #5b9bd5;
}

.actions-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #f7fafc;
  border: 1px solid #d0e0ee;
  border-radius: 4px;
  cursor: pointer;
}

.action-item:hover {
  background: #e8f2fa;
  border-color: #5b9bd5;
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
  flex-shrink: 0;
}

.view-grades,
.edit-profile {
  background: #5b9bd5;
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 2px;
}

.action-desc {
  font-size: 13px;
  color: #718096;
}

.action-arrow {
  color: #cbd5e0;
  font-size: 16px;
}
</style>
