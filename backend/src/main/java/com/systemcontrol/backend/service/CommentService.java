package com.systemcontrol.backend.service;

import com.systemcontrol.backend.model.Comment;
import com.systemcontrol.backend.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

@Service
public class CommentService {
    
    private final CommentRepository commentRepository;
    
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    
    public Comment createComment(String content, Long defectId, Long authorId) {
        String cleaned = content == null ? "" : Jsoup.clean(content, Safelist.basic());
        Comment comment = new Comment();
        comment.setContent(cleaned);
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
