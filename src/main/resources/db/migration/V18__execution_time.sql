-- Tempo de execucao.
-- services.duration_minutes: duracao estimada do servico (base para a futura agenda).
-- service_orders.started_at: quando a OS entrou em andamento; junto com finished_at da o
-- tempo real de execucao. OS anteriores a esta migracao ficam sem started_at (sem medicao).
ALTER TABLE services ADD COLUMN duration_minutes smallint;
ALTER TABLE service_orders ADD COLUMN started_at timestamptz;
