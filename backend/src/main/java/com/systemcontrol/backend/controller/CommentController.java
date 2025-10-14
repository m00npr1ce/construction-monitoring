package com.systemcontrol.backend.controller;

import com.systemcontrol.backend.model.Comment;
import com.systemcontrol.backend.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    private final CommentService commentService;
    
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        Long defectId = Long.valueOf(request.get("defectId").toString());
        
        // Get current user ID from security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // For now, we'll use a default user ID since we don't have user ID in JWT
        // In a real implementation, you'd extract user ID from JWT token
        Long authorId = 1L; // This should be extracted from JWT token
        
        Comment comment = commentService.createComment(content, defectId, authorId);
        return ResponseEntity.ok(comment);
    }
    
    @GetMapping("/defect/{defectId}")
    public List<Comment> getCommentsByDefect(@PathVariable Long defectId) {
        return commentService.getCommentsByDefectId(defectId);
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}




