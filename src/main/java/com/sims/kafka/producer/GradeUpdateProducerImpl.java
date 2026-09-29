package com.sims.kafka.producer;

import com.sims.kafka.dto.GradeUpdateEvent;
import com.sims.kafka.dto.KafkaMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 成绩更新生产者实现类
 * 实现异步消息发布、发布确认和重试机制
 * 需求: 1.1, 1.3, 1.4
 */
@Service
@Profile("!local")
public class GradeUpdateProducerImpl implements GradeUpdateProducer {

    private static final Logger logger = LoggerFactory.getLogger(GradeUpdateProducerImpl.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.grade-updates:grade-updates}")
    private String gradeUpdatesTopic;

    @Override
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<SendResult<String, Object>> publishGradeUpdate(GradeUpdateEvent event) {
        logger.info("开始发布成绩更新事件: {}", event.getEventId());
        
        // 验证事件有效性
        if (!event.isValid()) {
            logger.error("成绩更新事件无效: {}", event);
            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalArgumentException("成绩更新事件无效"));
            return failedFuture;
        }

        // 记录开始时间用于指标收集
        long startTime = System.currentTimeMillis();
        
        // 使用学生ID作为消息键，确保同一学生的消息按顺序处理
        String messageKey = "student_" + event.getStudentId();
        
        try {
            // 发送消息到Kafka
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(gradeUpdatesTopic, messageKey, event);
            
            // 添加成功和失败的回调处理
            future.whenComplete((result, exception) -> {
                long processingTime = System.currentTimeMillis() - startTime;
                
                if (exception == null) {
                    // 发送成功
                    logger.info("成绩更新事件发布成功: {} -> 主题: {}, 分区: {}, 偏移量: {}, 处理时间: {}ms",
                            event.getEventId(), 
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            processingTime);
                    
                    // 记录成功指标
                    recordMetrics(event, result, processingTime, true, null);
                } else {
                    // 发送失败
                    logger.error("成绩更新事件发布失败: {} - {}", event.getEventId(), exception.getMessage(), exception);
                    
                    // 记录失败指标
                    recordMetrics(event, null, processingTime, false, exception.getMessage());
                }
            });
            
            return future;
            
        } catch (Exception e) {
            logger.error("发布成绩更新事件时发生异常: {} - {}", event.getEventId(), e.getMessage(), e);
            
            // 记录异常指标
            long processingTime = System.currentTimeMillis() - startTime;
            recordMetrics(event, null, processingTime, false, e.getMessage());
            
            // 重新抛出异常以触发重试机制
            throw new RuntimeException("发布成绩更新事件失败", e);
        }
    }

    @Override
    public void publishGradeUpdateAsync(GradeUpdateEvent event, GradeUpdateCallback callback) {
        logger.info("异步发布成绩更新事件: {}", event.getEventId());
        
        CompletableFuture<SendResult<String, Object>> future = publishGradeUpdate(event);
        
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                // 调用成功回调
                if (callback != null) {
                    try {
                        callback.onSuccess(event, result);
                    } catch (Exception callbackException) {
                        logger.error("执行成功回调时发生异常: {}", callbackException.getMessage(), callbackException);
                    }
                }
            } else {
                // 调用失败回调
                if (callback != null) {
                    try {
                        callback.onFailure(event, exception);
                    } catch (Exception callbackException) {
                        logger.error("执行失败回调时发生异常: {}", callbackException.getMessage(), callbackException);
                    }
                }
            }
        });
    }

    /**
     * 记录Kafka操作指标
     */
    private void recordMetrics(GradeUpdateEvent event, SendResult<String, Object> result, 
                              long processingTime, boolean success, String errorMessage) {
        try {
            KafkaMetrics metrics = new KafkaMetrics(KafkaMetrics.OperationType.PRODUCE, gradeUpdatesTopic);
            metrics.setMessageCount(1L);
            metrics.setProcessingTimeMs(processingTime);
            metrics.setSuccess(success);
            metrics.setErrorMessage(errorMessage);
            
            if (result != null) {
                metrics.setPartition(result.getRecordMetadata().partition());
                metrics.setOffset(result.getRecordMetadata().offset());
                // 估算消息大小（简化计算）
                metrics.setMessageSize((long) event.toString().length());
            }
            
            // 这里可以将指标发送到监控系统或存储到数据库
            // 目前只记录日志
            logger.debug("Kafka生产者指标: {}", metrics);
            
        } catch (Exception e) {
            logger.warn("记录Kafka指标时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 创建默认的回调实现
     */
    public static GradeUpdateCallback createDefaultCallback() {
        return new GradeUpdateCallback() {
            @Override
            public void onSuccess(GradeUpdateEvent event, SendResult<String, Object> result) {
                if (result == null || result.getRecordMetadata() == null) {
                    logger.info("成绩更新事件发布成功（无 Kafka metadata，如 local NoOp）- 事件ID: {}",
                            event.getEventId());
                    return;
                }
                logger.info("成绩更新事件发布成功 - 事件ID: {}, 主题: {}, 分区: {}, 偏移量: {}",
                        event.getEventId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(GradeUpdateEvent event, Throwable exception) {
                logger.error("成绩更新事件发布失败 - 事件ID: {}, 错误: {}", 
                        event.getEventId(), exception.getMessage(), exception);
            }
        };
    }
}