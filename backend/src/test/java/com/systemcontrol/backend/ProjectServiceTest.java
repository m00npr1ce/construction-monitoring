package com.systemcontrol.backend;

import com.systemcontrol.backend.model.Project;
import com.systemcontrol.backend.repository.ProjectRepository;
import com.systemcontrol.backend.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    
    @InjectMocks
    private ProjectService projectService;
    
    private Project testProject;
    
    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStartDate(LocalDate.now());
        testProject.setEndDate(LocalDate.now().plusDays(30));
    }
    
    @Test
    void createProject_WithValidData_ShouldReturnSavedProject() {
        // Given
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        
        // When
        Project result = projectService.create(testProject);
        
        // Then
        assertNotNull(result);
        assertEquals(testProject.getName(), result.getName());
        assertEquals(testProject.getDescription(), result.getDescription());
        verify(projectRepository).save(testProject);
    }
    
    @Test
    void getProject_WithValidId_ShouldReturnProject() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // When
        Project result = projectService.get(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(testProject.getId(), result.getId());
        assertEquals(testProject.getName(), result.getName());
    }
    
    @Test
    void getProject_WithInvalidId_ShouldReturnNull() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When
        Project result = projectService.get(1L);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void listProjects_ShouldReturnAllProjects() {
        // Given
        List<Project> projects = Arrays.asList(testProject);
        when(projectRepository.findAll()).thenReturn(projects);
        
        // When
        List<Project> result = projectService.list();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProject.getName(), result.get(0).getName());
        verify(projectRepository).findAll();
    }
    
    @Test
    void updateProject_WithValidData_ShouldReturnUpdatedProject() {
        // Given
        Project existingProject = new Project();
        existingProject.setId(1L);
        existingProject.setName("Old Name");
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existingProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        
        // When
        Project result = projectService.update(1L, testProject);
        
        // Then
        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
    }
    
    @Test
    void deleteProject_WithValidId_ShouldDeleteProject() {
        // Given
        when(projectRepository.existsById(1L)).thenReturn(true);
        
        // When
        projectService.delete(1L);
        
        // Then
        verify(projectRepository).deleteById(1L);
    }
}




