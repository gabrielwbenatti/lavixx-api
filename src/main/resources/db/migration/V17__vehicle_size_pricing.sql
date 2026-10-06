-- Preco de tabela por porte do veiculo. O porte (pequeno/medio/grande) e opcional no
-- veiculo; o servico mantem `price` como preco padrao e pode ter um preco especifico por porte.
-- Regra: veiculo com porte E servico com preco para esse porte -> usa o preco do porte;
-- caso contrario -> usa o preco padrao.
CREATE TYPE vehicle_size AS ENUM ('small', 'medium', 'large');

ALTER TABLE vehicles ADD COLUMN size vehicle_size;

ALTER TABLE services ADD COLUMN price_small  numeric(10, 2);
ALTER TABLE services ADD COLUMN price_medium numeric(10, 2);
ALTER TABLE services ADD COLUMN price_large  numeric(10, 2);
