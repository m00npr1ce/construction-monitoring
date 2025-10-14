package com.systemcontrol.backend.controller;

import com.systemcontrol.backend.model.Attachment;
import com.systemcontrol.backend.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {
    
    private final FileService fileService;
    
    public AttachmentController(FileService fileService) {
        this.fileService = fileService;
    }
    
    @PostMapping("/upload/{defectId}")
    public ResponseEntity<?> uploadFile(@PathVariable Long defectId, @RequestParam("file") MultipartFile file) {
        try {
            Attachment attachment = fileService.saveFile(file, defectId);
            return ResponseEntity.ok(attachment);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
        }
    }
    
    @GetMapping("/defect/{defectId}")
    public List<Attachment> getAttachmentsByDefect(@PathVariable Long defectId) {
        return fileService.getAttachmentsByDefectId(defectId);
    }
    
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long attachmentId) {
        try {
            Attachment attachment = fileService.getAttachment(attachmentId);
            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            fileService.deleteAttachment(attachmentId);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to delete file: " + e.getMessage());
        }
    }
}




