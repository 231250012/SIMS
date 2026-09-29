package com.sims.service;

import com.sims.domain.SubjectCatalog;
import com.sims.dto.*;
import com.sims.entity.GradeRecord;
import com.sims.entity.Student;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.exception.StudentNotFoundException;
import com.sims.kafka.dto.GradeUpdateEvent;
import com.sims.kafka.producer.GradeUpdateProducer;
import com.sims.kafka.producer.GradeUpdateProducerImpl;
import com.sims.repository.GradeRecordRepository;
import com.sims.repository.StudentRepository;
import com.sims.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sims.security.CustomUserDetailsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成绩管理服务类
 * 实现成绩的CRUD操作、批量更新、趋势分析和排行榜功能
 */
@Service
@Transactional
public class GradeService {

    private static final Logger logger = LoggerFactory.getLogger(GradeService.class);
    
    @Autowired
    private GradeRecordRepository gradeRecordRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GradeUpdateProducer gradeUpdateProducer;

    @Autowired
    private GradeRankingRedisService gradeRankingRedisService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private Environment environment;

    @Autowired
    private com.sims.security.service.PermissionService permissionService;

    /**
     * 仅 local（无 Kafka Consumer）时同步写通知；local-kafka/dev/prod 由 Consumer 接管。
     */
    private boolean useSyncNotificationFallback() {
        return Arrays.asList(environment.getActiveProfiles()).contains("local");
    }

    /**
     * 创建或覆盖成绩记录（每名学生每科仅一条；再次录入视为更新）
     */
    @CacheEvict(value = {"gradeRecord", "gradeRanking", "gradeTrend"}, allEntries = true)
    public GradeRecordResponse createGradeRecord(GradeRecordRequest request) {
        logger.info("创建成绩记录: {}", request);

        SubjectCatalog.requireSupported(request.getSubject());
        permissionService.requireModifyStudentGrade(request.getStudentId());
        String subject = request.getSubject().trim();
        BigDecimal defaultMax = SubjectCatalog.maxScoreOf(subject);

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(request.getStudentId()));

        BigDecimal maxScore = request.getMaxScore() != null ? request.getMaxScore() : defaultMax;
        if (request.getScore().compareTo(maxScore) > 0) {
            throw new IllegalArgumentException("成绩不能超过该科目满分 " + maxScore);
        }

        List<GradeRecord> existingList = gradeRecordRepository.findByStudentIdAndSubject(student.getId(), subject);
        GradeRecord gradeRecord = existingList.isEmpty() ? new GradeRecord() : existingList.get(0);
        BigDecimal oldScore = existingList.isEmpty() ? null : existingList.get(0).getScore();

        gradeRecord.setStudent(student);
        gradeRecord.setSubject(subject);
        gradeRecord.setScore(request.getScore());
        gradeRecord.setMaxScore(maxScore);
        gradeRecord.setExamType(
                request.getExamType() == null || request.getExamType().isBlank()
                        ? "正式考试"
                        : request.getExamType());
        gradeRecord.setExamDate(request.getExamDate());
        gradeRecord.setSemester(request.getSemester());
        gradeRecord.setAcademicYear(request.getAcademicYear());
        gradeRecord.setRemarks(request.getRemarks());

        User teacher = null;
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId()).orElse(null);
        }
        if (teacher == null) {
            teacher = resolveCurrentTeacher();
        }
        if (teacher != null) {
            gradeRecord.setTeacher(teacher);
        }

        GradeRecord savedGradeRecord = gradeRecordRepository.save(gradeRecord);
        gradeRankingRedisService.updateScore(savedGradeRecord);
        publishGradeUpdateEvent(oldScore, savedGradeRecord, teacher);

        logger.info("成绩记录保存成功: {}", savedGradeRecord);
        return new GradeRecordResponse(savedGradeRecord);
    }

    /**
     * 批量创建/更新成绩记录
     * 需求 4.3: 实现成绩编辑和批量更新
     */
    @CacheEvict(value = {"gradeRecord", "gradeRanking", "gradeTrend"}, allEntries = true)
    public Map<String, Object> batchUpdateGrades(BatchGradeUpdateRequest request) {
        logger.info("批量更新成绩记录，数量: {}", request.getGradeRecords().size());
        
        Map<String, Object> result = new HashMap<>();
        List<GradeRecordResponse> successRecords = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (GradeRecordRequest gradeRequest : request.getGradeRecords()) {
            try {
                GradeRecordResponse response = createGradeRecord(gradeRequest);
                successRecords.add(response);
            } catch (Exception e) {
                logger.warn("批量更新成绩失败: {} - {}", gradeRequest, e.getMessage());
                errors.add("学生ID " + gradeRequest.getStudentId() + " 科目 " + gradeRequest.getSubject() + ": " + e.getMessage());
            }
        }
        
        result.put("message", "批量更新成绩完成");
        result.put("total", request.getGradeRecords().size());
        result.put("success", successRecords.size());
        result.put("failure", errors.size());
        result.put("successRecords", successRecords);
        result.put("errors", errors);
        
        logger.info("批量更新成绩完成 - 成功: {}, 失败: {}", successRecords.size(), errors.size());
        return result;
    }

    /**
     * 获取成绩记录列表
     */
    @Cacheable(value = "gradeRecord", key = "#page + '_' + #size + '_' + #studentId + '_' + #classId + '_' + #subject")
    public PageResponse<GradeRecordResponse> getGradeRecords(int page, int size, Long studentId, Long classId, 
                                                           String subject, String examType, String semester, 
                                                           String academicYear, LocalDate startDate, LocalDate endDate) {
        logger.info("查询成绩记录列表 - page: {}, size: {}, studentId: {}, classId: {}, subject: {}", 
                   page, size, studentId, classId, subject);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("examDate").descending().and(Sort.by("createTime").descending()));
        
        Page<GradeRecord> gradeRecordPage = gradeRecordRepository.searchGradeRecords(
                studentId, classId, subject, examType, semester, academicYear, startDate, endDate, pageable);
        
        List<GradeRecordResponse> gradeRecordResponses = gradeRecordPage.getContent().stream()
                .map(GradeRecordResponse::new)
                .collect(Collectors.toList());
        
        PageResponse<GradeRecordResponse> response = new PageResponse<>(
                gradeRecordResponses,
                gradeRecordPage.getNumber(),
                gradeRecordPage.getSize(),
                gradeRecordPage.getTotalElements(),
                gradeRecordPage.getTotalPages()
        );
        
        logger.info("查询到 {} 条成绩记录", gradeRecordPage.getTotalElements());
        return response;
    }

    /**
     * 获取学生成绩记录
     */
    @Cacheable(value = "gradeRecord", key = "'student_' + #studentId")
    public List<GradeRecordResponse> getStudentGradeRecords(Long studentId) {
        logger.info("查询学生成绩记录: {}", studentId);
        
        List<GradeRecord> gradeRecords = gradeRecordRepository.findByStudentId(studentId);
        
        return gradeRecords.stream()
                .map(GradeRecordResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 获取学生成绩趋势分析
     * 需求 5.4: 添加成绩趋势分析功能
     */
    @Cacheable(value = "gradeTrend", key = "#studentId + '_' + #subject")
    public GradeTrendResponse getGradeTrend(Long studentId, String subject) {
        logger.info("查询学生成绩趋势 - studentId: {}, subject: {}", studentId, subject);
        
        // 验证学生是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        
        // 获取成绩趋势数据
        List<GradeRecord> gradeRecords = gradeRecordRepository.findGradeTrendByStudentIdAndSubject(studentId, subject);
        
        GradeTrendResponse response = new GradeTrendResponse(studentId, student.getName(), subject);
        
        if (!gradeRecords.isEmpty()) {
            // 转换为趋势点
            List<GradeTrendResponse.GradeTrendPoint> trendPoints = gradeRecords.stream()
                    .map(gr -> new GradeTrendResponse.GradeTrendPoint(
                            gr.getExamDate(), gr.getScore(), gr.getMaxScore(), gr.getExamType()))
                    .collect(Collectors.toList());
            
            response.setTrendPoints(trendPoints);
            
            // 计算统计信息
            List<BigDecimal> scores = gradeRecords.stream().map(GradeRecord::getScore).collect(Collectors.toList());
            BigDecimal averageScore = scores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(scores.size()), 2, BigDecimal.ROUND_HALF_UP);
            
            BigDecimal highestScore = scores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal lowestScore = scores.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            
            response.setAverageScore(averageScore);
            response.setHighestScore(highestScore);
            response.setLowestScore(lowestScore);
            
            // 分析趋势
            String trend = analyzeTrend(scores);
            response.setTrend(trend);
        }
        
        logger.info("成绩趋势分析完成: {}", response);
        return response;
    }

    /**
     * 获取科目成绩排行榜（Redis ZSet，C语言 / 英语四级）
     */
    public GradeRankingResponse getGradeRanking(String subject, String scope, Long classId, int limit) {
        logger.info("查询成绩排行榜(ZSet) - subject: {}, scope: {}, classId: {}, limit: {}",
                subject, scope, classId, limit);
        return gradeRankingRedisService.getRanking(subject, scope, classId, limit);
    }

    public void rebuildRankings() {
        gradeRankingRedisService.rebuildAll();
    }

    /**
     * 更新成绩记录
     * 需求 1.1: 发布成绩更新事件到Kafka
     */
    @CacheEvict(value = {"gradeRecord", "gradeRanking", "gradeTrend"}, allEntries = true)
    public GradeRecordResponse updateGradeRecord(Long gradeRecordId, GradeRecordRequest request) {
        logger.info("更新成绩记录: {} - {}", gradeRecordId, request);
        
        GradeRecord gradeRecord = gradeRecordRepository.findById(gradeRecordId)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在: " + gradeRecordId));

        permissionService.requireModifyStudentGrade(gradeRecord.getStudent().getId());

        if (request.getSubject() != null) {
            SubjectCatalog.requireSupported(request.getSubject());
            gradeRecord.setSubject(request.getSubject().trim());
        }
        
        // 保存旧成绩用于Kafka事件
        BigDecimal oldScore = gradeRecord.getScore();
        
        // 更新字段
        if (request.getScore() != null) {
            gradeRecord.setScore(request.getScore());
        }
        if (request.getMaxScore() != null) {
            gradeRecord.setMaxScore(request.getMaxScore());
        } else if (request.getSubject() != null) {
            gradeRecord.setMaxScore(SubjectCatalog.maxScoreOf(request.getSubject()));
        }
        if (request.getExamType() != null) {
            gradeRecord.setExamType(request.getExamType());
        }
        if (request.getExamDate() != null) {
            gradeRecord.setExamDate(request.getExamDate());
        }
        if (request.getSemester() != null) {
            gradeRecord.setSemester(request.getSemester());
        }
        if (request.getAcademicYear() != null) {
            gradeRecord.setAcademicYear(request.getAcademicYear());
        }
        if (request.getRemarks() != null) {
            gradeRecord.setRemarks(request.getRemarks());
        }
        
        // 更新教师
        User teacher = gradeRecord.getTeacher();
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId()).orElse(null);
            gradeRecord.setTeacher(teacher);
        }
        if (teacher == null) {
            teacher = resolveCurrentTeacher();
            if (teacher != null) {
                gradeRecord.setTeacher(teacher);
            }
        }
        
        GradeRecord updatedGradeRecord = gradeRecordRepository.save(gradeRecord);
        gradeRankingRedisService.updateScore(updatedGradeRecord);
        
        // 发布成绩更新事件到Kafka（只有成绩发生变化时才发布）
        if (request.getScore() != null && !request.getScore().equals(oldScore)) {
            publishGradeUpdateEvent(oldScore, updatedGradeRecord, teacher);
        }
        
        logger.info("成绩记录更新成功: {}", updatedGradeRecord);
        return new GradeRecordResponse(updatedGradeRecord);
    }

    /**
     * 删除成绩记录
     */
    @CacheEvict(value = {"gradeRecord", "gradeRanking", "gradeTrend"}, allEntries = true)
    public void deleteGradeRecord(Long gradeRecordId) {
        logger.info("删除成绩记录: {}", gradeRecordId);
        
        GradeRecord gradeRecord = gradeRecordRepository.findById(gradeRecordId)
                .orElseThrow(() -> new RuntimeException("成绩记录不存在: " + gradeRecordId));

        permissionService.requireModifyStudentGrade(gradeRecord.getStudent().getId());

        gradeRankingRedisService.removeScore(gradeRecord);
        gradeRecordRepository.delete(gradeRecord);
        
        logger.info("成绩记录删除成功: {}", gradeRecordId);
    }

    /**
     * 获取学生所有科目的最新成绩
     */
    public List<GradeRecordResponse> getStudentLatestGrades(Long studentId) {
        logger.info("查询学生最新成绩: {}", studentId);
        
        List<GradeRecord> gradeRecords = gradeRecordRepository.findLatestGradesByStudentIdGroupBySubject(studentId);
        
        return gradeRecords.stream()
                .map(GradeRecordResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户权限获取成绩记录
     */
    public PageResponse<GradeRecordResponse> getGradeRecordsByUser(User currentUser, int page, int size, 
                                                                 Long studentId, Long classId, String subject) {
        logger.info("用户 {} 查询成绩记录", currentUser.getUsername());
        
        if (currentUser.getRole() == UserRole.ADMIN) {
            // 管理员可以查看所有成绩记录
            return getGradeRecords(page, size, studentId, classId, subject, null, null, null, null, null);
        } else if (currentUser.getRole() == UserRole.TEACHER) {
            // 教师只能查看自己管理的班级的成绩记录
            // 这里需要根据教师权限过滤
            return getGradeRecords(page, size, studentId, classId, subject, null, null, null, null, null);
        } else if (currentUser.getRole() == UserRole.STUDENT) {
            // 学生只能查看自己的成绩记录
            Student student = studentRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new StudentNotFoundException("用户ID: " + currentUser.getId() + " 对应的学生信息不存在"));
            return getGradeRecords(page, size, student.getId(), null, subject, null, null, null, null, null);
        }
        
        throw new AccessDeniedException("无权访问成绩记录");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 发布成绩更新事件到 Kafka。
     * 方案 A：Kafka 发布失败时同步兜底写通知；成功则由 Consumer 写通知。
     * local（NoOp）仍直接同步写通知。
     */
    private void publishGradeUpdateEvent(BigDecimal oldGrade, GradeRecord gradeRecord, User teacher) {
        try {
            GradeUpdateEvent event = new GradeUpdateEvent(
                    gradeRecord.getStudent().getId(),
                    gradeRecord.getStudent().getName(),
                    gradeRecord.getSubject(),
                    oldGrade,
                    gradeRecord.getScore(),
                    gradeRecord.getMaxScore(),
                    teacher != null ? teacher.getId() : null,
                    teacher != null ? teacher.getUsername() : null
            );

            event.setExamType(gradeRecord.getExamType());
            event.setSemester(gradeRecord.getSemester());
            event.setAcademicYear(gradeRecord.getAcademicYear());
            event.setRemarks(gradeRecord.getRemarks());

            if (useSyncNotificationFallback()) {
                // local：无真实 Kafka，同步写通知
                safeWriteNotification(event, "local同步");
                try {
                    gradeUpdateProducer.publishGradeUpdateAsync(event,
                            GradeUpdateProducerImpl.createDefaultCallback());
                } catch (Exception kafkaEx) {
                    logger.warn("NoOp 发布旁路异常（可忽略）: {}", kafkaEx.getMessage());
                }
            } else {
                // local-kafka / dev / prod：优先 Kafka；失败则同步兜底
                try {
                    gradeUpdateProducer.publishGradeUpdateAsync(event, new GradeUpdateProducer.GradeUpdateCallback() {
                        @Override
                        public void onSuccess(GradeUpdateEvent e, org.springframework.kafka.support.SendResult<String, Object> result) {
                            if (result == null || result.getRecordMetadata() == null) {
                                logger.info("成绩更新事件发布成功（无 metadata）- 事件ID: {}", e.getEventId());
                                return;
                            }
                            logger.info("成绩更新事件发布成功 - 事件ID: {}, 主题: {}, 分区: {}, 偏移量: {}",
                                    e.getEventId(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }

                        @Override
                        public void onFailure(GradeUpdateEvent e, Throwable exception) {
                            logger.warn("Kafka 发布失败，同步兜底写通知 - 事件ID: {}, 错误: {}",
                                    e.getEventId(), exception != null ? exception.getMessage() : "unknown");
                            safeWriteNotification(e, "Kafka失败兜底");
                        }
                    });
                } catch (Exception kafkaEx) {
                    logger.warn("Kafka 发布抛错，同步兜底写通知 - 事件ID: {}, 错误: {}",
                            event.getEventId(), kafkaEx.getMessage());
                    safeWriteNotification(event, "Kafka抛错兜底");
                }
            }

            logger.info("成绩更新事件已处理: 学生ID={}, 科目={}, 新成绩={}, syncFallbackProfile={}",
                    gradeRecord.getStudent().getId(), gradeRecord.getSubject(),
                    gradeRecord.getScore(), useSyncNotificationFallback());

        } catch (Exception e) {
            logger.error("处理成绩更新旁路失败: 学生ID={}, 科目={}, 错误={}",
                    gradeRecord.getStudent().getId(), gradeRecord.getSubject(), e.getMessage(), e);
        }
    }

    private void safeWriteNotification(GradeUpdateEvent event, String reason) {
        try {
            notificationService.createGradeUpdateNotification(event);
            logger.info("通知已写入（{}）: eventId={}", reason, event.getEventId());
        } catch (Exception notifyEx) {
            logger.error("写入成绩变更通知失败（{}）: eventId={}, 错误={}",
                    reason, event.getEventId(), notifyEx.getMessage(), notifyEx);
        }
    }

    /**
     * 从前端未传 teacherId 时，用当前登录教师补全（保证 Kafka 事件与通知文案可用）
     */
    private User resolveCurrentTeacher() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal principal) {
                User user = principal.getUser();
                if (user != null && (user.getRole() == UserRole.TEACHER || user.getRole() == UserRole.ADMIN)) {
                    return user;
                }
            }
        } catch (Exception e) {
            logger.debug("无法解析当前教师: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 分析成绩趋势
     */
    private String analyzeTrend(List<BigDecimal> scores) {
        if (scores.size() < 2) {
            return "STABLE";
        }
        
        // 简单的趋势分析：比较前半部分和后半部分的平均值
        int midPoint = scores.size() / 2;
        List<BigDecimal> firstHalf = scores.subList(0, midPoint);
        List<BigDecimal> secondHalf = scores.subList(midPoint, scores.size());
        
        BigDecimal firstAvg = firstHalf.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(firstHalf.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal secondAvg = secondHalf.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(secondHalf.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal difference = secondAvg.subtract(firstAvg);
        
        if (difference.compareTo(new BigDecimal("5")) > 0) {
            return "IMPROVING";
        } else if (difference.compareTo(new BigDecimal("-5")) < 0) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }
}