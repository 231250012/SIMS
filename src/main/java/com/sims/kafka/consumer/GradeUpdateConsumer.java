package com.sims.kafka.consumer;

import com.sims.kafka.dto.GradeUpdateEvent;
import com.sims.kafka.dto.KafkaMetrics;
import com.sims.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 成绩更新消费者：消费 GradeUpdateEvent 并写入站内通知。
 */
@Service
@Profile("!local")
public class GradeUpdateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(GradeUpdateConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(
        topics = "${kafka.topic.grade-updates:grade-updates}",
        groupId = "${spring.kafka.consumer.group-id:sims-grade-consumer-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGradeUpdate(
            @Payload GradeUpdateEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {

        long startTime = System.currentTimeMillis();

        logger.info("收到成绩更新事件 - 事件ID: {}, 学生ID: {}, 分区: {}, 偏移量: {}",
                event.getEventId(), event.getStudentId(), partition, offset);

        try {
            processGradeUpdateEvent(event);
            acknowledgment.acknowledge();

            long processingTime = System.currentTimeMillis() - startTime;
            logger.info("成绩更新事件处理完成 - 事件ID: {}, 处理时间: {}ms",
                    event.getEventId(), processingTime);
            recordConsumerMetrics(event, partition, offset, processingTime, true, null);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("处理成绩更新事件失败 - 事件ID: {}, 错误: {}",
                    event.getEventId(), e.getMessage(), e);
            recordConsumerMetrics(event, partition, offset, processingTime, false, e.getMessage());
            // 暂确认以免毒消息死循环；后续可改为进 DLQ
            acknowledgment.acknowledge();
        }
    }

    private void processGradeUpdateEvent(GradeUpdateEvent event) {
        if (event == null || !event.isValid()) {
            throw new IllegalArgumentException("成绩更新事件无效: " + event);
        }

        // 站内通知唯一写入口（Kafka 路径）
        notificationService.createGradeUpdateNotification(event);

        if (isImportantGradeUpdate(event)) {
            logger.info("重要成绩变更（分差≥10），可扩展家长通知 - 学生ID: {}, 科目: {}",
                    event.getStudentId(), event.getSubject());
        }
    }

    private boolean isImportantGradeUpdate(GradeUpdateEvent event) {
        if (event.getOldGrade() != null && event.getNewGrade() != null) {
            double change = Math.abs(event.getNewGrade().doubleValue() - event.getOldGrade().doubleValue());
            return change >= 10.0;
        }
        return false;
    }

    private void recordConsumerMetrics(GradeUpdateEvent event, int partition, long offset,
                                       long processingTime, boolean success, String errorMessage) {
        try {
            KafkaMetrics metrics = new KafkaMetrics(KafkaMetrics.OperationType.CONSUME, "grade-updates");
            metrics.setMessageCount(1L);
            metrics.setProcessingTimeMs(processingTime);
            metrics.setPartition(partition);
            metrics.setOffset(offset);
            metrics.setSuccess(success);
            metrics.setErrorMessage(errorMessage);
            metrics.setMessageSize((long) event.toString().length());
            logger.debug("Kafka消费者指标: {}", metrics);
        } catch (Exception e) {
            logger.warn("记录Kafka消费者指标时发生异常: {}", e.getMessage());
        }
    }
}
