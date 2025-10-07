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

    public DefectService(DefectRepository defectRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.defectRepository = defectRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Defect create(Defect d) {
        if (d.getProjectId() == null || !projectRepository.existsById(d.getProjectId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "project not found");
        }
        if (d.getAssigneeId() != null && !userRepository.existsById(d.getAssigneeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assignee not found");
        }
        return defectRepository.save(d);
    }
    public Defect update(Long id, Defect updated) {
        Defect exist = defectRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "defect not found"));
        if (updated.getProjectId() == null || !projectRepository.existsById(updated.getProjectId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "project not found");
        }
        if (updated.getAssigneeId() != null && !userRepository.existsById(updated.getAssigneeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assignee not found");
        }
        // copy mutable fields
        exist.setTitle(updated.getTitle());
        exist.setDescription(updated.getDescription());
        exist.setPriority(updated.getPriority());
        exist.setStatus(updated.getStatus());
        exist.setAssigneeId(updated.getAssigneeId());
        exist.setProjectId(updated.getProjectId());
        exist.setDueDate(updated.getDueDate());
        exist.setUpdatedAt(java.time.Instant.now());
        return defectRepository.save(exist);
    }
    public Defect get(Long id) { return defectRepository.findById(id).orElse(null); }
    public List<Defect> listByProject(Long projectId) { return defectRepository.findByProjectId(projectId); }
    public List<Defect> listAll() { return defectRepository.findAll(); }
    public void delete(Long id) { defectRepository.deleteById(id); }
}
