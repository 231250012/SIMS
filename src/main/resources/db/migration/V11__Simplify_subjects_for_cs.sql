-- 计算机科学技术专业：仅保留 C语言、英语四级两门科目，每名学生每科仅一条成绩

-- 1. 科目目录表
CREATE TABLE IF NOT EXISTS subject (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE COMMENT '科目编码',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '科目名称',
    max_score DECIMAL(6,2) NOT NULL DEFAULT 100.00 COMMENT '满分',
    description VARCHAR(200) COMMENT '说明',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO subject (code, name, max_score, description) VALUES
('C_LANG', 'C语言', 100.00, '计算机科学技术专业基础课，单次考试'),
('CET4', '英语四级', 710.00, '大学英语四级考试，单次成绩')
ON DUPLICATE KEY UPDATE
    max_score = VALUES(max_score),
    description = VALUES(description);

-- 2. 清理旧多科目/多次考试样例数据
DELETE FROM grade_record;

-- 3. 每名学生每科仅一条成绩
SET @uk_exists := (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'grade_record'
      AND index_name = 'uk_student_subject'
);
SET @sql_uk := IF(@uk_exists = 0,
    'ALTER TABLE grade_record ADD UNIQUE KEY uk_student_subject (student_id, subject)',
    'SELECT 1');
PREPARE stmt_uk FROM @sql_uk;
EXECUTE stmt_uk;
DEALLOCATE PREPARE stmt_uk;

-- 4. 班级对齐计科专业命名与年级
UPDATE `class`
SET name = '计科2401班',
    grade = '2024级',
    description = '计算机科学技术 2024 级 1 班'
WHERE name IN ('计算机科学1班', '计科2401班');

UPDATE `class`
SET name = '计科2402班',
    grade = '2024级',
    description = '计算机科学技术 2024 级 2 班'
WHERE name IN ('计算机科学2班', '计科2402班');

UPDATE `class`
SET name = '计科2403班',
    grade = '2024级',
    description = '计算机科学技术 2024 级 3 班'
WHERE name IN ('软件工程1班', '计科2403班');

-- 5. 写入两门科目的单次样例成绩
INSERT INTO grade_record (student_id, subject, score, max_score, exam_type, exam_date, semester, academic_year, teacher_id, remarks)
SELECT s.id, v.subject, v.score, v.max_score, '正式考试', v.exam_date, '2024秋季', '2024-2025',
       (SELECT id FROM `user` WHERE username = 'teacher1' LIMIT 1),
       v.remarks
FROM student s
JOIN (
    SELECT '张三' AS name, 'C语言' AS subject, 86.0 AS score, 100.0 AS max_score, DATE('2024-12-20') AS exam_date, '基础扎实' AS remarks
    UNION ALL SELECT '张三', '英语四级', 498.0, 710.0, DATE('2024-12-14'), '听力需加强'
    UNION ALL SELECT '李四', 'C语言', 92.0, 100.0, DATE('2024-12-20'), '算法思路清晰'
    UNION ALL SELECT '李四', '英语四级', 532.0, 710.0, DATE('2024-12-14'), '词汇量较好'
    UNION ALL SELECT '王五', 'C语言', 78.0, 100.0, DATE('2024-12-20'), '指针部分需复习'
    UNION ALL SELECT '王五', '英语四级', 425.0, 710.0, DATE('2024-12-14'), '未达425分线边缘'
    UNION ALL SELECT '赵六', 'C语言', 88.0, 100.0, DATE('2024-12-20'), '代码规范良好'
    UNION ALL SELECT '赵六', '英语四级', 456.0, 710.0, DATE('2024-12-14'), '阅读理解较好'
    UNION ALL SELECT '钱七', 'C语言', 95.0, 100.0, DATE('2024-12-20'), '表现优秀'
    UNION ALL SELECT '钱七', '英语四级', 580.0, 710.0, DATE('2024-12-14'), '综合能力突出'
) v ON s.name = v.name;
