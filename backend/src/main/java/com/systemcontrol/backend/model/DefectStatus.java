package com.systemcontrol.backend.model;

import java.util.Set;

public enum DefectStatus {
    NEW,
    IN_PROGRESS,
    IN_REVIEW,
    CLOSED,
    CANCELLED;
    
    // Определяем разрешенные переходы для каждого статуса
    private static final Set<DefectStatus> NEW_ALLOWED = Set.of(IN_PROGRESS, CANCELLED);
    private static final Set<DefectStatus> IN_PROGRESS_ALLOWED = Set.of(IN_REVIEW, CANCELLED);
    private static final Set<DefectStatus> IN_REVIEW_ALLOWED = Set.of(CLOSED, IN_PROGRESS);
    private static final Set<DefectStatus> CLOSED_ALLOWED = Set.of(IN_REVIEW); // Можно вернуть на проверку
    private static final Set<DefectStatus> CANCELLED_ALLOWED = Set.of(NEW); // Можно вернуть в новое
    
    /**
     * Проверяет, разрешен ли переход от текущего статуса к новому
     */
    public boolean canTransitionTo(DefectStatus newStatus) {
        if (newStatus == null) return false;
        if (this == newStatus) return true; // Остаться в том же статусе можно
        
        return switch (this) {
            case NEW -> NEW_ALLOWED.contains(newStatus);
            case IN_PROGRESS -> IN_PROGRESS_ALLOWED.contains(newStatus);
            case IN_REVIEW -> IN_REVIEW_ALLOWED.contains(newStatus);
            case CLOSED -> CLOSED_ALLOWED.contains(newStatus);
            case CANCELLED -> CANCELLED_ALLOWED.contains(newStatus);
        };
    }
    
    /**
     * Возвращает описание разрешенных переходов для текущего статуса
     */
    public String getAllowedTransitionsDescription() {
        return switch (this) {
            case NEW -> "Можно перевести в: В работе, Отменена";
            case IN_PROGRESS -> "Можно перевести в: На проверке, Отменена";
            case IN_REVIEW -> "Можно перевести в: Закрыта, В работе";
            case CLOSED -> "Можно перевести в: На проверке";
            case CANCELLED -> "Можно перевести в: Новая";
        };
    }
}
