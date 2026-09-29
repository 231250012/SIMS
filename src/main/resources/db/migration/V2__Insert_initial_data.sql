-- 插入初始管理员用户
-- 密码为 'admin123'，使用BCrypt加密
INSERT INTO `user` (username, password, role) VALUES 
('admin', '$2a$10$Lt2lIlF9beuwVRjL8ZhQvOkg3OtorbEFH3dvIXrMhLyL8skZb5I3G', 'ADMIN');

-- 插入示例教师用户
-- 密码为 'teacher123'，使用BCrypt加密
INSERT INTO `user` (username, password, role) VALUES 
('teacher1', '$2a$10$oYAfJ11AXRKBfKgAPqStlePR02VtwrFeWkN27RpTOBZtlGF/yOEv6', 'TEACHER'),
('teacher2', '$2a$10$oYAfJ11AXRKBfKgAPqStlePR02VtwrFeWkN27RpTOBZtlGF/yOEv6', 'TEACHER');

-- 插入示例学生用户
-- 密码为 'student123'，使用BCrypt加密
INSERT INTO `user` (username, password, role) VALUES 
('student1', '$2a$10$N8q1MEdk736op3lMHH76BuGci7TpQ5Z0vUslqbZbqbXkS20Vu.IaC', 'STUDENT'),
('student2', '$2a$10$N8q1MEdk736op3lMHH76BuGci7TpQ5Z0vUslqbZbqbXkS20Vu.IaC', 'STUDENT'),
('student3', '$2a$10$N8q1MEdk736op3lMHH76BuGci7TpQ5Z0vUslqbZbqbXkS20Vu.IaC', 'STUDENT'),
('student4', '$2a$10$N8q1MEdk736op3lMHH76BuGci7TpQ5Z0vUslqbZbqbXkS20Vu.IaC', 'STUDENT');

-- 插入示例班级
INSERT INTO `class` (name, teacher_id) VALUES 
('计算机科学1班', (SELECT id FROM `user` WHERE username = 'teacher1')),
('计算机科学2班', (SELECT id FROM `user` WHERE username = 'teacher2')),
('软件工程1班', (SELECT id FROM `user` WHERE username = 'teacher1'));

-- 插入示例学生信息
INSERT INTO student (name, age, class_id, score, user_id) VALUES 
('张三', 20, (SELECT id FROM `class` WHERE name = '计算机科学1班'), 85.50, (SELECT id FROM `user` WHERE username = 'student1')),
('李四', 21, (SELECT id FROM `class` WHERE name = '计算机科学1班'), 92.00, (SELECT id FROM `user` WHERE username = 'student2')),
('王五', 19, (SELECT id FROM `class` WHERE name = '计算机科学2班'), 78.75, (SELECT id FROM `user` WHERE username = 'student3')),
('赵六', 22, (SELECT id FROM `class` WHERE name = '软件工程1班'), 88.25, (SELECT id FROM `user` WHERE username = 'student4')),
('钱七', 20, (SELECT id FROM `class` WHERE name = '计算机科学1班'), 95.00, NULL),
('孙八', 21, (SELECT id FROM `class` WHERE name = '计算机科学2班'), 82.50, NULL),
('周九', 19, (SELECT id FROM `class` WHERE name = '软件工程1班'), 90.75, NULL),
('吴十', 20, NULL, 0.00, NULL); -- 未分配班级的学生

-- 插入初始API统计数据
INSERT INTO api_stats (endpoint, call_count) VALUES 
('/api/auth/login', 0),
('/api/auth/register', 0),
('/api/students', 0),
('/api/students/{id}', 0),
('/api/classes', 0),
('/api/classes/{id}', 0),
('/api/statistics/api-calls', 0),
('/api/statistics/overview', 0);