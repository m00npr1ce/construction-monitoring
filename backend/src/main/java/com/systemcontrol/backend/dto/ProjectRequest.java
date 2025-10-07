package com.systemcontrol.backend.dto;

import java.time.LocalDate;

public class ProjectRequest {
    public String name;
    public String description;
    public LocalDate startDate;
    public LocalDate endDate;
    public Long managerId;
}
