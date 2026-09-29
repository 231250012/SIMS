package com.sims.service;

import com.sims.dto.*;

import com.sims.entity.GradeRecord;
import com.sims.entity.Student;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.exception.ClassNotFoundException;
import com.sims.exception.StudentNotFoundException;
import com.sims.repository.ClassRepository;
import com.sims.repository.GradeRecordRepository;
import com.sims.repository.StudentRepository;
import com.sims.repository.UserRepository;
import com.sims.security.annotation.RequireClassAccess;
import com.sims.security.annotation.RequireRole;
import com.sims.security.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级管理服务
 */
@Service
@Transactional
public class ClassService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassService.class);
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GradeRecordRepository gradeRecordRepository;
    
    @Autowired
    private PermissionService permissionService;
    

    
    /**
     * 创建班级（仅管理员）
     */
    @RequireRole({UserRole.ADMIN})
    public ClassResponse createClass(CreateClassRequest request) {
        // 检查班级名称是否已存在
        if (classRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("班级名称已存在: " + request.getName());
        }
        
        com.sims.entity.Class newClass = new com.sims.entity.Class();
        newClass.setName(request.getName().trim());
        if (request.getGrade() != null && !request.getGrade().isBlank()) {
            newClass.setGrade(request.getGrade().trim());
        }
        if (request.getDescription() != null) {
            newClass.setDescription(request.getDescription());
        }
        if (request.getMaxStudents() != null && request.getMaxStudents() > 0) {
            newClass.setMaxStudents(request.getMaxStudents());
        }
        
        // 如果指定了教师，验证教师是否存在并分配
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("教师不存在: " + request.getTeacherId()));
            
            // 验证用户是否为教师角色
            if (teacher.getRole() != UserRole.TEACHER && teacher.getRole() != UserRole.ADMIN) {
                throw new IllegalArgumentException("指定用户不是教师角色");
            }
            
            newClass.setTeacher(teacher);
        }
        
        com.sims.entity.Class savedClass = classRepository.save(newClass);
        return convertToResponse(savedClass);
    }
    
    /**
     * 获取班级列表（根据用户权限过滤）
     */
    @RequireRole({UserRole.ADMIN, UserRole.TEACHER})
    @Transactional(readOnly = true)
    public List<ClassResponse> getClasses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        List<com.sims.entity.Class> classes;
        
        // 管理员可以查看所有班级，教师只能查看自己管理的班级
        if (currentUser.getRole() == UserRole.ADMIN) {
            classes = classRepository.findAll();
        } else if (currentUser.getRole() == UserRole.TEACHER) {
            classes = classRepository.findByTeacherId(currentUser.getId());
        } else {
            throw new AccessDeniedException("学生用户无权查看班级列表");
        }
        
        return classes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 注册页公开班级选项（含负责教师，无需登录）
     */
    @Transactional(readOnly = true)
    public List<ClassResponse> listPublicClassOptions() {
        return classRepository.findAllWithTeacher().stream()
                .map(c -> {
                    ClassResponse response = new ClassResponse();
                    response.setId(c.getId());
                    response.setName(c.getName());
                    response.setGrade(c.getGrade());
                    if (c.getTeacher() != null) {
                        response.setTeacherId(c.getTeacher().getId());
                        response.setTeacherName(c.getTeacher().getUsername());
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 获取班级详情
     */
    @RequireClassAccess(classIdParam = "classId")
    @Transactional(readOnly = true)
    public ClassResponse getClass(Long classId) {
        com.sims.entity.Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException(classId));
        
        // 权限检查
        checkClassAccess(classEntity);
        
        ClassResponse response = convertToResponse(classEntity);
        
        // 包含学生列表
        List<StudentResponse> students = classEntity.getStudents().stream()
                .map(this::convertStudentToResponse)
                .collect(Collectors.toList());
        response.setStudents(students);
        
        return response;
    }
    
    /**
     * 更新班级信息
     */
    @RequireClassAccess(classIdParam = "classId")
    public ClassResponse updateClass(Long classId, UpdateClassRequest request) {
        com.sims.entity.Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException(classId));
        
        // 权限检查
        checkClassAccess(classEntity);
        
        // 更新班级名称
        if (request.getName() != null && !request.getName().equals(classEntity.getName())) {
            if (classRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("班级名称已存在: " + request.getName());
            }
            classEntity.setName(request.getName());
        }
        
        // 更新教师
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("教师不存在: " + request.getTeacherId()));
            
            // 验证用户是否为教师角色
            if (teacher.getRole() != UserRole.TEACHER && teacher.getRole() != UserRole.ADMIN) {
                throw new IllegalArgumentException("指定用户不是教师角色");
            }
            
            classEntity.setTeacher(teacher);
        }
        
        com.sims.entity.Class savedClass = classRepository.save(classEntity);
        return convertToResponse(savedClass);
    }
    
    /**
     * 删除班级
     */
    @RequireRole(UserRole.ADMIN)
    public void deleteClass(Long classId) {
        com.sims.entity.Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException(classId));
        
        // 权限检查
        checkClassAccess(classEntity);
        
        // 检查班级是否有学生
        if (!classEntity.getStudents().isEmpty()) {
            throw new IllegalArgumentException("班级中还有学生，无法删除");
        }
        
        classRepository.delete(classEntity);
    }
    
    /**
     * 分配学生到班级
     * 需求 3.4: 更新学生的班级归属关系
     * 需求 5.3: 发送Kafka事件通知
     */
    @RequireClassAccess(classIdParam = "classId")
    public void assignStudentToClass(Long studentId, Long classId) {
        logger.info("分配学生到班级: studentId={}, classId={}", studentId, classId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        
        com.sims.entity.Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException(classId));
        
        // 权限检查
        checkClassAccess(classEntity);
        
        // 记录旧班级信息
        Long oldClassId = student.getStudentClass() != null ? student.getStudentClass().getId() : null;
        
        // 分配学生到班级
        student.setStudentClass(classEntity);
        Student savedStudent = studentRepository.save(student);
        

        
        logger.info("学生班级分配成功: studentId={}, oldClassId={}, newClassId={}", 
                   studentId, oldClassId, classId);
    }
    
    /**
     * 获取班级学生列表
     */
    @RequireClassAccess(classIdParam = "classId")
    @Transactional(readOnly = true)
    public List<StudentResponse> getClassStudents(Long classId) {
        com.sims.entity.Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException(classId));
        
        // 权限检查
        checkClassAccess(classEntity);
        
        return classEntity.getStudents().stream()
                .map(this::convertStudentToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 从班级中移除学生
     */
    public void removeStudentFromClass(Long studentId) {
        logger.info("从班级中移除学生: studentId={}", studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        
        if (student.getStudentClass() != null) {
            // 权限检查
            checkClassAccess(student.getStudentClass());
            
            Long oldClassId = student.getStudentClass().getId();
            String oldClassName = student.getStudentClass().getName();
            
            student.setStudentClass(null);
            Student savedStudent = studentRepository.save(student);
            

            
            logger.info("学生从班级移除成功: studentId={}, oldClassId={}", studentId, oldClassId);
        }
    }
    
    /**
     * 检查用户是否有权限访问指定班级
     */
    private void checkClassAccess(com.sims.entity.Class classEntity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        // 管理员有所有权限
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        
        // 教师只能访问自己管理的班级
        if (currentUser.getRole() == UserRole.TEACHER) {
            if (classEntity.getTeacher() == null || !classEntity.getTeacher().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("无权访问该班级");
            }
            return;
        }
        
        // 学生无权访问班级管理功能
        throw new AccessDeniedException("学生用户无权访问班级管理功能");
    }
    
    /**
     * 转换Class实体为ClassResponse
     */
    private ClassResponse convertToResponse(com.sims.entity.Class classEntity) {
        ClassResponse response = new ClassResponse();
        response.setId(classEntity.getId());
        response.setName(classEntity.getName());
        response.setStudentCount(classEntity.getStudentCount());
        
        if (classEntity.getTeacher() != null) {
            response.setTeacherId(classEntity.getTeacher().getId());
            response.setTeacherName(classEntity.getTeacher().getUsername());
        }
        
        return response;
    }
    
    /**
     * 转换Student实体为StudentResponse
     */
    private StudentResponse convertStudentToResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setName(student.getName());
        response.setAge(student.getAge());
        response.setScore(student.getScore() != null ? student.getScore() : BigDecimal.ZERO);
        response.setAvatar(student.getAvatar());
        response.setCreateTime(student.getCreateTime());
        
        if (student.getStudentClass() != null) {
            response.setClassId(student.getStudentClass().getId());
            response.setClassName(student.getStudentClass().getName());
        }
        
        return response;
    }
    

    
    /**
     * 获取教师仪表板统计信息
     */
    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @Transactional(readOnly = true)
    public TeacherStatsResponse getTeacherDashboardStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        List<com.sims.entity.Class> classes;
        
        // 管理员可以查看所有班级，教师只能查看自己管理的班级
        if (currentUser.getRole() == UserRole.ADMIN) {
            classes = classRepository.findAll();
        } else if (currentUser.getRole() == UserRole.TEACHER) {
            classes = classRepository.findByTeacherId(currentUser.getId());
        } else {
            throw new AccessDeniedException("学生用户无权查看班级统计");
        }
        
        // 计算统计信息（不含平均分）
        int totalClasses = classes.size();
        int totalStudents = classes.stream()
                .mapToInt(cls -> cls.getStudents().size())
                .sum();
        
        TeacherStatsResponse response = new TeacherStatsResponse(totalClasses, totalStudents);
        
        // 设置班级列表
        List<ClassResponse> classResponses = classes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        response.setMyClasses(classResponses);
        
        // 获取优秀学生（前5名）
        List<Student> topStudents = classes.stream()
                .flatMap(cls -> cls.getStudents().stream())
                .filter(student -> student.getScore() != null)
                .sorted((s1, s2) -> s2.getScore().compareTo(s1.getScore()))
                .limit(5)
                .collect(Collectors.toList());
        
        List<StudentRankResponse> topStudentResponses = topStudents.stream()
                .map((student) -> {
                    StudentRankResponse rankResponse = new StudentRankResponse();
                    rankResponse.setId(student.getId());
                    rankResponse.setName(student.getName());
                    rankResponse.setAge(student.getAge());
                    rankResponse.setScore(student.getScore());
                    if (student.getStudentClass() != null) {
                        rankResponse.setClassName(student.getStudentClass().getName());
                    }
                    return rankResponse;
                })
                .collect(Collectors.toList());
        
        response.setTopStudents(topStudentResponses);
        
        return response;
    }

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                return userRepository.findByUsername(username).orElse(null);
            }
        } catch (Exception e) {
            logger.warn("获取当前用户失败: {}", e.getMessage());
        }
        return null;
    }
}