package com.sims.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 创建学生请求DTO
 */
public class CreateStudentRequest {
    
    @NotBlank(message = "学生姓名不能为空")
    @Size(min = 1, max = 50, message = "学生姓名长度必须在1-50个字符之间")
    private String name;
    
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 100, message = "年龄不能超过100")
    private Integer age;
    
    private Long classId;
    
    @DecimalMin(value = "0.0", message = "成绩不能为负数")
    @DecimalMax(value = "100.0", message = "成绩不能超过100分")
    private BigDecimal score;
    
    private String avatar;
    
    public CreateStudentRequest() {}
    
    public CreateStudentRequest(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
    
    public CreateStudentRequest(String name, Integer age, Long classId) {
        this.name = name;
        this.age = age;
        this.classId = classId;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
    
    @Override
    public String toString() {
        return "CreateStudentRequest{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", classId=" + classId +
                ", score=" + score +
                '}';
    }
}