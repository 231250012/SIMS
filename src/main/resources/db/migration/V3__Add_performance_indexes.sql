-- 为用户表添加复合索引
CREATE INDEX idx_user_role_username ON `user`(role, username);
CREATE INDEX idx_user_create_time ON `user`(create_time);

-- 为班级表添加复合索引
CREATE INDEX idx_class_teacher_name ON `class`(teacher_id, name);

-- 为学生表添加复合索引
CREATE INDEX idx_student_class_score ON student(class_id, score);
CREATE INDEX idx_student_age_score ON student(age, score);
CREATE INDEX idx_student_create_time ON student(create_time);

-- 为API统计表添加复合索引
CREATE INDEX idx_api_stats_count_update ON api_stats(call_count, last_update);

-- 添加数据完整性约束
ALTER TABLE `user` ADD CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT'));
ALTER TABLE `user` ADD CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3);

-- 添加学生姓名长度约束
ALTER TABLE student ADD CONSTRAINT chk_student_name_length CHECK (LENGTH(name) >= 1);

-- 添加班级名称长度约束
ALTER TABLE `class` ADD CONSTRAINT chk_class_name_length CHECK (LENGTH(name) >= 1);

-- 添加API端点长度约束
ALTER TABLE api_stats ADD CONSTRAINT chk_endpoint_length CHECK (LENGTH(endpoint) >= 1);
ALTER TABLE api_stats ADD CONSTRAINT chk_call_count_positive CHECK (call_count >= 0);