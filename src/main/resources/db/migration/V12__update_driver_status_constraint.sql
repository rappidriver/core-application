-- Add PENDING_APPROVAL and AVAILABLE to driver status constraint
-- First drop the existing constraint
ALTER TABLE drivers DROP CONSTRAINT IF EXISTS chk_driver_status;

-- Then add the new constraint with all valid statuses
ALTER TABLE drivers ADD CONSTRAINT chk_driver_status 
    CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'AVAILABLE', 'INACTIVE', 'BUSY', 'BLOCKED'));
