CREATE TABLE projects (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	description TEXT,
	start_date DATE,
	end_date DATE
);
