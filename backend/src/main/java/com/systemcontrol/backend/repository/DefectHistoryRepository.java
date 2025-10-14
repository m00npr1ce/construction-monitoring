package com.systemcontrol.backend.repository;

import com.systemcontrol.backend.model.DefectHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefectHistoryRepository extends JpaRepository<DefectHistory, Long> {
    List<DefectHistory> findByDefectIdOrderByCreatedAtAsc(Long defectId);
}