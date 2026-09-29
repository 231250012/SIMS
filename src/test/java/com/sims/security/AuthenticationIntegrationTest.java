package com.sims.security;


import com.sims.entity.User;
import com.sims.entity.UserRole;
import com.sims.repository.UserRepository;
import com.sims.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 认证系统集成测试
 */
@SpringBootTest
@ActiveProfiles("test")

@Transactional
public class AuthenticationIntegrationTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(UserRole.STUDENT);
        testUser = userRepository.save(testUser);
    }

    @Test
    void shouldLoadUserByUsername() {
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void shouldGenerateValidJwtToken() {
        // Given
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testuser");
        assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();
    }

    @Test
    void shouldGenerateTokenWithRole() {
        // When
        String token = jwtUtil.generateTokenWithRole("testuser", "STUDENT");

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testuser");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("STUDENT");
    }

    @Test
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // When & Then
        assertThat(jwtUtil.validateToken(invalidToken, userDetails)).isFalse();
    }

    @Test
    void shouldEncodePasswordCorrectly() {
        // Given
        String rawPassword = "password123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrongpassword", encodedPassword)).isFalse();
    }
}