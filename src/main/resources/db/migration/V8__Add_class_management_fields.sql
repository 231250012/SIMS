-- 为班级管理功能添加缺失的字段

-- 1. 为班级表添加更多信息字段（含 grade，供后续视图与统计使用）
ALTER TABLE `class` 
ADD COLUMN grade VARCHAR(20) COMMENT '年级',
ADD COLUMN description TEXT COMMENT '班级描述',
ADD COLUMN max_students INT DEFAULT 50 COMMENT '最大学生数量',
ADD COLUMN create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 2. 为学生表添加更多个人信息字段
ALTER TABLE student 
ADD COLUMN student_id VARCHAR(20) UNIQUE COMMENT '学号',
ADD COLUMN gender VARCHAR(10) COMMENT '性别',
ADD COLUMN birth_date DATE COMMENT '出生日期',
ADD COLUMN phone VARCHAR(20) COMMENT '联系电话',
ADD COLUMN address TEXT COMMENT '家庭住址',
ADD COLUMN parent_name VARCHAR(50) COMMENT '家长姓名',
ADD COLUMN parent_phone VARCHAR(20) COMMENT '家长电话',
ADD COLUMN enrollment_date DATE COMMENT '入学日期',
ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '学生状态（ACTIVE-在校，GRADUATED-毕业，TRANSFERRED-转学，SUSPENDED-休学）',
ADD COLUMN update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 3. 创建教师备注表（用于教师对学生的备注）
CREATE TABLE teacher_student_note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    note_type VARCHAR(20) DEFAULT 'GENERAL' COMMENT '备注类型（GENERAL-一般，BEHAVIOR-行为，ACADEMIC-学业，HEALTH-健康）',
    content TEXT NOT NULL COMMENT '备注内容',
    is_private BOOLEAN DEFAULT FALSE COMMENT '是否私有备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (teacher_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    INDEX idx_teacher_student (teacher_id, student_id),
    INDEX idx_note_type (note_type),
    INDEX idx_create_time (create_time)
);

-- 4. 创建成绩详细记录表（支持多科目成绩）
CREATE TABLE grade_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    subject VARCHAR(50) NOT NULL COMMENT '科目名称',
    score DECIMAL(5,2) NOT NULL COMMENT '成绩',
    max_score DECIMAL(5,2) DEFAULT 100.00 COMMENT '满分',
    exam_type VARCHAR(20) DEFAULT 'REGULAR' COMMENT '考试类型（REGULAR-平时，MIDTERM-期中，FINAL-期末，MOCK-模拟）',
    exam_date DATE NOT NULL COMMENT '考试日期',
    semester VARCHAR(20) COMMENT '学期',
    academic_year VARCHAR(10) COMMENT '学年',
    teacher_id BIGINT COMMENT '任课教师ID',
    remarks TEXT COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES `user`(id) ON DELETE SET NULL,
    INDEX idx_student_subject (student_id, subject),
    INDEX idx_exam_date (exam_date),
    INDEX idx_semester (semester, academic_year),
    INDEX idx_score (score),
    CONSTRAINT chk_grade_score CHECK (score >= 0.00 AND score <= max_score)
);

-- 5. 创建班级统计视图（用于快速查询班级统计信息）
CREATE VIEW class_statistics AS
SELECT 
    c.id as class_id,
    c.name as class_name,
    c.grade,
    COUNT(s.id) as student_count,
    COALESCE(AVG(s.score), 0) as average_score,
    COALESCE(MAX(s.score), 0) as max_score,
    COALESCE(MIN(s.score), 0) as min_score,
    u.username as teacher_name
FROM `class` c
LEFT JOIN student s ON c.id = s.class_id AND s.status = 'ACTIVE'
LEFT JOIN `user` u ON c.teacher_id = u.id
GROUP BY c.id, c.name, c.grade, u.username;

-- 6. 更新现有数据，为班级添加年级信息
UPDATE `class` 
SET grade = CASE 
    WHEN name LIKE '%高一%' OR name LIKE '%一年级%' THEN '高一'
    WHEN name LIKE '%高二%' OR name LIKE '%二年级%' THEN '高二'
    WHEN name LIKE '%高三%' OR name LIKE '%三年级%' THEN '高三'
    ELSE '其他'
END;

-- 7. 为现有学生生成学号（格式：年份+班级ID+序号）
UPDATE student s
JOIN `class` c ON s.class_id = c.id
SET s.student_id = CONCAT(
    YEAR(COALESCE(s.create_time, NOW())),
    LPAD(c.id, 2, '0'),
    LPAD(s.id, 4, '0')
)
WHERE s.student_id IS NULL;

-- 8. 添加新的索引以提高查询性能
CREATE INDEX idx_class_grade ON `class`(grade);
CREATE INDEX idx_student_student_id ON student(student_id);
CREATE INDEX idx_student_status ON student(status);
CREATE INDEX idx_student_enrollment_date ON student(enrollment_date);