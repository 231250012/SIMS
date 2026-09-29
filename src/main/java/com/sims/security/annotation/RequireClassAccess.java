package com.sims.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 班级访问权限注解
 * 用于验证用户是否有权限访问指定班级
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireClassAccess {
    
    /**
     * 班级ID参数名称
     */
    String classIdParam() default "classId";
    
    /**
     * 错误消息
     */
    String message() default "无权访问该班级";
}