-- 创建用户表
CREATE TABLE `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建班级表
CREATE TABLE `class` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    teacher_id BIGINT,
    FOREIGN KEY (teacher_id) REFERENCES `user`(id) ON DELETE SET NULL
);

-- 创建学生表
CREATE TABLE student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    class_id BIGINT,
    score DECIMAL(5,2) DEFAULT 0.00,
    avatar VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT,
    FOREIGN KEY (class_id) REFERENCES `class`(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE SET NULL,
    CONSTRAINT chk_age CHECK (age > 0 AND age <= 100),
    CONSTRAINT chk_score CHECK (score >= 0.00 AND score <= 100.00)
);

-- 创建API统计表
CREATE TABLE api_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    endpoint VARCHAR(100) NOT NULL UNIQUE,
    call_count BIGINT NOT NULL DEFAULT 0,
    last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_user_username ON `user`(username);
CREATE INDEX idx_user_role ON `user`(role);
CREATE INDEX idx_class_name ON `class`(name);
CREATE INDEX idx_class_teacher_id ON `class`(teacher_id);
CREATE INDEX idx_student_name ON student(name);
CREATE INDEX idx_student_class_id ON student(class_id);
CREATE INDEX idx_student_score ON student(score);
CREATE INDEX idx_student_user_id ON student(user_id);
CREATE INDEX idx_api_stats_endpoint ON api_stats(endpoint);
CREATE INDEX idx_api_stats_call_count ON api_stats(call_count);
CREATE INDEX idx_api_stats_last_update ON api_stats(last_update);