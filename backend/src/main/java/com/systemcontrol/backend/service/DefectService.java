package com.systemcontrol.backend.service;

import com.systemcontrol.backend.model.Defect;
import com.systemcontrol.backend.repository.DefectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefectService {
    private final DefectRepository defectRepository;

    public DefectService(DefectRepository defectRepository) {
        this.defectRepository = defectRepository;
    }

    public Defect create(Defect d) { return defectRepository.save(d); }
    public Defect get(Long id) { return defectRepository.findById(id).orElse(null); }
    public List<Defect> listByProject(Long projectId) { return defectRepository.findByProjectId(projectId); }
    public List<Defect> listAll() { return defectRepository.findAll(); }
    public void delete(Long id) { defectRepository.deleteById(id); }
}
