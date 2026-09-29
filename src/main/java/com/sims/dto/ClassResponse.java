package com.sims.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 班级响应DTO
 */
public class ClassResponse {
    
    private Long id;
    private String name;
    private String grade;
    private String description;
    private Integer maxStudents;
    private Long teacherId;
    private String teacherName;
    private int studentCount;
    private int activeStudentCount;
    private BigDecimal averageScore;
    private List<StudentResponse> students;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 构造函数
    public ClassResponse() {}
    
    public ClassResponse(Long id, String name, Long teacherId, String teacherName, int studentCount) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.studentCount = studentCount;
        this.activeStudentCount = studentCount;
    }
    
    public ClassResponse(Long id, String name, String grade, String description, Integer maxStudents,
                        Long teacherId, String teacherName, int studentCount, int activeStudentCount,
                        BigDecimal averageScore) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.description = description;
        this.maxStudents = maxStudents;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.studentCount = studentCount;
        this.activeStudentCount = activeStudentCount;
        this.averageScore = averageScore;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getMaxStudents() {
        return maxStudents;
    }
    
    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
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
    
    public int getStudentCount() {
        return studentCount;
    }
    
    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }
    
    public int getActiveStudentCount() {
        return activeStudentCount;
    }
    
    public void setActiveStudentCount(int activeStudentCount) {
        this.activeStudentCount = activeStudentCount;
    }
    
    public BigDecimal getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }
    
    public List<StudentResponse> getStudents() {
        return students;
    }
    
    public void setStudents(List<StudentResponse> students) {
        this.students = students;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}