-- Observacoes livres na OS (ex.: "riscado no para-choque", "cuidado com o vidro").
ALTER TABLE service_orders ADD COLUMN observations varchar(1000);
