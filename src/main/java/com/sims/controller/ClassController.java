package com.sims.controller;

import com.sims.dto.*;
import com.sims.service.ClassService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级管理控制器
 */
@RestController
@RequestMapping("/api/classes")
public class ClassController {
    
    @Autowired
    private ClassService classService;
    
    /**
     * 创建班级
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassResponse> createClass(@Valid @RequestBody CreateClassRequest request) {
        ClassResponse response = classService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 获取班级列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<ClassResponse>> getClasses() {
        List<ClassResponse> classes = classService.getClasses();
        return ResponseEntity.ok(classes);
    }
    
    /**
     * 获取班级详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassResponse> getClass(@PathVariable Long id) {
        ClassResponse response = classService.getClass(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新班级信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<ClassResponse> updateClass(@PathVariable Long id, 
                                                   @Valid @RequestBody UpdateClassRequest request) {
        ClassResponse response = classService.updateClass(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除班级
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 分配学生到班级
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Void> assignStudentToClass(@PathVariable Long id, 
                                                   @RequestParam Long studentId) {
        classService.assignStudentToClass(studentId, id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取班级学生列表
     */
    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<StudentResponse>> getClassStudents(@PathVariable Long id) {
        List<StudentResponse> students = classService.getClassStudents(id);
        return ResponseEntity.ok(students);
    }
    
    /**
     * 从班级中移除学生
     */
    @DeleteMapping("/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Void> removeStudentFromClass(@PathVariable Long studentId) {
        classService.removeStudentFromClass(studentId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取教师仪表板统计信息
     */
    @GetMapping("/teacher/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<TeacherStatsResponse> getTeacherDashboardStats() {
        TeacherStatsResponse response = classService.getTeacherDashboardStats();
        return ResponseEntity.ok(response);
    }
}