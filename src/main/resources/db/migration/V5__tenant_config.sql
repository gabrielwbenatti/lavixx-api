-- Adiciona campos de configuração ao tenant
ALTER TABLE tenants ADD COLUMN operating_hours_start VARCHAR(5) DEFAULT '08:00';
ALTER TABLE tenants ADD COLUMN operating_hours_end VARCHAR(5) DEFAULT '18:00';
ALTER TABLE tenants ADD COLUMN default_service_tax NUMERIC(5, 2) DEFAULT 0;
