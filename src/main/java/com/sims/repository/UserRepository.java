package com.sims.repository;

import com.sims.entity.User;
import com.sims.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据角色查找用户列表
     * @param role 用户角色
     * @return 用户列表
     */
    List<User> findByRole(UserRole role);
    
    /**
     * 查找所有教师用户
     * @return 教师用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = 'TEACHER'")
    List<User> findAllTeachers();
    
    /**
     * 查找所有学生用户
     * @return 学生用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT'")
    List<User> findAllStudents();
    
    /**
     * 查找所有管理员用户
     * @return 管理员用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();
    
    /**
     * 根据用户名模糊查询
     * @param username 用户名关键字
     * @return 匹配的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    List<User> findByUsernameContaining(@Param("username") String username);
    
    /**
     * 统计各角色用户数量
     * @param role 用户角色
     * @return 用户数量
     */
    long countByRole(UserRole role);
    
    /**
     * 查找有管理班级的教师
     * @return 有班级的教师列表
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.managedClasses c WHERE u.role = 'TEACHER'")
    List<User> findTeachersWithClasses();
    
    /**
     * 查找没有管理班级的教师
     * @return 没有班级的教师列表
     */
    @Query("SELECT u FROM User u WHERE u.role = 'TEACHER' AND u.managedClasses IS EMPTY")
    List<User> findTeachersWithoutClasses();
}