package com.sims.controller;

import com.sims.kafka.KafkaHealthCheck;
import com.sims.kafka.dto.GradeUpdateEvent;
import com.sims.kafka.dto.KafkaMetrics;
import com.sims.kafka.producer.GradeUpdateProducer;
import com.sims.kafka.producer.GradeUpdateProducerImpl;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Kafka监控和管理控制器
 * 提供Kafka集群状态查看、消息发送测试等功能
 */
@RestController
@RequestMapping("/api/kafka")
@PreAuthorize("hasRole('ADMIN')")
@Profile("!local")
public class KafkaMonitorController {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMonitorController.class);

    @Autowired
    private GradeUpdateProducer gradeUpdateProducer;

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Autowired
    private KafkaHealthCheck kafkaHealthCheck;

    @Value("${kafka.topic.grade-updates:grade-updates}")
    private String gradeUpdatesTopic;

    @Value("${spring.kafka.consumer.group-id:sims-grade-consumer-group}")
    private String consumerGroupId;

    /**
     * 获取Kafka集群状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getKafkaStatus() {
        logger.info("获取Kafka集群状态");
        
        Map<String, Object> status = new HashMap<>();
        
        // 首先进行快速健康检查
        boolean isAvailable = kafkaHealthCheck.isKafkaAvailable();
        status.put("connected", isAvailable);
        
        if (!isAvailable) {
            status.put("error", "无法连接到Kafka服务器");
            status.put("suggestions", Arrays.asList(
                "检查Kafka服务器是否在 localhost:9092 运行",
                "检查网络连接是否正常",
                "检查防火墙设置",
                "验证Kafka配置是否正确"
            ));
            status.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(status);
        }
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            // 获取主题信息
            Map<String, TopicDescription> topics = adminClient.describeTopics(
                Collections.singletonList(gradeUpdatesTopic)
            ).all().get();
            
            TopicDescription topicDesc = topics.get(gradeUpdatesTopic);
            if (topicDesc != null) {
                Map<String, Object> topicInfo = new HashMap<>();
                topicInfo.put("name", topicDesc.name());
                topicInfo.put("partitions", topicDesc.partitions().size());
                topicInfo.put("replicationFactor", topicDesc.partitions().get(0).replicas().size());
                topicInfo.put("isInternal", topicDesc.isInternal());
                status.put("topic", topicInfo);
            }
            
            // 获取消费者组信息
            try {
                Map<String, ConsumerGroupDescription> consumerGroups = adminClient.describeConsumerGroups(
                    Collections.singletonList(consumerGroupId)
                ).all().get();
                
                ConsumerGroupDescription groupDesc = consumerGroups.get(consumerGroupId);
                if (groupDesc != null) {
                    Map<String, Object> groupInfo = new HashMap<>();
                    groupInfo.put("groupId", groupDesc.groupId());
                    groupInfo.put("state", groupDesc.state().toString());
                    groupInfo.put("members", groupDesc.members().size());
                    groupInfo.put("coordinator", groupDesc.coordinator().toString());
                    status.put("consumerGroup", groupInfo);
                }
            } catch (Exception e) {
                logger.warn("获取消费者组信息失败: {}", e.getMessage());
                status.put("consumerGroup", "未找到消费者组或消费者组未激活");
            }
            
            // 获取消费者组偏移量信息
            try {
                Map<TopicPartition, OffsetAndMetadata> offsets = adminClient.listConsumerGroupOffsets(consumerGroupId)
                    .partitionsToOffsetAndMetadata().get();
                
                List<Map<String, Object>> offsetInfo = new ArrayList<>();
                for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
                    Map<String, Object> partitionOffset = new HashMap<>();
                    partitionOffset.put("topic", entry.getKey().topic());
                    partitionOffset.put("partition", entry.getKey().partition());
                    partitionOffset.put("offset", entry.getValue().offset());
                    partitionOffset.put("metadata", entry.getValue().metadata());
                    offsetInfo.add(partitionOffset);
                }
                status.put("offsets", offsetInfo);
            } catch (Exception e) {
                logger.warn("获取偏移量信息失败: {}", e.getMessage());
                status.put("offsets", "无法获取偏移量信息");
            }
            
            status.put("connected", true);
            status.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            logger.error("获取Kafka状态失败: {}", e.getMessage(), e);
            status.put("connected", false);
            status.put("error", e.getMessage());
            status.put("timestamp", LocalDateTime.now());
        }
        
        return ResponseEntity.ok(status);
    }

    /**
     * 发送测试消息
     */
    @PostMapping("/test-message")
    public ResponseEntity<Map<String, Object>> sendTestMessage(
            @RequestParam(defaultValue = "1001") Long studentId,
            @RequestParam(defaultValue = "数学") String subject,
            @RequestParam(defaultValue = "85.5") BigDecimal score) {
        
        logger.info("发送测试消息 - 学生ID: {}, 科目: {}, 成绩: {}", studentId, subject, score);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建测试事件
            GradeUpdateEvent testEvent = new GradeUpdateEvent(
                studentId,
                "测试学生" + studentId,
                subject,
                null, // 旧成绩
                score, // 新成绩
                BigDecimal.valueOf(100), // 满分
                1L, // 教师ID
                "测试教师"
            );
            testEvent.setRemarks("这是一条测试消息");
            
            logger.info("准备发送Kafka测试消息: 学生ID={}, 科目={}, 成绩={}", studentId, subject, score);
            
            // 发送消息
            gradeUpdateProducer.publishGradeUpdateAsync(testEvent, 
                GradeUpdateProducerImpl.createDefaultCallback());
            
            logger.info("Kafka测试消息已提交发送: 事件ID={}", testEvent.getEventId());
            
            result.put("success", true);
            result.put("message", "测试消息发送成功");
            result.put("eventId", testEvent.getEventId());
            result.put("timestamp", LocalDateTime.now());
            
            logger.info("测试消息发送成功 - 事件ID: {}", testEvent.getEventId());
            
        } catch (Exception e) {
            logger.error("发送测试消息失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量发送测试消息
     */
    @PostMapping("/test-batch")
    public ResponseEntity<Map<String, Object>> sendBatchTestMessages(
            @RequestParam(defaultValue = "5") int count) {
        
        logger.info("批量发送测试消息 - 数量: {}", count);
        
        Map<String, Object> result = new HashMap<>();
        List<String> eventIds = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        String[] subjects = {"数学", "语文", "英语", "物理", "化学"};
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            try {
                Long studentId = 1001L + (i % 10); // 模拟10个不同的学生
                String subject = subjects[i % subjects.length];
                BigDecimal score = BigDecimal.valueOf(60 + random.nextInt(40)); // 60-100分
                
                GradeUpdateEvent testEvent = new GradeUpdateEvent(
                    studentId,
                    "测试学生" + studentId,
                    subject,
                    null,
                    score,
                    BigDecimal.valueOf(100),
                    1L,
                    "测试教师"
                );
                testEvent.setRemarks("批量测试消息 #" + (i + 1));
                
                gradeUpdateProducer.publishGradeUpdateAsync(testEvent, 
                    GradeUpdateProducerImpl.createDefaultCallback());
                
                eventIds.add(testEvent.getEventId());
                successCount++;
                
                // 添加小延迟避免过快发送
                Thread.sleep(100);
                
            } catch (Exception e) {
                logger.error("发送批量测试消息失败 - 索引: {}, 错误: {}", i, e.getMessage());
                failureCount++;
            }
        }
        
        result.put("success", failureCount == 0);
        result.put("totalCount", count);
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("eventIds", eventIds);
        result.put("timestamp", LocalDateTime.now());
        
        logger.info("批量测试消息发送完成 - 成功: {}, 失败: {}", successCount, failureCount);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取Kafka配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getKafkaConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("bootstrapServers", kafkaAdmin.getConfigurationProperties().get("bootstrap.servers"));
        config.put("gradeUpdatesTopic", gradeUpdatesTopic);
        config.put("consumerGroupId", consumerGroupId);
        config.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(config);
    }

    /**
     * 获取主题详细信息
     */
    @GetMapping("/topic/{topicName}")
    public ResponseEntity<Map<String, Object>> getTopicInfo(@PathVariable String topicName) {
        logger.info("获取主题详细信息: {}", topicName);
        
        Map<String, Object> topicInfo = new HashMap<>();
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            Map<String, TopicDescription> topics = adminClient.describeTopics(
                Collections.singletonList(topicName)
            ).all().get();
            
            TopicDescription topicDesc = topics.get(topicName);
            if (topicDesc != null) {
                topicInfo.put("name", topicDesc.name());
                topicInfo.put("partitions", topicDesc.partitions().size());
                topicInfo.put("replicationFactor", topicDesc.partitions().get(0).replicas().size());
                topicInfo.put("isInternal", topicDesc.isInternal());
                
                // 分区详细信息
                List<Map<String, Object>> partitionDetails = new ArrayList<>();
                topicDesc.partitions().forEach(partition -> {
                    Map<String, Object> partitionInfo = new HashMap<>();
                    partitionInfo.put("partition", partition.partition());
                    partitionInfo.put("leader", partition.leader().toString());
                    partitionInfo.put("replicas", partition.replicas().size());
                    partitionInfo.put("isr", partition.isr().size());
                    partitionDetails.add(partitionInfo);
                });
                topicInfo.put("partitionDetails", partitionDetails);
                
                topicInfo.put("found", true);
            } else {
                topicInfo.put("found", false);
                topicInfo.put("message", "主题不存在");
            }
            
        } catch (Exception e) {
            logger.error("获取主题信息失败: {}", e.getMessage(), e);
            topicInfo.put("found", false);
            topicInfo.put("error", e.getMessage());
        }
        
        topicInfo.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(topicInfo);
    }
}