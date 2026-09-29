package com.sims.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 管理员为教师更换管理班级
 */
public class AssignTeacherClassRequest {

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    /** 是否同时设为班主任 */
    private Boolean isHomeroomTeacher;

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Boolean getIsHomeroomTeacher() {
        return isHomeroomTeacher;
    }

    public void setIsHomeroomTeacher(Boolean isHomeroomTeacher) {
        this.isHomeroomTeacher = isHomeroomTeacher;
    }
}
