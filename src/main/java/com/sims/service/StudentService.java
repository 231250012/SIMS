package com.sims.service;

import com.sims.dto.*;
import com.sims.entity.Class;
import com.sims.entity.Student;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.exception.ClassNotFoundException;
import com.sims.exception.StudentNotFoundException;
import com.sims.entity.GradeRecord;
import com.sims.repository.ClassRepository;
import com.sims.repository.GradeRecordRepository;
import com.sims.repository.StudentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学生管理服务类
 * 实现学生的CRUD操作、分页查询、筛选功能、缓存管理和事件发布
 */
@Service
@Transactional
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    
    // Redis缓存键前缀
    private static final String STUDENT_CACHE_PREFIX = "student:";
    private static final String STUDENT_LIST_CACHE_PREFIX = "student_list:";
    private static final String SCORE_RANKING_KEY = "score_ranking";
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private GradeRecordRepository gradeRecordRepository;
    
    @Autowired
    private com.sims.security.service.PermissionService permissionService;

    /**
     * 创建学生
     * 需求 2.1: 保存学生信息到数据库并发送Kafka事件通知
     */
    public StudentResponse createStudent(CreateStudentRequest request) {
        logger.info("创建学生: {}", request);

        // 非班主任不可创建学生；管理员或班主任可创建
        permissionService.requireCreateStudentInClass(request.getClassId());
        
        // 创建学生实体
        Student student = new Student();
        student.setName(request.getName());
        student.setAge(request.getAge());
        
        // 设置成绩，如果未提供则默认为0
        if (request.getScore() != null) {
            student.setScore(request.getScore());
        }
        
        // 设置头像
        if (request.getAvatar() != null) {
            student.setAvatar(request.getAvatar());
        }
        
        // 如果指定了班级，验证班级是否存在并分配
        if (request.getClassId() != null) {
            Class studentClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new ClassNotFoundException(request.getClassId()));
            student.setStudentClass(studentClass);
        }
        
        // 保存学生到数据库
        Student savedStudent = studentRepository.save(student);

        logger.info("学生创建成功: {}", savedStudent);
        return new StudentResponse(savedStudent);
    }

    /**
     * 获取学生列表（支持分页和筛选）
     * 需求 2.2: 返回分页的学生数据并支持按条件筛选
     */
    @Cacheable(value = "studentList", key = "#page + '_' + #size + '_' + #name + '_' + #classId + '_' + #minAge + '_' + #maxAge")
    public PageResponse<StudentResponse> getStudents(int page, int size, String name, Long classId, Integer minAge, Integer maxAge) {
        logger.info("查询学生列表 - page: {}, size: {}, name: {}, classId: {}, minAge: {}, maxAge: {}", 
                   page, size, name, classId, minAge, maxAge);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        
        // 使用多条件搜索
        Page<Student> studentPage = studentRepository.searchStudents(name, classId, minAge, maxAge, pageable);
        
        // 转换为响应DTO
        List<StudentResponse> studentResponses = studentPage.getContent().stream()
                .map(StudentResponse::new)
                .collect(Collectors.toList());
        
        PageResponse<StudentResponse> response = new PageResponse<>(
                studentResponses,
                studentPage.getNumber(),
                studentPage.getSize(),
                studentPage.getTotalElements(),
                studentPage.getTotalPages()
        );
        
        logger.info("查询到 {} 个学生", studentPage.getTotalElements());
        return response;
    }

    /**
     * 根据当前用户权限获取学生列表
     * 需求 3.1, 3.2: 教师只能查看自己班级的学生
     */
    public PageResponse<StudentResponse> getStudentsByUser(User currentUser, int page, int size, String name, Long classId, Integer minAge, Integer maxAge) {
        logger.info("用户 {} 查询学生列表", currentUser.getUsername());
        
        if (currentUser.getRole() == UserRole.ADMIN) {
            // 管理员可以查看所有学生
            return getStudents(page, size, name, classId, minAge, maxAge);
        } else if (currentUser.getRole() == UserRole.TEACHER) {
            // 教师只能查看自己管理的班级的学生
            Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
            Page<Student> studentPage = studentRepository.findByTeacherId(currentUser.getId(), pageable);
            
            // 如果指定了筛选条件，需要进一步过滤
            if (name != null || classId != null || minAge != null || maxAge != null) {
                // 获取教师管理的班级ID列表
                List<Class> teacherClasses = classRepository.findByTeacherId(currentUser.getId());
                List<Long> teacherClassIds = teacherClasses.stream().map(Class::getId).collect(Collectors.toList());
                
                // 如果指定了班级ID，检查是否在教师管理的班级中
                if (classId != null && !teacherClassIds.contains(classId)) {
                    throw new AccessDeniedException("无权访问指定班级的学生信息");
                }
                
                // 使用多条件搜索，但限制在教师的班级范围内
                studentPage = studentRepository.searchStudents(name, classId, minAge, maxAge, pageable);
                // 过滤出教师管理的学生
                List<Student> filteredStudents = studentPage.getContent().stream()
                        .filter(student -> student.getStudentClass() != null && 
                                teacherClassIds.contains(student.getStudentClass().getId()))
                        .collect(Collectors.toList());
                
                // 重新构建分页结果
                studentPage = new org.springframework.data.domain.PageImpl<>(
                        filteredStudents, pageable, filteredStudents.size());
            }
            
            List<StudentResponse> studentResponses = studentPage.getContent().stream()
                    .map(StudentResponse::new)
                    .collect(Collectors.toList());
            
            return new PageResponse<>(
                    studentResponses,
                    studentPage.getNumber(),
                    studentPage.getSize(),
                    studentPage.getTotalElements(),
                    studentPage.getTotalPages()
            );
        } else {
            // 学生只能查看自己的信息
            throw new AccessDeniedException("学生用户无权查看学生列表");
        }
    }

    /**
     * 获取学生详情
     */
    @Cacheable(value = "student", key = "#studentId")
    public StudentResponse getStudent(Long studentId) {
        logger.info("查询学生详情: {}", studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        
        return new StudentResponse(student);
    }

    /**
     * 根据当前用户权限获取学生详情
     * 需求 3.2, 4.4, 4.5: 权限控制
     */
    public StudentResponse getStudentByUser(Long studentId, User currentUser) {
        logger.info("用户 {} 查询学生详情: {}", currentUser.getUsername(), studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        
        // 权限检查
        if (currentUser.getRole() == UserRole.ADMIN) {
            // 管理员可以查看所有学生
            return new StudentResponse(student);
        } else if (currentUser.getRole() == UserRole.TEACHER) {
            // 教师只能查看自己班级的学生
            if (student.getStudentClass() == null || 
                !student.getStudentClass().getTeacher().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("无权访问该学生信息");
            }
            return new StudentResponse(student);
        } else if (currentUser.getRole() == UserRole.STUDENT) {
            // 学生只能查看自己的信息
            if (student.getUser() == null || !student.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("只能查看自己的信息");
            }
            return new StudentResponse(student);
        }
        
        throw new AccessDeniedException("无权访问学生信息");
    }

    /**
     * 更新学生信息
     * 需求 2.3: 修改数据库记录并清除相关缓存
     */
    @CacheEvict(value = {"student", "studentList"}, allEntries = true)
    public StudentResponse updateStudent(Long studentId, UpdateStudentRequest request) {
        logger.info("更新学生信息: {} - {}", studentId, request);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        boolean profileChange = request.getName() != null
                || request.getAge() != null
                || request.getAvatar() != null
                || request.getClassId() != null
                || request.getGender() != null
                || request.getBirthDate() != null
                || request.getPhone() != null
                || request.getAddress() != null
                || request.getParentName() != null
                || request.getParentPhone() != null;
        boolean scoreOnly = request.getScore() != null && !profileChange;

        if (scoreOnly) {
            // 非班主任也可仅改成绩
            permissionService.requireModifyStudentGrade(studentId);
            student.setScore(request.getScore());
            Student updatedStudent = studentRepository.save(student);
            logger.info("学生成绩更新成功: {}", updatedStudent);
            return new StudentResponse(updatedStudent);
        }

        // 资料变更：仅管理员或班主任
        permissionService.requireStudentProfileManagement(studentId);
        
        // 更新字段
        if (request.getName() != null) {
            student.setName(request.getName());
        }
        
        if (request.getAge() != null) {
            student.setAge(request.getAge());
        }
        
        if (request.getScore() != null) {
            student.setScore(request.getScore());
        }
        
        if (request.getAvatar() != null) {
            student.setAvatar(request.getAvatar());
        }
        
        if (request.getClassId() != null) {
            Class studentClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new ClassNotFoundException(request.getClassId()));
            // 调班也需对目标班有班主任权限（管理员放行）
            permissionService.requireCreateStudentInClass(request.getClassId());
            student.setStudentClass(studentClass);
        }
        
        // 保存更新
        Student updatedStudent = studentRepository.save(student);

        logger.info("学生信息更新成功: {}", updatedStudent);
        return new StudentResponse(updatedStudent);
    }

    /**
     * 删除学生
     * 需求 2.4: 从数据库中移除记录并更新相关统计
     */
    @CacheEvict(value = {"student", "studentList"}, allEntries = true)
    public void deleteStudent(Long studentId) {
        logger.info("删除学生: {}", studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        permissionService.requireStudentProfileManagement(studentId);
        
        // 删除学生
        studentRepository.delete(student);

        logger.info("学生删除成功: {}", studentId);
    }

    /**
     * 获取成绩排行榜
     * 直接从数据库查询排行榜
     */
    public List<StudentRankResponse> getScoreRanking(int limit) {
        logger.info("查询成绩排行榜，限制: {}", limit);
        
        // 直接从数据库查询排行榜
        List<Student> allStudents = studentRepository.findScoreRanking();
        List<Student> students = allStudents.size() > limit ? 
            allStudents.subList(0, limit) : allStudents;
        
        List<StudentRankResponse> ranking = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            ranking.add(new StudentRankResponse(student, i + 1));
        }
        
        logger.info("查询到排行榜 {} 条记录", ranking.size());
        return ranking;
    }

    /**
     * 获取班级成绩排行榜
     */
    public List<StudentRankResponse> getClassScoreRanking(Long classId, int limit) {
        logger.info("查询班级成绩排行榜 - classId: {}, limit: {}", classId, limit);
        
        List<Student> students = studentRepository.findScoreRankingByClass(classId);
        
        return students.stream()
                .limit(limit)
                .map(student -> {
                    int rank = students.indexOf(student) + 1;
                    return new StudentRankResponse(student, rank);
                })
                .collect(Collectors.toList());
    }

    /**
     * 更新学生成绩
     * 需求 3.3: 保存成绩并更新Redis中的成绩排行榜
     */
    @CacheEvict(value = {"student", "studentList"}, allEntries = true)
    public StudentResponse updateStudentScore(Long studentId, BigDecimal score) {
        logger.info("更新学生成绩: {} - {}", studentId, score);

        permissionService.requireModifyStudentGrade(studentId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        
        student.setScore(score);
        Student updatedStudent = studentRepository.save(student);

        logger.info("学生成绩更新成功: {}", updatedStudent);
        return new StudentResponse(updatedStudent);
    }

    /**
     * 根据用户ID获取学生信息
     */
    public StudentResponse getStudentByUserId(Long userId) {
        logger.info("根据用户ID查询学生信息: {}", userId);
        
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("用户ID: " + userId + " 对应的学生信息不存在"));
        
        return new StudentResponse(student);
    }

    /**
     * 根据用户ID更新学生信息
     */
    @CacheEvict(value = {"student", "studentList"}, allEntries = true)
    public StudentResponse updateStudentByUserId(Long userId, UpdateStudentRequest request) {
        logger.info("根据用户ID更新学生信息: {} - {}", userId, request);
        
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("用户ID: " + userId + " 对应的学生信息不存在"));
        
        // 更新字段
        if (request.getName() != null) {
            student.setName(request.getName());
        }
        
        if (request.getAge() != null) {
            student.setAge(request.getAge());
        }
        
        if (request.getGender() != null) {
            student.setGender(request.getGender());
        }
        
        if (request.getBirthDate() != null) {
            student.setBirthDate(java.time.LocalDate.parse(request.getBirthDate()));
        }
        
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }
        
        if (request.getAddress() != null) {
            student.setAddress(request.getAddress());
        }
        
        if (request.getParentName() != null) {
            student.setParentName(request.getParentName());
        }
        
        if (request.getParentPhone() != null) {
            student.setParentPhone(request.getParentPhone());
        }
        
        if (request.getAvatar() != null) {
            student.setAvatar(request.getAvatar());
        }
        
        // 保存更新
        Student updatedStudent = studentRepository.save(student);
        
        logger.info("学生信息更新成功: {}", updatedStudent);
        return new StudentResponse(updatedStudent);
    }

    /**
     * 根据用户ID获取学生成绩（C语言 / 英语四级，每科一条）
     */
    public List<Map<String, Object>> getStudentGradesByUserId(Long userId) {
        logger.info("根据用户ID查询学生成绩历史: {}", userId);
        
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("用户ID: " + userId + " 对应的学生信息不存在"));

        List<GradeRecord> records = gradeRecordRepository.findByStudentId(student.getId());
        List<Map<String, Object>> grades = records.stream().map(gr -> {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", gr.getId());
            item.put("subject", gr.getSubject());
            item.put("score", gr.getScore());
            item.put("maxScore", gr.getMaxScore());
            item.put("examType", gr.getExamType());
            item.put("examDate", gr.getExamDate());
            item.put("semester", gr.getSemester());
            item.put("gradeLevel", gr.getGradeLevel());
            item.put("percentage", gr.getPercentage());
            return item;
        }).collect(Collectors.toList());
        
        logger.info("查询到学生成绩记录 {} 条", grades.size());
        return grades;
    }

    // ==================== 私有辅助方法 ====================





    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof com.sims.security.CustomUserDetailsService.CustomUserPrincipal) {
                com.sims.security.CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                    (com.sims.security.CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                return userPrincipal.getUser();
            }
        } catch (Exception e) {
            logger.warn("获取当前用户失败: {}", e.getMessage());
        }
        return null;
    }


}