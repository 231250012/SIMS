package com.sims.security.service;


import com.sims.entity.Student;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.exception.ClassNotFoundException;
import com.sims.exception.StudentNotFoundException;
import com.sims.repository.ClassRepository;
import com.sims.repository.StudentRepository;
import com.sims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 权限检查服务
 * 提供程序化的权限验证方法
 */
@Service
public class PermissionService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("用户未登录");
        }
        
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("用户不存在"));
    }
    
    /**
     * 检查用户是否有指定角色
     */
    public boolean hasRole(UserRole role) {
        try {
            User currentUser = getCurrentUser();
            return currentUser.getRole() == role;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查用户是否有任意一个指定角色
     */
    public boolean hasAnyRole(UserRole... roles) {
        try {
            User currentUser = getCurrentUser();
            for (UserRole role : roles) {
                if (currentUser.getRole() == role) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查用户是否有权限访问指定班级
     */
    public boolean canAccessClass(Long classId) {
        try {
            User currentUser = getCurrentUser();
            
            // 管理员有所有权限
            if (currentUser.getRole() == UserRole.ADMIN) {
                return true;
            }
            
            // 查找班级
            com.sims.entity.Class classEntity = classRepository.findById(classId).orElse(null);
            if (classEntity == null) {
                return false;
            }
            
            // 教师只能访问自己管理的班级
            if (currentUser.getRole() == UserRole.TEACHER) {
                return classEntity.getTeacher() != null && 
                       classEntity.getTeacher().getId().equals(currentUser.getId());
            }
            
            // 学生无权访问班级管理功能
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查用户是否有权限访问指定学生信息
     */
    public boolean canAccessStudent(Long studentId) {
        try {
            User currentUser = getCurrentUser();
            
            // 管理员有所有权限
            if (currentUser.getRole() == UserRole.ADMIN) {
                return true;
            }
            
            // 查找学生
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                return false;
            }
            
            // 教师只能访问自己班级的学生
            if (currentUser.getRole() == UserRole.TEACHER) {
                return student.getStudentClass() != null && 
                       student.getStudentClass().getTeacher() != null &&
                       student.getStudentClass().getTeacher().getId().equals(currentUser.getId());
            }
            
            // 学生只能访问自己的信息
            if (currentUser.getRole() == UserRole.STUDENT) {
                return studentId.equals(currentUser.getId());
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证班级访问权限，如果无权限则抛出异常
     */
    public void requireClassAccess(Long classId) {
        if (!canAccessClass(classId)) {
            throw new AccessDeniedException("无权访问该班级");
        }
    }
    
    /**
     * 验证学生访问权限，如果无权限则抛出异常
     */
    public void requireStudentAccess(Long studentId) {
        if (!canAccessStudent(studentId)) {
            throw new AccessDeniedException("无权访问该学生信息");
        }
    }
    
    /**
     * 验证角色权限，如果无权限则抛出异常
     */
    public void requireRole(UserRole role) {
        if (!hasRole(role)) {
            throw new AccessDeniedException("权限不足，需要" + role + "角色");
        }
    }
    
    /**
     * 验证任意角色权限，如果无权限则抛出异常
     */
    public void requireAnyRole(UserRole... roles) {
        if (!hasAnyRole(roles)) {
            StringBuilder sb = new StringBuilder("权限不足，需要以下任意角色：");
            for (int i = 0; i < roles.length; i++) {
                if (i > 0) sb.append("、");
                sb.append(roles[i]);
            }
            throw new AccessDeniedException(sb.toString());
        }
    }
    
    /**
     * 检查当前用户是否是指定班级的教师
     */
    public boolean isTeacherOfClass(Long classId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser.getRole() != UserRole.TEACHER) {
                return false;
            }
            
            return classRepository.isTeacherManagingClass(currentUser.getId(), classId);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查当前用户是否是指定学生的教师
     */
    public boolean isTeacherOfStudent(Long studentId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser.getRole() != UserRole.TEACHER) {
                return false;
            }
            
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null || student.getStudentClass() == null) {
                return false;
            }
            
            return isTeacherOfClass(student.getStudentClass().getId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否可管理学生档案（创建/改资料/删除）：管理员，或该班班主任
     */
    public boolean canManageStudentProfile(Long studentId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser.getRole() == UserRole.ADMIN) {
                return true;
            }
            if (currentUser.getRole() != UserRole.TEACHER || !currentUser.getIsHomeroomTeacher()) {
                return false;
            }
            return isTeacherOfStudent(studentId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否可向指定班级创建学生：管理员，或该班班主任
     */
    public boolean canCreateStudentInClass(Long classId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser.getRole() == UserRole.ADMIN) {
                return true;
            }
            if (currentUser.getRole() != UserRole.TEACHER || !currentUser.getIsHomeroomTeacher()) {
                return false;
            }
            if (classId == null) {
                return false;
            }
            return isTeacherOfClass(classId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否可改成绩：管理员，或该班负责教师（含非班主任）
     */
    public boolean canModifyStudentGrade(Long studentId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser.getRole() == UserRole.ADMIN) {
                return true;
            }
            if (currentUser.getRole() != UserRole.TEACHER) {
                return false;
            }
            return isTeacherOfStudent(studentId);
        } catch (Exception e) {
            return false;
        }
    }

    public void requireStudentProfileManagement(Long studentId) {
        if (!canManageStudentProfile(studentId)) {
            throw new AccessDeniedException("无权修改学生资料，仅管理员或班主任可操作");
        }
    }

    public void requireCreateStudentInClass(Long classId) {
        if (!canCreateStudentInClass(classId)) {
            throw new AccessDeniedException("无权创建学生，仅管理员或班主任可操作");
        }
    }

    public void requireModifyStudentGrade(Long studentId) {
        if (!canModifyStudentGrade(studentId)) {
            throw new AccessDeniedException("无权修改该学生成绩");
        }
    }
}