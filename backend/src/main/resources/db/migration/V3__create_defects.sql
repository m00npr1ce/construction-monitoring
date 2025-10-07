CREATE TYPE defect_status AS ENUM ('NEW','IN_PROGRESS','IN_REVIEW','CLOSED','CANCELLED');
CREATE TYPE defect_priority AS ENUM ('LOW','MEDIUM','HIGH','CRITICAL');

CREATE TABLE defects (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    priority defect_priority NOT NULL DEFAULT 'MEDIUM',
    status defect_status NOT NULL DEFAULT 'NEW',
    assignee_id BIGINT,
    project_id BIGINT,
    due_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_defect_assignee FOREIGN KEY (assignee_id) REFERENCES users(id),
    CONSTRAINT fk_defect_project FOREIGN KEY (project_id) REFERENCES projects(id)
);
