package com.sims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建班级请求DTO
 */
public class CreateClassRequest {
    
    @NotBlank(message = "班级名称不能为空")
    @Size(min = 1, max = 50, message = "班级名称长度必须在1-50个字符之间")
    private String name;

    private String grade;
    private String description;
    private Integer maxStudents;
    
    private Long teacherId;
    
    // 构造函数
    public CreateClassRequest() {}
    
    public CreateClassRequest(String name, Long teacherId) {
        this.name = name;
        this.teacherId = teacherId;
    }
    
    // Getters and Setters
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
}