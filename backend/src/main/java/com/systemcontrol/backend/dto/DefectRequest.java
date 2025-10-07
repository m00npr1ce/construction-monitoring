package com.systemcontrol.backend.dto;

import com.systemcontrol.backend.model.DefectStatus;
import com.systemcontrol.backend.model.Priority;

import java.time.Instant;

public class DefectRequest {
    public String title;
    public String description;
    public Priority priority;
    public DefectStatus status;
    public Long assigneeId;
    public Long projectId;
    public Instant dueDate;
}
