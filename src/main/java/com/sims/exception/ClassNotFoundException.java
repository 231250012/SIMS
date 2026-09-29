package com.sims.exception;

/**
 * 班级未找到异常
 */
public class ClassNotFoundException extends RuntimeException {
    
    public ClassNotFoundException(Long classId) {
        super("班级不存在: " + classId);
    }
    
    public ClassNotFoundException(String message) {
        super(message);
    }
    
    public ClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}