package com.sims.service;

import com.sims.dto.AuthResponse;
import com.sims.dto.LoginRequest;
import com.sims.dto.RegisterRequest;
import com.sims.entity.User;
import com.sims.entity.Student;
import com.sims.entity.UserRole;
import com.sims.repository.ClassRepository;
import com.sims.repository.UserRepository;
import com.sims.repository.StudentRepository;
import com.sims.security.CustomUserDetailsService;
import com.sims.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

/**
 * 认证服务类
 * 处理用户登录、注册和会话管理
 */
@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassRepository classRepository;

    /**
     * 用户登录认证
     * 需求 1.1: 验证凭据并生成JWT令牌
     * 需求 1.3: 将会话信息存储到Redis中并设置30分钟过期时间
     */
    public AuthResponse authenticate(LoginRequest loginRequest) {
        try {
            // 验证用户凭据
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // 获取用户详情
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) userDetails;
            
            User user = userPrincipal.getUser();

            // 生成JWT令牌
            String token = jwtUtil.generateTokenWithRole(user.getUsername(), user.getRole().name());

            // 使用SessionService创建会话（需求 1.3）
            sessionService.createSession(token, user);

            return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole(),
                    user.getIsHomeroomTeacher());

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    /**
     * 获取当前用户信息
     */
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
    }

    /**
     * 用户登出
     */
    public void logout(String sessionId) {
        sessionService.deleteSession(sessionId);
    }

    /**
     * 用户登出（通过用户ID）
     */
    public void logout(Long userId) {
        sessionService.forceLogout(userId);
    }

    /**
     * 验证会话是否有效
     * 需求 1.4: 验证JWT令牌的有效性
     */
    public boolean isSessionValid(String sessionId) {
        return sessionService.isSessionValid(sessionId);
    }

    /**
     * 获取会话信息
     */
    public Map<String, Object> getSessionInfo(String sessionId) {
        return sessionService.getSession(sessionId);
    }

    /**
     * 刷新会话过期时间
     */
    public void refreshSession(String sessionId) {
        sessionService.refreshSession(sessionId);
    }

    /**
     * 验证JWT令牌
     * 需求 1.4: 验证JWT令牌的有效性
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            boolean isTokenValid = jwtUtil.validateToken(token, userDetails);
            
            // 同时验证会话是否存在于Redis中
            boolean isSessionValid = sessionService.isSessionValid(token);
            
            return isTokenValid && isSessionValid;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查会话是否即将过期
     */
    public boolean isSessionExpiringSoon(String sessionId) {
        return sessionService.isSessionExpiringSoon(sessionId);
    }

    /**
     * 获取会话剩余时间
     */
    public Long getSessionRemainingTime(String sessionId) {
        return sessionService.getSessionRemainingTime(sessionId);
    }

    /**
     * 获取活跃会话数量（用于统计）
     */
    public long getActiveSessionCount() {
        return sessionService.getActiveSessionCount();
    }

    /**
     * 用户注册。学生选择班级后写入 class_id，自动归属该班负责教师管理。
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("密码和确认密码不匹配");
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        if (registerRequest.getRole() == UserRole.STUDENT) {
            if (registerRequest.getStudentName() == null || registerRequest.getStudentName().trim().isEmpty()) {
                throw new RuntimeException("学生用户必须填写真实姓名");
            }
            if (registerRequest.getAge() == null) {
                throw new RuntimeException("学生用户必须填写年龄");
            }
            if (registerRequest.getClassId() == null) {
                throw new RuntimeException("学生用户必须选择所属班级");
            }
        }

        if (registerRequest.getRole() == UserRole.TEACHER) {
            if (registerRequest.getClassId() == null) {
                throw new RuntimeException("教师用户必须选择管理班级");
            }
        }

        // 禁止公开自助注册管理员
        if (registerRequest.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("不允许通过公开注册创建管理员账号");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());
        if (registerRequest.getRole() == UserRole.TEACHER) {
            user.setIsHomeroomTeacher(Boolean.TRUE.equals(registerRequest.getIsHomeroomTeacher()));
        } else {
            user.setIsHomeroomTeacher(false);
        }
        user = userRepository.save(user);

        if (registerRequest.getRole() == UserRole.STUDENT) {
            com.sims.entity.Class clazz = classRepository.findById(registerRequest.getClassId())
                    .orElseThrow(() -> new RuntimeException("所选班级不存在"));

            Student student = new Student();
            student.setName(registerRequest.getStudentName().trim());
            student.setAge(registerRequest.getAge());
            student.setScore(java.math.BigDecimal.ZERO);
            student.setUser(user);
            student.setStudentClass(clazz);
            student.setStatus("ACTIVE");
            student.setEnrollmentDate(LocalDate.now());
            student = studentRepository.save(student);

            // 学号：年份 + 班级ID + 学生ID
            student.setStudentId(String.format("%d%02d%04d",
                    LocalDate.now().getYear(),
                    clazz.getId(),
                    student.getId()));
            studentRepository.save(student);
        }

        if (registerRequest.getRole() == UserRole.TEACHER) {
            com.sims.entity.Class clazz = classRepository.findById(registerRequest.getClassId())
                    .orElseThrow(() -> new RuntimeException("所选班级不存在"));
            // 若该班已有其他教师，注册时接手管理（与管理员调班一致）
            if (clazz.getTeacher() != null && !clazz.getTeacher().getId().equals(user.getId())) {
                // 解除原教师对该班的绑定即可，班级一对一教师
            }
            clazz.setTeacher(user);
            classRepository.save(clazz);
        }

        String token = jwtUtil.generateTokenWithRole(user.getUsername(), user.getRole().name());
        sessionService.createSession(token, user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole(),
                user.getIsHomeroomTeacher());
    }
}