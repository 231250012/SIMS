-- 插入示例成绩记录（按学生姓名关联，避免硬编码 ID）

INSERT INTO grade_record (student_id, subject, score, max_score, exam_type, exam_date, semester, academic_year, teacher_id, remarks)
SELECT s.id, v.subject, v.score, 100.0, '期中考试', v.exam_date, '2024秋季', '2024-2025',
       (SELECT id FROM `user` WHERE username = 'teacher1' LIMIT 1),
       v.remarks
FROM student s
JOIN (
    SELECT '李四' AS name, '数学' AS subject, 92.0 AS score, DATE('2024-11-15') AS exam_date, '表现优秀' AS remarks
    UNION ALL SELECT '李四', '语文', 88.5, DATE('2024-11-16'), '作文有待提高'
    UNION ALL SELECT '李四', '英语', 95.0, DATE('2024-11-17'), '口语表达流利'
    UNION ALL SELECT '李四', '物理', 89.0, DATE('2024-11-18'), '实验操作熟练'
    UNION ALL SELECT '李四', '化学', 91.5, DATE('2024-11-19'), '理论基础扎实'
    UNION ALL SELECT '钱七', '数学', 96.0, DATE('2024-11-15'), '数学天赋突出'
    UNION ALL SELECT '钱七', '语文', 92.0, DATE('2024-11-16'), '文学素养很好'
    UNION ALL SELECT '钱七', '英语', 94.5, DATE('2024-11-17'), '语法掌握扎实'
    UNION ALL SELECT '钱七', '物理', 98.0, DATE('2024-11-18'), '物理思维敏捷'
    UNION ALL SELECT '钱七', '化学', 93.5, DATE('2024-11-19'), '实验报告详细'
    UNION ALL SELECT '张三', '数学', 85.0, DATE('2024-11-15'), '基础扎实'
    UNION ALL SELECT '张三', '语文', 80.0, DATE('2024-11-16'), '阅读理解良好'
    UNION ALL SELECT '王五', '数学', 76.0, DATE('2024-11-15'), '需加强练习'
    UNION ALL SELECT '赵六', '英语', 88.0, DATE('2024-11-17'), '口语不错'
) v ON s.name = v.name;
