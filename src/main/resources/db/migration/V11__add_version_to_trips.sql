-- Add version column for optimistic locking on trips table
ALTER TABLE trips ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
