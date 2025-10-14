package com.systemcontrol.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "defect_history")
public class DefectHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long defectId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String fieldName;

    @Column(columnDefinition = "text")
    private String oldValue;

    @Column(columnDefinition = "text")
    private String newValue;

    @Column(nullable = false)
    private String action; // CREATED, UPDATED, STATUS_CHANGED, ASSIGNED, etc.

    private Instant createdAt = Instant.now();

    public DefectHistory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDefectId() { return defectId; }
    public void setDefectId(Long defectId) { this.defectId = defectId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
