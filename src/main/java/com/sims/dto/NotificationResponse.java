package com.sims.dto;

/**
 * 站内通知响应
 */
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String content;
    private Long relatedStudentId;
    private String relatedSubject;
    private String payload;
    private Boolean isRead;
    private String createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getRelatedStudentId() {
        return relatedStudentId;
    }

    public void setRelatedStudentId(Long relatedStudentId) {
        this.relatedStudentId = relatedStudentId;
    }

    public String getRelatedSubject() {
        return relatedSubject;
    }

    public void setRelatedSubject(String relatedSubject) {
        this.relatedSubject = relatedSubject;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
