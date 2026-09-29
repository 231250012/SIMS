package com.sims.repository;

import com.sims.entity.GradeRecord;
import com.sims.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 成绩记录数据访问接口
 */
@Repository
public interface GradeRecordRepository extends JpaRepository<GradeRecord, Long> {
    
    /**
     * 根据学生ID查找成绩记录
     */
    List<GradeRecord> findByStudentId(Long studentId);
    
    /**
     * 根据学生ID分页查找成绩记录
     */
    Page<GradeRecord> findByStudentId(Long studentId, Pageable pageable);
    
    /**
     * 根据学生和科目查找成绩记录
     */
    List<GradeRecord> findByStudentIdAndSubject(Long studentId, String subject);
    
    /**
     * 根据班级ID查找成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.student.studentClass.id = :classId")
    List<GradeRecord> findByClassId(@Param("classId") Long classId);
    
    /**
     * 根据教师ID查找成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.teacher.id = :teacherId")
    List<GradeRecord> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 根据科目查找成绩记录
     */
    List<GradeRecord> findBySubject(String subject);
    
    /**
     * 根据考试类型查找成绩记录
     */
    List<GradeRecord> findByExamType(String examType);
    
    /**
     * 根据学期查找成绩记录
     */
    List<GradeRecord> findBySemester(String semester);
    
    /**
     * 根据学年查找成绩记录
     */
    List<GradeRecord> findByAcademicYear(String academicYear);
    
    /**
     * 根据考试日期范围查找成绩记录
     */
    List<GradeRecord> findByExamDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据成绩范围查找成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.score BETWEEN :minScore AND :maxScore")
    List<GradeRecord> findByScoreBetween(@Param("minScore") BigDecimal minScore, @Param("maxScore") BigDecimal maxScore);
    
    /**
     * 获取学生最新成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.student.id = :studentId ORDER BY gr.examDate DESC, gr.createTime DESC")
    List<GradeRecord> findLatestGradesByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 获取学生某科目最新成绩
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.student.id = :studentId AND gr.subject = :subject ORDER BY gr.examDate DESC, gr.createTime DESC")
    Optional<GradeRecord> findLatestGradeByStudentIdAndSubject(@Param("studentId") Long studentId, @Param("subject") String subject);
    
    /**
     * 计算学生平均成绩
     */
    @Query("SELECT AVG(gr.score) FROM GradeRecord gr WHERE gr.student.id = :studentId")
    Optional<BigDecimal> calculateAverageScoreByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 计算学生某科目平均成绩
     */
    @Query("SELECT AVG(gr.score) FROM GradeRecord gr WHERE gr.student.id = :studentId AND gr.subject = :subject")
    Optional<BigDecimal> calculateAverageScoreByStudentIdAndSubject(@Param("studentId") Long studentId, @Param("subject") String subject);
    
    /**
     * 计算班级某科目平均成绩
     */
    @Query("SELECT AVG(gr.score) FROM GradeRecord gr WHERE gr.student.studentClass.id = :classId AND gr.subject = :subject")
    Optional<BigDecimal> calculateAverageScoreByClassIdAndSubject(@Param("classId") Long classId, @Param("subject") String subject);
    
    /**
     * 获取班级某科目成绩排行榜
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.student.studentClass.id = :classId AND gr.subject = :subject ORDER BY gr.score DESC")
    List<GradeRecord> findClassSubjectRanking(@Param("classId") Long classId, @Param("subject") String subject);
    
    /**
     * 获取全校某科目成绩排行榜
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.subject = :subject ORDER BY gr.score DESC")
    List<GradeRecord> findSubjectRanking(@Param("subject") String subject);
    
    /**
     * 获取学生成绩趋势数据
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.student.id = :studentId AND gr.subject = :subject ORDER BY gr.examDate ASC")
    List<GradeRecord> findGradeTrendByStudentIdAndSubject(@Param("studentId") Long studentId, @Param("subject") String subject);
    
    /**
     * 获取班级成绩分布统计
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN gr.score >= 90 THEN '优秀' " +
           "WHEN gr.score >= 80 THEN '良好' " +
           "WHEN gr.score >= 70 THEN '中等' " +
           "WHEN gr.score >= 60 THEN '及格' " +
           "ELSE '不及格' END as gradeLevel, " +
           "COUNT(gr) as count " +
           "FROM GradeRecord gr " +
           "WHERE gr.student.studentClass.id = :classId AND gr.subject = :subject " +
           "GROUP BY " +
           "CASE " +
           "WHEN gr.score >= 90 THEN '优秀' " +
           "WHEN gr.score >= 80 THEN '良好' " +
           "WHEN gr.score >= 70 THEN '中等' " +
           "WHEN gr.score >= 60 THEN '及格' " +
           "ELSE '不及格' END")
    List<Object[]> findGradeDistributionByClassIdAndSubject(@Param("classId") Long classId, @Param("subject") String subject);
    
    /**
     * 获取学生所有科目的最新成绩
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE gr.student.id = :studentId AND " +
           "gr.id IN (SELECT MAX(gr2.id) FROM GradeRecord gr2 WHERE gr2.student.id = :studentId GROUP BY gr2.subject)")
    List<GradeRecord> findLatestGradesByStudentIdGroupBySubject(@Param("studentId") Long studentId);
    
    /**
     * 获取学生所有科目的最新成绩（返回科目和分数）
     */
    @Query("SELECT gr.subject, gr.score FROM GradeRecord gr WHERE gr.student.id = :studentId AND " +
           "gr.id IN (SELECT MAX(gr2.id) FROM GradeRecord gr2 WHERE gr2.student.id = :studentId GROUP BY gr2.subject)")
    List<Object[]> findLatestGradesByStudent(@Param("studentId") Long studentId);
    
    /**
     * 多条件搜索成绩记录
     */
    @Query("SELECT gr FROM GradeRecord gr WHERE " +
           "(:studentId IS NULL OR gr.student.id = :studentId) AND " +
           "(:classId IS NULL OR gr.student.studentClass.id = :classId) AND " +
           "(:subject IS NULL OR gr.subject = :subject) AND " +
           "(:examType IS NULL OR gr.examType = :examType) AND " +
           "(:semester IS NULL OR gr.semester = :semester) AND " +
           "(:academicYear IS NULL OR gr.academicYear = :academicYear) AND " +
           "(:startDate IS NULL OR gr.examDate >= :startDate) AND " +
           "(:endDate IS NULL OR gr.examDate <= :endDate)")
    Page<GradeRecord> searchGradeRecords(@Param("studentId") Long studentId,
                                        @Param("classId") Long classId,
                                        @Param("subject") String subject,
                                        @Param("examType") String examType,
                                        @Param("semester") String semester,
                                        @Param("academicYear") String academicYear,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        Pageable pageable);
}