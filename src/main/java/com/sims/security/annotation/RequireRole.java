package com.sims.security.annotation;

import com.sims.entity.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 用于方法级别的角色权限控制
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * 需要的角色
     */
    UserRole[] value();
    
    /**
     * 错误消息
     */
    String message() default "权限不足";
}