package com.sims.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 更新学生请求DTO
 */
public class UpdateStudentRequest {
    
    @Size(min = 1, max = 50, message = "学生姓名长度必须在1-50个字符之间")
    private String name;
    
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 100, message = "年龄不能超过100")
    private Integer age;
    
    @Size(max = 10, message = "性别长度不能超过10个字符")
    private String gender;
    
    private String birthDate; // Format: YYYY-MM-DD
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号码")
    private String phone;
    
    @Size(max = 500, message = "地址长度不能超过500个字符")
    private String address;
    
    @Size(max = 50, message = "家长姓名长度不能超过50个字符")
    private String parentName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的家长手机号码")
    private String parentPhone;
    
    private Long classId;
    
    @DecimalMin(value = "0.0", message = "成绩不能为负数")
    @DecimalMax(value = "100.0", message = "成绩不能超过100分")
    private BigDecimal score;
    
    private String avatar;
    
    public UpdateStudentRequest() {}
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "UpdateStudentRequest{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", parentName='" + parentName + '\'' +
                ", parentPhone='" + parentPhone + '\'' +
                ", classId=" + classId +
                ", score=" + score +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}