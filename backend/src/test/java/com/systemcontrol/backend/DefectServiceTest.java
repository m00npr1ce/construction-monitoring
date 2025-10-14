package com.systemcontrol.backend;

import com.systemcontrol.backend.model.Defect;
import com.systemcontrol.backend.model.DefectStatus;
import com.systemcontrol.backend.model.Priority;
import com.systemcontrol.backend.repository.DefectRepository;
import com.systemcontrol.backend.repository.ProjectRepository;
import com.systemcontrol.backend.repository.UserRepository;
import com.systemcontrol.backend.service.DefectHistoryService;
import com.systemcontrol.backend.service.DefectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefectServiceTest {

    @Mock
    private DefectRepository defectRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DefectHistoryService defectHistoryService;
    
    @InjectMocks
    private DefectService defectService;
    
    private Defect testDefect;
    
    @BeforeEach
    void setUp() {
        testDefect = new Defect();
        testDefect.setId(1L);
        testDefect.setTitle("Test Defect");
        testDefect.setDescription("Test Description");
        testDefect.setPriority(Priority.HIGH);
        testDefect.setStatus(DefectStatus.NEW);
        testDefect.setProjectId(1L);
        testDefect.setAssigneeId(1L);
        testDefect.setDueDate(Instant.now().plusSeconds(86400));
    }
    
    @Test
    void createDefect_WithValidData_ShouldReturnSavedDefect() {
        // Given
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(defectRepository.save(any(Defect.class))).thenReturn(testDefect);
        
        // When
        Defect result = defectService.create(testDefect);
        
        // Then
        assertNotNull(result);
        assertEquals(testDefect.getTitle(), result.getTitle());
        verify(defectRepository).save(testDefect);
        verify(defectHistoryService).recordChange(anyLong(), anyLong(), anyString(), any(), any(), anyString());
    }
    
    @Test
    void createDefect_WithInvalidProject_ShouldThrowException() {
        // Given
        when(projectRepository.existsById(1L)).thenReturn(false);
        
        // When & Then
        assertThrows(ResponseStatusException.class, () -> defectService.create(testDefect));
        verify(defectRepository, never()).save(any(Defect.class));
    }
    
    @Test
    void createDefect_WithInvalidAssignee_ShouldThrowException() {
        // Given
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(false);
        
        // When & Then
        assertThrows(ResponseStatusException.class, () -> defectService.create(testDefect));
        verify(defectRepository, never()).save(any(Defect.class));
    }
    
    @Test
    void updateDefect_WithValidData_ShouldReturnUpdatedDefect() {
        // Given
        Defect existingDefect = new Defect();
        existingDefect.setId(1L);
        existingDefect.setTitle("Old Title");
        existingDefect.setStatus(DefectStatus.NEW);
        existingDefect.setPriority(Priority.LOW);
        
        when(defectRepository.findById(1L)).thenReturn(Optional.of(existingDefect));
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(defectRepository.save(any(Defect.class))).thenReturn(testDefect);
        
        // When
        Defect result = defectService.update(1L, testDefect);
        
        // Then
        assertNotNull(result);
        verify(defectRepository).save(any(Defect.class));
        verify(defectHistoryService, atLeastOnce()).recordChange(anyLong(), anyLong(), anyString(), any(), any(), anyString());
    }
    
    @Test
    void updateDefect_WithNonExistentDefect_ShouldThrowException() {
        // Given
        when(defectRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResponseStatusException.class, () -> defectService.update(1L, testDefect));
        verify(defectRepository, never()).save(any(Defect.class));
    }
    
    @Test
    void getDefect_WithValidId_ShouldReturnDefect() {
        // Given
        when(defectRepository.findById(1L)).thenReturn(Optional.of(testDefect));
        
        // When
        Defect result = defectService.get(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(testDefect.getId(), result.getId());
    }
    
    @Test
    void getDefect_WithInvalidId_ShouldReturnNull() {
        // Given
        when(defectRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When
        Defect result = defectService.get(1L);
        
        // Then
        assertNull(result);
    }
}




