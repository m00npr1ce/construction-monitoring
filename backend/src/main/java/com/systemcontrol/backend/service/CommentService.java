package com.systemcontrol.backend.service;

import com.systemcontrol.backend.model.Comment;
import com.systemcontrol.backend.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    
    private final CommentRepository commentRepository;
    
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    
    public Comment createComment(String content, Long defectId, Long authorId) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setDefectId(defectId);
        comment.setAuthorId(authorId);
        return commentRepository.save(comment);
    }
    
    public List<Comment> getCommentsByDefectId(Long defectId) {
        return commentRepository.findByDefectIdOrderByCreatedAtAsc(defectId);
    }
    
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
