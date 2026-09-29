package com.sims.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 通知消息数据模型
 * 用于向学生发送成绩更新通知
 * 需求: 3.2
 */
public class NotificationMessage {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("studentId")
    private Long studentId;

    @JsonProperty("studentName")
    private String studentName;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("newGrade")
    private BigDecimal newGrade;

    @JsonProperty("maxScore")
    private BigDecimal maxScore;

    @JsonProperty("gradePercentage")
    private BigDecimal gradePercentage;

    @JsonProperty("updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @JsonProperty("messageContent")
    private String messageContent;

    @JsonProperty("type")
    private NotificationType type;

    @JsonProperty("priority")
    private Priority priority;

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    @JsonProperty("teacherName")
    private String teacherName;

    @JsonProperty("examType")
    private String examType;

    @JsonProperty("isRead")
    private boolean isRead;

    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ==================== 构造函数 ====================

    public NotificationMessage() {
        this.messageId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.type = NotificationType.GRADE_UPDATE;
        this.priority = Priority.NORMAL;
    }

    public NotificationMessage(Long studentId, String studentName, String subject, 
                              BigDecimal newGrade, BigDecimal maxScore, LocalDateTime updateTime) {
        this();
        this.studentId = studentId;
        this.studentName = studentName;
        this.subject = subject;
        this.newGrade = newGrade;
        this.maxScore = maxScore;
        this.updateTime = updateTime;
        this.gradePercentage = calculatePercentage(newGrade, maxScore);
        this.messageContent = generateMessageContent();
    }

    // ==================== Getter和Setter ====================

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public BigDecimal getNewGrade() {
        return newGrade;
    }

    public void setNewGrade(BigDecimal newGrade) {
        this.newGrade = newGrade;
        this.gradePercentage = calculatePercentage(newGrade, this.maxScore);
        this.messageContent = generateMessageContent();
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
        this.gradePercentage = calculatePercentage(this.newGrade, maxScore);
        this.messageContent = generateMessageContent();
    }

    public BigDecimal getGradePercentage() {
        return gradePercentage;
    }

    public void setGradePercentage(BigDecimal gradePercentage) {
        this.gradePercentage = gradePercentage;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // 为了兼容性，添加recipientId的getter/setter
    public Long getRecipientId() {
        return studentId;
    }

    public void setRecipientId(Long recipientId) {
        this.studentId = recipientId;
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算成绩百分比
     */
    private BigDecimal calculatePercentage(BigDecimal grade, BigDecimal maxScore) {
        if (grade == null || maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return grade.divide(maxScore, 4, BigDecimal.ROUND_HALF_UP)
                   .multiply(new BigDecimal("100"));
    }

    /**
     * 生成通知消息内容
     */
    private String generateMessageContent() {
        if (studentName == null || subject == null || newGrade == null) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        content.append("亲爱的 ").append(studentName).append("，");
        content.append("您的 ").append(subject).append(" 成绩已更新：");
        content.append(newGrade);
        
        if (maxScore != null && maxScore.compareTo(BigDecimal.ZERO) > 0) {
            content.append("/").append(maxScore);
            content.append(" (").append(gradePercentage.setScale(1, BigDecimal.ROUND_HALF_UP)).append("%)");
        }
        
        if (examType != null && !examType.trim().isEmpty()) {
            content.append(" - ").append(examType);
        }
        
        if (teacherName != null && !teacherName.trim().isEmpty()) {
            content.append("，由 ").append(teacherName).append(" 老师录入");
        }
        
        content.append("。");
        
        return content.toString();
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * 检查通知是否有效
     */
    public boolean isValid() {
        return studentId != null && 
               studentName != null && !studentName.trim().isEmpty() &&
               subject != null && !subject.trim().isEmpty() &&
               newGrade != null &&
               updateTime != null;
    }

    // ==================== Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationMessage that = (NotificationMessage) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "messageId='" + messageId + '\'' +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", subject='" + subject + '\'' +
                ", newGrade=" + newGrade +
                ", maxScore=" + maxScore +
                ", gradePercentage=" + gradePercentage +
                ", updateTime=" + updateTime +
                ", messageContent='" + messageContent + '\'' +
                ", type=" + type +
                ", teacherName='" + teacherName + '\'' +
                ", examType='" + examType + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }

    // ==================== 枚举定义 ====================

    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        GRADE_UPDATE("成绩更新"),
        EXAM_REMINDER("考试提醒"),
        ASSIGNMENT_DUE("作业截止"),
        SYSTEM_NOTICE("系统通知");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 优先级枚举
     */
    public enum Priority {
        LOW("低"),
        NORMAL("普通"),
        HIGH("高"),
        URGENT("紧急");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}