-- HIST-2026-007: Add PostGIS GIST and composite indexes for driver location queries

-- Ensure PostGIS extension is enabled
CREATE EXTENSION IF NOT EXISTS postgis;

-- Add GIST index for geospatial queries on location column
CREATE INDEX IF NOT EXISTS idx_drivers_location_gist
ON drivers USING GIST (location);

-- Add composite index for tenant_id, status, and location
CREATE INDEX IF NOT EXISTS idx_drivers_tenant_status_location
ON drivers (tenant_id, status) INCLUDE (location);