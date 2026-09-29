package com.sims.entity;

/**
 * 用户角色枚举
 */
public enum UserRole {
    /**
     * 管理员 - 具有系统最高权限
     */
    ADMIN("管理员"),
    
    /**
     * 教师 - 可以管理分配给自己的班级和学生
     */
    TEACHER("教师"),
    
    /**
     * 学生 - 只能查看和修改自己的信息
     */
    STUDENT("学生");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}