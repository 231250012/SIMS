package com.sims.dto;

import com.sims.entity.Student;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生响应DTO
 */
public class StudentResponse {
    
    private Long id;
    private String name;
    private String studentId;
    private Integer age;
    private String gender;
    private String birthDate;
    private String phone;
    private String address;
    private String parentName;
    private String parentPhone;
    private String enrollmentDate;
    private String status;
    private Long classId;
    private String className;
    private String teacherName;
    private BigDecimal score;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public StudentResponse() {}
    
    public StudentResponse(Student student) {
        this.id = student.getId();
        this.name = student.getName();
        this.studentId = student.getStudentId();
        this.age = student.getAge();
        this.gender = student.getGender();
        this.birthDate = student.getBirthDate() != null ? student.getBirthDate().toString() : null;
        this.phone = student.getPhone();
        this.address = student.getAddress();
        this.parentName = student.getParentName();
        this.parentPhone = student.getParentPhone();
        this.enrollmentDate = student.getEnrollmentDate() != null ? student.getEnrollmentDate().toString() : null;
        this.status = student.getStatus();
        this.classId = student.getStudentClass() != null ? student.getStudentClass().getId() : null;
        this.className = student.getClassName();
        this.teacherName = student.getTeacherName();
        this.score = student.getScore();
        this.avatar = student.getAvatar();
        this.createTime = student.getCreateTime();
        this.updateTime = student.getUpdateTime();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public Long getClassId() {
        return classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getTeacherName() {
        return teacherName;
    }
    
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getParentName() {
        return parentName;
    }
    
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    
    public String getParentPhone() {
        return parentPhone;
    }
    
    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }
    
    public String getEnrollmentDate() {
        return enrollmentDate;
    }
    
    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    @Override
    public String toString() {
        return "StudentResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", studentId='" + studentId + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", className='" + className + '\'' +
                ", score=" + score +
                ", createTime=" + createTime +
                '}';
    }
}