export interface SubjectOption {
  name: string
  maxScore: number
  label: string
}

/** 计算机科学技术专业：仅两门科目，每科一次成绩 */
export const SUBJECT_OPTIONS: SubjectOption[] = [
  { name: 'C语言', maxScore: 100, label: 'C语言（满分100）' },
  { name: '英语四级', maxScore: 710, label: '英语四级（满分710）' }
]

export const DEFAULT_EXAM_TYPE = '正式考试'

export function getSubjectMaxScore(subject: string): number {
  return SUBJECT_OPTIONS.find(s => s.name === subject)?.maxScore ?? 100
}
