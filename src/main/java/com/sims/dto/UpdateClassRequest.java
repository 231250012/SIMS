package com.sims.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新班级请求DTO
 */
public class UpdateClassRequest {
    
    @Size(min = 1, max = 50, message = "班级名称长度必须在1-50个字符之间")
    private String name;
    
    private Long teacherId;
    
    // 构造函数
    public UpdateClassRequest() {}
    
    public UpdateClassRequest(String name, Long teacherId) {
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
    
    public Long getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}