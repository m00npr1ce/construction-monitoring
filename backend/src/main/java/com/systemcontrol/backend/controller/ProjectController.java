package com.systemcontrol.backend.controller;

import com.systemcontrol.backend.dto.ProjectRequest;
import com.systemcontrol.backend.model.Project;
import com.systemcontrol.backend.model.User;
import com.systemcontrol.backend.repository.ProjectRepository;
import com.systemcontrol.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectController(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Project> list() {
        return projectRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProjectRequest req) {
        Project p = new Project();
        p.setName(req.name);
        p.setDescription(req.description);
        p.setStartDate(req.startDate);
        p.setEndDate(req.endDate);
        if (req.managerId != null) {
            Optional<User> u = userRepository.findById(req.managerId);
            u.ifPresent(p::setManager);
        }
        Project saved = projectRepository.save(p);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return projectRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProjectRequest req) {
        Optional<Project> op = projectRepository.findById(id);
        if (op.isEmpty()) return ResponseEntity.notFound().build();
        Project p = op.get();
        p.setName(req.name);
        p.setDescription(req.description);
        p.setStartDate(req.startDate);
        p.setEndDate(req.endDate);
        if (req.managerId != null) userRepository.findById(req.managerId).ifPresent(p::setManager);
        projectRepository.save(p);
        return ResponseEntity.ok(p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!projectRepository.existsById(id)) return ResponseEntity.notFound().build();
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
