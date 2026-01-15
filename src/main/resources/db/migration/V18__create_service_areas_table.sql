-- V18: Create service_areas table for tenant operating zones
-- Uses JSONB for GeoJSON polygon storage (PostGIS geometry can be added later)

CREATE TABLE IF NOT EXISTS service_areas (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    geojson_polygon JSONB NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_service_area_tenant_id ON service_areas(tenant_id);
CREATE INDEX IF NOT EXISTS idx_service_area_active ON service_areas(active);

-- Optional: GIN index for JSONB queries (if needed for polygon queries)
CREATE INDEX IF NOT EXISTS idx_service_area_geojson ON service_areas USING GIN (geojson_polygon);

-- Comments for documentation
COMMENT ON TABLE service_areas IS 'Geographic operating zones for each tenant (city/region)';
COMMENT ON COLUMN service_areas.id IS 'Unique identifier for the service area';
COMMENT ON COLUMN service_areas.tenant_id IS 'Tenant (city) this service area belongs to';
COMMENT ON COLUMN service_areas.name IS 'Human-readable name (e.g., "Downtown Goiânia")';
COMMENT ON COLUMN service_areas.geojson_polygon IS 'GeoJSON Polygon defining the operating zone';
COMMENT ON COLUMN service_areas.active IS 'Whether the service area is currently active';
COMMENT ON COLUMN service_areas.created_at IS 'Timestamp when the service area was created';
COMMENT ON COLUMN service_areas.updated_at IS 'Timestamp when the service area was last updated';
