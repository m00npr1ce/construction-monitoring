package com.systemcontrol.backend.dto;

import com.systemcontrol.backend.model.DefectStatus;
import com.systemcontrol.backend.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class DefectRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    public String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    public String description;
    
    public Priority priority;
    public DefectStatus status;
    public Long assigneeId;
    
    @NotNull(message = "Project ID is required")
    public Long projectId;
    
    public Instant dueDate;
}
