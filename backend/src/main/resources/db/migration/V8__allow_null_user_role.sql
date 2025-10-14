-- Allow NULL in users.role to support registration without a role
ALTER TABLE users ALTER COLUMN role DROP NOT NULL;

