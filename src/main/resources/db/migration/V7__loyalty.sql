-- Programa de fidelidade (cartão): a cada N lavagens concluídas por cliente,
-- o cliente ganha um prêmio (desconto/grátis) para aplicar em uma OS.

-- Configuração por estabelecimento.
ALTER TABLE tenants ADD COLUMN loyalty_enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE tenants ADD COLUMN loyalty_target SMALLINT NOT NULL DEFAULT 10;
-- Prêmio como percentual de desconto na OS (100 = lavagem grátis).
ALTER TABLE tenants ADD COLUMN loyalty_reward_percent NUMERIC(5, 2) NOT NULL DEFAULT 100;

-- Prêmios já resgatados pelo cliente (o progresso vem de OS concluídas − resgatados).
ALTER TABLE customers ADD COLUMN loyalty_rewards_redeemed INTEGER NOT NULL DEFAULT 0;

-- Desconto de fidelidade (%) aplicado a esta OS (0 = nenhum). Congelado ao resgatar.
ALTER TABLE service_orders ADD COLUMN loyalty_reward_percent NUMERIC(5, 2) NOT NULL DEFAULT 0;
