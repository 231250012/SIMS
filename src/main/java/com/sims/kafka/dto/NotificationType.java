package com.sims.kafka.dto;

/**
 * 通知类型枚举
 * 定义系统中不同类型的通知
 */
public enum NotificationType {
    
    /**
     * 成绩更新通知
     */
    GRADE_UPDATE("成绩更新"),
    
    /**
     * 成绩提醒通知
     */
    GRADE_REMINDER("成绩提醒"),
    
    /**
     * 系统通知
     */
    SYSTEM_NOTIFICATION("系统通知"),
    
    /**
     * 紧急通知
     */
    URGENT_NOTIFICATION("紧急通知");

    private final String displayName;

    NotificationType(String displayName) {
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