package com.sims.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Kafka指标数据模型
 * 用于收集和存储Kafka操作的性能指标
 * 需求: 5.2, 5.3
 */
public class KafkaMetrics {

    @JsonProperty("metricId")
    private String metricId;

    @JsonProperty("operationType")
    private OperationType operationType;

    @JsonProperty("messageCount")
    private Long messageCount;

    @JsonProperty("messageSize")
    private Long messageSize;

    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;

    @JsonProperty("consumptionRate")
    private Double consumptionRate;

    @JsonProperty("lagMs")
    private Long lagMs;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @JsonProperty("topicName")
    private String topicName;

    @JsonProperty("partition")
    private Integer partition;

    @JsonProperty("offset")
    private Long offset;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("errorMessage")
    private String errorMessage;

    // ==================== 构造函数 ====================

    public KafkaMetrics() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    public KafkaMetrics(OperationType operationType, String topicName) {
        this();
        this.operationType = operationType;
        this.topicName = topicName;
        this.metricId = generateMetricId();
    }

    // ==================== Getter和Setter ====================

    public String getMetricId() {
        return metricId;
    }

    public void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }

    public Long getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(Long messageSize) {
        this.messageSize = messageSize;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Duration getProcessingTime() {
        return processingTimeMs != null ? Duration.ofMillis(processingTimeMs) : Duration.ZERO;
    }

    public void setProcessingTime(Duration processingTime) {
        this.processingTimeMs = processingTime != null ? processingTime.toMillis() : null;
    }

    public Double getConsumptionRate() {
        return consumptionRate;
    }

    public void setConsumptionRate(Double consumptionRate) {
        this.consumptionRate = consumptionRate;
    }

    public Long getLagMs() {
        return lagMs;
    }

    public void setLagMs(Long lagMs) {
        this.lagMs = lagMs;
    }

    public Duration getLag() {
        return lagMs != null ? Duration.ofMillis(lagMs) : Duration.ZERO;
    }

    public void setLag(Duration lag) {
        this.lagMs = lag != null ? lag.toMillis() : null;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            this.success = false;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成指标ID
     */
    private String generateMetricId() {
        return String.format("%s_%s_%d", 
                operationType != null ? operationType.name() : "UNKNOWN",
                topicName != null ? topicName : "unknown",
                System.currentTimeMillis());
    }

    /**
     * 计算吞吐量（消息/秒）
     */
    public Double getThroughput() {
        if (messageCount == null || processingTimeMs == null || processingTimeMs == 0) {
            return 0.0;
        }
        return (messageCount.doubleValue() * 1000.0) / processingTimeMs.doubleValue();
    }

    /**
     * 计算平均消息大小
     */
    public Double getAverageMessageSize() {
        if (messageCount == null || messageSize == null || messageCount == 0) {
            return 0.0;
        }
        return messageSize.doubleValue() / messageCount.doubleValue();
    }

    /**
     * 标记为失败
     */
    public void markAsFailed(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }

    /**
     * 检查指标是否有效
     */
    public boolean isValid() {
        return operationType != null && 
               topicName != null && !topicName.trim().isEmpty() &&
               timestamp != null;
    }

    // ==================== Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaMetrics that = (KafkaMetrics) o;
        return Objects.equals(metricId, that.metricId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricId);
    }

    @Override
    public String toString() {
        return "KafkaMetrics{" +
                "metricId='" + metricId + '\'' +
                ", operationType=" + operationType +
                ", messageCount=" + messageCount +
                ", messageSize=" + messageSize +
                ", processingTimeMs=" + processingTimeMs +
                ", consumptionRate=" + consumptionRate +
                ", lagMs=" + lagMs +
                ", timestamp=" + timestamp +
                ", topicName='" + topicName + '\'' +
                ", partition=" + partition +
                ", offset=" + offset +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    // ==================== 内部枚举 ====================

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        PRODUCE("生产"),
        CONSUME("消费"),
        ADMIN("管理");

        private final String displayName;

        OperationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}