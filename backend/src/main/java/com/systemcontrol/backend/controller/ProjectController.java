package com.systemcontrol.backend.controller;

import com.systemcontrol.backend.dto.ProjectRequest;
import com.systemcontrol.backend.model.Project;
import com.systemcontrol.backend.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> list() { return projectService.list(); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProjectRequest req) {
        if (req.getName() == null || req.getName().isBlank()) return ResponseEntity.badRequest().body("name required");
        Project p = new Project();
        p.setName(req.getName()); p.setDescription(req.getDescription()); p.setStartDate(req.getStartDate()); p.setEndDate(req.getEndDate());
        Project created = projectService.create(p);
        return ResponseEntity.created(URI.create("/api/projects/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Project p = projectService.get(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProjectRequest req) {
        Project exist = projectService.get(id);
        if (exist == null) return ResponseEntity.notFound().build();
        exist.setName(req.getName()); exist.setDescription(req.getDescription()); exist.setStartDate(req.getStartDate()); exist.setEndDate(req.getEndDate());
        Project updated = projectService.update(id, exist);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
