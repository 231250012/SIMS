package com.sims.service;

import com.sims.domain.SubjectCatalog;
import com.sims.dto.GradeRankingResponse;
import com.sims.entity.GradeRecord;
import com.sims.entity.Student;
import com.sims.repository.GradeRecordRepository;
import com.sims.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于 Redis ZSet 的科目成绩排行榜（C语言 / 英语四级）。
 * score = 成绩分值，member = studentId。
 */
@Service
public class GradeRankingRedisService {

    private static final Logger logger = LoggerFactory.getLogger(GradeRankingRedisService.class);
    private static final String SCHOOL_KEY_PREFIX = "sims:grade:rank:";
    private static final String CLASS_KEY_PREFIX = "sims:grade:rank:class:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private GradeRecordRepository gradeRecordRepository;

    @Autowired
    private StudentRepository studentRepository;

    public String schoolKey(String subject) {
        return SCHOOL_KEY_PREFIX + subject;
    }

    public String classKey(Long classId, String subject) {
        return CLASS_KEY_PREFIX + classId + ":" + subject;
    }

    public void updateScore(GradeRecord record) {
        if (record == null || record.getStudent() == null || !SubjectCatalog.isSupported(record.getSubject())) {
            return;
        }
        String subject = record.getSubject().trim();
        String member = String.valueOf(record.getStudent().getId());
        double score = record.getScore().doubleValue();

        stringRedisTemplate.opsForZSet().add(schoolKey(subject), member, score);

        Long classId = record.getStudent().getStudentClass() != null
                ? record.getStudent().getStudentClass().getId()
                : null;
        if (classId != null) {
            stringRedisTemplate.opsForZSet().add(classKey(classId, subject), member, score);
        }
        logger.debug("ZSet 更新排行: subject={}, studentId={}, score={}", subject, member, score);
    }

    public void removeScore(GradeRecord record) {
        if (record == null || record.getStudent() == null || !SubjectCatalog.isSupported(record.getSubject())) {
            return;
        }
        String subject = record.getSubject().trim();
        String member = String.valueOf(record.getStudent().getId());
        stringRedisTemplate.opsForZSet().remove(schoolKey(subject), member);

        Long classId = record.getStudent().getStudentClass() != null
                ? record.getStudent().getStudentClass().getId()
                : null;
        if (classId != null) {
            stringRedisTemplate.opsForZSet().remove(classKey(classId, subject), member);
        }
    }

    @Transactional(readOnly = true)
    public GradeRankingResponse getRanking(String subject, String scope, Long classId, int limit) {
        SubjectCatalog.requireSupported(subject);
        subject = subject.trim();
        int safeLimit = Math.max(1, Math.min(limit, 100));

        String key = "CLASS".equalsIgnoreCase(scope) && classId != null
                ? classKey(classId, subject)
                : schoolKey(subject);

        Long size = stringRedisTemplate.opsForZSet().zCard(key);
        if (size == null || size == 0) {
            rebuild(subject, "CLASS".equalsIgnoreCase(scope) ? classId : null);
        }

        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, safeLimit - 1);

        GradeRankingResponse response = new GradeRankingResponse(subject,
                "CLASS".equalsIgnoreCase(scope) ? "CLASS" : "SCHOOL");
        response.setClassId(classId);

        if (tuples == null || tuples.isEmpty()) {
            response.setRankings(Collections.emptyList());
            response.setTotalStudents(0);
            response.setHighestScore(BigDecimal.ZERO);
            response.setLowestScore(BigDecimal.ZERO);
            return response;
        }

        List<Long> studentIds = tuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        Map<Long, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a));

        BigDecimal maxScore = SubjectCatalog.maxScoreOf(subject);
        List<GradeRankingResponse.RankingEntry> rankings = new ArrayList<>();
        int rank = 1;
        BigDecimal highest = null;
        BigDecimal lowest = null;

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) continue;
            Long sid = Long.valueOf(tuple.getValue());
            Student student = studentMap.get(sid);
            BigDecimal score = BigDecimal.valueOf(tuple.getScore());

            String name = student != null ? student.getName() : ("学生#" + sid);
            String className = student != null && student.getStudentClass() != null
                    ? student.getStudentClass().getName() : "";

            rankings.add(new GradeRankingResponse.RankingEntry(
                    rank++, sid, name, className, score, maxScore));

            if (highest == null || score.compareTo(highest) > 0) highest = score;
            if (lowest == null || score.compareTo(lowest) < 0) lowest = score;

            if (response.getClassName() == null && !className.isEmpty()) {
                response.setClassName(className);
            }
        }

        response.setRankings(rankings);
        response.setTotalStudents(rankings.size());
        response.setHighestScore(highest != null ? highest : BigDecimal.ZERO);
        response.setLowestScore(lowest != null ? lowest : BigDecimal.ZERO);
        return response;
    }

    /** 从数据库重建指定科目（及可选班级）的 ZSet */
    @Transactional(readOnly = true)
    public void rebuild(String subject, Long classId) {
        SubjectCatalog.requireSupported(subject);
        subject = subject.trim();

        List<GradeRecord> records;
        if (classId != null) {
            records = gradeRecordRepository.findClassSubjectRanking(classId, subject);
            String key = classKey(classId, subject);
            stringRedisTemplate.delete(key);
            for (GradeRecord gr : records) {
                if (gr.getStudent() != null && gr.getScore() != null) {
                    stringRedisTemplate.opsForZSet().add(key,
                            String.valueOf(gr.getStudent().getId()),
                            gr.getScore().doubleValue());
                }
            }
            logger.info("重建班级科目排行 ZSet: classId={}, subject={}, size={}", classId, subject, records.size());
        } else {
            records = gradeRecordRepository.findSubjectRanking(subject);
            String key = schoolKey(subject);
            stringRedisTemplate.delete(key);
            for (GradeRecord gr : records) {
                if (gr.getStudent() == null || gr.getScore() == null) continue;
                stringRedisTemplate.opsForZSet().add(key,
                        String.valueOf(gr.getStudent().getId()),
                        gr.getScore().doubleValue());
                if (gr.getStudent().getStudentClass() != null) {
                    Long cid = gr.getStudent().getStudentClass().getId();
                    stringRedisTemplate.opsForZSet().add(classKey(cid, subject),
                            String.valueOf(gr.getStudent().getId()),
                            gr.getScore().doubleValue());
                }
            }
            logger.info("重建全校科目排行 ZSet: subject={}, size={}", subject, records.size());
        }
    }

    public void rebuildAll() {
        for (String subject : SubjectCatalog.orderedNames()) {
            rebuild(subject, null);
        }
    }
}
