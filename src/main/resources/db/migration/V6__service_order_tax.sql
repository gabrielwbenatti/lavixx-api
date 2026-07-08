-- Congela a taxa de serviço (%) aplicada na OS no momento da criação, para que
-- alterações futuras em tenants.default_service_tax não alterem OS já existentes.
ALTER TABLE service_orders ADD COLUMN service_tax NUMERIC(5, 2) NOT NULL DEFAULT 0;
