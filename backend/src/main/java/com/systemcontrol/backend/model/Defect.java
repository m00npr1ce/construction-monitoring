package com.systemcontrol.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "defects")
public class Defect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(32)")
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(32)")
    private DefectStatus status = DefectStatus.NEW;

    private Long assigneeId;
    private Long projectId;
    private Instant dueDate;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public Defect() {}

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public DefectStatus getStatus() { return status; }
    public void setStatus(DefectStatus status) { this.status = status; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
