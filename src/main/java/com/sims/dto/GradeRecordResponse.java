package com.sims.dto;

import com.sims.entity.GradeRecord;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * 成绩记录响应DTO
 */
public class GradeRecordResponse {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private Long id;
    private Long studentId;
    private String studentName;
    private String className;
    private String subject;
    private BigDecimal score;
    private BigDecimal maxScore;
    private String examType;
    private String examDate;
    private String semester;
    private String academicYear;
    private Long teacherId;
    private String teacherName;
    private String remarks;
    private String createTime;
    private BigDecimal percentage;
    private String gradeLevel;
    
    // 构造函数
    public GradeRecordResponse() {}
    
    public GradeRecordResponse(GradeRecord gradeRecord) {
        this.id = gradeRecord.getId();
        this.studentId = gradeRecord.getStudent().getId();
        this.studentName = gradeRecord.getStudent().getName();
        this.className = gradeRecord.getStudent().getClassName();
        this.subject = gradeRecord.getSubject();
        this.score = gradeRecord.getScore();
        this.maxScore = gradeRecord.getMaxScore();
        this.examType = gradeRecord.getExamType();
        this.examDate = gradeRecord.getExamDate() != null ? gradeRecord.getExamDate().format(DATE_FMT) : null;
        this.semester = gradeRecord.getSemester();
        this.academicYear = gradeRecord.getAcademicYear();
        this.teacherId = gradeRecord.getTeacher() != null ? gradeRecord.getTeacher().getId() : null;
        this.teacherName = gradeRecord.getTeacher() != null ? gradeRecord.getTeacher().getUsername() : null;
        this.remarks = gradeRecord.getRemarks();
        this.createTime = gradeRecord.getCreateTime() != null ? gradeRecord.getCreateTime().format(DATETIME_FMT) : null;
        this.percentage = gradeRecord.getPercentage();
        this.gradeLevel = gradeRecord.getGradeLevel();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
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
    
    public String getExamDate() {
        return examDate;
    }
    
    public void setExamDate(String examDate) {
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
    
    public String getTeacherName() {
        return teacherName;
    }
    
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public String getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    
    public BigDecimal getPercentage() {
        return percentage;
    }
    
    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
    
    public String getGradeLevel() {
        return gradeLevel;
    }
    
    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }
    
    @Override
    public String toString() {
        return "GradeRecordResponse{" +
                "id=" + id +
                ", studentName='" + studentName + '\'' +
                ", className='" + className + '\'' +
                ", subject='" + subject + '\'' +
                ", score=" + score +
                ", maxScore=" + maxScore +
                ", examType='" + examType + '\'' +
                ", examDate=" + examDate +
                ", gradeLevel='" + gradeLevel + '\'' +
                '}';
    }
}