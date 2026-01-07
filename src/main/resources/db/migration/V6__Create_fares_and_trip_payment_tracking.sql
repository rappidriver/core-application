-- Migration V6: Create fares table and add payment tracking to trips
-- Date: 2026-01-03
-- Description: Adds Fare persistence and integrates payment tracking with trips

-- Create fares table
CREATE TABLE IF NOT EXISTS fares (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL UNIQUE,
    tenant_id UUID NOT NULL,
    
    -- Fare components
    base_fare DECIMAL(10, 2) NOT NULL,
    distance_km DOUBLE PRECISION NOT NULL,
    duration_minutes INTEGER NOT NULL,
    distance_fare DECIMAL(10, 2) NOT NULL,
    time_fare DECIMAL(10, 2) NOT NULL,
    
    -- Multipliers
    multiplier_type VARCHAR(20) NOT NULL CHECK (multiplier_type IN ('NORMAL', 'PEAK', 'LATE_NIGHT')),
    time_multiplier DECIMAL(3, 2) NOT NULL,
    vehicle_category VARCHAR(20) NOT NULL CHECK (vehicle_category IN ('HATCHBACK', 'SEDAN', 'SUV')),
    vehicle_multiplier DECIMAL(3, 2) NOT NULL,
    
    -- Totals
    total_before_multiplier DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    minimum_fare DECIMAL(10, 2) NOT NULL,
    
    -- Additional info
    explanation TEXT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    calculated_at TIMESTAMP NOT NULL,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT fk_fare_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT chk_distance_positive CHECK (distance_km >= 0),
    CONSTRAINT chk_duration_positive CHECK (duration_minutes >= 0),
    CONSTRAINT chk_total_positive CHECK (total_amount > 0)
);

-- Create indexes for fares
CREATE INDEX idx_fare_trip ON fares(trip_id);
CREATE INDEX idx_fare_tenant ON fares(tenant_id);
CREATE INDEX idx_fare_tenant_created ON fares(tenant_id, created_at);

-- Add payment tracking columns to trips table
ALTER TABLE trips ADD COLUMN IF NOT EXISTS fare_id UUID;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS payment_id UUID;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) DEFAULT 'PENDING';

-- Add foreign keys for payment tracking
ALTER TABLE trips ADD CONSTRAINT fk_trip_fare 
    FOREIGN KEY (fare_id) REFERENCES fares(id) ON DELETE SET NULL;
ALTER TABLE trips ADD CONSTRAINT fk_trip_payment 
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL;

-- Add check constraint for payment_status
ALTER TABLE trips ADD CONSTRAINT chk_payment_status 
    CHECK (payment_status IN ('PENDING', 'PROCESSING', 'PAID', 'PAYMENT_FAILED', 'REFUNDED'));

-- Create indexes for payment tracking
CREATE INDEX idx_trip_fare ON trips(fare_id) WHERE fare_id IS NOT NULL;
CREATE INDEX idx_trip_payment ON trips(payment_id) WHERE payment_id IS NOT NULL;
CREATE INDEX idx_trip_payment_status ON trips(payment_status);
CREATE INDEX idx_trip_completed_unpaid ON trips(status, payment_status) 
    WHERE status = 'COMPLETED' AND payment_status != 'PAID';

-- Trigger to update fares.updated_at
CREATE OR REPLACE FUNCTION update_fare_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_fare_updated_at
    BEFORE UPDATE ON fares
    FOR EACH ROW
    EXECUTE FUNCTION update_fare_updated_at();

-- Comment on tables and columns
COMMENT ON TABLE fares IS 'Stores calculated fares for completed trips';
COMMENT ON COLUMN fares.trip_id IS 'Unique reference to the trip this fare belongs to';
COMMENT ON COLUMN fares.multiplier_type IS 'Time-based multiplier applied (NORMAL/PEAK/LATE_NIGHT)';
COMMENT ON COLUMN fares.vehicle_category IS 'Vehicle category multiplier (HATCHBACK/SEDAN/SUV)';
COMMENT ON COLUMN fares.explanation IS 'Human-readable explanation of how the fare was calculated';

COMMENT ON COLUMN trips.fare_id IS 'Reference to calculated fare for this trip';
COMMENT ON COLUMN trips.payment_id IS 'Reference to payment processed for this trip';
COMMENT ON COLUMN trips.payment_status IS 'Current payment status (PENDING/PROCESSING/PAID/PAYMENT_FAILED/REFUNDED)';
