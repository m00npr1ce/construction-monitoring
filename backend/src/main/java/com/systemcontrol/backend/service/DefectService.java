package com.systemcontrol.backend.service;

import com.systemcontrol.backend.model.Defect;
import com.systemcontrol.backend.repository.DefectRepository;
import com.systemcontrol.backend.repository.ProjectRepository;
import com.systemcontrol.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefectService {
    private final DefectRepository defectRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DefectHistoryService defectHistoryService;

    public DefectService(DefectRepository defectRepository, ProjectRepository projectRepository, UserRepository userRepository, DefectHistoryService defectHistoryService) {
        this.defectRepository = defectRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.defectHistoryService = defectHistoryService;
    }

    private static String clean(String s) {
        if (s == null) return null;
        return org.jsoup.Jsoup.clean(s, org.jsoup.safety.Safelist.basic());
    }

    public Defect create(Defect d) {
        if (d.getProjectId() == null || !projectRepository.existsById(d.getProjectId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "project not found");
        }
        if (d.getAssigneeId() != null && !userRepository.existsById(d.getAssigneeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assignee not found");
        }
        d.setTitle(clean(d.getTitle()));
        d.setDescription(clean(d.getDescription()));
        Defect saved = defectRepository.save(d);
        // Record creation in history
        defectHistoryService.recordChange(saved.getId(), d.getAssigneeId() != null ? d.getAssigneeId() : 0L, "CREATED", null, null, "CREATED");
        return saved;
    }
    public Defect update(Long id, Defect updated) {
        Defect exist = defectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "defect not found"));
        if (updated.getProjectId() == null || !projectRepository.existsById(updated.getProjectId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "project not found");
        }
        if (updated.getAssigneeId() != null && !userRepository.existsById(updated.getAssigneeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assignee not found");
        }
        
        // Validate status transition
        if (!exist.getStatus().equals(updated.getStatus())) {
            if (!exist.getStatus().canTransitionTo(updated.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Недопустимый переход статуса. " + exist.getStatus().getAllowedTransitionsDescription());
            }
        }
        
        // Record changes in history
        if (!exist.getTitle().equals(updated.getTitle())) {
            defectHistoryService.recordChange(id, updated.getAssigneeId() != null ? updated.getAssigneeId() : 0L, "title", exist.getTitle(), updated.getTitle(), "UPDATED");
        }
        if (!exist.getStatus().equals(updated.getStatus())) {
            defectHistoryService.recordChange(id, updated.getAssigneeId() != null ? updated.getAssigneeId() : 0L, "status", exist.getStatus().toString(), updated.getStatus().toString(), "STATUS_CHANGED");
        }
        if (!exist.getPriority().equals(updated.getPriority())) {
            defectHistoryService.recordChange(id, updated.getAssigneeId() != null ? updated.getAssigneeId() : 0L, "priority", exist.getPriority().toString(), updated.getPriority().toString(), "UPDATED");
        }
        if (!java.util.Objects.equals(exist.getAssigneeId(), updated.getAssigneeId())) {
            defectHistoryService.recordChange(id, updated.getAssigneeId() != null ? updated.getAssigneeId() : 0L, "assignee", exist.getAssigneeId() != null ? exist.getAssigneeId().toString() : null, updated.getAssigneeId() != null ? updated.getAssigneeId().toString() : null, "ASSIGNED");
        }
        
        // copy mutable fields (with sanitization)
        exist.setTitle(clean(updated.getTitle()));
        exist.setDescription(clean(updated.getDescription()));
        exist.setPriority(updated.getPriority());
        exist.setStatus(updated.getStatus());
        exist.setAssigneeId(updated.getAssigneeId());
        exist.setProjectId(updated.getProjectId());
        exist.setDueDate(updated.getDueDate());
        exist.setUpdatedAt(java.time.Instant.now());
        return defectRepository.save(exist);
    }
    /**
     * Изменяет только статус дефекта с валидацией переходов
     */
    public Defect updateStatus(Long id, com.systemcontrol.backend.model.DefectStatus newStatus, Long userId) {
        Defect exist = defectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "defect not found"));
        
        // Validate status transition
        if (!exist.getStatus().canTransitionTo(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Недопустимый переход статуса. " + exist.getStatus().getAllowedTransitionsDescription());
        }
        
        // Record status change in history
        defectHistoryService.recordChange(id, userId != null ? userId : 0L, "status", 
            exist.getStatus().toString(), newStatus.toString(), "STATUS_CHANGED");
        
        // Update status
        exist.setStatus(newStatus);
        exist.setUpdatedAt(java.time.Instant.now());
        
        return defectRepository.save(exist);
    }
    
    /**
     * Переводит дефект к следующему статусу в workflow
     */
    public Defect moveToNextStatus(Long id, Long userId) {
        Defect defect = defectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "defect not found"));
        
        if (!defect.getStatus().canMoveToNext()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Дефект уже в финальном статусе: " + defect.getStatus());
        }
        
        com.systemcontrol.backend.model.DefectStatus nextStatus = defect.getStatus().getNextStatus();
        
        // Record status change in history
        defectHistoryService.recordChange(id, userId != null ? userId : 0L, "status", 
            defect.getStatus().toString(), nextStatus.toString(), "STATUS_CHANGED");
        
        // Update status
        defect.setStatus(nextStatus);
        defect.setUpdatedAt(java.time.Instant.now());
        
        return defectRepository.save(defect);
    }
    
    /**
     * Отменяет дефект
     */
    public Defect cancelDefect(Long id, Long userId) {
        Defect defect = defectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "defect not found"));
        
        if (defect.getStatus() == com.systemcontrol.backend.model.DefectStatus.CLOSED || 
            defect.getStatus() == com.systemcontrol.backend.model.DefectStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Дефект уже в финальном статусе: " + defect.getStatus());
        }
        
        // Record status change in history
        defectHistoryService.recordChange(id, userId != null ? userId : 0L, "status", 
            defect.getStatus().toString(), com.systemcontrol.backend.model.DefectStatus.CANCELLED.toString(), "CANCELLED");
        
        // Update status
        defect.setStatus(com.systemcontrol.backend.model.DefectStatus.CANCELLED);
        defect.setUpdatedAt(java.time.Instant.now());
        
        return defectRepository.save(defect);
    }
    
    /**
     * Получает разрешенные переходы для дефекта
     */
    public java.util.List<com.systemcontrol.backend.model.DefectStatus> getAllowedStatusTransitions(Long id) {
        Defect defect = defectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "defect not found"));
        return java.util.Arrays.stream(com.systemcontrol.backend.model.DefectStatus.values())
            .filter(status -> defect.getStatus().canTransitionTo(status))
            .collect(java.util.stream.Collectors.toList());
    }

    public Defect get(Long id) { return defectRepository.findById(id).orElse(null); }
    public List<Defect> listByProject(Long projectId) { return defectRepository.findByProjectId(projectId); }
    public List<Defect> listAll() { return defectRepository.findAll(); }
    public void delete(Long id) { defectRepository.deleteById(id); }
}
