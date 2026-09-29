package com.sims.kafka;

import com.sims.kafka.config.KafkaConfiguration;
import com.sims.kafka.dto.GradeUpdateEvent;
import com.sims.kafka.producer.GradeUpdateProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kafka集成测试
 * 验证Kafka生产者和消费者的基本功能
 */
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, 
               brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"},
               topics = {"grade-updates", "grade-updates-dlq"})
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9093",
    "kafka.topic.grade-updates=grade-updates",
    "kafka.topic.grade-updates-dlq=grade-updates-dlq"
})
public class KafkaIntegrationTest {

    @Autowired
    private GradeUpdateProducer gradeUpdateProducer;

    @Test
    public void testKafkaProducerConfiguration() {
        assertNotNull(gradeUpdateProducer, "GradeUpdateProducer应该被正确注入");
    }

    @Test
    public void testPublishGradeUpdateEvent() throws Exception {
        // 创建测试事件
        GradeUpdateEvent event = new GradeUpdateEvent(
                1L, "张三", "数学", 
                new BigDecimal("80"), new BigDecimal("95"), new BigDecimal("100"),
                1L, "李老师"
        );
        event.setExamType("期中考试");
        event.setSemester("2024春季");
        event.setAcademicYear("2023-2024");

        // 发布事件
        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future = 
                gradeUpdateProducer.publishGradeUpdate(event);

        // 等待发送完成
        org.springframework.kafka.support.SendResult<String, Object> result = 
                future.get(10, TimeUnit.SECONDS);

        // 验证结果
        assertNotNull(result, "发送结果不应为空");
        assertEquals("grade-updates", result.getRecordMetadata().topic(), "主题名称应该正确");
        assertTrue(result.getRecordMetadata().offset() >= 0, "偏移量应该大于等于0");
        
        System.out.println("成功发布成绩更新事件到Kafka: " + event.getEventId());
        System.out.println("主题: " + result.getRecordMetadata().topic());
        System.out.println("分区: " + result.getRecordMetadata().partition());
        System.out.println("偏移量: " + result.getRecordMetadata().offset());
    }

    @Test
    public void testInvalidGradeUpdateEvent() {
        // 创建无效事件（缺少必需字段）
        GradeUpdateEvent invalidEvent = new GradeUpdateEvent();
        invalidEvent.setStudentId(null); // 缺少学生ID

        // 发布无效事件应该失败
        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> future = 
                gradeUpdateProducer.publishGradeUpdate(invalidEvent);

        // 验证异常
        assertThrows(Exception.class, () -> {
            future.get(5, TimeUnit.SECONDS);
        }, "发布无效事件应该抛出异常");
    }

    @Test
    public void testGradeUpdateEventValidation() {
        // 测试有效事件
        GradeUpdateEvent validEvent = new GradeUpdateEvent(
                1L, "张三", "数学", 
                new BigDecimal("80"), new BigDecimal("95"), new BigDecimal("100"),
                1L, "李老师"
        );
        assertTrue(validEvent.isValid(), "完整的事件应该是有效的");

        // 测试无效事件
        GradeUpdateEvent invalidEvent = new GradeUpdateEvent();
        assertFalse(invalidEvent.isValid(), "空事件应该是无效的");

        // 测试部分无效事件
        GradeUpdateEvent partialEvent = new GradeUpdateEvent();
        partialEvent.setStudentId(1L);
        partialEvent.setStudentName("张三");
        // 缺少科目
        assertFalse(partialEvent.isValid(), "缺少必需字段的事件应该是无效的");
    }
}