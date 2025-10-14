CREATE TABLE defect_history (
    id BIGSERIAL PRIMARY KEY,
    defect_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_defect_history_defect_id ON defect_history(defect_id);
CREATE INDEX idx_defect_history_user_id ON defect_history(user_id);
