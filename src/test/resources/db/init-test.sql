-- PostgreSQL Init Script for Test Database
-- This script runs the initial schema setup for Testcontainers

-- Note: PostGIS extension is optional for driver registration E2E test
-- Use postgis/postgis:16 image if you need geospatial queries

-- Create initial schema (simplified Flyway equivalent)
-- This should match V1__Initial_schema.sql through V10__create_outbox_table.sql

-- V1: Tenants and basic tables
CREATE TABLE IF NOT EXISTS tenant (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- V2: Driver and Passenger tables
CREATE TABLE IF NOT EXISTS driver (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    location POINT,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, email),
    UNIQUE (tenant_id, cpf)
);

CREATE TABLE IF NOT EXISTS passenger (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, email),
    UNIQUE (tenant_id, cpf)
);

-- V4: Vehicle table
CREATE TABLE IF NOT EXISTS vehicle (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    driver_id UUID NOT NULL REFERENCES driver(id),
    plate VARCHAR(20) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    color VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, plate)
);

-- V2: Trip table
CREATE TABLE IF NOT EXISTS trip (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    passenger_id UUID NOT NULL REFERENCES passenger(id),
    driver_id UUID REFERENCES driver(id),
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    pickup_location POINT NOT NULL,
    dropoff_location POINT NOT NULL,
    estimated_fare NUMERIC(10, 2),
    final_fare NUMERIC(10, 2),
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- V3: Driver License table
CREATE TABLE IF NOT EXISTS driver_license (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL UNIQUE REFERENCES driver(id),
    license_number VARCHAR(20) NOT NULL,
    category VARCHAR(5) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    is_definitive BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- V5: Payment tables
CREATE TABLE IF NOT EXISTS payment (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trip(id),
    amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- V7: Rating table
CREATE TABLE IF NOT EXISTS rating (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL UNIQUE REFERENCES trip(id),
    rater_id UUID NOT NULL,
    rated_id UUID NOT NULL,
    score INT NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- V8: Notification table
CREATE TABLE IF NOT EXISTS notification (
    id UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    sent_at TIMESTAMP
);

-- V15: Admin users and driver approvals
CREATE TABLE IF NOT EXISTS admin_users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, email)
);

CREATE TABLE IF NOT EXISTS driver_approvals (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL REFERENCES driver(id),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_documents TEXT NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by_admin_id UUID REFERENCES admin_users(id),
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert test tenant
INSERT INTO tenants (id, name, slug) VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Test Tenant', 'test-tenant') ON CONFLICT DO NOTHING;

-- Insert test admin user (matches Keycloak admin-test user)
INSERT INTO admin_users (id, tenant_id, email, role, full_name) 
VALUES ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'admin@test.com', 'SUPER_ADMIN', 'Test Admin') 
ON CONFLICT DO NOTHING;

-- Create indexes for efficient queries
CREATE INDEX IF NOT EXISTS idx_driver_tenant_id ON driver(tenant_id);
CREATE INDEX IF NOT EXISTS idx_driver_email ON driver(email);
CREATE INDEX IF NOT EXISTS idx_passenger_tenant_id ON passenger(tenant_id);
CREATE INDEX IF NOT EXISTS idx_trip_tenant_id ON trip(tenant_id);
CREATE INDEX IF NOT EXISTS idx_trip_driver_id ON trip(driver_id);
CREATE INDEX IF NOT EXISTS idx_trip_passenger_id ON trip(passenger_id);
CREATE INDEX IF NOT EXISTS idx_trip_status ON trip(status);
CREATE INDEX IF NOT EXISTS idx_vehicle_driver_id ON vehicle(driver_id);
CREATE INDEX IF NOT EXISTS idx_outbox_status_next_attempt ON outbox_event(status, next_attempt_at);
CREATE INDEX IF NOT EXISTS idx_outbox_aggregate_id ON outbox_event(aggregate_id);

