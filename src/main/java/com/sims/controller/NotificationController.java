package com.sims.controller;

import com.sims.dto.NotificationResponse;
import com.sims.dto.PageResponse;
import com.sims.entity.User;
import com.sims.security.CustomUserDetailsService;
import com.sims.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 站内通知 API
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean unreadOnly) {
        User user = requireUser();
        if (user == null) {
            return unauthorized();
        }
        try {
            PageResponse<NotificationResponse> response =
                    notificationService.listMyNotifications(user.getId(), page, size, unreadOnly);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取通知列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("获取通知列表失败", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount() {
        User user = requireUser();
        if (user == null) {
            return unauthorized();
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("count", notificationService.countUnread(user.getId()));
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            logger.error("获取未读数失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("获取未读数失败", e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        User user = requireUser();
        if (user == null) {
            return unauthorized();
        }
        try {
            return ResponseEntity.ok(notificationService.markRead(user.getId(), id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("操作失败", e.getMessage()));
        } catch (Exception e) {
            logger.error("标记已读失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("标记已读失败", e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllRead() {
        User user = requireUser();
        if (user == null) {
            return unauthorized();
        }
        try {
            int updated = notificationService.markAllRead(user.getId());
            Map<String, Object> body = new HashMap<>();
            body.put("updated", updated);
            body.put("message", "已全部标为已读");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            logger.error("全部已读失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("全部已读失败", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        User user = requireUser();
        if (user == null) {
            return unauthorized();
        }
        try {
            notificationService.delete(user.getId(), id);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "已删除");
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("删除失败", e.getMessage()));
        } catch (Exception e) {
            logger.error("删除通知失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("删除通知失败", e.getMessage()));
        }
    }

    private User requireUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal principal) {
                return principal.getUser();
            }
        } catch (Exception e) {
            logger.warn("获取当前用户失败", e);
        }
        return null;
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("未认证", "请先登录"));
    }

    private Map<String, String> error(String error, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
