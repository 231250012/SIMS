package com.sims.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 成绩趋势响应DTO
 */
public class GradeTrendResponse {
    
    private Long studentId;
    private String studentName;
    private String subject;
    private List<GradeTrendPoint> trendPoints;
    private BigDecimal averageScore;
    private BigDecimal highestScore;
    private BigDecimal lowestScore;
    private String trend; // IMPROVING, DECLINING, STABLE
    
    // 构造函数
    public GradeTrendResponse() {}
    
    public GradeTrendResponse(Long studentId, String studentName, String subject) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.subject = subject;
    }
    
    // 内部类：成绩趋势点
    public static class GradeTrendPoint {
        private LocalDate examDate;
        private BigDecimal score;
        private BigDecimal maxScore;
        private String examType;
        private BigDecimal percentage;
        
        public GradeTrendPoint() {}
        
        public GradeTrendPoint(LocalDate examDate, BigDecimal score, BigDecimal maxScore, String examType) {
            this.examDate = examDate;
            this.score = score;
            this.maxScore = maxScore;
            this.examType = examType;
            if (maxScore != null && maxScore.compareTo(BigDecimal.ZERO) > 0) {
                this.percentage = score.divide(maxScore, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
            }
        }
        
        // Getters and Setters
        public LocalDate getExamDate() {
            return examDate;
        }
        
        public void setExamDate(LocalDate examDate) {
            this.examDate = examDate;
        }
        
        public BigDecimal getScore() {
            return score;
        }
        
        public void setScore(BigDecimal score) {
            this.score = score;
        }
        
        public BigDecimal getMaxScore() {
            return maxScore;
        }
        
        public void setMaxScore(BigDecimal maxScore) {
            this.maxScore = maxScore;
        }
        
        public String getExamType() {
            return examType;
        }
        
        public void setExamType(String examType) {
            this.examType = examType;
        }
        
        public BigDecimal getPercentage() {
            return percentage;
        }
        
        public void setPercentage(BigDecimal percentage) {
            this.percentage = percentage;
        }
    }
    
    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentName() {
        return studentName;
    }
    
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public List<GradeTrendPoint> getTrendPoints() {
        return trendPoints;
    }
    
    public void setTrendPoints(List<GradeTrendPoint> trendPoints) {
        this.trendPoints = trendPoints;
    }
    
    public BigDecimal getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }
    
    public BigDecimal getHighestScore() {
        return highestScore;
    }
    
    public void setHighestScore(BigDecimal highestScore) {
        this.highestScore = highestScore;
    }
    
    public BigDecimal getLowestScore() {
        return lowestScore;
    }
    
    public void setLowestScore(BigDecimal lowestScore) {
        this.lowestScore = lowestScore;
    }
    
    public String getTrend() {
        return trend;
    }
    
    public void setTrend(String trend) {
        this.trend = trend;
    }
    
    @Override
    public String toString() {
        return "GradeTrendResponse{" +
                "studentName='" + studentName + '\'' +
                ", subject='" + subject + '\'' +
                ", averageScore=" + averageScore +
                ", trend='" + trend + '\'' +
                ", pointsCount=" + (trendPoints != null ? trendPoints.size() : 0) +
                '}';
    }
}