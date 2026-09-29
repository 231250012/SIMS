package com.sims.controller;

import com.sims.dto.AssignTeacherClassRequest;
import com.sims.dto.TeacherAdminResponse;
import com.sims.service.TeacherAdminService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/teachers")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminTeacherController {

    private static final Logger logger = LoggerFactory.getLogger(AdminTeacherController.class);

    @Autowired
    private TeacherAdminService teacherAdminService;

    @GetMapping
    public ResponseEntity<?> list() {
        try {
            List<TeacherAdminResponse> teachers = teacherAdminService.listTeachers();
            return ResponseEntity.ok(teachers);
        } catch (Exception e) {
            logger.error("获取教师列表失败", e);
            return ResponseEntity.badRequest().body(error("获取教师列表失败", e.getMessage()));
        }
    }

    @PutMapping("/{id}/class")
    public ResponseEntity<?> assignClass(@PathVariable Long id,
                                         @Valid @RequestBody AssignTeacherClassRequest request) {
        try {
            return ResponseEntity.ok(teacherAdminService.assignClass(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("调整班级失败", e.getMessage()));
        } catch (Exception e) {
            logger.error("调整教师班级失败", e);
            return ResponseEntity.badRequest().body(error("调整班级失败", e.getMessage()));
        }
    }

    @PutMapping("/{id}/homeroom")
    public ResponseEntity<?> setHomeroom(@PathVariable Long id,
                                         @RequestBody Map<String, Boolean> body) {
        try {
            boolean flag = Boolean.TRUE.equals(body.get("isHomeroomTeacher"));
            return ResponseEntity.ok(teacherAdminService.updateHomeroomFlag(id, flag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("更新失败", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error("更新失败", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            teacherAdminService.deleteTeacher(id);
            Map<String, Object> ok = new HashMap<>();
            ok.put("message", "教师已删除");
            ok.put("teacherId", id);
            return ResponseEntity.ok(ok);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("删除失败", e.getMessage()));
        } catch (Exception e) {
            logger.error("删除教师失败", e);
            return ResponseEntity.badRequest().body(error("删除失败", e.getMessage()));
        }
    }

    private Map<String, String> error(String error, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message);
        return body;
    }
}
