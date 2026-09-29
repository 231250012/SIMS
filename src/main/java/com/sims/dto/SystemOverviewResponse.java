package com.sims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统概览响应DTO
 */
public class SystemOverviewResponse {
    
    private Long totalApiCalls;
    private Long totalApiCallsRedis;
    private Long totalStudents;
    private Long totalClasses;
    private Long totalUsers;
    private Long totalApiEndpoints;
    private Double averageApiCalls;
    private Double averageScore;
    private String topPerformingClass;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdateTime;
    
    private List<ApiStatResponse> topApiEndpoints;
    private List<ApiStatResponse> recentApiCalls;
    
    // 构造函数
    public SystemOverviewResponse() {}
    
    // Getters and Setters
    public Long getTotalApiCalls() {
        return totalApiCalls;
    }
    
    public void setTotalApiCalls(Long totalApiCalls) {
        this.totalApiCalls = totalApiCalls;
    }
    
    public Long getTotalApiCallsRedis() {
        return totalApiCallsRedis;
    }
    
    public void setTotalApiCallsRedis(Long totalApiCallsRedis) {
        this.totalApiCallsRedis = totalApiCallsRedis;
    }
    
    public Long getTotalStudents() {
        return totalStudents;
    }
    
    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }
    
    public Long getTotalClasses() {
        return totalClasses;
    }
    
    public void setTotalClasses(Long totalClasses) {
        this.totalClasses = totalClasses;
    }
    
    public Long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public Long getTotalApiEndpoints() {
        return totalApiEndpoints;
    }
    
    public void setTotalApiEndpoints(Long totalApiEndpoints) {
        this.totalApiEndpoints = totalApiEndpoints;
    }
    
    public Double getAverageApiCalls() {
        return averageApiCalls;
    }
    
    public void setAverageApiCalls(Double averageApiCalls) {
        this.averageApiCalls = averageApiCalls;
    }
    
    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    
    public List<ApiStatResponse> getTopApiEndpoints() {
        return topApiEndpoints;
    }
    
    public void setTopApiEndpoints(List<ApiStatResponse> topApiEndpoints) {
        this.topApiEndpoints = topApiEndpoints;
    }
    
    public List<ApiStatResponse> getRecentApiCalls() {
        return recentApiCalls;
    }
    
    public void setRecentApiCalls(List<ApiStatResponse> recentApiCalls) {
        this.recentApiCalls = recentApiCalls;
    }
    
    public Double getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
    
    public String getTopPerformingClass() {
        return topPerformingClass;
    }
    
    public void setTopPerformingClass(String topPerformingClass) {
        this.topPerformingClass = topPerformingClass;
    }
    
    @Override
    public String toString() {
        return "SystemOverviewResponse{" +
                "totalApiCalls=" + totalApiCalls +
                ", totalApiCallsRedis=" + totalApiCallsRedis +
                ", totalStudents=" + totalStudents +
                ", totalClasses=" + totalClasses +
                ", totalUsers=" + totalUsers +
                ", totalApiEndpoints=" + totalApiEndpoints +
                ", averageApiCalls=" + averageApiCalls +
                ", lastUpdateTime=" + lastUpdateTime +
                ", topApiEndpoints=" + (topApiEndpoints != null ? topApiEndpoints.size() : 0) + " items" +
                ", recentApiCalls=" + (recentApiCalls != null ? recentApiCalls.size() : 0) + " items" +
                '}';
    }
}