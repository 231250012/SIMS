package com.sims.controller;

import com.sims.dto.*;
import com.sims.entity.User;
import com.sims.exception.ClassNotFoundException;
import com.sims.exception.StudentNotFoundException;
import com.sims.security.CustomUserDetailsService;
import com.sims.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 学生管理控制器
 * 提供学生CRUD操作的RESTful API接口
 */
@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:5MB}")
    private String maxFileSize;

    /**
     * 获取学生列表（支持分页和筛选）
     * 需求 2.2: 返回分页的学生数据并支持按条件筛选
     */
    @GetMapping
    public ResponseEntity<?> getStudents(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @Min(1) @Max(100) Integer minAge,
            @RequestParam(required = false) @Min(1) @Max(100) Integer maxAge) {
        
        try {
            logger.info("获取学生列表 - page: {}, size: {}, name: {}, classId: {}, minAge: {}, maxAge: {}", 
                       page, size, name, classId, minAge, maxAge);

            // 获取当前用户
            User currentUser = getCurrentUser();
            
            PageResponse<StudentResponse> response;
            if (currentUser != null) {
                // 根据用户权限获取学生列表
                response = studentService.getStudentsByUser(currentUser, page, size, name, classId, minAge, maxAge);
            } else {
                // 如果没有认证用户，返回所有学生（管理员权限）
                response = studentService.getStudents(page, size, name, classId, minAge, maxAge);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("权限不足", e.getMessage()));
        } catch (Exception e) {
            logger.error("获取学生列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取学生列表失败", e.getMessage()));
        }
    }

    /**
     * 获取学生详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        try {
            logger.info("获取学生详情: {}", id);

            User currentUser = getCurrentUser();
            StudentResponse response;
            
            if (currentUser != null) {
                // 根据用户权限获取学生详情
                response = studentService.getStudentByUser(id, currentUser);
            } else {
                // 如果没有认证用户，直接获取（管理员权限）
                response = studentService.getStudent(id);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("学生不存在", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("权限不足", e.getMessage()));
        } catch (Exception e) {
            logger.error("获取学生详情失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取学生详情失败", e.getMessage()));
        }
    }

    /**
     * 创建学生
     * 需求 2.1: 保存学生信息到数据库并发送Kafka事件通知
     */
    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        try {
            logger.info("创建学生: {}", request);

            StudentResponse response = studentService.createStudent(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("班级不存在", e.getMessage()));
        } catch (Exception e) {
            logger.error("创建学生失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("创建学生失败", e.getMessage()));
        }
    }

    /**
     * 更新学生信息
     * 需求 2.3: 修改数据库记录并清除相关缓存
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, 
                                         @Valid @RequestBody UpdateStudentRequest request) {
        try {
            logger.info("更新学生信息: {} - {}", id, request);

            StudentResponse response = studentService.updateStudent(id, request);
            
            return ResponseEntity.ok(response);
            
        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("学生不存在", e.getMessage()));
        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("班级不存在", e.getMessage()));
        } catch (Exception e) {
            logger.error("更新学生信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("更新学生信息失败", e.getMessage()));
        }
    }

    /**
     * 删除学生
     * 需求 2.4: 从数据库中移除记录并更新相关统计
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            logger.info("删除学生: {}", id);

            studentService.deleteStudent(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "学生删除成功");
            response.put("studentId", id.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("学生不存在", e.getMessage()));
        } catch (Exception e) {
            logger.error("删除学生失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("删除学生失败", e.getMessage()));
        }
    }

    /**
     * 获取成绩排行榜
     * 需求 5.4: 使用Redis ZSet返回排序结果
     */
    @GetMapping("/ranking")
    public ResponseEntity<?> getScoreRanking(@RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        try {
            logger.info("获取成绩排行榜，限制: {}", limit);

            List<StudentRankResponse> ranking = studentService.getScoreRanking(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ranking", ranking);
            response.put("total", ranking.size());
            response.put("limit", limit);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取成绩排行榜失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取成绩排行榜失败", e.getMessage()));
        }
    }

    /**
     * 获取班级成绩排行榜
     */
    @GetMapping("/ranking/class/{classId}")
    public ResponseEntity<?> getClassScoreRanking(@PathVariable Long classId,
                                                @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        try {
            logger.info("获取班级成绩排行榜 - classId: {}, limit: {}", classId, limit);

            List<StudentRankResponse> ranking = studentService.getClassScoreRanking(classId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ranking", ranking);
            response.put("classId", classId);
            response.put("total", ranking.size());
            response.put("limit", limit);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取班级成绩排行榜失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取班级成绩排行榜失败", e.getMessage()));
        }
    }

    /**
     * 更新学生成绩
     * 需求 3.3: 保存成绩并更新Redis中的成绩排行榜
     */
    @PutMapping("/{id}/score")
    public ResponseEntity<?> updateStudentScore(@PathVariable Long id,
                                              @RequestBody Map<String, BigDecimal> scoreRequest) {
        try {
            BigDecimal score = scoreRequest.get("score");
            if (score == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("参数错误", "成绩不能为空"));
            }

            if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("参数错误", "成绩必须在0-100之间"));
            }

            logger.info("更新学生成绩: {} - {}", id, score);

            StudentResponse response = studentService.updateStudentScore(id, score);
            
            return ResponseEntity.ok(response);
            
        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("学生不存在", e.getMessage()));
        } catch (Exception e) {
            logger.error("更新学生成绩失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("更新学生成绩失败", e.getMessage()));
        }
    }

    /**
     * 上传学生头像
     * 需求 4.3: 存储图片文件并更新头像URL
     */
    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> uploadAvatar(@PathVariable Long id,
                                        @RequestParam("file") MultipartFile file) {
        try {
            logger.info("上传学生头像: {} - 文件: {}", id, file.getOriginalFilename());

            // 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("文件错误", "上传文件不能为空"));
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("文件类型错误", "只支持图片文件"));
            }

            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("文件过大", "文件大小不能超过5MB"));
            }

            // 确保学生存在
            StudentResponse student = studentService.getStudent(id);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String filename = "avatar_" + id + "_" + UUID.randomUUID().toString() + extension;

            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, "avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 生成访问URL
            String avatarUrl = "/uploads/avatars/" + filename;

            // 更新学生头像
            UpdateStudentRequest updateRequest = new UpdateStudentRequest();
            updateRequest.setAvatar(avatarUrl);
            StudentResponse updatedStudent = studentService.updateStudent(id, updateRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "头像上传成功");
            response.put("avatarUrl", avatarUrl);
            response.put("student", updatedStudent);

            return ResponseEntity.ok(response);

        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("学生不存在", e.getMessage()));
        } catch (IOException e) {
            logger.error("文件保存失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("文件保存失败", e.getMessage()));
        } catch (Exception e) {
            logger.error("上传头像失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("上传头像失败", e.getMessage()));
        }
    }

    /**
     * 批量导入学生
     */
    @PostMapping("/batch")
    public ResponseEntity<?> batchCreateStudents(@Valid @RequestBody List<CreateStudentRequest> requests) {
        try {
            logger.info("批量创建学生，数量: {}", requests.size());

            if (requests.size() > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("批量限制", "单次最多导入100个学生"));
            }

            Map<String, Object> result = new HashMap<>();
            int successCount = 0;
            int failureCount = 0;
            
            for (CreateStudentRequest request : requests) {
                try {
                    studentService.createStudent(request);
                    successCount++;
                } catch (Exception e) {
                    logger.warn("创建学生失败: {} - {}", request.getName(), e.getMessage());
                    failureCount++;
                }
            }

            result.put("message", "批量导入完成");
            result.put("total", requests.size());
            result.put("success", successCount);
            result.put("failure", failureCount);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("批量创建学生失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("批量创建学生失败", e.getMessage()));
        }
    }

    /**
     * 获取当前学生的个人资料
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile() {
        try {
            logger.info("获取当前学生个人资料");

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("未认证", "请先登录"));
            }

            // 根据用户ID查找对应的学生记录
            StudentResponse response = studentService.getStudentByUserId(currentUser.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("学生信息不存在", e.getMessage()));
        } catch (Exception e) {
            logger.error("获取学生个人资料失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取学生个人资料失败", e.getMessage()));
        }
    }

    /**
     * 更新当前学生的个人资料
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UpdateStudentRequest request) {
        try {
            logger.info("更新当前学生个人资料: {}", request);

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("未认证", "请先登录"));
            }

            // 根据用户ID更新对应的学生记录
            StudentResponse response = studentService.updateStudentByUserId(currentUser.getId(), request);
            
            return ResponseEntity.ok(response);
            
        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("学生信息不存在", e.getMessage()));
        } catch (Exception e) {
            logger.error("更新学生个人资料失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("更新学生个人资料失败", e.getMessage()));
        }
    }

    /**
     * 获取当前学生的成绩历史
     */
    @GetMapping("/grades")
    public ResponseEntity<?> getMyGrades() {
        try {
            logger.info("获取当前学生成绩历史");

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("未认证", "请先登录"));
            }

            // 根据用户ID获取学生的成绩记录
            List<Map<String, Object>> grades = studentService.getStudentGradesByUserId(currentUser.getId());
            
            return ResponseEntity.ok(grades);
            
        } catch (StudentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("学生信息不存在", e.getMessage()));
        } catch (Exception e) {
            logger.error("获取学生成绩历史失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取学生成绩历史失败", e.getMessage()));
        }
    }

    /**
     * 获取学生统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStudentStatistics() {
        try {
            logger.info("获取学生统计信息");

            // 这里可以调用统计服务获取更详细的统计信息
            // 暂时返回基本统计
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("message", "学生统计信息");
            statistics.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            logger.error("获取学生统计信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取学生统计信息失败", e.getMessage()));
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取当前认证用户
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
                
                CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                    (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                
                return userPrincipal.getUser();
            }
        } catch (Exception e) {
            logger.warn("获取当前用户失败", e);
        }
        
        return null;
    }

    /**
     * 创建错误响应
     */
    private Map<String, String> createErrorResponse(String error, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return errorResponse;
    }
}