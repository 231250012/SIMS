<template>
  <div class="class-card" @click="handleClick">
    <div class="class-header">
      <div class="class-name">{{ classInfo.name }}</div>
      <div class="class-grade">{{ classInfo.grade || '其他' }}</div>
    </div>
    <div class="class-stats">
      <div class="stat-item">
        <span class="stat-label">学生数</span>
        <span class="stat-value">{{ classInfo.studentCount || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">班主任</span>
        <span class="stat-value teacher">{{ classInfo.teacherName || '未分配' }}</span>
      </div>
    </div>
    <div class="class-actions" v-if="showActions">
      <el-button 
        type="text" 
        size="small" 
        @click.stop="$emit('manage', classInfo)"
      >
        管理
      </el-button>
      <el-button 
        type="text" 
        size="small" 
        @click.stop="$emit('viewStudents', classInfo)"
      >
        学生
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ClassInfo } from '@/api/class'

interface Props {
  classInfo: ClassInfo
  showActions?: boolean
}

interface Emits {
  (e: 'click', classInfo: ClassInfo): void
  (e: 'manage', classInfo: ClassInfo): void
  (e: 'viewStudents', classInfo: ClassInfo): void
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false
})

const emit = defineEmits<Emits>()

const handleClick = () => {
  emit('click', props.classInfo)
}
</script>

<style scoped>
.class-card {
  background: #f8fafc;
  border-radius: 4px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  position: relative;
}

.class-card:hover {
  background: #e2e8f0;
  border-color: #5b9bd5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(91, 155, 213, 0.15);
}

.class-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.class-name {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.class-grade {
  background: #5b9bd5;
  color: white;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.class-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: #718096;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.stat-value.teacher {
  font-size: 14px;
}

.class-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #e2e8f0;
}

.class-actions .el-button {
  padding: 4px 8px;
  font-size: 12px;
}
</style>
