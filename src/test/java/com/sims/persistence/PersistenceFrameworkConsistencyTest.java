package com.sims.persistence;


import com.sims.entity.Student;
import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.repository.StudentRepository;
import com.sims.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

/**
 * Property 16: 持久化框架一致性
 * 
 * 验证需求: Requirements 6.1, 6.2, 6.3
 * 
 * 对于任何数据库操作，无论使用JPA还是MyBatis模式，
 * 系统应该保持相同的业务逻辑接口和行为
 */
@SpringBootTest
@ActiveProfiles("test")

@Transactional
public class PersistenceFrameworkConsistencyTest {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    private Random random = new Random();

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        studentRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * 属性测试: 用户创建和查询的一致性
     * 验证无论使用哪种持久化框架，用户的创建和查询行为应该一致
     */
    @Test
    void userCreateAndRetrieveConsistency() {
        // 运行多次测试以模拟属性测试
        for (int i = 0; i < 10; i++) {
            String username = generateValidUsername();
            String password = generateValidPassword();
        
        // Given: 创建一个用户
        User originalUser = new User();
        originalUser.setUsername(username);
        originalUser.setPassword(password);
        originalUser.setRole(UserRole.STUDENT);
        
        // When: 保存用户
        User savedUser = userRepository.save(originalUser);
        
        // Then: 验证保存成功
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(username);
        assertThat(savedUser.getPassword()).isEqualTo(password);
        
        // When: 通过ID查询用户
        Optional<User> retrievedById = userRepository.findById(savedUser.getId());
        
        // Then: 验证查询结果一致性
        assertThat(retrievedById).isPresent();
        assertThat(retrievedById.get().getUsername()).isEqualTo(username);
        assertThat(retrievedById.get().getPassword()).isEqualTo(password);
        
        // When: 通过用户名查询用户
        Optional<User> retrievedByUsername = userRepository.findByUsername(username);
        
            // Then: 验证查询结果一致性
            assertThat(retrievedByUsername).isPresent();
            assertThat(retrievedByUsername.get().getId()).isEqualTo(savedUser.getId());
            assertThat(retrievedByUsername.get().getPassword()).isEqualTo(password);
            
            // 清理数据
            userRepository.delete(savedUser);
        }
    }

    /**
     * 属性测试: 学生创建和关联查询的一致性
     * 验证学生与用户的关联关系在不同持久化框架下保持一致
     */
    @Test
    void studentCreateAndRelationshipConsistency() {
        // 运行多次测试以模拟属性测试
        for (int i = 0; i < 5; i++) {
            String studentName = generateValidStudentName();
            Integer age = generateValidAge();
            String username = generateValidUsername();
        
        // Given: 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword("password123");
        user.setRole(UserRole.STUDENT);
        User savedUser = userRepository.save(user);
        
        // Given: 创建学生
        Student student = new Student();
        student.setName(studentName);
        student.setAge(age);
        student.setUser(savedUser);
        student.setScore(new BigDecimal("85.5"));
        
        // When: 保存学生
        Student savedStudent = studentRepository.save(student);
        
        // Then: 验证保存成功和关联关系
        assertThat(savedStudent.getId()).isNotNull();
        assertThat(savedStudent.getName()).isEqualTo(studentName);
        assertThat(savedStudent.getAge()).isEqualTo(age);
        assertThat(savedStudent.getUser().getId()).isEqualTo(savedUser.getId());
        
        // When: 通过姓名查询学生
        var retrievedByName = studentRepository.findByName(studentName);
        
        // Then: 验证查询结果和关联关系一致性
        assertThat(retrievedByName).isNotEmpty();
        assertThat(retrievedByName.get(0).getName()).isEqualTo(studentName);
        assertThat(retrievedByName.get(0).getUser().getUsername()).isEqualTo(username);
        
        // When: 通过ID查询学生
        Optional<Student> retrievedById = studentRepository.findById(savedStudent.getId());
        
            // Then: 验证查询结果一致性
            assertThat(retrievedById).isPresent();
            assertThat(retrievedById.get().getId()).isEqualTo(savedStudent.getId());
            assertThat(retrievedById.get().getAge()).isEqualTo(age);
            
            // 清理数据
            studentRepository.delete(savedStudent);
            userRepository.delete(savedUser);
        }
    }

    /**
     * 属性测试: 数据更新操作的一致性
     * 验证数据更新操作在不同持久化框架下的行为一致性
     */
    @Test
    void dataUpdateConsistency() {
        // 运行多次测试以模拟属性测试
        for (int i = 0; i < 5; i++) {
            String originalUsername = generateValidUsername();
            String newUsername = generateValidUsername();
            String originalPassword = generateValidPassword();
            String newPassword = generateValidPassword();
            
            // 确保用户名和密码不同
            if (originalUsername.equals(newUsername) || originalPassword.equals(newPassword)) {
                continue;
            }
        
        // Given: 创建并保存用户
        User user = new User();
        user.setUsername(originalUsername);
        user.setPassword(originalPassword);
        user.setRole(UserRole.STUDENT);
        User savedUser = userRepository.save(user);
        
        // When: 更新用户信息
        savedUser.setUsername(newUsername);
        savedUser.setPassword(newPassword);
        User updatedUser = userRepository.save(savedUser);
        
        // Then: 验证更新成功
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getUsername()).isEqualTo(newUsername);
        assertThat(updatedUser.getPassword()).isEqualTo(newPassword);
        
        // When: 重新查询用户
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        
        // Then: 验证持久化更新的一致性
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getUsername()).isEqualTo(newUsername);
        assertThat(retrievedUser.get().getPassword()).isEqualTo(newPassword);
        
        // Then: 验证旧用户名不再存在
        Optional<User> oldUser = userRepository.findByUsername(originalUsername);
        assertThat(oldUser).isEmpty();
        
            // Then: 验证新用户名可以查询到
            Optional<User> newUser = userRepository.findByUsername(newUsername);
            assertThat(newUser).isPresent();
            assertThat(newUser.get().getId()).isEqualTo(savedUser.getId());
            
            // 清理数据
            userRepository.delete(updatedUser);
        }
    }

    // 数据生成器方法
    private String generateValidUsername() {
        return "user_" + generateRandomString(5, 15) + "_" + random.nextInt(1000);
    }

    private String generateValidPassword() {
        return "pwd_" + generateRandomString(6, 15);
    }

    private String generateValidStudentName() {
        return "Student " + generateRandomString(3, 8);
    }

    private Integer generateValidAge() {
        return 18 + random.nextInt(8); // 18-25
    }
    
    private String generateRandomString(int minLength, int maxLength) {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }
}