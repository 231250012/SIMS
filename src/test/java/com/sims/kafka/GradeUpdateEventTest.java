package com.sims.kafka;

import com.sims.kafka.dto.GradeUpdateEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GradeUpdateEvent单元测试
 * 验证成绩更新事件的基本功能
 */
public class GradeUpdateEventTest {

    @Test
    public void testGradeUpdateEventCreation() {
        // 创建成绩更新事件
        GradeUpdateEvent event = new GradeUpdateEvent(
                1L, "张三", "数学", 
                new BigDecimal("80"), new BigDecimal("95"), new BigDecimal("100"),
                1L, "李老师"
        );

        // 验证基本属性
        assertNotNull(event.getEventId(), "事件ID不应为空");
        assertEquals(1L, event.getStudentId(), "学生ID应该正确");
        assertEquals("张三", event.getStudentName(), "学生姓名应该正确");
        assertEquals("数学", event.getSubject(), "科目应该正确");
        assertEquals(new BigDecimal("80"), event.getOldGrade(), "旧成绩应该正确");
        assertEquals(new BigDecimal("95"), event.getNewGrade(), "新成绩应该正确");
        assertEquals(new BigDecimal("100"), event.getMaxScore(), "满分应该正确");
        assertEquals(1L, event.getTeacherId(), "教师ID应该正确");
        assertEquals("李老师", event.getTeacherName(), "教师姓名应该正确");
        assertNotNull(event.getTimestamp(), "时间戳不应为空");
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

        // 测试无效事件 - 缺少学生ID
        GradeUpdateEvent invalidEvent1 = new GradeUpdateEvent();
        invalidEvent1.setStudentName("张三");
        invalidEvent1.setSubject("数学");
        invalidEvent1.setNewGrade(new BigDecimal("95"));
        invalidEvent1.setTeacherId(1L);
        assertFalse(invalidEvent1.isValid(), "缺少学生ID的事件应该是无效的");

        // 测试无效事件 - 缺少学生姓名
        GradeUpdateEvent invalidEvent2 = new GradeUpdateEvent();
        invalidEvent2.setStudentId(1L);
        invalidEvent2.setSubject("数学");
        invalidEvent2.setNewGrade(new BigDecimal("95"));
        invalidEvent2.setTeacherId(1L);
        assertFalse(invalidEvent2.isValid(), "缺少学生姓名的事件应该是无效的");

        // 测试无效事件 - 缺少科目
        GradeUpdateEvent invalidEvent3 = new GradeUpdateEvent();
        invalidEvent3.setStudentId(1L);
        invalidEvent3.setStudentName("张三");
        invalidEvent3.setNewGrade(new BigDecimal("95"));
        invalidEvent3.setTeacherId(1L);
        assertFalse(invalidEvent3.isValid(), "缺少科目的事件应该是无效的");
    }

    @Test
    public void testGradeChangeCalculation() {
        // 测试成绩提高
        GradeUpdateEvent improvementEvent = new GradeUpdateEvent(
                1L, "张三", "数学", 
                new BigDecimal("80"), new BigDecimal("95"), new BigDecimal("100"),
                1L, "李老师"
        );
        assertEquals(new BigDecimal("15"), improvementEvent.getGradeChange(), "成绩变化应该正确");
        assertTrue(improvementEvent.isImprovement(), "应该识别为成绩提高");

        // 测试成绩下降
        GradeUpdateEvent declineEvent = new GradeUpdateEvent(
                1L, "张三", "数学", 
                new BigDecimal("95"), new BigDecimal("80"), new BigDecimal("100"),
                1L, "李老师"
        );
        assertEquals(new BigDecimal("-15"), declineEvent.getGradeChange(), "成绩变化应该正确");
        assertFalse(declineEvent.isImprovement(), "应该识别为成绩下降");

        // 测试成绩无变化
        GradeUpdateEvent noChangeEvent = new GradeUpdateEvent(
                1L, "张三", "数学", 
                new BigDecimal("85"), new BigDecimal("85"), new BigDecimal("100"),
                1L, "李老师"
        );
        assertEquals(new BigDecimal("0"), noChangeEvent.getGradeChange(), "成绩变化应该为0");
        assertFalse(noChangeEvent.isImprovement(), "应该识别为无提高");
    }

    @Test
    public void testGradePercentageCalculation() {
        GradeUpdateEvent event = new GradeUpdateEvent(
                1L, "张三", "数学", 
                new BigDecimal("80"), new BigDecimal("95"), new BigDecimal("100"),
                1L, "李老师"
        );

        BigDecimal percentage = event.getGradePercentage();
        assertEquals(new BigDecimal("95.0000"), percentage, "成绩百分比应该正确");

        // 测试满分为0的情况
        event.setMaxScore(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, event.getGradePercentage(), "满分为0时百分比应该为0");

        // 测试新成绩为null的情况
        event.setNewGrade(null);
        event.setMaxScore(new BigDecimal("100"));
        assertEquals(BigDecimal.ZERO, event.getGradePercentage(), "新成绩为null时百分比应该为0");
    }

    @Test
    public void testEventIdUniqueness() {
        GradeUpdateEvent event1 = new GradeUpdateEvent();
        GradeUpdateEvent event2 = new GradeUpdateEvent();
        
        assertNotEquals(event1.getEventId(), event2.getEventId(), "不同事件的ID应该不同");
    }

    @Test
    public void testTimestampGeneration() {
        LocalDateTime before = LocalDateTime.now();
        GradeUpdateEvent event = new GradeUpdateEvent();
        LocalDateTime after = LocalDateTime.now();
        
        assertNotNull(event.getTimestamp(), "时间戳不应为空");
        assertTrue(event.getTimestamp().isAfter(before.minusSeconds(1)), "时间戳应该在创建时间之后");
        assertTrue(event.getTimestamp().isBefore(after.plusSeconds(1)), "时间戳应该在创建时间之前");
    }

    @Test
    public void testEqualsAndHashCode() {
        GradeUpdateEvent event1 = new GradeUpdateEvent();
        GradeUpdateEvent event2 = new GradeUpdateEvent();
        
        // 不同的事件应该不相等
        assertNotEquals(event1, event2, "不同事件应该不相等");
        assertNotEquals(event1.hashCode(), event2.hashCode(), "不同事件的hashCode应该不同");
        
        // 相同的事件应该相等
        event2.setEventId(event1.getEventId());
        assertEquals(event1, event2, "相同ID的事件应该相等");
        assertEquals(event1.hashCode(), event2.hashCode(), "相同ID的事件hashCode应该相同");
    }

    @Test
    public void testToString() {
        GradeUpdateEvent event = new GradeUpdateEvent(
                1L, "张三", "数学", 
                new BigDecimal("80"), new BigDecimal("95"), new BigDecimal("100"),
                1L, "李老师"
        );
        
        String toString = event.toString();
        assertNotNull(toString, "toString不应为空");
        assertTrue(toString.contains("张三"), "toString应该包含学生姓名");
        assertTrue(toString.contains("数学"), "toString应该包含科目");
        assertTrue(toString.contains("95"), "toString应该包含新成绩");
    }
}