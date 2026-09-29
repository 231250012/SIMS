package com.sims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 班级实体类
 */
@Entity
@Table(name = "`class`")
public class Class {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    @NotBlank(message = "班级名称不能为空")
    @Size(min = 1, max = 50, message = "班级名称长度必须在1-50个字符之间")
    private String name;
    
    // 年级信息
    @Column(length = 10)
    private String grade;
    
    // 班级描述
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // 最大学生数量
    @Column(name = "max_students")
    private Integer maxStudents = 50;
    
    // 班级负责教师
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    // 班级中的学生
    @OneToMany(mappedBy = "studentClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Student> students = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // 构造函数
    public Class() {}
    
    public Class(String name, User teacher) {
        this.name = name;
        this.teacher = teacher;
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
    
    public User getTeacher() {
        return teacher;
    }
    
    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }
    
    public List<Student> getStudents() {
        return students;
    }
    
    public void setStudents(List<Student> students) {
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
    
    // 便利方法
    public void addStudent(Student student) {
        students.add(student);
        student.setStudentClass(this);
    }
    
    public void removeStudent(Student student) {
        students.remove(student);
        student.setStudentClass(null);
    }
    
    public int getStudentCount() {
        return students.size();
    }
    
    public int getActiveStudentCount() {
        return (int) students.stream()
                .filter(student -> "ACTIVE".equals(student.getStatus()))
                .count();
    }
    
    public boolean isFull() {
        return getActiveStudentCount() >= maxStudents;
    }
    
    @Override
    public String toString() {
        return "Class{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", grade='" + grade + '\'' +
                ", teacher=" + (teacher != null ? teacher.getUsername() : "null") +
                ", studentCount=" + getActiveStudentCount() +
                ", maxStudents=" + maxStudents +
                '}';
    }
}