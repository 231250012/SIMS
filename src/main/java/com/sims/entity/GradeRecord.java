package com.sims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成绩记录实体类
 */
@Entity
@Table(name = "grade_record", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_subject", columnNames = {"student_id", "subject"})
})
public class GradeRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 学生
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "学生不能为空")
    private Student student;
    
    // 科目名称
    @Column(nullable = false, length = 50)
    @NotBlank(message = "科目名称不能为空")
    private String subject;
    
    // 成绩
    @Column(precision = 5, scale = 2, nullable = false)
    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0.0", message = "成绩不能为负数")
    private BigDecimal score;
    
    // 满分
    @Column(name = "max_score", precision = 5, scale = 2)
    private BigDecimal maxScore = new BigDecimal("100.00");
    
    // 考试类型
    @Column(name = "exam_type", length = 20)
    private String examType = "REGULAR"; // REGULAR, MIDTERM, FINAL, MOCK
    
    // 考试日期
    @Column(name = "exam_date", nullable = false)
    @NotNull(message = "考试日期不能为空")
    private LocalDate examDate;
    
    // 学期
    @Column(length = 20)
    private String semester;
    
    // 学年
    @Column(name = "academic_year", length = 10)
    private String academicYear;
    
    // 任课教师
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    // 备注
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    // 构造函数
    public GradeRecord() {}
    
    public GradeRecord(Student student, String subject, BigDecimal score, LocalDate examDate) {
        this.student = student;
        this.subject = subject;
        this.score = score;
        this.examDate = examDate;
    }
    
    public GradeRecord(Student student, String subject, BigDecimal score, BigDecimal maxScore, 
                      String examType, LocalDate examDate, String semester, String academicYear, User teacher) {
        this.student = student;
        this.subject = subject;
        this.score = score;
        this.maxScore = maxScore;
        this.examType = examType;
        this.examDate = examDate;
        this.semester = semester;
        this.academicYear = academicYear;
        this.teacher = teacher;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
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
    
    public User getTeacher() {
        return teacher;
    }
    
    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    // 便利方法
    public BigDecimal getPercentage() {
        if (maxScore != null && maxScore.compareTo(BigDecimal.ZERO) > 0) {
            return score.divide(maxScore, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
    
    public String getGradeLevel() {
        BigDecimal percentage = getPercentage();
        if (percentage.compareTo(new BigDecimal("90")) >= 0) return "优秀";
        if (percentage.compareTo(new BigDecimal("80")) >= 0) return "良好";
        if (percentage.compareTo(new BigDecimal("70")) >= 0) return "中等";
        if (percentage.compareTo(new BigDecimal("60")) >= 0) return "及格";
        return "不及格";
    }
    
    @Override
    public String toString() {
        return "GradeRecord{" +
                "id=" + id +
                ", student=" + (student != null ? student.getName() : "null") +
                ", subject='" + subject + '\'' +
                ", score=" + score +
                ", maxScore=" + maxScore +
                ", examType='" + examType + '\'' +
                ", examDate=" + examDate +
                ", semester='" + semester + '\'' +
                ", academicYear='" + academicYear + '\'' +
                '}';
    }
}