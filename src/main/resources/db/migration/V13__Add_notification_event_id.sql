-- 成绩事件幂等键：同一 eventId 只写一条通知（Kafka 成功消费与发布失败兜底共用）
ALTER TABLE notification
    ADD COLUMN event_id VARCHAR(64) NULL COMMENT 'GradeUpdateEvent.eventId，用于幂等' AFTER type;

CREATE UNIQUE INDEX uk_notification_event_id ON notification (event_id);
