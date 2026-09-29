<template>
  <div class="student-item" @click="handleClick">
    <div class="student-rank">{{ rank }}</div>
    <el-avatar :size="40" class="student-avatar" :src="student.avatar">
      {{ student.name.charAt(0) }}
    </el-avatar>
    <div class="student-info">
      <div class="student-name">{{ student.name }}</div>
      <div class="student-score">{{ formatScore(student.score) }}分</div>
      <div v-if="student.className" class="student-class">{{ student.className }}</div>
    </div>
    <el-button 
      type="text" 
      size="small"
      @click.stop="$emit('view', student)"
    >
      查看
    </el-button>
  </div>
</template>

<script setup lang="ts">
import type { StudentInfo } from '@/api/class'

interface Props {
  student: StudentInfo
  rank: number
}

interface Emits {
  (e: 'click', student: StudentInfo): void
  (e: 'view', student: StudentInfo): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formatScore = (score: number) => {
  return score ? score.toFixed(1) : '0.0'
}

const handleClick = () => {
  emit('click', props.student)
}
</script>

<style scoped>
.student-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.student-item:hover {
  background: #e2e8f0;
  transform: translateY(-1px);
}

.student-rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #5b9bd5;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.student-avatar {
  border: 2px solid #e2e8f0;
  flex-shrink: 0;
}

.student-info {
  flex: 1;
  min-width: 0;
}

.student-name {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.student-score {
  font-size: 12px;
  color: #718096;
  margin-bottom: 2px;
}

.student-class {
  font-size: 11px;
  color: #a0aec0;
}

.el-button {
  flex-shrink: 0;
}
</style>