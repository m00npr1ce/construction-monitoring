package com.systemcontrol.backend.repository;

import com.systemcontrol.backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
