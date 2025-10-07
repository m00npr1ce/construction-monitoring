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
    public Defect get(Long id) { return defectRepository.findById(id).orElse(null); }
    public List<Defect> listByProject(Long projectId) { return defectRepository.findByProjectId(projectId); }
    public List<Defect> listAll() { return defectRepository.findAll(); }
    public void delete(Long id) { defectRepository.deleteById(id); }
}
