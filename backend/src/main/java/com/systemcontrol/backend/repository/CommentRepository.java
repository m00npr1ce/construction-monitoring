package com.systemcontrol.backend.repository;

import com.systemcontrol.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDefectIdOrderByCreatedAtAsc(Long defectId);
}