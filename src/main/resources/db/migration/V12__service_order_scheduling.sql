-- Agendamento futuro (equipe marca horario antes do cliente chegar) e previsao
-- de retirada (cliente ja deixou o veiculo e informou quando volta buscar).
ALTER TYPE service_status ADD VALUE 'scheduled' BEFORE 'waiting';

ALTER TABLE service_orders ADD COLUMN scheduled_at timestamp;
ALTER TABLE service_orders ADD COLUMN estimated_pickup_at timestamp;
