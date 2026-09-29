package com.sims.dto;

import java.util.List;

/**
 * 教师统计信息响应DTO
 */
public class TeacherStatsResponse {
    
    private int totalClasses;
    private int totalStudents;
    private List<ClassResponse> myClasses;
    private List<StudentRankResponse> topStudents;
    
    public TeacherStatsResponse() {}
    
    public TeacherStatsResponse(int totalClasses, int totalStudents) {
        this.totalClasses = totalClasses;
        this.totalStudents = totalStudents;
    }
    
    public int getTotalClasses() {
        return totalClasses;
    }
    
    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }
    
    public int getTotalStudents() {
        return totalStudents;
    }
    
    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }
    
    public List<ClassResponse> getMyClasses() {
        return myClasses;
    }
    
    public void setMyClasses(List<ClassResponse> myClasses) {
        this.myClasses = myClasses;
    }
    
    public List<StudentRankResponse> getTopStudents() {
        return topStudents;
    }
    
    public void setTopStudents(List<StudentRankResponse> topStudents) {
        this.topStudents = topStudents;
    }
}
