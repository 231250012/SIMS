package com.sims.dto;

import com.sims.entity.Student;

import java.math.BigDecimal;

/**
 * 学生排行响应DTO
 */
public class StudentRankResponse {
    
    private Long id;
    private String name;
    private Integer age;
    private String className;
    private BigDecimal score;
    private Integer rank;
    
    public StudentRankResponse() {}
    
    public StudentRankResponse(Student student, Integer rank) {
        this.id = student.getId();
        this.name = student.getName();
        this.age = student.getAge();
        this.className = student.getClassName();
        this.score = student.getScore();
        this.rank = rank;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public Integer getRank() {
        return rank;
    }
    
    public void setRank(Integer rank) {
        this.rank = rank;
    }
    
    @Override
    public String toString() {
        return "StudentRankResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", className='" + className + '\'' +
                ", score=" + score +
                ", rank=" + rank +
                '}';
    }
}