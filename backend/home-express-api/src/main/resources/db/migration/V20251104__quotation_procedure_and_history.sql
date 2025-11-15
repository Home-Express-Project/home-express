-- Quotation acceptance workflow: history table, triggers, stored procedure

DROP TRIGGER IF EXISTS trg_quotation_status_history;
DROP TRIGGER IF EXISTS trg_quotations_validate_acceptance;
DROP TRIGGER IF EXISTS trg_quotations_accepted;
DROP PROCEDURE IF EXISTS sp_accept_quotation;

CREATE TABLE IF NOT EXISTS quotation_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    quotation_id BIGINT NOT NULL,
    old_status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED') DEFAULT NULL,
    new_status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED') NOT NULL,
    changed_by BIGINT DEFAULT NULL,
    changed_by_role ENUM('CUSTOMER', 'TRANSPORT', 'MANAGER', 'SYSTEM') DEFAULT NULL,
    reason TEXT DEFAULT NULL,
    metadata JSON DEFAULT NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_quotation_status_history_quotation (quotation_id, changed_at),
    CONSTRAINT fk_quotation_status_history_quotation
        FOREIGN KEY (quotation_id) REFERENCES quotations(quotation_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for all quotation status changes';

DELIMITER $$

CREATE TRIGGER trg_quotations_validate_acceptance
BEFORE UPDATE ON quotations
FOR EACH ROW
BEGIN
  DECLARE booking_customer_id BIGINT DEFAULT NULL;
  DECLARE booking_exists INT DEFAULT 0;

  IF NEW.status = 'ACCEPTED' AND OLD.status != 'ACCEPTED' THEN
    SELECT COUNT(*)
    INTO booking_exists
    FROM bookings
    WHERE booking_id = NEW.booking_id;

    IF booking_exists = 0 THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Booking not found for quotation';
    END IF;

    SELECT customer_id
    INTO booking_customer_id
    FROM bookings
    WHERE booking_id = NEW.booking_id
    LIMIT 1;

    IF NEW.accepted_by IS NULL OR NEW.accepted_by != booking_customer_id THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Only booking owner can accept quotation';
    END IF;

    IF NEW.expires_at IS NOT NULL AND NEW.expires_at < NOW() THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Cannot accept expired quotation';
    END IF;

    IF NEW.accepted_at IS NULL THEN
      SET NEW.accepted_at = NOW();
    END IF;
  END IF;
END$$

CREATE TRIGGER trg_quotations_accepted
AFTER UPDATE ON quotations
FOR EACH ROW
BEGIN
  IF NEW.status = 'ACCEPTED' AND OLD.status != 'ACCEPTED' THEN
    UPDATE bookings
    SET
      status = IF(status IN ('PENDING', 'QUOTED'), 'CONFIRMED', status),
      transport_id = NEW.transport_id,
      final_price = NEW.quoted_price,
      updated_at = CURRENT_TIMESTAMP
    WHERE booking_id = NEW.booking_id;
  END IF;
END$$

CREATE TRIGGER trg_quotation_status_history
AFTER UPDATE ON quotations
FOR EACH ROW
BEGIN
  IF NEW.status <> OLD.status THEN
    INSERT INTO quotation_status_history
      (quotation_id, old_status, new_status, changed_at)
    VALUES
      (NEW.quotation_id, OLD.status, NEW.status, NOW());
  END IF;
END$$

CREATE PROCEDURE sp_accept_quotation(
  IN p_quotation_id BIGINT,
  IN p_customer_id BIGINT,
  IN p_ip_address VARCHAR(45)
)
BEGIN
  DECLARE v_booking_id BIGINT;
  DECLARE v_current_status VARCHAR(20);
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    RESIGNAL;
  END;

  START TRANSACTION;

  SELECT booking_id, status
  INTO v_booking_id, v_current_status
  FROM quotations
  WHERE quotation_id = p_quotation_id
  FOR UPDATE;

  IF v_booking_id IS NULL THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Quotation not found';
  END IF;

  IF v_current_status <> 'PENDING' THEN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Quotation already processed';
  END IF;

  UPDATE quotations
  SET
    status = 'ACCEPTED',
    accepted_by = p_customer_id,
    accepted_at = NOW(),
    accepted_ip = p_ip_address
  WHERE quotation_id = p_quotation_id;

  UPDATE quotations
  SET
    status = 'REJECTED',
    responded_at = NOW()
  WHERE
    booking_id = v_booking_id
    AND quotation_id <> p_quotation_id
    AND status = 'PENDING';

  COMMIT;
END$$

DELIMITER ;
