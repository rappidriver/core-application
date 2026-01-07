-- Enable PostGIS extension for geospatial queries
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create schemas for multi-tenancy if needed
-- CREATE SCHEMA IF NOT EXISTS tenant_1;
-- CREATE SCHEMA IF NOT EXISTS tenant_2;

-- Verify PostGIS installation
SELECT PostGIS_Version();
