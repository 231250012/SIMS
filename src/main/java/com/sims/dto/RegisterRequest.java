package com.sims.dto;

import com.sims.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 注册请求DTO
 */
public class RegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String password;
    
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    @NotNull(message = "用户角色不能为空")
    private UserRole role;
    
    // 可选字段，用于学生注册时关联学生信息
    private String studentName;
    private Integer age;
    private Long classId;

    /** 教师注册：是否班主任（非班主任仅可改成绩） */
    private Boolean isHomeroomTeacher;
    
    public RegisterRequest() {}
    
    public RegisterRequest(String username, String password, String confirmPassword, UserRole role) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.role = role;
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
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Boolean getIsHomeroomTeacher() {
        return isHomeroomTeacher;
    }

    public void setIsHomeroomTeacher(Boolean isHomeroomTeacher) {
        this.isHomeroomTeacher = isHomeroomTeacher;
    }
    
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                ", role=" + role +
                ", studentName='" + studentName + '\'' +
                ", age=" + age +
                ", classId=" + classId +
                ", isHomeroomTeacher=" + isHomeroomTeacher +
                '}';
    }
}