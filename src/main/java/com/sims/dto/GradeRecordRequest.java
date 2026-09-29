package com.sims.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成绩记录请求DTO
 */
public class GradeRecordRequest {
    
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    
    @NotBlank(message = "科目名称不能为空")
    @Size(max = 50, message = "科目名称长度不能超过50个字符")
    private String subject;
    
    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0.0", message = "成绩不能为负数")
    @DecimalMax(value = "999.99", message = "成绩不能超过999.99")
    private BigDecimal score;
    
    @DecimalMin(value = "0.0", message = "满分不能为负数")
    @DecimalMax(value = "999.99", message = "满分不能超过999.99")
    private BigDecimal maxScore = new BigDecimal("100.00");
    
    @Size(max = 20, message = "考试类型长度不能超过20个字符")
    private String examType = "REGULAR";
    
    @NotNull(message = "考试日期不能为空")
    private LocalDate examDate;
    
    @Size(max = 20, message = "学期长度不能超过20个字符")
    private String semester;
    
    @Size(max = 10, message = "学年长度不能超过10个字符")
    private String academicYear;
    
    private Long teacherId;
    
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String remarks;
    
    // 构造函数
    public GradeRecordRequest() {}
    
    public GradeRecordRequest(Long studentId, String subject, BigDecimal score, LocalDate examDate) {
        this.studentId = studentId;
        this.subject = subject;
        this.score = score;
        this.examDate = examDate;
    }
    
    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public BigDecimal getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }
    
    public String getExamType() {
        return examType;
    }
    
    public void setExamType(String examType) {
        this.examType = examType;
    }
    
    public LocalDate getExamDate() {
        return examDate;
    }
    
    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
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
    
    public Long getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    @Override
    public String toString() {
        return "GradeRecordRequest{" +
                "studentId=" + studentId +
                ", subject='" + subject + '\'' +
                ", score=" + score +
                ", maxScore=" + maxScore +
                ", examType='" + examType + '\'' +
                ", examDate=" + examDate +
                ", semester='" + semester + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", teacherId=" + teacherId +
                '}';
    }
}