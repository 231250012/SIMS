package com.sims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * API统计实体类
 * 用于记录各个API接口的调用次数
 */
@Entity
@Table(name = "api_stats")
public class ApiStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100, unique = true)
    @NotBlank(message = "API端点不能为空")
    private String endpoint;
    
    @Column(name = "call_count", nullable = false)
    @PositiveOrZero(message = "调用次数不能为负数")
    private Long callCount = 0L;
    
    @UpdateTimestamp
    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;
    
    // 构造函数
    public ApiStats() {}
    
    public ApiStats(String endpoint) {
        this.endpoint = endpoint;
        this.callCount = 0L;
    }
    
    public ApiStats(String endpoint, Long callCount) {
        this.endpoint = endpoint;
        this.callCount = callCount;
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
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    // 便利方法
    public void incrementCallCount() {
        this.callCount++;
    }
    
    public void incrementCallCount(long increment) {
        this.callCount += increment;
    }
    
    @Override
    public String toString() {
        return "ApiStats{" +
                "id=" + id +
                ", endpoint='" + endpoint + '\'' +
                ", callCount=" + callCount +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}