-- Convert PostgreSQL enum columns to VARCHAR to be compatible with JPA EnumType.STRING
BEGIN;

-- Change priority enum -> varchar
ALTER TABLE defects
  ALTER COLUMN priority TYPE VARCHAR(32) USING priority::text;
ALTER TABLE defects
  ALTER COLUMN priority SET DEFAULT 'MEDIUM';

-- Change status enum -> varchar
ALTER TABLE defects
  ALTER COLUMN status TYPE VARCHAR(32) USING status::text;
ALTER TABLE defects
  ALTER COLUMN status SET DEFAULT 'NEW';

COMMIT;

-- Note: we intentionally do NOT drop the enum types yet to avoid problems
-- with other environments. If you want to remove the enum type objects,
-- add a DROP TYPE statement in a later migration after confirming all
-- deployments have switched to the varchar columns.
