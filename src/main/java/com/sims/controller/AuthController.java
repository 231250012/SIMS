package com.sims.controller;

import com.sims.dto.AuthResponse;
import com.sims.dto.ClassResponse;
import com.sims.dto.LoginRequest;
import com.sims.dto.RegisterRequest;
import com.sims.entity.User;
import com.sims.security.CustomUserDetailsService;
import com.sims.service.AuthService;
import com.sims.service.ClassService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、登出和认证相关的API请求
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ClassService classService;

    @Autowired
    private com.sims.repository.UserRepository userRepository;

    /**
     * 注册页可选班级列表（公开）
     */
    @GetMapping("/classes")
    public ResponseEntity<List<ClassResponse>> listRegisterClasses() {
        return ResponseEntity.ok(classService.listPublicClassOptions());
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("=== LOGIN REQUEST RECEIVED ===");
        System.out.println("Username: " + loginRequest.getUsername());
        System.out.println("Password length: " + (loginRequest.getPassword() != null ? loginRequest.getPassword().length() : "null"));
        
        try {
            AuthResponse authResponse = authService.authenticate(loginRequest);
            System.out.println("Login successful for username: " + loginRequest.getUsername());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            System.out.println("Login failed for username: " + loginRequest.getUsername());
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "认证失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 简单测试端点
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Auth endpoint is working");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * 测试密码验证
     */
    @PostMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            System.out.println("Testing password for username: " + username);
            
            User user = authService.getCurrentUser(username);
            System.out.println("Found user: " + user.getUsername());
            System.out.println("Stored password hash: " + user.getPassword());
            
            // 手动验证密码
            org.springframework.security.crypto.password.PasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            boolean matches = encoder.matches(password, user.getPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("passwordMatches", matches);
            response.put("providedPassword", password);
            response.put("storedHashPrefix", user.getPassword().substring(0, 10));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 生成密码哈希
     */
    @PostMapping("/generate-hash")
    public ResponseEntity<?> generateHash(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            
            org.springframework.security.crypto.password.PasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            String hash = encoder.encode(password);
            
            Map<String, Object> response = new HashMap<>();
            response.put("password", password);
            response.put("hash", hash);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 临时端点：更新用户密码哈希
     */
    @PostMapping("/update-passwords")
    public ResponseEntity<?> updatePasswords() {
        try {
            org.springframework.security.crypto.password.PasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            // 更新admin密码
            User admin = authService.getCurrentUser("admin");
            admin.setPassword(encoder.encode("admin123"));
            
            // 更新teacher密码
            User teacher1 = authService.getCurrentUser("teacher1");
            teacher1.setPassword(encoder.encode("teacher123"));
            
            // 更新student密码
            User student1 = authService.getCurrentUser("student1");
            student1.setPassword(encoder.encode("student123"));
            
            // 保存到数据库
            userRepository.save(admin);
            userRepository.save(teacher1);
            userRepository.save(student1);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "密码已更新");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 测试端点 - 检查用户是否存在
     */
    @GetMapping("/test-user/{username}")
    public ResponseEntity<?> testUser(@PathVariable String username) {
        try {
            User user = authService.getCurrentUser(username);
            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("passwordLength", user.getPassword().length());
            response.put("passwordPrefix", user.getPassword().substring(0, Math.min(10, user.getPassword().length())));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "用户不存在");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("Register attempt for username: " + registerRequest.getUsername());
            AuthResponse authResponse = authService.register(registerRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            System.out.println("Register failed for username: " + registerRequest.getUsername() + ", error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "注册失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("未认证");
            }

            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            User user = userPrincipal.getUser();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            userInfo.put("isHomeroomTeacher", user.getIsHomeroomTeacher());
            userInfo.put("createTime", user.getCreateTime());
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取用户信息失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                    (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                
                authService.logout(userPrincipal.getUserId());
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "登出成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "登出失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 验证令牌
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                boolean isValid = authService.validateToken(token);
                
                Map<String, Object> response = new HashMap<>();
                response.put("valid", isValid);
                
                if (isValid) {
                    response.put("message", "令牌有效");
                    // 添加会话信息
                    Map<String, Object> sessionInfo = authService.getSessionInfo(token);
                    if (sessionInfo != null) {
                        response.put("sessionInfo", sessionInfo);
                        response.put("remainingTime", authService.getSessionRemainingTime(token));
                        response.put("expiringSoon", authService.isSessionExpiringSoon(token));
                    }
                } else {
                    response.put("message", "令牌无效或已过期");
                }
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "缺少Authorization头");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "令牌验证失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取会话信息
     */
    @GetMapping("/session")
    public ResponseEntity<?> getSessionInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (!authService.validateToken(token)) {
                    return ResponseEntity.status(401).body("会话无效或已过期");
                }
                
                Map<String, Object> sessionInfo = authService.getSessionInfo(token);
                if (sessionInfo != null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sessionInfo", sessionInfo);
                    response.put("remainingTime", authService.getSessionRemainingTime(token));
                    response.put("expiringSoon", authService.isSessionExpiringSoon(token));
                    response.put("activeSessionCount", authService.getActiveSessionCount());
                    
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(404).body("会话不存在");
                }
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "缺少Authorization头");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取会话信息失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 刷新会话
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshSession(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (!authService.validateToken(token)) {
                    return ResponseEntity.status(401).body("会话无效或已过期");
                }
                
                authService.refreshSession(token);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "会话刷新成功");
                response.put("remainingTime", authService.getSessionRemainingTime(token));
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "缺少Authorization头");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "刷新会话失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}