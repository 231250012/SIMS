package com.sims.repository;

import com.sims.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 学生数据访问接口
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    /**
     * 根据姓名查找学生
     * @param name 学生姓名
     * @return 学生列表
     */
    List<Student> findByName(String name);
    
    /**
     * 根据姓名模糊查询学生
     * @param name 姓名关键字
     * @return 匹配的学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.name LIKE %:name%")
    List<Student> findByNameContaining(@Param("name") String name);
    
    /**
     * 根据班级ID查找学生
     * @param classId 班级ID
     * @return 学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.studentClass.id = :classId")
    List<Student> findByClassId(@Param("classId") Long classId);
    
    /**
     * 根据班级ID分页查找学生
     * @param classId 班级ID
     * @param pageable 分页参数
     * @return 学生分页数据
     */
    @Query("SELECT s FROM Student s WHERE s.studentClass.id = :classId")
    Page<Student> findByClassId(@Param("classId") Long classId, Pageable pageable);
    
    /**
     * 根据教师ID查找学生（该教师管理的所有班级的学生）
     * @param teacherId 教师ID
     * @return 学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.studentClass.teacher.id = :teacherId")
    List<Student> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 根据教师ID分页查找学生
     * @param teacherId 教师ID
     * @param pageable 分页参数
     * @return 学生分页数据
     */
    @Query("SELECT s FROM Student s WHERE s.studentClass.teacher.id = :teacherId")
    Page<Student> findByTeacherId(@Param("teacherId") Long teacherId, Pageable pageable);
    
    /**
     * 查找未分配班级的学生
     * @return 未分配班级的学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.studentClass IS NULL")
    List<Student> findStudentsWithoutClass();
    
    /**
     * 根据年龄范围查找学生
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.age BETWEEN :minAge AND :maxAge")
    List<Student> findByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);
    
    /**
     * 根据成绩范围查找学生
     * @param minScore 最低分数
     * @param maxScore 最高分数
     * @return 学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.score BETWEEN :minScore AND :maxScore")
    List<Student> findByScoreBetween(@Param("minScore") BigDecimal minScore, @Param("maxScore") BigDecimal maxScore);
    
    /**
     * 按成绩降序查找前N名学生
     * @param limit 限制数量
     * @return 学生列表
     */
    @Query("SELECT s FROM Student s ORDER BY s.score DESC")
    List<Student> findTopStudentsByScore(Pageable pageable);
    
    /**
     * 查找成绩排行榜（按成绩降序）
     * @return 学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.score > 0 ORDER BY s.score DESC")
    List<Student> findScoreRanking();
    
    /**
     * 根据班级查找成绩排行榜
     * @param classId 班级ID
     * @return 学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.studentClass.id = :classId AND s.score > 0 ORDER BY s.score DESC")
    List<Student> findScoreRankingByClass(@Param("classId") Long classId);
    
    /**
     * 统计班级学生数量
     * @param classId 班级ID
     * @return 学生数量
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.studentClass.id = :classId")
    long countByClassId(@Param("classId") Long classId);
    
    /**
     * 统计教师管理的学生数量
     * @param teacherId 教师ID
     * @return 学生数量
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.studentClass.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 计算班级平均分
     * @param classId 班级ID
     * @return 平均分
     */
    @Query("SELECT AVG(s.score) FROM Student s WHERE s.studentClass.id = :classId")
    Optional<BigDecimal> calculateAverageScoreByClass(@Param("classId") Long classId);
    
    /**
     * 计算全校平均分
     * @return 平均分
     */
    @Query("SELECT AVG(s.score) FROM Student s WHERE s.score > 0")
    Optional<BigDecimal> calculateOverallAverageScore();
    
    /**
     * 统计成绩区间内的学生数量
     * @param minScore 最低分数
     * @param maxScore 最高分数
     * @return 学生数量
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.score BETWEEN :minScore AND :maxScore")
    Long countByScoreBetween(@Param("minScore") BigDecimal minScore, @Param("maxScore") BigDecimal maxScore);
    
    /**
     * 统计成绩低于指定分数的学生数量
     * @param score 分数
     * @return 学生数量
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.score < :score")
    Long countByScoreLessThan(@Param("score") BigDecimal score);
    
    /**
     * 根据用户ID查找学生
     * @param userId 用户ID
     * @return 学生信息
     */
    @Query("SELECT s FROM Student s WHERE s.user.id = :userId")
    Optional<Student> findByUserId(@Param("userId") Long userId);
    
    /**
     * 多条件搜索学生（支持姓名、班级、年龄范围）
     * @param name 姓名关键字
     * @param classId 班级ID
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @param pageable 分页参数
     * @return 学生分页数据
     */
    @Query("SELECT s FROM Student s WHERE " +
           "(:name IS NULL OR s.name LIKE %:name%) AND " +
           "(:classId IS NULL OR s.studentClass.id = :classId) AND " +
           "(:minAge IS NULL OR s.age >= :minAge) AND " +
           "(:maxAge IS NULL OR s.age <= :maxAge)")
    Page<Student> searchStudents(@Param("name") String name,
                                @Param("classId") Long classId,
                                @Param("minAge") Integer minAge,
                                @Param("maxAge") Integer maxAge,
                                Pageable pageable);

    /**
     * 根据班级ID查找学生（排除指定学生）
     * @param classId 班级ID
     * @param excludeStudentId 要排除的学生ID
     * @return 学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.studentClass.id = :classId AND s.id != :excludeStudentId")
    List<Student> findByStudentClassIdAndIdNot(@Param("classId") Long classId, @Param("excludeStudentId") Long excludeStudentId);

    /**
     * 根据班级ID查找所有学生
     * @param classId 班级ID
     * @return 学生列表
     */
    @Query("SELECT s FROM Student s WHERE s.studentClass.id = :classId")
    List<Student> findByStudentClassId(@Param("classId") Long classId);
}