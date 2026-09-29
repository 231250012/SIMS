package com.sims.dto;

/**
 * 成绩分布响应DTO
 */
public class ScoreDistributionResponse {
    
    private String name;
    private Long count;
    
    public ScoreDistributionResponse() {}
    
    public ScoreDistributionResponse(String name, Long count) {
        this.name = name;
        this.count = count;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Long getCount() {
        return count;
    }
    
    public void setCount(Long count) {
        this.count = count;
    }
    
    @Override
    public String toString() {
        return "ScoreDistributionResponse{" +
                "name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}