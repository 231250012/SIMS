package com.sims.exception;

/**
 * 学生未找到异常
 */
public class StudentNotFoundException extends RuntimeException {
    
    public StudentNotFoundException(Long studentId) {
        super("学生不存在: " + studentId);
    }
    
    public StudentNotFoundException(String message) {
        super(message);
    }
    
    public StudentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}