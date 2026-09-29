package com.sims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * API统计响应DTO
 */
public class ApiStatResponse {
    
    private Long id;
    private String endpoint;
    private Long callCount;
    private Long redisCallCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdate;
    
    // 构造函数
    public ApiStatResponse() {}
    
    public ApiStatResponse(String endpoint, Long callCount) {
        this.endpoint = endpoint;
        this.callCount = callCount;
    }
    
    public ApiStatResponse(Long id, String endpoint, Long callCount, LocalDateTime lastUpdate) {
        this.id = id;
        this.endpoint = endpoint;
        this.callCount = callCount;
        this.lastUpdate = lastUpdate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public Long getCallCount() {
        return callCount;
    }
    
    public void setCallCount(Long callCount) {
        this.callCount = callCount;
    }
    
    public Long getRedisCallCount() {
        return redisCallCount;
    }
    
    public void setRedisCallCount(Long redisCallCount) {
        this.redisCallCount = redisCallCount;
    }
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    @Override
    public String toString() {
        return "ApiStatResponse{" +
                "id=" + id +
                ", endpoint='" + endpoint + '\'' +
                ", callCount=" + callCount +
                ", redisCallCount=" + redisCallCount +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}