-- 成绩变更等站内通知（一期：本地同步落库，后续可接 Kafka 消费者写同一表）

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type VARCHAR(32) NOT NULL COMMENT '通知类型，如 GRADE_UPDATE',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content VARCHAR(500) NOT NULL COMMENT '正文',
    related_student_id BIGINT COMMENT '关联学生ID',
    related_subject VARCHAR(50) COMMENT '关联科目',
    payload TEXT COMMENT '扩展 JSON（旧分/新分/教师等）',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    INDEX idx_notification_user_read_time (user_id, is_read, create_time),
    INDEX idx_notification_create_time (create_time)
) COMMENT='站内通知';
