-- V3: Add driver_license_is_definitive column to drivers table
-- Business Rule: CNH must be definitive (not PPD/temporary permission)

ALTER TABLE drivers
ADD COLUMN driver_license_is_definitive BOOLEAN NOT NULL DEFAULT true;

COMMENT ON COLUMN drivers.driver_license_is_definitive IS 'Indicates if CNH is definitive (true) or temporary/PPD (false)';
