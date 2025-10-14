package com.systemcontrol.backend;

import com.systemcontrol.backend.model.Attachment;
import com.systemcontrol.backend.repository.AttachmentRepository;
import com.systemcontrol.backend.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class FileServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;
    
    @InjectMocks
    private FileService fileService;
    
    private Attachment testAttachment;
    private MultipartFile testFile;
    
    @BeforeEach
    void setUp() {
        // Ensure FileService uses a temp upload dir in unit tests (no Spring context here)
        try {
            java.lang.reflect.Field f = FileService.class.getDeclaredField("uploadDir");
            f.setAccessible(true);
            f.set(fileService, "uploads-test");
        } catch (Exception ignored) {}
        testAttachment = new Attachment();
        testAttachment.setId(1L);
        testAttachment.setFileName("test-file.jpg");
        testAttachment.setOriginalFileName("original-file.jpg");
        testAttachment.setContentType("image/jpeg");
        testAttachment.setFileSize(1024L);
        testAttachment.setFilePath("/uploads/test-file.jpg");
        testAttachment.setDefectId(1L);
        
        testFile = new MockMultipartFile(
                "file",
                "test-file.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
    }
    
    @Test
    void saveFile_WithValidFile_ShouldReturnAttachment() throws IOException {
        // Given
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> {
            Attachment a = invocation.getArgument(0, Attachment.class);
            a.setId(1L);
            return a;
        });
        
        // When
        Attachment result = fileService.saveFile(testFile, 1L);
        
        // Then
        assertNotNull(result);
        assertEquals("test-file.jpg", result.getOriginalFileName());
        assertEquals(1L, result.getDefectId());
        verify(attachmentRepository).save(any(Attachment.class));
    }
    
    @Test
    void getAttachmentsByDefectId_WithValidId_ShouldReturnAttachments() {
        // Given
        List<Attachment> attachments = Arrays.asList(testAttachment);
        when(attachmentRepository.findByDefectId(1L)).thenReturn(attachments);
        
        // When
        List<Attachment> result = fileService.getAttachmentsByDefectId(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAttachment.getFileName(), result.get(0).getFileName());
        verify(attachmentRepository).findByDefectId(1L);
    }
    
    @Test
    void getAttachment_WithValidId_ShouldReturnAttachment() {
        // Given
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));
        
        // When
        Attachment result = fileService.getAttachment(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(testAttachment.getId(), result.getId());
        verify(attachmentRepository).findById(1L);
    }
    
    @Test
    void getAttachment_WithInvalidId_ShouldReturnNull() {
        // Given
        when(attachmentRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When
        Attachment result = fileService.getAttachment(1L);
        
        // Then
        assertNull(result);
        verify(attachmentRepository).findById(1L);
    }
    
    @Test
    void deleteAttachment_WithValidId_ShouldDeleteAttachment() throws IOException {
        // Given
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));
        
        // When
        fileService.deleteAttachment(1L);
        
        // Then
        verify(attachmentRepository).deleteById(1L);
    }
    
    @Test
    void deleteAttachment_WithInvalidId_ShouldNotThrowException() throws IOException {
        // Given
        when(attachmentRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertDoesNotThrow(() -> fileService.deleteAttachment(1L));
        verify(attachmentRepository, never()).deleteById(anyLong());
    }
}




