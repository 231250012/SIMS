package com.sims.repository;

import com.sims.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 班级数据访问接口
 */
@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {

    @Query("SELECT DISTINCT c FROM Class c LEFT JOIN FETCH c.teacher ORDER BY c.id")
    List<Class> findAllWithTeacher();
    
    /**
     * 根据班级名称查找班级
     * @param name 班级名称
     * @return 班级信息
     */
    Optional<Class> findByName(String name);
    
    /**
     * 检查班级名称是否存在
     * @param name 班级名称
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据班级名称模糊查询
     * @param name 班级名称关键字
     * @return 匹配的班级列表
     */
    @Query("SELECT c FROM Class c WHERE c.name LIKE %:name%")
    List<Class> findByNameContaining(@Param("name") String name);
    
    /**
     * 根据教师ID查找班级
     * @param teacherId 教师ID
     * @return 班级列表
     */
    @Query("SELECT c FROM Class c WHERE c.teacher.id = :teacherId")
    List<Class> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 查找没有分配教师的班级
     * @return 班级列表
     */
    @Query("SELECT c FROM Class c WHERE c.teacher IS NULL")
    List<Class> findClassesWithoutTeacher();
    
    /**
     * 查找有学生的班级
     * @return 班级列表
     */
    @Query("SELECT DISTINCT c FROM Class c WHERE c.students IS NOT EMPTY")
    List<Class> findClassesWithStudents();
    
    /**
     * 查找没有学生的班级
     * @return 班级列表
     */
    @Query("SELECT c FROM Class c WHERE c.students IS EMPTY")
    List<Class> findEmptyClasses();
    
    /**
     * 统计教师管理的班级数量
     * @param teacherId 教师ID
     * @return 班级数量
     */
    @Query("SELECT COUNT(c) FROM Class c WHERE c.teacher.id = :teacherId")
    long countByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 查找班级及其学生数量
     * @return 班级和学生数量的统计信息
     */
    @Query("SELECT c, SIZE(c.students) FROM Class c")
    List<Object[]> findClassesWithStudentCount();
    
    /**
     * 根据学生数量范围查找班级
     * @param minCount 最少学生数
     * @param maxCount 最多学生数
     * @return 班级列表
     */
    @Query("SELECT c FROM Class c WHERE SIZE(c.students) BETWEEN :minCount AND :maxCount")
    List<Class> findByStudentCountBetween(@Param("minCount") int minCount, @Param("maxCount") int maxCount);
    
    /**
     * 查找学生数量最多的班级
     * @return 班级列表（按学生数量降序）
     */
    @Query("SELECT c FROM Class c ORDER BY SIZE(c.students) DESC")
    List<Class> findClassesOrderByStudentCountDesc();
    
    /**
     * 查找特定教师管理的班级名称列表
     * @param teacherId 教师ID
     * @return 班级名称列表
     */
    @Query("SELECT c.name FROM Class c WHERE c.teacher.id = :teacherId")
    List<String> findClassNamesByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 检查教师是否管理指定班级
     * @param teacherId 教师ID
     * @param classId 班级ID
     * @return 是否管理
     */
    @Query("SELECT COUNT(c) > 0 FROM Class c WHERE c.id = :classId AND c.teacher.id = :teacherId")
    boolean isTeacherManagingClass(@Param("teacherId") Long teacherId, @Param("classId") Long classId);
    
    /**
     * 查找可以分配给教师的班级（没有教师或当前教师管理的班级）
     * @param teacherId 教师ID
     * @return 班级列表
     */
    @Query("SELECT c FROM Class c WHERE c.teacher IS NULL OR c.teacher.id = :teacherId")
    List<Class> findAvailableClassesForTeacher(@Param("teacherId") Long teacherId);
}