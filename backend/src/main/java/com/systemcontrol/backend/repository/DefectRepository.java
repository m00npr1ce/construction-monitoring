package com.systemcontrol.backend.repository;

import com.systemcontrol.backend.model.Defect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectRepository extends JpaRepository<Defect, Long> {
    List<Defect> findByProjectId(Long projectId);
    List<Defect> findByAssigneeId(Long assigneeId);
    List<Defect> findByStatus(String status);
}
