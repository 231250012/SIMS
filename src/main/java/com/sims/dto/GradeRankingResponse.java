package com.sims.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩排行榜响应DTO
 */
public class GradeRankingResponse {
    
    private String subject;
    private String scope; // CLASS, SCHOOL
    private Long classId;
    private String className;
    private List<RankingEntry> rankings;
    private BigDecimal averageScore;
    private BigDecimal highestScore;
    private BigDecimal lowestScore;
    private Integer totalStudents;
    
    // 构造函数
    public GradeRankingResponse() {}
    
    public GradeRankingResponse(String subject, String scope) {
        this.subject = subject;
        this.scope = scope;
    }
    
    // 内部类：排行榜条目
    public static class RankingEntry {
        private Integer rank;
        private Long studentId;
        private String studentName;
        private String className;
        private BigDecimal score;
        private BigDecimal maxScore;
        private BigDecimal percentage;
        private String gradeLevel;
        
        public RankingEntry() {}
        
        public RankingEntry(Integer rank, Long studentId, String studentName, String className, 
                           BigDecimal score, BigDecimal maxScore) {
            this.rank = rank;
            this.studentId = studentId;
            this.studentName = studentName;
            this.className = className;
            this.score = score;
            this.maxScore = maxScore;
            if (maxScore != null && maxScore.compareTo(BigDecimal.ZERO) > 0) {
                this.percentage = score.divide(maxScore, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
                BigDecimal percent = this.percentage;
                if (percent.compareTo(new BigDecimal("90")) >= 0) this.gradeLevel = "优秀";
                else if (percent.compareTo(new BigDecimal("80")) >= 0) this.gradeLevel = "良好";
                else if (percent.compareTo(new BigDecimal("70")) >= 0) this.gradeLevel = "中等";
                else if (percent.compareTo(new BigDecimal("60")) >= 0) this.gradeLevel = "及格";
                else this.gradeLevel = "不及格";
            }
        }
        
        // Getters and Setters
        public Integer getRank() {
            return rank;
        }
        
        public void setRank(Integer rank) {
            this.rank = rank;
        }
        
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
        
        public BigDecimal getMaxScore() {
            return maxScore;
        }
        
        public void setMaxScore(BigDecimal maxScore) {
            this.maxScore = maxScore;
        }
        
        public BigDecimal getPercentage() {
            return percentage;
        }
        
        public void setPercentage(BigDecimal percentage) {
            this.percentage = percentage;
        }
        
        public String getGradeLevel() {
            return gradeLevel;
        }
        
        public void setGradeLevel(String gradeLevel) {
            this.gradeLevel = gradeLevel;
        }
    }
    
    // Getters and Setters
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public List<RankingEntry> getRankings() {
        return rankings;
    }
    
    public void setRankings(List<RankingEntry> rankings) {
        this.rankings = rankings;
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
    
    public Integer getTotalStudents() {
        return totalStudents;
    }
    
    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }
    
    @Override
    public String toString() {
        return "GradeRankingResponse{" +
                "subject='" + subject + '\'' +
                ", scope='" + scope + '\'' +
                ", className='" + className + '\'' +
                ", totalStudents=" + totalStudents +
                ", averageScore=" + averageScore +
                '}';
    }
}