-- Data de emissao da OS: campo de negocio, editavel por admin (ex.: lancamento
-- retroativo de OS antigas), separado do created_at (auditoria, nunca editado).
-- Preenchido com o created_at de cada OS ja existente para manter continuidade.
ALTER TABLE service_orders ADD COLUMN issued_at timestamp;
UPDATE service_orders SET issued_at = created_at;
ALTER TABLE service_orders ALTER COLUMN issued_at SET NOT NULL;
