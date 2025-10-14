package com.systemcontrol.backend.service;

import com.systemcontrol.backend.model.DefectHistory;
import com.systemcontrol.backend.repository.DefectHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class DefectHistoryService {
    
    private final DefectHistoryRepository defectHistoryRepository;
    
    public DefectHistoryService(DefectHistoryRepository defectHistoryRepository) {
        this.defectHistoryRepository = defectHistoryRepository;
    }
    
    public DefectHistory recordChange(Long defectId, Long userId, String fieldName, String oldValue, String newValue, String action) {
        DefectHistory history = new DefectHistory();
        history.setDefectId(defectId);
        history.setUserId(userId);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setAction(action);
        return defectHistoryRepository.save(history);
    }
}