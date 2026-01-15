-- Enable PostGIS extension for local and test databases
-- This migration ensures PostGIS functions like ST_MakePoint are available
CREATE EXTENSION IF NOT EXISTS postgis;