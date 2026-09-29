package com.sims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体类
 * 支持管理员、教师、学生三种角色
 */
@Entity
@Table(name = "`user`")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    @Column(nullable = false)
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /** 教师是否班主任；非班主任仅可修改学生成绩 */
    @Column(name = "is_homeroom_teacher", nullable = false)
    private Boolean isHomeroomTeacher = false;
    
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    // 教师管理的班级（仅当角色为教师时有效）
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Class> managedClasses = new ArrayList<>();
    
    // 学生信息（仅当角色为学生时有效）
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Student studentInfo;
    
    // 构造函数
    public User() {}
    
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getIsHomeroomTeacher() {
        return isHomeroomTeacher != null && isHomeroomTeacher;
    }

    public void setIsHomeroomTeacher(Boolean isHomeroomTeacher) {
        this.isHomeroomTeacher = isHomeroomTeacher != null && isHomeroomTeacher;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public List<Class> getManagedClasses() {
        return managedClasses;
    }
    
    public void setManagedClasses(List<Class> managedClasses) {
        this.managedClasses = managedClasses;
    }
    
    public Student getStudentInfo() {
        return studentInfo;
    }
    
    public void setStudentInfo(Student studentInfo) {
        this.studentInfo = studentInfo;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", createTime=" + createTime +
                '}';
    }
}