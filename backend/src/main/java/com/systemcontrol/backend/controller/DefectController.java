package com.systemcontrol.backend.controller;

import com.systemcontrol.backend.dto.DefectRequest;
import com.systemcontrol.backend.model.Defect;
import com.systemcontrol.backend.model.Priority;
import com.systemcontrol.backend.model.DefectStatus;
import com.systemcontrol.backend.service.DefectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/defects")
public class DefectController {
    private final DefectService defectService;

    public DefectController(DefectService defectService) { this.defectService = defectService; }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody DefectRequest req) {
        Defect d = new Defect();
        d.setTitle(req.title);
        d.setDescription(req.description);
        // apply sensible defaults when client omits priority/status
        d.setPriority(req.priority == null ? Priority.MEDIUM : req.priority);
        d.setStatus(req.status == null ? DefectStatus.NEW : req.status);
        d.setAssigneeId(req.assigneeId);
        d.setProjectId(req.projectId);
        d.setDueDate(req.dueDate);
        Defect created = defectService.create(d);
        return ResponseEntity.created(URI.create("/api/defects/" + created.getId())).body(created);
    }

    @GetMapping
    public List<Defect> list(@RequestParam(required = false) Long projectId) {
        if (projectId != null) return defectService.listByProject(projectId);
        return defectService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Defect d = defectService.get(id);
        if (d == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(d);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) { defectService.delete(id); return ResponseEntity.noContent().build(); }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody com.systemcontrol.backend.model.Defect req) {
        com.systemcontrol.backend.model.Defect updated = defectService.update(id, req);
        return ResponseEntity.ok(updated);
    }
}
