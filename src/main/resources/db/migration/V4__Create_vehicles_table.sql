-- V4__Create_vehicles_table.sql
-- Migration para criar tabela de veículos com regras de negócio enforçadas

CREATE TABLE IF NOT EXISTS vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    driver_id UUID,
    
    -- Vehicle details
    license_plate VARCHAR(8) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(30) NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    number_of_doors INTEGER NOT NULL,
    seats INTEGER NOT NULL,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_vehicle_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_vehicle_driver FOREIGN KEY (driver_id) REFERENCES drivers(id),
    CONSTRAINT chk_vehicle_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')),
    CONSTRAINT chk_vehicle_type CHECK (vehicle_type IN ('SEDAN', 'HATCHBACK', 'SUV')),
    CONSTRAINT chk_number_of_doors CHECK (number_of_doors = 4),
    CONSTRAINT chk_vehicle_seats CHECK (seats >= 1 AND seats <= 12),
    CONSTRAINT chk_vehicle_year CHECK (year >= 2015 AND year <= 2030)
);

-- Indexes
CREATE UNIQUE INDEX idx_vehicles_license_plate_tenant ON vehicles(license_plate, tenant_id);
CREATE INDEX idx_vehicles_driver ON vehicles(driver_id);
CREATE INDEX idx_vehicles_tenant ON vehicles(tenant_id);
CREATE INDEX idx_vehicles_status_tenant ON vehicles(status, tenant_id);

-- Only one ACTIVE vehicle per driver (conditional unique index)
CREATE UNIQUE INDEX idx_vehicles_active_per_driver 
    ON vehicles(driver_id) 
    WHERE status = 'ACTIVE';

-- Comments
COMMENT ON TABLE vehicles IS 'Vehicles registered in the platform';
COMMENT ON COLUMN vehicles.license_plate IS 'Brazilian license plate (ABC-1234 or ABC1D23)';
COMMENT ON COLUMN vehicles.vehicle_type IS 'Type: SEDAN, HATCHBACK, SUV (passenger vehicles only)';
COMMENT ON COLUMN vehicles.number_of_doors IS 'Must be exactly 4 doors';
COMMENT ON INDEX idx_vehicles_active_per_driver IS 'Ensures only one active vehicle per driver';

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_vehicles_updated_at 
    BEFORE UPDATE ON vehicles
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
