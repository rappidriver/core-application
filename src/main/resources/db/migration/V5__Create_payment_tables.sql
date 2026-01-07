-- V5__Create_payment_tables.sql
-- Migration to create payment and fare configuration tables

-- Fare configurations table (one per tenant)
CREATE TABLE IF NOT EXISTS fare_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    
    -- Pricing parameters
    base_fare NUMERIC(10, 2) NOT NULL,
    price_per_km NUMERIC(10, 2) NOT NULL,
    price_per_minute NUMERIC(10, 2) NOT NULL,
    minimum_fare NUMERIC(10, 2) NOT NULL,
    platform_commission_rate NUMERIC(5, 4) NOT NULL DEFAULT 0.2000,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_fare_config_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT chk_base_fare CHECK (base_fare >= 0),
    CONSTRAINT chk_price_per_km CHECK (price_per_km >= 0),
    CONSTRAINT chk_price_per_minute CHECK (price_per_minute >= 0),
    CONSTRAINT chk_minimum_fare CHECK (minimum_fare >= 0),
    CONSTRAINT chk_commission_rate CHECK (platform_commission_rate >= 0 AND platform_commission_rate <= 1)
);

-- Only one fare configuration per tenant
CREATE UNIQUE INDEX idx_fare_config_tenant ON fare_configurations(tenant_id);

-- Comments
COMMENT ON TABLE fare_configurations IS 'Fare pricing configurations per tenant';
COMMENT ON COLUMN fare_configurations.base_fare IS 'Base fare charged per trip (in BRL)';
COMMENT ON COLUMN fare_configurations.price_per_km IS 'Price per kilometer traveled (in BRL)';
COMMENT ON COLUMN fare_configurations.price_per_minute IS 'Price per minute of trip duration (in BRL)';
COMMENT ON COLUMN fare_configurations.minimum_fare IS 'Minimum fare charged per trip (in BRL)';
COMMENT ON COLUMN fare_configurations.platform_commission_rate IS 'Platform commission rate (0.0 to 1.0)';

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    trip_id UUID NOT NULL,
    
    -- Amount breakdown
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    platform_fee NUMERIC(10, 2) NOT NULL,
    driver_amount NUMERIC(10, 2) NOT NULL,
    
    -- Payment method
    payment_method_type VARCHAR(20) NOT NULL,
    card_last_4 VARCHAR(4),
    card_brand VARCHAR(50),
    pix_key VARCHAR(200),
    
    -- Status and tracking
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    gateway_transaction_id VARCHAR(100),
    failure_reason VARCHAR(500),
    processed_at TIMESTAMP,
    refunded_at TIMESTAMP,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_payment_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_payment_trip FOREIGN KEY (trip_id) REFERENCES trips(id),
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    CONSTRAINT chk_payment_platform_fee CHECK (platform_fee >= 0),
    CONSTRAINT chk_payment_driver_amount CHECK (driver_amount >= 0),
    CONSTRAINT chk_payment_currency CHECK (currency IN ('BRL', 'USD', 'EUR')),
    CONSTRAINT chk_payment_method_type CHECK (payment_method_type IN ('CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'CASH')),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    CONSTRAINT chk_card_payment_has_details CHECK (
        payment_method_type NOT IN ('CREDIT_CARD', 'DEBIT_CARD') OR 
        (card_last_4 IS NOT NULL AND card_brand IS NOT NULL)
    ),
    CONSTRAINT chk_pix_payment_has_key CHECK (
        payment_method_type != 'PIX' OR pix_key IS NOT NULL
    ),
    CONSTRAINT chk_amount_breakdown CHECK (
        platform_fee + driver_amount = amount
    )
);

-- Indexes
CREATE UNIQUE INDEX idx_payment_trip ON payments(trip_id);
CREATE INDEX idx_payment_tenant ON payments(tenant_id);
CREATE INDEX idx_payment_tenant_created ON payments(tenant_id, created_at);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_gateway_transaction ON payments(gateway_transaction_id) WHERE gateway_transaction_id IS NOT NULL;

-- Comments
COMMENT ON TABLE payments IS 'Payment records for trips';
COMMENT ON COLUMN payments.amount IS 'Total payment amount (platform_fee + driver_amount)';
COMMENT ON COLUMN payments.platform_fee IS 'Amount retained by platform as commission';
COMMENT ON COLUMN payments.driver_amount IS 'Amount transferred to driver';
COMMENT ON COLUMN payments.payment_method_type IS 'CREDIT_CARD, DEBIT_CARD, PIX, or CASH';
COMMENT ON COLUMN payments.card_last_4 IS 'Last 4 digits of card (for card payments)';
COMMENT ON COLUMN payments.gateway_transaction_id IS 'External payment gateway transaction ID';
COMMENT ON INDEX idx_payment_trip IS 'Ensures one payment per trip';

-- Triggers for updated_at
CREATE TRIGGER update_fare_config_updated_at
    BEFORE UPDATE ON fare_configurations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payment_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert default fare configurations for existing tenants
INSERT INTO fare_configurations (tenant_id, base_fare, price_per_km, price_per_minute, minimum_fare, platform_commission_rate)
SELECT 
    id as tenant_id,
    5.00 as base_fare,
    2.50 as price_per_km,
    0.50 as price_per_minute,
    10.00 as minimum_fare,
    0.2000 as platform_commission_rate
FROM tenants
WHERE NOT EXISTS (
    SELECT 1 FROM fare_configurations WHERE fare_configurations.tenant_id = tenants.id
);
