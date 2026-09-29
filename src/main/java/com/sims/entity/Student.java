package com.sims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生实体类
 */
@Entity
@Table(name = "student")
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "学生姓名不能为空")
    @Size(min = 1, max = 50, message = "学生姓名长度必须在1-50个字符之间")
    private String name;
    
    // 学号
    @Column(name = "student_id", unique = true, length = 20)
    private String studentId;
    
    @Column(nullable = false)
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 100, message = "年龄不能超过100")
    private Integer age;
    
    // 性别
    @Column(length = 10)
    private String gender;
    
    // 出生日期
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    // 联系电话
    @Column(length = 20)
    private String phone;
    
    // 家庭住址
    @Column(columnDefinition = "TEXT")
    private String address;
    
    // 家长姓名
    @Column(name = "parent_name", length = 50)
    private String parentName;
    
    // 家长电话
    @Column(name = "parent_phone", length = 20)
    private String parentPhone;
    
    // 入学日期
    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;
    
    // 学生状态
    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, GRADUATED, TRANSFERRED, SUSPENDED
    
    // 所属班级
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Class studentClass;
    
    // 成绩（平均分）
    @Column(precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "成绩不能为负数")
    @DecimalMax(value = "100.0", message = "成绩不能超过100分")
    private BigDecimal score = BigDecimal.ZERO;
    
    // 头像URL
    @Column(length = 255)
    private String avatar;
    
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 关联的用户账号（如果学生有系统账号）
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // 构造函数
    public Student() {}
    
    public Student(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
    
    public Student(String name, Integer age, Class studentClass) {
        this.name = name;
        this.age = age;
        this.studentClass = studentClass;
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
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public LocalDate getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getParentName() {
        return parentName;
    }
    
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    
    public String getParentPhone() {
        return parentPhone;
    }
    
    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }
    
    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Class getStudentClass() {
        return studentClass;
    }
    
    public void setStudentClass(Class studentClass) {
        this.studentClass = studentClass;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // 便利方法
    public String getClassName() {
        return studentClass != null ? studentClass.getName() : "未分配班级";
    }
    
    public String getTeacherName() {
        return studentClass != null && studentClass.getTeacher() != null 
            ? studentClass.getTeacher().getUsername() : "无";
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", score=" + score +
                ", className='" + getClassName() + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}