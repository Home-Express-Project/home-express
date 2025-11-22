-- Reinstate missing foreign key between contracts and quotations after clean-up

ALTER TABLE contracts
  ADD CONSTRAINT fk_contracts_quotation
  FOREIGN KEY (quotation_id)
  REFERENCES quotations(quotation_id)
  ON DELETE NO ACTION;
