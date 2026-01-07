-- V13: Update trip status constraint to match domain model values
-- Domain uses: REQUESTED, DRIVER_ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED

-- Drop the old constraint
ALTER TABLE trips DROP CONSTRAINT IF EXISTS chk_trip_status;

-- Add the new constraint with correct domain values
ALTER TABLE trips ADD CONSTRAINT chk_trip_status 
    CHECK (status IN ('REQUESTED', 'DRIVER_ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'));
