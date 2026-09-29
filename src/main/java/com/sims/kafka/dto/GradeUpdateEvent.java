package com.sims.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 成绩更新事件数据传输对象
 * 包含成绩变更的完整信息，用于Kafka消息传递
 * 需求: 1.2
 */
public class GradeUpdateEvent {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("studentId")
    private Long studentId;

    @JsonProperty("studentName")
    private String studentName;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("oldGrade")
    private BigDecimal oldGrade;

    @JsonProperty("newGrade")
    private BigDecimal newGrade;

    @JsonProperty("maxScore")
    private BigDecimal maxScore;

    @JsonProperty("teacherId")
    private Long teacherId;

    @JsonProperty("teacherName")
    private String teacherName;

    @JsonProperty("examType")
    private String examType;

    @JsonProperty("semester")
    private String semester;

    @JsonProperty("academicYear")
    private String academicYear;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @JsonProperty("remarks")
    private String remarks;

    // ==================== 构造函数 ====================

    public GradeUpdateEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public GradeUpdateEvent(Long studentId, String studentName, String subject, 
                           BigDecimal oldGrade, BigDecimal newGrade, BigDecimal maxScore,
                           Long teacherId, String teacherName) {
        this();
        this.studentId = studentId;
        this.studentName = studentName;
        this.subject = subject;
        this.oldGrade = oldGrade;
        this.newGrade = newGrade;
        this.maxScore = maxScore;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    // ==================== Getter和Setter ====================

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public BigDecimal getOldGrade() {
        return oldGrade;
    }

    public void setOldGrade(BigDecimal oldGrade) {
        this.oldGrade = oldGrade;
    }

    public BigDecimal getNewGrade() {
        return newGrade;
    }

    public void setNewGrade(BigDecimal newGrade) {
        this.newGrade = newGrade;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
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

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查是否为有效的成绩更新事件
     */
    public boolean isValid() {
        return studentId != null &&
               studentName != null && !studentName.trim().isEmpty() &&
               subject != null && !subject.trim().isEmpty() &&
               newGrade != null &&
               timestamp != null;
    }

    /**
     * 计算成绩变化
     */
    public BigDecimal getGradeChange() {
        if (oldGrade == null || newGrade == null) {
            return BigDecimal.ZERO;
        }
        return newGrade.subtract(oldGrade);
    }

    /**
     * 判断成绩是否提高
     */
    public boolean isImprovement() {
        return getGradeChange().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 获取成绩百分比
     */
    public BigDecimal getGradePercentage() {
        if (newGrade == null || maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return newGrade.divide(maxScore, 4, BigDecimal.ROUND_HALF_UP)
                      .multiply(new BigDecimal("100"));
    }

    // ==================== Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradeUpdateEvent that = (GradeUpdateEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "GradeUpdateEvent{" +
                "eventId='" + eventId + '\'' +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", subject='" + subject + '\'' +
                ", oldGrade=" + oldGrade +
                ", newGrade=" + newGrade +
                ", maxScore=" + maxScore +
                ", teacherId=" + teacherId +
                ", teacherName='" + teacherName + '\'' +
                ", examType='" + examType + '\'' +
                ", semester='" + semester + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", timestamp=" + timestamp +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}