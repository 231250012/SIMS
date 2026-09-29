package com.sims.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sims.dto.NotificationResponse;
import com.sims.dto.PageResponse;
import com.sims.entity.Notification;
import com.sims.entity.Student;
import com.sims.kafka.dto.GradeUpdateEvent;
import com.sims.repository.NotificationRepository;
import com.sims.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站内通知服务：成绩变更等事件落库为用户可读消息。
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final String TYPE_GRADE_UPDATE = "GRADE_UPDATE";

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 根据成绩更新事件为学生账号写入站内通知（无绑定账号则跳过；按 eventId 幂等）
     */
    @Transactional
    public void createGradeUpdateNotification(GradeUpdateEvent event) {
        if (event == null || event.getStudentId() == null) {
            return;
        }

        if (event.getEventId() != null && notificationRepository.existsByEventId(event.getEventId())) {
            logger.info("通知已存在，跳过重复写入: eventId={}", event.getEventId());
            return;
        }

        Student student = studentRepository.findById(event.getStudentId()).orElse(null);
        if (student == null || student.getUser() == null) {
            logger.debug("学生无系统账号，跳过通知: studentId={}", event.getStudentId());
            return;
        }

        try {
            Long userId = student.getUser().getId();
            String subject = event.getSubject() != null ? event.getSubject() : "科目";
            String teacherName = event.getTeacherName() != null ? event.getTeacherName() : "教师";

            String title = "成绩更新：" + subject;
            String content = buildGradeContent(teacherName, subject, event.getOldGrade(),
                    event.getNewGrade(), event.getMaxScore());

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("eventId", event.getEventId());
            payloadMap.put("oldScore", event.getOldGrade());
            payloadMap.put("newScore", event.getNewGrade());
            payloadMap.put("maxScore", event.getMaxScore());
            payloadMap.put("teacherId", event.getTeacherId());
            payloadMap.put("teacherName", teacherName);
            payloadMap.put("examType", event.getExamType());

            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(TYPE_GRADE_UPDATE);
            notification.setEventId(event.getEventId());
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedStudentId(event.getStudentId());
            notification.setRelatedSubject(subject);
            notification.setPayload(objectMapper.writeValueAsString(payloadMap));
            notification.setIsRead(false);

            notificationRepository.save(notification);
            logger.info("已写入成绩变更通知: userId={}, studentId={}, subject={}, eventId={}",
                    userId, event.getStudentId(), subject, event.getEventId());
        } catch (org.springframework.dao.DataIntegrityViolationException dup) {
            // 并发下唯一索引冲突视为已写入
            logger.info("通知并发幂等命中，跳过: eventId={}", event.getEventId());
        } catch (Exception e) {
            logger.error("写入成绩变更通知失败: studentId={}, error={}",
                    event.getStudentId(), e.getMessage(), e);
            throw new IllegalStateException("写入成绩变更通知失败: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listMyNotifications(Long userId, int page, int size, Boolean unreadOnly) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        Page<Notification> result;
        if (Boolean.TRUE.equals(unreadOnly)) {
            result = notificationRepository.findByUserIdAndIsReadOrderByCreateTimeDesc(userId, false, pageable);
        } else {
            result = notificationRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
        }

        List<NotificationResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public NotificationResponse markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("通知不存在"));
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该通知");
        }
        notification.setIsRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllReadByUserId(userId);
    }

    @Transactional
    public void delete(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("通知不存在"));
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作该通知");
        }
        notificationRepository.delete(notification);
    }

    private String buildGradeContent(String teacherName, String subject,
                                     BigDecimal oldGrade, BigDecimal newGrade, BigDecimal maxScore) {
        String newScoreText = newGrade != null ? newGrade.toPlainString() : "-";
        String maxText = maxScore != null ? maxScore.toPlainString() : "-";
        if (oldGrade == null) {
            return String.format("教师 %s 录入了您的 %s 成绩：%s（满分 %s）",
                    teacherName, subject, newScoreText, maxText);
        }
        return String.format("教师 %s 将您的 %s 成绩从 %s 更新为 %s（满分 %s）",
                teacherName, subject, oldGrade.toPlainString(), newScoreText, maxText);
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setType(n.getType());
        r.setTitle(n.getTitle());
        r.setContent(n.getContent());
        r.setRelatedStudentId(n.getRelatedStudentId());
        r.setRelatedSubject(n.getRelatedSubject());
        r.setPayload(n.getPayload());
        r.setIsRead(n.getIsRead());
        r.setCreateTime(n.getCreateTime() != null ? n.getCreateTime().format(DATETIME_FMT) : null);
        return r;
    }
}
