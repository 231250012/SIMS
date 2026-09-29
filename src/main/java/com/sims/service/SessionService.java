package com.sims.service;

import com.sims.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理服务
 * 使用 Redis 存储会话，TTL 30 分钟
 */
@Service
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSION_PREFIX = "user_session:";
    private static final long SESSION_TIMEOUT_MINUTES = 30;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 创建用户会话，写入 Redis 并设置过期时间
     */
    public void createSession(String sessionId, User user) {
        String sessionKey = SESSION_PREFIX + sessionId;
        String userSessionKey = USER_SESSION_PREFIX + user.getId();

        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("userId", String.valueOf(user.getId()));
        sessionData.put("username", user.getUsername());
        sessionData.put("role", user.getRole().name());
        sessionData.put("loginTime", LocalDateTime.now().toString());
        sessionData.put("lastAccessTime", LocalDateTime.now().toString());

        stringRedisTemplate.opsForHash().putAll(sessionKey, sessionData);
        stringRedisTemplate.expire(sessionKey, Duration.ofMinutes(SESSION_TIMEOUT_MINUTES));

        stringRedisTemplate.opsForValue().set(
                userSessionKey,
                sessionId,
                Duration.ofMinutes(SESSION_TIMEOUT_MINUTES)
        );

        logger.debug("Redis 会话已创建: sessionId={}, userId={}", sessionId, user.getId());
    }

    /**
     * 获取会话信息；存在则刷新 TTL（滑动过期）
     */
    public Map<String, Object> getSession(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;

        Map<Object, Object> raw = stringRedisTemplate.opsForHash().entries(sessionKey);
        if (raw == null || raw.isEmpty()) {
            return null;
        }

        refreshTtl(sessionId, raw.get("userId"));

        Map<String, Object> session = new HashMap<>();
        raw.forEach((k, v) -> session.put(String.valueOf(k), v));
        session.put("lastAccessTime", LocalDateTime.now().toString());
        stringRedisTemplate.opsForHash().put(sessionKey, "lastAccessTime", LocalDateTime.now().toString());
        return session;
    }

    /**
     * 验证会话是否有效
     */
    public boolean isSessionValid(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        Boolean exists = stringRedisTemplate.hasKey(SESSION_PREFIX + sessionId);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 刷新会话过期时间
     */
    public void refreshSession(String sessionId) {
        if (!isSessionValid(sessionId)) {
            return;
        }
        Object userId = stringRedisTemplate.opsForHash().get(SESSION_PREFIX + sessionId, "userId");
        refreshTtl(sessionId, userId);
    }

    /**
     * 删除会话（用户登出）
     */
    public void deleteSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        Object userId = stringRedisTemplate.opsForHash().get(sessionKey, "userId");
        if (userId != null) {
            stringRedisTemplate.delete(USER_SESSION_PREFIX + userId);
        }
        stringRedisTemplate.delete(sessionKey);
        logger.debug("Redis 会话已删除: sessionId={}", sessionId);
    }

    /**
     * 获取用户的当前会话ID
     */
    public String getUserCurrentSession(Long userId) {
        return stringRedisTemplate.opsForValue().get(USER_SESSION_PREFIX + userId);
    }

    /**
     * 强制用户下线
     */
    public void forceLogout(Long userId) {
        String currentSessionId = getUserCurrentSession(userId);
        if (currentSessionId != null) {
            deleteSession(currentSessionId);
        }
    }

    /**
     * 获取会话剩余时间（秒）
     */
    public Long getSessionRemainingTime(String sessionId) {
        if (!isSessionValid(sessionId)) {
            return 0L;
        }
        Long ttl = stringRedisTemplate.getExpire(SESSION_PREFIX + sessionId, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0L;
    }

    /**
     * 检查会话是否即将过期（剩余时间少于5分钟）
     */
    public boolean isSessionExpiringSoon(String sessionId) {
        Long remainingTime = getSessionRemainingTime(sessionId);
        return remainingTime != null && remainingTime > 0 && remainingTime < 300;
    }

    /**
     * 获取活跃会话数量（扫描 session:* 键，仅用于统计）
     */
    public long getActiveSessionCount() {
        var keys = stringRedisTemplate.keys(SESSION_PREFIX + "*");
        return keys != null ? keys.size() : 0L;
    }

    private void refreshTtl(String sessionId, Object userId) {
        stringRedisTemplate.expire(SESSION_PREFIX + sessionId, Duration.ofMinutes(SESSION_TIMEOUT_MINUTES));
        if (userId != null) {
            stringRedisTemplate.expire(
                    USER_SESSION_PREFIX + userId,
                    Duration.ofMinutes(SESSION_TIMEOUT_MINUTES)
            );
        }
    }
}
