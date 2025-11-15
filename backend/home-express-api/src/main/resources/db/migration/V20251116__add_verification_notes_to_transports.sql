-- Add verification notes to transports with backward-compatible checks
SET @schema := DATABASE();

SET @sql := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @schema
              AND TABLE_NAME = 'transports'
              AND COLUMN_NAME = 'verification_notes'
        ),
        'DO 0',
        'ALTER TABLE transports ADD COLUMN verification_notes TEXT'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE transports
    MODIFY COLUMN verification_notes TEXT
        COMMENT 'Notes from admin when approving/rejecting transport verification';
