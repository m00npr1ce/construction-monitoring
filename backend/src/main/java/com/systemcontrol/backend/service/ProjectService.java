package com.systemcontrol.backend.service;

import com.systemcontrol.backend.model.Project;
import com.systemcontrol.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> list() { return projectRepository.findAll(); }
    public Project create(Project p) { return projectRepository.save(p); }
    public Project get(Long id) { return projectRepository.findById(id).orElse(null); }
    public Project update(Long id, Project p) { p.setId(id); return projectRepository.save(p); }
    public void delete(Long id) { projectRepository.deleteById(id); }
}
