package com.sims.security.aspect;


import com.sims.entity.Student;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.exception.ClassNotFoundException;
import com.sims.exception.StudentNotFoundException;
import com.sims.repository.ClassRepository;
import com.sims.repository.StudentRepository;
import com.sims.repository.UserRepository;
import com.sims.security.annotation.RequireClassAccess;
import com.sims.security.annotation.RequireRole;
import com.sims.security.annotation.RequireStudentAccess;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * 权限控制切面
 * 处理自定义权限注解的验证逻辑
 */
@Aspect
@Component
public class PermissionAspect {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    /**
     * 角色权限验证
     */
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        User currentUser = getCurrentUser();
        
        boolean hasRequiredRole = Arrays.stream(requireRole.value())
                .anyMatch(role -> role == currentUser.getRole());
        
        if (!hasRequiredRole) {
            throw new AccessDeniedException(requireRole.message());
        }
    }
    
    /**
     * 班级访问权限验证
     */
    @Before("@annotation(requireClassAccess)")
    public void checkClassAccess(JoinPoint joinPoint, RequireClassAccess requireClassAccess) {
        User currentUser = getCurrentUser();
        
        // 管理员有所有权限
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        
        // 获取班级ID参数
        Long classId = getParameterValue(joinPoint, requireClassAccess.classIdParam(), Long.class);
        if (classId == null) {
            throw new IllegalArgumentException("班级ID参数不能为空");
        }
        
        // 查找班级
        com.sims.entity.Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException(classId));
        
        // 教师只能访问自己管理的班级
        if (currentUser.getRole() == UserRole.TEACHER) {
            if (classEntity.getTeacher() == null || 
                !classEntity.getTeacher().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(requireClassAccess.message());
            }
            return;
        }
        
        // 学生无权访问班级管理功能
        throw new AccessDeniedException(requireClassAccess.message());
    }
    
    /**
     * 学生访问权限验证
     */
    @Before("@annotation(requireStudentAccess)")
    public void checkStudentAccess(JoinPoint joinPoint, RequireStudentAccess requireStudentAccess) {
        User currentUser = getCurrentUser();
        
        // 管理员有所有权限
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        
        // 获取学生ID参数
        Long studentId = getParameterValue(joinPoint, requireStudentAccess.studentIdParam(), Long.class);
        if (studentId == null) {
            throw new IllegalArgumentException("学生ID参数不能为空");
        }
        
        // 查找学生
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        
        // 教师只能访问自己班级的学生
        if (currentUser.getRole() == UserRole.TEACHER) {
            if (student.getStudentClass() == null || 
                student.getStudentClass().getTeacher() == null ||
                !student.getStudentClass().getTeacher().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(requireStudentAccess.message());
            }
            return;
        }
        
        // 学生只能访问自己的信息
        if (currentUser.getRole() == UserRole.STUDENT) {
            // 检查学生是否有对应的用户账号，且是当前用户
            if (!studentId.equals(currentUser.getId())) {
                throw new AccessDeniedException(requireStudentAccess.message());
            }
            return;
        }
        
        throw new AccessDeniedException(requireStudentAccess.message());
    }
    
    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("用户未登录");
        }
        
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("用户不存在"));
    }
    
    /**
     * 从方法参数中获取指定参数的值
     */
    @SuppressWarnings("unchecked")
    private <T> T getParameterValue(JoinPoint joinPoint, String paramName, Class<T> paramType) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            
            // 检查参数名称
            if (paramName.equals(parameter.getName())) {
                Object value = args[i];
                if (value != null && paramType.isAssignableFrom(value.getClass())) {
                    return (T) value;
                }
            }
            
            // 检查@PathVariable注解
            if (parameter.isAnnotationPresent(org.springframework.web.bind.annotation.PathVariable.class)) {
                org.springframework.web.bind.annotation.PathVariable pathVar = 
                    parameter.getAnnotation(org.springframework.web.bind.annotation.PathVariable.class);
                String pathVarName = pathVar.value().isEmpty() ? pathVar.name() : pathVar.value();
                if (pathVarName.isEmpty()) {
                    pathVarName = parameter.getName();
                }
                
                if (paramName.equals(pathVarName)) {
                    Object value = args[i];
                    if (value != null && paramType.isAssignableFrom(value.getClass())) {
                        return (T) value;
                    }
                }
            }
            
            // 检查@RequestParam注解
            if (parameter.isAnnotationPresent(org.springframework.web.bind.annotation.RequestParam.class)) {
                org.springframework.web.bind.annotation.RequestParam requestParam = 
                    parameter.getAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
                String requestParamName = requestParam.value().isEmpty() ? requestParam.name() : requestParam.value();
                if (requestParamName.isEmpty()) {
                    requestParamName = parameter.getName();
                }
                
                if (paramName.equals(requestParamName)) {
                    Object value = args[i];
                    if (value != null && paramType.isAssignableFrom(value.getClass())) {
                        return (T) value;
                    }
                }
            }
        }
        
        return null;
    }
}