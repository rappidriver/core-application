-- V2__Create_driver_passenger_trip_tables.sql
-- Tables for core domain entities

-- ========================================
-- DRIVERS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    
    -- Driver License (CNH)
    driver_license_number VARCHAR(11) NOT NULL,
    driver_license_category VARCHAR(2) NOT NULL,
    driver_license_issue_date DATE NOT NULL,
    driver_license_expiration_date DATE NOT NULL,
    
    -- Status and Location
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    location_latitude DOUBLE PRECISION,
    location_longitude DOUBLE PRECISION,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_driver_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT chk_driver_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BUSY', 'BLOCKED')),
    CONSTRAINT chk_driver_license_category CHECK (driver_license_category IN ('A', 'B', 'AB', 'C', 'D', 'E', 'AC', 'AD', 'AE'))
);

-- Indexes for drivers
CREATE UNIQUE INDEX idx_drivers_email ON drivers(email);
CREATE UNIQUE INDEX idx_drivers_cpf ON drivers(cpf);
CREATE INDEX idx_drivers_tenant ON drivers(tenant_id);
CREATE INDEX idx_drivers_status ON drivers(status);
CREATE INDEX idx_drivers_status_tenant ON drivers(status, tenant_id);
CREATE INDEX idx_drivers_location ON drivers USING GIST (
    ST_SetSRID(ST_MakePoint(location_longitude, location_latitude), 4326)
) WHERE location_latitude IS NOT NULL AND location_longitude IS NOT NULL;

-- Comments
COMMENT ON TABLE drivers IS 'Drivers registered in the platform';
COMMENT ON COLUMN drivers.driver_license_number IS 'Brazilian CNH number (11 digits)';
COMMENT ON COLUMN drivers.driver_license_category IS 'CNH category (A, B, AB, C, D, E, AC, AD, AE)';

-- Trigger for updated_at
CREATE TRIGGER update_drivers_updated_at BEFORE UPDATE ON drivers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========================================
-- PASSENGERS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS passengers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_passenger_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT chk_passenger_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))
);

-- Indexes for passengers
CREATE UNIQUE INDEX idx_passengers_email ON passengers(email);
CREATE INDEX idx_passengers_tenant ON passengers(tenant_id);
CREATE INDEX idx_passengers_status ON passengers(status);
CREATE INDEX idx_passengers_status_tenant ON passengers(status, tenant_id);

-- Comments
COMMENT ON TABLE passengers IS 'Passengers using the platform';

-- Trigger for updated_at
CREATE TRIGGER update_passengers_updated_at BEFORE UPDATE ON passengers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========================================
-- TRIPS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    passenger_id UUID NOT NULL,
    driver_id UUID,
    
    -- Locations
    pickup_latitude DOUBLE PRECISION NOT NULL,
    pickup_longitude DOUBLE PRECISION NOT NULL,
    dropoff_latitude DOUBLE PRECISION NOT NULL,
    dropoff_longitude DOUBLE PRECISION NOT NULL,
    
    -- Trip details
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    distance_km DOUBLE PRECISION,
    fare_amount DECIMAL(10, 2),
    
    -- Timestamps
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_trip_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_trip_passenger FOREIGN KEY (passenger_id) REFERENCES passengers(id),
    CONSTRAINT fk_trip_driver FOREIGN KEY (driver_id) REFERENCES drivers(id),
    CONSTRAINT chk_trip_status CHECK (status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- Indexes for trips
CREATE INDEX idx_trips_tenant ON trips(tenant_id);
CREATE INDEX idx_trips_passenger ON trips(passenger_id);
CREATE INDEX idx_trips_driver ON trips(driver_id);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_status_tenant ON trips(status, tenant_id);
CREATE INDEX idx_trips_requested_at ON trips(requested_at DESC);
CREATE INDEX idx_trips_pickup_location ON trips USING GIST (
    ST_SetSRID(ST_MakePoint(pickup_longitude, pickup_latitude), 4326)
);
CREATE INDEX idx_trips_dropoff_location ON trips USING GIST (
    ST_SetSRID(ST_MakePoint(dropoff_longitude, dropoff_latitude), 4326)
);

-- Comments
COMMENT ON TABLE trips IS 'Trip requests and history';
COMMENT ON COLUMN trips.distance_km IS 'Calculated distance in kilometers';
COMMENT ON COLUMN trips.fare_amount IS 'Calculated fare in currency';

-- Trigger for updated_at
CREATE TRIGGER update_trips_updated_at BEFORE UPDATE ON trips
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
