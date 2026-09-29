package com.sims.controller;

import com.sims.domain.SubjectCatalog;
import com.sims.dto.*;
import com.sims.entity.User;
import com.sims.security.CustomUserDetailsService;
import com.sims.service.GradeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩管理控制器
 * 提供成绩CRUD操作、批量更新、趋势分析和排行榜的RESTful API接口
 */
@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
public class GradeController {

    private static final Logger logger = LoggerFactory.getLogger(GradeController.class);

    @Autowired
    private GradeService gradeService;

    /**
     * 当前专业支持的科目（C语言、英语四级）
     */
    @GetMapping("/subjects")
    public ResponseEntity<?> listSubjects() {
        List<Map<String, Object>> subjects = SubjectCatalog.orderedNames().stream()
                .map(name -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", name);
                    item.put("maxScore", SubjectCatalog.maxScoreOf(name));
                    return item;
                })
                .toList();
        return ResponseEntity.ok(subjects);
    }

    /**
     * 从数据库重建 Redis ZSet 排行榜
     */
    @PostMapping("/ranking/rebuild")
    public ResponseEntity<?> rebuildRankings() {
        try {
            gradeService.rebuildRankings();
            Map<String, Object> body = new HashMap<>();
            body.put("message", "排行榜已重建");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            logger.error("重建排行榜失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("重建排行榜失败", e.getMessage()));
        }
    }

    /**
     * 获取成绩记录列表（支持分页和筛选）
     * 需求 4.3, 5.4: 扩展现有成绩管理API
     */
    @GetMapping
    public ResponseEntity<?> getGradeRecords(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String examType,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            logger.info("获取成绩记录列表 - page: {}, size: {}, studentId: {}, classId: {}, subject: {}", 
                       page, size, studentId, classId, subject);

            // 获取当前用户
            User currentUser = getCurrentUser();
            
            PageResponse<GradeRecordResponse> response;
            if (currentUser != null) {
                // 根据用户权限获取成绩记录
                response = gradeService.getGradeRecordsByUser(currentUser, page, size, studentId, classId, subject);
            } else {
                // 如果没有认证用户，返回所有成绩记录（管理员权限）
                response = gradeService.getGradeRecords(page, size, studentId, classId, subject, 
                                                      examType, semester, academicYear, startDate, endDate);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("权限不足", e.getMessage()));
        } catch (Exception e) {
            logger.error("获取成绩记录列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取成绩记录列表失败", e.getMessage()));
        }
    }

    /**
     * 创建成绩记录
     * 需求 4.3: 实现成绩编辑和批量更新
     */
    @PostMapping
    public ResponseEntity<?> createGradeRecord(@Valid @RequestBody GradeRecordRequest request) {
        try {
            logger.info("创建成绩记录: {}", request);

            GradeRecordResponse response = gradeService.createGradeRecord(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("创建成绩记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("创建成绩记录失败", e.getMessage()));
        }
    }

    /**
     * 批量更新成绩记录
     * 需求 4.3: 实现成绩编辑和批量更新
     */
    @PostMapping("/batch")
    public ResponseEntity<?> batchUpdateGrades(@Valid @RequestBody BatchGradeUpdateRequest request) {
        try {
            logger.info("批量更新成绩记录，数量: {}", request.getGradeRecords().size());

            Map<String, Object> result = gradeService.batchUpdateGrades(request);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("批量更新成绩记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("批量更新成绩记录失败", e.getMessage()));
        }
    }

    /**
     * 更新成绩记录
     * 需求 4.3: 实现成绩编辑和批量更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGradeRecord(@PathVariable Long id, 
                                             @Valid @RequestBody GradeRecordRequest request) {
        try {
            logger.info("更新成绩记录: {} - {}", id, request);

            GradeRecordResponse response = gradeService.updateGradeRecord(id, request);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("更新成绩记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("更新成绩记录失败", e.getMessage()));
        }
    }

    /**
     * 删除成绩记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGradeRecord(@PathVariable Long id) {
        try {
            logger.info("删除成绩记录: {}", id);

            gradeService.deleteGradeRecord(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "成绩记录删除成功");
            response.put("gradeRecordId", id.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("删除成绩记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("删除成绩记录失败", e.getMessage()));
        }
    }

    /**
     * 获取学生成绩记录
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentGradeRecords(@PathVariable Long studentId) {
        try {
            logger.info("获取学生成绩记录: {}", studentId);

            List<GradeRecordResponse> response = gradeService.getStudentGradeRecords(studentId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取学生成绩记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取学生成绩记录失败", e.getMessage()));
        }
    }

    /**
     * 获取学生最新成绩
     */
    @GetMapping("/student/{studentId}/latest")
    public ResponseEntity<?> getStudentLatestGrades(@PathVariable Long studentId) {
        try {
            logger.info("获取学生最新成绩: {}", studentId);

            List<GradeRecordResponse> response = gradeService.getStudentLatestGrades(studentId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取学生最新成绩失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取学生最新成绩失败", e.getMessage()));
        }
    }

    /**
     * 获取学生成绩趋势分析
     * 需求 5.4: 添加成绩趋势分析功能
     */
    @GetMapping("/trend/{studentId}")
    public ResponseEntity<?> getGradeTrend(@PathVariable Long studentId,
                                         @RequestParam String subject) {
        try {
            logger.info("获取学生成绩趋势 - studentId: {}, subject: {}", studentId, subject);

            GradeTrendResponse response = gradeService.getGradeTrend(studentId, subject);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取学生成绩趋势失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取学生成绩趋势失败", e.getMessage()));
        }
    }

    /**
     * 获取成绩排行榜
     * 需求 5.5: 开发排行榜显示组件
     */
    @GetMapping("/ranking")
    public ResponseEntity<?> getGradeRanking(@RequestParam String subject,
                                           @RequestParam(defaultValue = "SCHOOL") String scope,
                                           @RequestParam(required = false) Long classId,
                                           @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        try {
            logger.info("获取成绩排行榜 - subject: {}, scope: {}, classId: {}, limit: {}", 
                       subject, scope, classId, limit);

            GradeRankingResponse response = gradeService.getGradeRanking(subject, scope, classId, limit);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取成绩排行榜失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取成绩排行榜失败", e.getMessage()));
        }
    }

    /**
     * 获取班级成绩排行榜
     * 需求 5.5: 开发排行榜显示组件
     */
    @GetMapping("/ranking/class/{classId}")
    public ResponseEntity<?> getClassGradeRanking(@PathVariable Long classId,
                                                @RequestParam String subject,
                                                @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        try {
            logger.info("获取班级成绩排行榜 - classId: {}, subject: {}, limit: {}", classId, subject, limit);

            GradeRankingResponse response = gradeService.getGradeRanking(subject, "CLASS", classId, limit);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取班级成绩排行榜失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取班级成绩排行榜失败", e.getMessage()));
        }
    }

    /**
     * 获取当前学生的成绩记录
     */
    @GetMapping("/my-grades")
    public ResponseEntity<?> getMyGrades() {
        try {
            logger.info("获取当前学生成绩记录");

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("未认证", "请先登录"));
            }

            // 根据用户权限获取成绩记录
            PageResponse<GradeRecordResponse> response = gradeService.getGradeRecordsByUser(
                    currentUser, 0, 100, null, null, null);
            
            return ResponseEntity.ok(response.getContent());
            
        } catch (Exception e) {
            logger.error("获取当前学生成绩记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取当前学生成绩记录失败", e.getMessage()));
        }
    }

    /**
     * 获取当前学生的成绩趋势
     */
    @GetMapping("/my-trend")
    public ResponseEntity<?> getMyGradeTrend(@RequestParam String subject) {
        try {
            logger.info("获取当前学生成绩趋势 - subject: {}", subject);

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("未认证", "请先登录"));
            }

            // 这里需要根据用户ID获取对应的学生ID
            // 暂时返回模拟数据，实际应该查询学生表
            GradeTrendResponse response = new GradeTrendResponse();
            response.setStudentName(currentUser.getUsername());
            response.setSubject(subject);
            response.setTrend("STABLE");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取当前学生成绩趋势失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("获取当前学生成绩趋势失败", e.getMessage()));
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