package com.sims.dto;

import com.sims.entity.UserRole;

/**
 * 认证响应DTO
 */
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private UserRole role;
    private Boolean isHomeroomTeacher;
    private String message;
    
    public AuthResponse() {}
    
    public AuthResponse(String token, Long userId, String username, UserRole role) {
        this(token, userId, username, role, false);
    }

    public AuthResponse(String token, Long userId, String username, UserRole role, Boolean isHomeroomTeacher) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.isHomeroomTeacher = Boolean.TRUE.equals(isHomeroomTeacher);
        this.message = "登录成功";
    }
    
    public AuthResponse(String message) {
        this.message = message;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getIsHomeroomTeacher() {
        return Boolean.TRUE.equals(isHomeroomTeacher);
    }

    public void setIsHomeroomTeacher(Boolean isHomeroomTeacher) {
        this.isHomeroomTeacher = Boolean.TRUE.equals(isHomeroomTeacher);
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
