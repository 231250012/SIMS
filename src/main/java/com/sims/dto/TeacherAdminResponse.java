package com.sims.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 教师管理视图
 */
public class TeacherAdminResponse {
    private Long id;
    private String username;
    private Boolean isHomeroomTeacher;
    private LocalDateTime createTime;
    private List<ClassBrief> managedClasses = new ArrayList<>();

    public static class ClassBrief {
        private Long id;
        private String name;
        private String grade;

        public ClassBrief() {}

        public ClassBrief(Long id, String name, String grade) {
            this.id = id;
            this.name = name;
            this.grade = grade;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Boolean getIsHomeroomTeacher() { return Boolean.TRUE.equals(isHomeroomTeacher); }
    public void setIsHomeroomTeacher(Boolean isHomeroomTeacher) {
        this.isHomeroomTeacher = Boolean.TRUE.equals(isHomeroomTeacher);
    }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public List<ClassBrief> getManagedClasses() { return managedClasses; }
    public void setManagedClasses(List<ClassBrief> managedClasses) { this.managedClasses = managedClasses; }
}
