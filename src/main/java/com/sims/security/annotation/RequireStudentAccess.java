package com.sims.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 学生访问权限注解
 * 用于验证用户是否有权限访问指定学生信息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireStudentAccess {
    
    /**
     * 学生ID参数名称
     */
    String studentIdParam() default "studentId";
    
    /**
     * 错误消息
     */
    String message() default "无权访问该学生信息";
}