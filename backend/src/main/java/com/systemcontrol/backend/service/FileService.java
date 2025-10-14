package com.systemcontrol.backend.service;

import com.systemcontrol.backend.model.Attachment;
import com.systemcontrol.backend.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    private final AttachmentRepository attachmentRepository;
    
    public FileService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }
    
    public Attachment saveFile(MultipartFile file, Long defectId) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Save attachment record
        Attachment attachment = new Attachment();
        attachment.setFileName(uniqueFilename);
        attachment.setOriginalFileName(originalFilename);
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setFilePath(filePath.toString());
        attachment.setDefectId(defectId);
        
        return attachmentRepository.save(attachment);
    }
    
    public List<Attachment> getAttachmentsByDefectId(Long defectId) {
        return attachmentRepository.findByDefectId(defectId);
    }
    
    public void deleteAttachment(Long attachmentId) throws IOException {
        Attachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
        if (attachment != null) {
            // Delete file from filesystem
            Files.deleteIfExists(Paths.get(attachment.getFilePath()));
            // Delete record from database
            attachmentRepository.deleteById(attachmentId);
        }
    }
    
    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId).orElse(null);
    }
}
