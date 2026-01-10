-- V15__create_driver_approval_tables.sql
-- Adds tables for driver approval workflow and admin users

CREATE TABLE IF NOT EXISTS admin_users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, email)
);

CREATE INDEX IF NOT EXISTS idx_admin_users_tenant_role ON admin_users(tenant_id, role);

CREATE TABLE IF NOT EXISTS driver_approvals (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL REFERENCES drivers(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_documents TEXT NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by_admin_id UUID REFERENCES admin_users(id),
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_driver_approvals_tenant_status ON driver_approvals(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_driver_approvals_driver ON driver_approvals(driver_id);

CREATE OR REPLACE FUNCTION update_driver_approvals_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_driver_approvals_updated_at
BEFORE UPDATE ON driver_approvals
FOR EACH ROW EXECUTE FUNCTION update_driver_approvals_updated_at();
