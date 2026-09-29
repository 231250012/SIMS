package com.sims.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 批量成绩更新请求DTO
 */
public class BatchGradeUpdateRequest {
    
    @NotEmpty(message = "成绩记录列表不能为空")
    @Size(max = 100, message = "单次最多更新100条成绩记录")
    @Valid
    private List<GradeRecordRequest> gradeRecords;
    
    // 构造函数
    public BatchGradeUpdateRequest() {}
    
    public BatchGradeUpdateRequest(List<GradeRecordRequest> gradeRecords) {
        this.gradeRecords = gradeRecords;
    }
    
    // Getters and Setters
    public List<GradeRecordRequest> getGradeRecords() {
        return gradeRecords;
    }
    
    public void setGradeRecords(List<GradeRecordRequest> gradeRecords) {
        this.gradeRecords = gradeRecords;
    }
    
    @Override
    public String toString() {
        return "BatchGradeUpdateRequest{" +
                "gradeRecords=" + (gradeRecords != null ? gradeRecords.size() : 0) + " records" +
                '}';
    }
}